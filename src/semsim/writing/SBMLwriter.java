package semsim.writing;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;

import javax.xml.stream.XMLStreamException;

import org.jdom.Content;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;
import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.AbstractNamedSBase;
import org.sbml.jsbml.CVTerm;
import org.sbml.jsbml.CVTerm.Qualifier;
import org.sbml.jsbml.Compartment;
import org.sbml.jsbml.Constraint;
import org.sbml.jsbml.ExplicitRule;
import org.sbml.jsbml.JSBML;
import org.sbml.jsbml.KineticLaw;
import org.sbml.jsbml.LocalParameter;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.ModifierSpeciesReference;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.QuantityWithUnit;
import org.sbml.jsbml.RateRule;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLWriter;
import org.sbml.jsbml.SBase;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;
import org.sbml.jsbml.Symbol;
import org.sbml.jsbml.Unit;
import org.sbml.jsbml.UnitDefinition;
import org.sbml.jsbml.Unit.Kind;
import org.sbml.jsbml.validator.SyntaxChecker;
import semsim.SemSimLibrary;
import semsim.SemSimObject;
import semsim.annotation.Annotation;
import semsim.annotation.ReferenceOntologyAnnotation;
import semsim.annotation.ReferenceTerm;
import semsim.annotation.Relation;
import semsim.definitions.RDFNamespace;
import semsim.definitions.SBMLconstants;
import semsim.definitions.SemSimRelations;
import semsim.definitions.SemSimRelations.SemSimRelation;
import semsim.definitions.SemSimRelations.StructuralRelation;
import semsim.model.collection.SemSimModel;
import semsim.model.computational.Computation;
import semsim.model.computational.Event;
import semsim.model.computational.RelationalConstraint;
import semsim.model.computational.Event.EventAssignment;
import semsim.model.computational.datastructures.DataStructure;
import semsim.model.computational.datastructures.MappableVariable;
import semsim.model.computational.units.UnitFactor;
import semsim.model.computational.units.UnitOfMeasurement;
import semsim.model.physical.PhysicalDependency;
import semsim.model.physical.object.ReferencePhysicalDependency;
import semsim.model.physical.PhysicalEntity;
import semsim.model.physical.PhysicalModelComponent;
import semsim.model.physical.PhysicalProcess;
import semsim.model.physical.object.CompositePhysicalEntity;
import semsim.model.physical.object.CustomPhysicalEntity;
import semsim.model.physical.object.CustomPhysicalProcess;
import semsim.model.physical.object.ReferencePhysicalEntity;
import semsim.reading.SBMLreader;
import semsim.reading.ModelClassifier.ModelType;
import semsim.utilities.SemSimUtil;

public class SBMLwriter extends ModelWriter {
	
	private SBMLDocument sbmldoc;
	private Model sbmlmodel;
	private static final int sbmllevel = 3;
	private static final int sbmlversion = 1;
	
	private static final Namespace sbmlNS = Namespace.getNamespace(SBMLDocument.URI_NAMESPACE_L3V1Core);
	
	private static final String cellmlInlineUnitsNSdeclaration = "xmlns:cellml=\"http://www.cellml.org/cellml/1.0#\"";	
	
	private LinkedHashMap<CompositePhysicalEntity, Compartment> entityCompartmentMap = new LinkedHashMap<CompositePhysicalEntity, Compartment>();
	private LinkedHashMap<CompositePhysicalEntity, Species> entitySpeciesMap = new LinkedHashMap<CompositePhysicalEntity, Species>();
	private LinkedHashMap<CustomPhysicalProcess, Reaction> processReactionMap = new LinkedHashMap<CustomPhysicalProcess, Reaction>();
	
	// Data Structures whose annotations will be omitted in the SemSimRDF block.
	// This includes species and reaction elements. DataStructures representing
	// properties of species and reactions will have their annotations stored
	// in the usual SBML annotation element.
	private Set<DataStructure> DSsToOmitFromSemSimRDF = new HashSet<DataStructure>();
	
	private Set<DataStructure> candidateDSsForCompartments = new HashSet<DataStructure>();
	private Set<DataStructure> candidateDSsForSpecies = new HashSet<DataStructure>();
	private Set<DataStructure> candidateDSsForReactions = new HashSet<DataStructure>();
	private Set<DataStructure> globalParameters = new HashSet<DataStructure>(); // Boolean indicates whether to write out assignment for parameter
	
	private SemSimRDFwriter rdfblock;

	private Set<String> metaIDsUsed = new HashSet<String>();
	
	public static String semsimAnnotationElementName = "semsimAnnotation";
	
	public SBMLwriter(SemSimModel model) {
		super(model);
	}

	public String encodeModel() {
		sbmldoc = new SBMLDocument(sbmllevel, sbmlversion);
		String sbmlmodname = semsimmodel.getName();
		
		// Create a valid model name if current name is invalid
		if( ! SyntaxChecker.isValidId(sbmlmodname, sbmllevel, sbmlversion)) sbmlmodname = "default_name";
		
		sbmlmodel = sbmldoc.createModel(sbmlmodname);
		
		// If we're writing from a model with FunctionalSubmodels, flatten model first
		if(semsimmodel.getFunctionalSubmodels().size() > 0)
			SemSimUtil.flattenModel(semsimmodel);
		
		// TODO: Need to work out how to store SBase names and notes in SemSim objects
		// Currently notes are set in description field and names are ignored
		
		addNotesAndMetadataID(semsimmodel, sbmlmodel);
		
		sortDataStructuresIntoSBMLgroups();
		addUnits();
		addCompartments();
		addSpecies();
		addReactions();
		addEvents();
		addGlobalParameters();
		addConstraints();
		
		Document doc = addSemSimRDF();

		// If no errors, write out the model. (Converted to XML doc in addSemSimRDF()).		
		// First catch errors
		if(sbmldoc.getErrorCount() > 0){
			
			for(int i = 0; i< sbmldoc.getErrorCount(); i++)
				System.err.println(sbmldoc.getError(i));
			return null;
		}
		return new XMLOutputter().outputString(doc);
	}
	
	@Override
	public boolean writeToStream(OutputStream stream) {
		String outputstring = encodeModel();
		if (outputstring == null) {
			return false;
		}
		commitStringtoStream(stream, outputstring);
		return true;
	}
	
	/**
	 *  Determine which data structures potentially simulate properties of SBML compartments,
	 *  species, and reactions
	 */
	private void sortDataStructuresIntoSBMLgroups(){

		for(DataStructure ds : semsimmodel.getAssociatedDataStructures()){
			
			if(ds.hasPhysicalProperty()){
				
				URI propphysdefuri = ds.getPhysicalProperty().getPhysicalDefinitionURI();
				
				if(SBMLconstants.OPB_PROPERTIES_FOR_COMPARTMENTS.contains(propphysdefuri))
					candidateDSsForCompartments.add(ds);
				
				else if(SBMLconstants.OPB_PROPERTIES_FOR_SPECIES.contains(propphysdefuri))
					candidateDSsForSpecies.add(ds);
				
				else if(SBMLconstants.OPB_PROPERTIES_FOR_REACTIONS.contains(propphysdefuri))
					candidateDSsForReactions.add(ds);
				
				else globalParameters.add(ds);
				
			}
			else if ( ! ds.isSolutionDomain()) globalParameters.add(ds);
		}
	}
	
	
	/**
	 *  Collect units for SBML model
	 */
	private void addUnits(){

		for(UnitOfMeasurement uom : semsimmodel.getUnits()){	
			
			// Do not overwrite base units
			if(Kind.isValidUnitKindString(uom.getName(), sbmllevel, sbmlversion)) 
				continue;
			
			// If we're not looking at an SBML Level 2 reserved base unit...
			else if( ! SBMLconstants.SBML_LEVEL_2_RESERVED_UNITS_MAP.containsValue(uom.getName())){
				
				// ...see if we need to explicitly write it out. This happens if the model re-defines
				// the reserved unit. For example if the model uses hours instead of seconds for the reserved unit "time".
				if(SBMLconstants.SBML_LEVEL_2_RESERVED_UNITS_MAP.containsKey(uom.getName())){
			
					String baseunitname = SBMLconstants.SBML_LEVEL_2_RESERVED_UNITS_MAP.get(uom.getName());
	
					if(uom.getUnitFactors().size()==1){
						
						UnitFactor uf = uom.getUnitFactors().toArray(new UnitFactor[]{})[0];
						double exp = uf.getExponent();
						double mult = uf.getMultiplier();
						String prefix = uf.getPrefix();
						
						if(uf.getBaseUnit().getName().equals(baseunitname) && (exp==1 || exp==0) && (mult==1 || mult==0) && (prefix == null))
								continue;  // Do not write out the unit if it matches the default settings for an SBML reserved unit
					}
				}
			
				// If we're here we are explicitly writing out the unit to the SBML file
				UnitDefinition ud = sbmlmodel.createUnitDefinition(uom.getName());
				
				addNotesAndMetadataID(uom, ud);
				
				Set<UnitFactor> ufset = SemSimUtil.recurseBaseUnits(uom, 1.0, ModelWriter.sslib);
				
				for(UnitFactor uf : ufset){
					
					//if(uf.getBaseUnit()==null){
					//	System.err.println("Found a null base unit for a unit factor in unit " + uom.getName());
					//	continue;
					//}
					//else System.out.println(uom.getName() + " has unit factor " + uf.getBaseUnit().getName());
					
					String factorbasename = uf.getBaseUnit().getName();
					
					Unit sbmluf = new Unit(sbmllevel, sbmlversion);

 					// SBML Validation Rule #20421) A Unit object must have the required attributes 
					// 'kind', 'exponent', 'scale' and 'multiplier', and may have the optional attributes 
					// 'metaid' and 'sboTerm'. 
										
					if(Kind.isValidUnitKindString(factorbasename, sbmllevel, sbmlversion)){
						
						sbmluf.setKind(Kind.valueOf(factorbasename.toUpperCase())); // set Kind attribute
						sbmluf.setExponent(uf.getExponent()); // set Exponent attribute
						
						if(uf.getMultiplier() == 0 ) sbmluf.setMultiplier(1); // set Multiplier attribute
						else sbmluf.setMultiplier(uf.getMultiplier());
												
						if(uf.getPrefix() != null && ! uf.getPrefix().equals("")){ // set Scale attribute
							Integer power = sslib.getUnitPrefixesAndPowersMap().get(uf.getPrefix());
							
							if(power!=null) sbmluf.setScale(power);
							else System.err.println("Couldn't find power for prefix " + uf.getPrefix());
						}
						else sbmluf.setScale(1);
						
						ud.addUnit(sbmluf);
					}
					else System.err.println("Error adding unit factor " + uf.getBaseUnit().getName()
							+ " to declaration for " + uom.getName() + ": " + uf.getBaseUnit().getName() + " is not a valid SBML unit Kind");
				}
			}
		}
	}
	
	
	/**
	 *  Collect compartments for SBML model
	 */
	private void addCompartments(){
		
		for(DataStructure ds : candidateDSsForCompartments){
			
			PhysicalModelComponent pmc = ds.getAssociatedPhysicalModelComponent();
			
			URI propphysdefuri = ds.getPhysicalProperty().getPhysicalDefinitionURI();
			
			// Go to next data structure if there isn't a physical property associated with the current one
			if(propphysdefuri == null) continue;
			
			Compartment comp = null;
			
			if(propphysdefuri.equals(SemSimLibrary.OPB_FLUID_VOLUME_URI))
				comp = sbmlmodel.createCompartment(ds.getName());
			
			// Only write out spatialDimensions attribute if compartment is not 3D
			else if(propphysdefuri.equals(SemSimLibrary.OPB_AREA_OF_SPATIAL_ENTITY_URI)){
				comp = sbmlmodel.createCompartment(ds.getName());
				comp.setSpatialDimensions(2);
			}
			else if(propphysdefuri.equals(SemSimLibrary.OPB_SPAN_OF_SPATIAL_ENTITY_URI)){
				comp = sbmlmodel.createCompartment(ds.getName());
				comp.setSpatialDimensions(1);
			}
			
			 // A Compartment object must have the required attributes 
			// 'id' and 'constant', and may have the optional attributes 
			// 'metaid', 'sboTerm', 'name', 'spatialDimensions', 'size' and 'units'. 
			comp.setConstant(ds.getComputationInputs().size()==0);
			
			// Go to next data structure if we didn't find an appropriate OPB property
			// or if the associated physical entity is NOT a composite physical entity
			if(comp == null || ! (pmc instanceof CompositePhysicalEntity)) continue;
						
			entityCompartmentMap.put((CompositePhysicalEntity)pmc, comp);

			CompositePhysicalEntity pmcAsCPE = (CompositePhysicalEntity)pmc;
			boolean oneentity =  pmcAsCPE.getArrayListOfEntities().size() == 1;
			boolean onerefentity = oneentity && pmcAsCPE.getArrayListOfEntities().get(0).hasPhysicalDefinitionAnnotation();
						
			// If composite physical entity is just one entity, store annotation in <compartment> block
			// and omit writing SemSim annotation
			if(onerefentity){
				DSsToOmitFromSemSimRDF.add(ds);
				ReferencePhysicalEntity refent = (ReferencePhysicalEntity) pmcAsCPE.getArrayListOfEntities().get(0);
				addAnnotationsAsCVterms(refent, comp);
			}
			
			boolean hasinputs = ds.getComputationInputs().size()>0;
			boolean hasIC = ds.hasStartValue();
			
			// If the compartment is a constant size
			if( ! hasinputs && ! hasIC){
				Double sizeAsDouble = getConstantValueForPropertyOfEntity(ds);	
				comp.setSize(sizeAsDouble); // Same as setValue();
			}
			// Otherwise create rule in model for compartment
			else if(ds.getComputation().hasMathML()) addRuleToModel(ds, comp);
			
			// Add units if needed
			setUnitsForModelComponent(comp, ds);
			
			addNotesAndMetadataID(pmc, comp);
		}
	}
	
	/**
	 *  Collect chemical species for SBML model
	 */
	private void addSpecies(){
		
		for(DataStructure ds : candidateDSsForSpecies){
			
			Computation dscomputation = ds.getComputation();
			
			// Assume that the first index in the physical entity associated with the data structure
			// is the chemical, item, or particle and the rest of the entity is the compartment
			PhysicalModelComponent pmc = ds.getAssociatedPhysicalModelComponent();
			
			if(pmc instanceof CompositePhysicalEntity){
				CompositePhysicalEntity fullcpe = (CompositePhysicalEntity)pmc;
								
				// From libSBML 5 spec:
				// It is important to note that there is no default value for the 'compartment'
				// attribute on Species; every species in an SBML model must be assigned a compartment explicitly. 
				
				// ...So we assign the compartment
				
				ArrayList<PhysicalEntity> compentlist = new ArrayList<PhysicalEntity>();
				compentlist.addAll(fullcpe.getArrayListOfEntities());
				
				ArrayList<StructuralRelation> comprellist = new ArrayList<StructuralRelation>();
				comprellist.addAll(fullcpe.getArrayListOfStructuralRelations());
				
				Boolean compartmentDefined = (compentlist.size()>1 && comprellist.size()>0);
				CompositePhysicalEntity compcpe = fullcpe;
				Compartment cptmt = null;

				if(compartmentDefined){
					compentlist.remove(0);
					comprellist.remove(0);

					compcpe = new CompositePhysicalEntity(compentlist, comprellist);
					
					SBase temp = lookupSBaseComponentInEntityMap(compcpe, entityCompartmentMap);
					
					// If we've found a suitable compartment in our SBase-component/physical entity map, use it...
					if(temp != null && (temp instanceof Compartment))
						cptmt = (Compartment)temp;
				}
					
				
				// .. if we don't have a compartment for the species, create a new one and add to entity-compartment map
				if(cptmt == null){
					int c = 0;
					String newcidstart = "compartment_";
					
					// Make a unique id for compartment
					while(sbmlmodel.containsCompartment(newcidstart + c)){
						c = c + 1;
					}
										
					cptmt = sbmlmodel.createCompartment(newcidstart + c);
					cptmt.setConstant(true); // Assume compartment is constant since we don't have any other info about it in the model
					cptmt.setSpatialDimensions(3); // Assuming a 3-D compartment
					cptmt.setSize(1.0); // Assuming size = 1
					
					if(compartmentDefined)  // If there was a compartment defined in the annotation, map the SemSim and SBML representations
						entityCompartmentMap.put(compcpe, cptmt);
				}
									
				// If the index entity in the composite entity is a custom term, use
				// the name of the custom term and be sure to store its annotations
				
				PhysicalEntity indexent = fullcpe.getArrayListOfEntities().get(0);
				
				Species species = sbmlmodel.createSpecies(ds.getName(), cptmt);
				
				if(indexent instanceof CustomPhysicalEntity)
					species.setName(indexent.getName());
				
				// Do not store the annotation for this data structure in the SemSim RDF
				// Preserve semantics in <species> element
				DSsToOmitFromSemSimRDF.add(ds);
				addAnnotationsAsCVterms(indexent, species);
					
				// In SBML Level 3 the hasSubstanceUnitsOnly must be set either way. In Level 2 the default is false.
				URI physdefprop = ds.getPhysicalProperty().getPhysicalDefinitionURI();
				
				boolean substanceonly = (physdefprop.equals(SemSimLibrary.OPB_CHEMICAL_MOLAR_AMOUNT_URI)
						|| physdefprop.equals(SemSimLibrary.OPB_PARTICLE_COUNT_URI)
						|| physdefprop.equals(SemSimLibrary.OPB_MASS_OF_SOLID_ENTITY_URI));
				
				species.setHasOnlySubstanceUnits(substanceonly);
				
				//TODO: deal with units on species if needed?
				
				boolean solvedwithconservation = false;
				
				// Get whether the species is computed using a conservation equation
				if( dscomputation.hasPhysicalDependency()){
					PhysicalDependency dep = dscomputation.getPhysicalDependency();
					
					if(dep.hasPhysicalDefinitionAnnotation()){
						URI uri = ((ReferencePhysicalDependency)dep).getPhysicalDefinitionURI();
						
						if(uri.equals(SemSimLibrary.OPB_DERIVATIVE_CONSTRAINT_URI)) 
							solvedwithconservation = true;
					}
				}
				
				boolean hasinputs = ds.getComputationInputs().size()>0;
				boolean hasIC = ds.hasStartValue();
				
				// See if the species is a source or sink in a process
				boolean issourceorsink = false;
				
				for(PhysicalProcess proc : semsimmodel.getPhysicalProcesses()){
					if(proc.getSourcePhysicalEntities().contains(fullcpe)
							|| proc.getSinkPhysicalEntities().contains(fullcpe)){
						issourceorsink = true;
						break;
					}
				}
								
				// Set isConstant and isBoundaryCondition values
				species.setConstant( ! hasinputs);

				if( ! hasinputs) species.setBoundaryCondition(issourceorsink);
				else species.setBoundaryCondition( ! solvedwithconservation);
				
				// If the species amount isn't solved with a conservation equation...
				if( ! solvedwithconservation){
					
					// ...if the species amount is constant
					if( ! hasinputs && ! hasIC){
						Double formulaAsDouble = getConstantValueForPropertyOfEntity(ds);		
					
						if(substanceonly) species.setInitialAmount(formulaAsDouble);
						else if (formulaAsDouble!=null) species.setInitialConcentration(formulaAsDouble);
					}
					// ...or if the species amount is set with a rule
					else addRuleToModel(ds, species);
				}
				
				// Set start value, if present.
				// TODO: We assumed the start value is a constant double. Eventually need to accommodate
				// expressions as initial conditions.
				if(ds.hasStartValue()){
					Double init = Double.parseDouble(ds.getStartValue());
					
					if(substanceonly) species.setInitialAmount(init);
					else if (init!=null) species.setInitialConcentration(init);
				}
				
				entitySpeciesMap.put(fullcpe, species);
				
				addNotesAndMetadataID(fullcpe, species);
			}
			
			// Otherwise the data structure is not associated with a physical entity and we 
			// treat it as a global parameter
			else globalParameters.add(ds);
		}
	}
	
	/**
	 *  Collect chemical reactions for SBML model
	 */
	private void addReactions(){
		
		for(DataStructure ds : candidateDSsForReactions){
			
			PhysicalModelComponent pmc = ds.getAssociatedPhysicalModelComponent();
			
			if(pmc instanceof CustomPhysicalProcess){
				
				CustomPhysicalProcess process = (CustomPhysicalProcess)pmc;
				
				Reaction rxn = sbmlmodel.createReaction(ds.getName());
				
				processReactionMap.put(process, rxn);
				
				// A <reaction> object must have the required attributes 'id',
				// 'reversible' and 'fast', and may have the optional attributes
				// 'metaid', 'sboTerm', 'name' and 'compartment'.
				rxn.setReversible(true); // TODO: extend SemSim object model to include this "reversible" attribute somewhere
				rxn.setFast(false); // TODO: extend SemSim object model to include this "fast" attribute somewhere
				
				// Do not store the annotation for this data structure in the SemSim RDF.
				// Preserve semantics in <reaction> element.
				DSsToOmitFromSemSimRDF.add(ds);
				
				KineticLaw kl = new KineticLaw();
				String mathml = ds.getComputation().getMathML();
	
				// Set reactants, products and modifiers based on SemSim sources, sinks and mediators.
				// Sources first...
				for(PhysicalEntity source : process.getSourcePhysicalEntities()){
					SBase tempsource = lookupSBaseComponentInEntityMap(source, entitySpeciesMap);
					
					if( tempsource != null && (tempsource instanceof Species)){
						SpeciesReference specref = new SpeciesReference((Species)tempsource);
						
						// Assume that the 'constant' attribute here is the same as that 
						// for the Species object.
						specref.setConstant(((Species)tempsource).isConstant()); 
						specref.setStoichiometry(process.getSourceStoichiometry(source));
						rxn.addReactant(specref);
					}
					else System.err.println("Couldn't find source " + source.getName() + " in map for reaction " + ds.getName());
				}
				
				// Sinks next...
				for(PhysicalEntity sink : process.getSinkPhysicalEntities()){
					SBase tempsink = lookupSBaseComponentInEntityMap(sink, entitySpeciesMap);
					
					if( tempsink != null && (tempsink instanceof Species)){
						SpeciesReference specref = new SpeciesReference((Species)tempsink);
						specref.setConstant(((Species)tempsink).isConstant()); 
						specref.setStoichiometry(process.getSinkStoichiometry(sink));
						rxn.addProduct(specref);
					}
					else System.err.println("Couldn't find sink " + sink.getName() + " in map for reaction " + ds.getName());
				}
				
				// Then mediators.
				for(PhysicalEntity mediator : process.getMediatorPhysicalEntities()){
					SBase tempmediator = lookupSBaseComponentInEntityMap(mediator, entitySpeciesMap);
					
					if( tempmediator != null && (tempmediator instanceof Species)){
						ModifierSpeciesReference specref = new ModifierSpeciesReference((Species)tempmediator);
						rxn.addModifier(specref);
					}
				}
				
				// Find any local parameters associated with reaction and add them to kinetic law
				Set<DataStructure> tempgpset = new HashSet<DataStructure>();
				tempgpset.addAll(globalParameters);
				
				for(DataStructure gp : tempgpset){
					
					String fullnm = gp.getName();
					
					if(fullnm.startsWith(SBMLreader.reactionprefix + ds.getName() + ".")){
						globalParameters.remove(gp);
						String localnm = fullnm.replace(SBMLreader.reactionprefix + ds.getName() + ".", "");
						mathml = mathml.replaceAll("<ci>\\s*" + gp.getName() + "\\s*</ci>", "<ci>" + localnm + "</ci>");

						LocalParameter lp = kl.createLocalParameter(localnm);
						String formula = getFormulaFromRHSofMathML(gp.getComputation().getMathML(), fullnm);
						lp.setValue(Double.parseDouble(formula));
						
						// Add units, if needed
						// Deal with units on compartments if needed
						setUnitsForModelComponent(lp, gp);
						assignMetaIDtoParameterIfAnnotated(gp);
						addNotesAndMetadataID(gp, lp);	
					}
				}
				
				kl.setMath(getASTNodeFromRHSofMathML(mathml, ds.getName()));
				rxn.setKineticLaw(kl);
				
				addNotesAndMetadataID(process, rxn);	
			}
			
			// Otherwise the data structure is not associated with a process
			else globalParameters.add(ds);

		}
	}
	
	/**
	 *  Collect discrete events for SBML model
	 */
	private void addEvents(){
		for(Event e : semsimmodel.getEvents()){
			
			org.sbml.jsbml.Event sbmle = sbmlmodel.createEvent();
			
			// Store delay info
			if(e.hasDelayMathML())
				sbmle.createDelay(JSBML.readMathMLFromString(e.getDelayMathML()));
				
			// Store priority info
			if(e.hasPriorityMathML())
				sbmle.createPriority(JSBML.readMathMLFromString(e.getPriorityMathML()));
			
			// Store name in "id" attribute
			if(e.hasName())
				sbmle.setId(e.getName());
				
			// Store description in "name" attribute
			if(e.hasDescription())
				sbmle.setName(e.getDescription());
			
			if(e.hasMetadataID())
				sbmle.setMetaId(e.getMetadataID());
			
			// Store trigger info
			sbmle.createTrigger(JSBML.readMathMLFromString(e.getTriggerMathML()));
			
			// Store each event assignment
			for(EventAssignment ea : e.getEventAssignments()){
				DataStructure output = ea.getOutput();
				sbmle.createEventAssignment(output.getName(), getASTNodeFromRHSofMathML(ea.getMathML(), output.getName()));
			}
			
			addNotesAndMetadataID(e, sbmle);
		}
	}
	
	/**
	 *  Collect global parameters for SBML model
	 */
	private void addGlobalParameters(){
		
		for(DataStructure ds : globalParameters){
			
			Parameter par = sbmlmodel.createParameter(ds.getName());
						
			String mathml = ds.getComputation().getMathML();
			
			boolean hasinputs = ds.getComputationInputs().size() > 0;
			boolean usesevents = ds.getComputation().hasEvents();
			boolean hasmathml = (mathml != null && ! mathml.equals(""));
			boolean hasIC = ds.hasStartValue();
			
			if(usesevents){
				par.setConstant(false);
				
				if(hasmathml){
					
					// Assumption: valid to have a parameter that updates with an event and is solved with an ODE
					// but not valid if parameter updates with an event and is solved algebraically.
					if(hasinputs && hasIC){
						RateRule rr = sbmlmodel.createRateRule();
						rr.setMath(getASTNodeFromRHSofMathML(mathml, ds.getName()));
						rr.setVariable(ds.getName());
						par.setValue(Double.parseDouble(ds.getStartValue()));
					}
					else{
						String formula = getFormulaFromRHSofMathML(mathml, ds.getName());
						par.setValue(Double.parseDouble(formula));
					}
				}
			}
			else if(hasinputs){
				par.setConstant(false);
				addRuleToModel(ds, par);
			}
			else if(hasmathml){
				par.setConstant(true);
				String formula = getFormulaFromRHSofMathML(mathml, ds.getName());
				
				if(formula != null){

					try {
			            Double d = Double.parseDouble(formula);
						par.setValue(d);
			        } catch (NumberFormatException e) {
			        	// If we're here we have some equation that just uses numbers, not variables
			        	addRuleToModel(ds, par);
			        }
				}
			}
			else if(ds instanceof MappableVariable){

				Double doubleval = getConstantValueForPropertyOfEntity(ds);
				
				if(doubleval != null) 
					par.setValue(doubleval);
				
				par.setConstant(true);
			}
			else par.setConstant(true);
						
			assignMetaIDtoParameterIfAnnotated(ds);
			
			// TODO: we assume no 0 = f(p) type rules (i.e. SBML algebraic rules). Need to eventually account for them

			addNotesAndMetadataID(ds, par);
			
			setUnitsForModelComponent(par, ds);
		}
	}
	
	
	/**
	 *  Add the relational constraints to the SBML model
	 */
	private void addConstraints(){
		
		for(RelationalConstraint rc : semsimmodel.getRelationalConstraints()){
			Constraint c = sbmlmodel.createConstraint();
			c.setMath(getASTNodeFromRHSofMathML(rc.getMathML(), ""));
		}
	}
	
	/**
	 *  Add the SemSim-structured annotations for the model elements
	 */ 
	private Document addSemSimRDF(){
		
		rdfblock = new SemSimRDFwriter(semsimmodel, ModelType.SBML_MODEL);
		
		rdfblock.setRDFforModelLevelAnnotations();
		
		// NOTE: SemSim-style submodels are not currently preserved on SBML export
		
		for(DataStructure ds : semsimmodel.getAssociatedDataStructures()){
		
			// Only the info for compartments and parameters are stored (i.e. not species or reactions)
			// because the <species> and <reaction> elements already contain the needed info
			if(DSsToOmitFromSemSimRDF.contains(ds)) continue;
			else rdfblock.setRDFforDataStructureAnnotations(ds);
		}
		
		Document doc = null;

		try {
			
			String sbmlstring = new SBMLWriter().writeSBMLToString(sbmldoc);
			
			SAXBuilder builder = new SAXBuilder();
			
			InputStream is = new ByteArrayInputStream(sbmlstring.getBytes("UTF-8"));
			doc = builder.build(is);
			
			// Add the RDF metadata to the appropriate element in the SBML file
			if( ! rdfblock.rdf.isEmpty()){
				
				String rawrdf = SemSimRDFwriter.getRDFmodelAsString(rdfblock.rdf);			
				Content newrdf = ModelWriter.makeXMLContentFromString(rawrdf);
				
				Element modelel = doc.getRootElement().getChild("model", sbmlNS);
				
				if(modelel == null){
					System.err.println("SBML writer error - no 'model' element found in XML doc");
					return doc;
				}
				
				Element modelannel = modelel.getChild("annotation", sbmlNS);
				
				if(modelannel == null){
					modelannel = new Element("annotation", sbmlNS);
					modelel.addContent(modelannel);
				}
				
				Element ssannel = modelannel.getChild(semsimAnnotationElementName, RDFNamespace.SEMSIM.createJdomNamespace());
				
				if(ssannel == null){
					ssannel = new Element(semsimAnnotationElementName, RDFNamespace.SEMSIM.createJdomNamespace()); 
					modelannel.addContent(ssannel);
				}
				// Remove old RDF if present
				else modelannel.removeContent(ssannel);
				
				// Add the SemSim RDF
				if(newrdf !=null) ssannel.addContent(newrdf);
			}
		} catch (XMLStreamException | IOException | JDOMException e) {
			e.printStackTrace();
		} 
		
		return doc;
	}
	
	
	//*** HELPER METHODS ***//
	
	/**
	 *  Returns a JSBML ASTNode for the right-hand-side of a mathml expression
	 * @param mathml The MathML expression
	 * @param localname The name of the variable solved by the expression
	 * @return An ASTNode representation of the right-hand-side of the expression
	 */
	private ASTNode getASTNodeFromRHSofMathML(String mathml, String localname){
		
		String RHS = SemSimUtil.getRHSofMathML(mathml, localname);
		
		// Deal with unit declarations on <cn> elements in MathML 
		if(RHS.contains("cellml:units")){
			RHS = RHS.replaceFirst("xmlns=\"" + RDFNamespace.MATHML.getNamespaceasString() + "\"",
					"xmlns=\"" + RDFNamespace.MATHML.getNamespaceasString() + "\"" + " "
							+ "xmlns:sbml3_1=\"" + SBMLDocument.URI_NAMESPACE_L3V1Core + "\"");
			RHS = RHS.replace(cellmlInlineUnitsNSdeclaration, "");
			RHS = RHS.replace("cellml:units", "sbml3_1:units");
		}
		
		// TODO: In JSBML 1.1. there is a bug that prevents <cn> elements with type="e-notation"
		// AND units to be written out correctly. Will be fixed in 1.2 release.
		
		return JSBML.readMathMLFromString(RHS);
	}
	
	/**
	 *  Returns a JSBML formula representation of the RHS of a mathml expression
	 * @param mathml The MathML expression
	 * @param localname The name of the variable solved by the expression
	 * @return An ASTNode representation of the right-hand-side of the expression
	 */
	private String getFormulaFromRHSofMathML(String mathml, String localname){
		ASTNode node = getASTNodeFromRHSofMathML(mathml, localname);
		return JSBML.formulaToString(node);	
	}
	
	private SBase lookupSBaseComponentInEntityMap(PhysicalModelComponent pmc, LinkedHashMap<? extends PhysicalModelComponent, ? extends SBase> map){
		
		for(PhysicalModelComponent testpmc : map.keySet()){
							
			if(pmc.equals(testpmc)) return map.get(testpmc);
		}
		return null;
	}
	
	
	/**
	 *  Fill in "name" and "metadataID" attributes on an SBase object
	 * @param sso The SemSimObject that contains the name and metadataID to be copied
	 * @param sbo The AbstractNamedSBase object that the name and metadataID will be copied to
	 */
	private void addNotesAndMetadataID(SemSimObject sso, AbstractNamedSBase sbo){
		
		//TODO: When the jsbml folks fix the issue with redeclaring xml namespaces in notes, uncomment block below
//		if(sso.hasDescription()){
//			try {
//				sbo.setNotes(sso.getDescription());
//			} catch (XMLStreamException e) {
//				e.printStackTrace();
//			}
//		}
		
		if(sso.hasMetadataID()){
			
			if( metaIDsUsed.contains(sso.getMetadataID()))
				System.err.println("Warning: attempt to assign metadataID " + sso.getMetadataID() + " to two or more SBase objects: ");
			
			else{
				sbo.setMetaId(sso.getMetadataID());
				metaIDsUsed.add(sso.getMetadataID());
			}
		}
	}
	
	private void setUnitsForModelComponent(QuantityWithUnit qwu, DataStructure ds){
		
		if(ds.hasUnits()){
			String uomname = ds.getUnit().getName();
			
			if( sbmlmodel.containsUnitDefinition(uomname)){ 
				qwu.setUnits(uomname);
			}
		}
	}
	
	private void addRuleToModel(DataStructure ds, Symbol sbmlobj){
		ExplicitRule rule = null;
		
		if(ds.getComputation().hasMathML()){
			String mathml = ds.getComputation().getMathML();
			
			if(ds.hasStartValue()){
				rule = sbmlmodel.createRateRule();
				sbmlobj.setValue(Double.parseDouble(ds.getStartValue())); // Set IC for parameters
			}
			else rule = sbmlmodel.createAssignmentRule();
			
			rule.setMath(getASTNodeFromRHSofMathML(mathml, ds.getName()));
			rule.setVariable(ds.getName());
		}
	}
	
	private void addAnnotationsAsCVterms(PhysicalModelComponent pmc, SBase sbmlobj){
		
		
		// For ReferencePhysicalEntities and ReferencePhysicalProcesses, we collect annotations this way
		if(pmc instanceof ReferenceTerm){
			CVTerm cvterm = new CVTerm();
			ReferenceTerm refterm = (ReferenceTerm)pmc;
			cvterm.setQualifier(Qualifier.BQB_IS);
			String uriasstring = SemSimRDFwriter.convertURItoIdentifiersDotOrgFormat(refterm.getPhysicalDefinitionURI()).toString();
			cvterm.addResourceURI(uriasstring);
			sbmlobj.addCVTerm(cvterm);
		}
		
		// For custom physical components we do it this way
		else{
			// Preserve semantics on an sbml element
			for(Annotation ann: pmc.getAnnotations()){
								
				if(ann instanceof ReferenceOntologyAnnotation){
					CVTerm cvterm = new CVTerm();
					Qualifier qualifier = null;

					ReferenceOntologyAnnotation refann = (ReferenceOntologyAnnotation)ann; 
					Relation relation = refann.getRelation();
					
					if(relation.equals(SemSimRelation.BQB_IS_VERSION_OF)
							|| relation.equals(StructuralRelation.HAS_PART) 
							|| relation.equals(StructuralRelation.BQB_HAS_PART)){
						qualifier = SemSimRelations.getBiologicalQualifierFromRelation(relation);
					}
					else continue;
					
					cvterm.setQualifier(qualifier);
					String uriasstring = SemSimRDFwriter.convertURItoIdentifiersDotOrgFormat(refann.getReferenceURI()).toString();
					cvterm.addResourceURI(uriasstring);
					sbmlobj.addCVTerm(cvterm);
				}
			}
		}
	}
	
	private Double getConstantValueForPropertyOfEntity(DataStructure ds){
		Double formulaAsDouble = null;
		
		if(ds.getComputation().hasMathML()){
			String formula = getFormulaFromRHSofMathML(ds.getComputation().getMathML(), ds.getName());
			formulaAsDouble = Double.parseDouble(formula);
		}
		
		// If the Data Structure is a MappableVariable and constant it won't have
		// any MathML associated with it in the computation, so we look up the CellMLintialValue
		else if(ds instanceof MappableVariable){
			MappableVariable mv = (MappableVariable)ds;
			
			if(mv.hasCellMLinitialValue())	
				formulaAsDouble = Double.parseDouble(mv.getCellMLinitialValue());
			else
				System.err.println(mv.getName() + " will need to be set at runtime "); }
		

		return formulaAsDouble;
	}
	
	private void assignMetaIDtoParameterIfAnnotated(DataStructure ds){
		// If the parameter is annotated, and doesn't have a meta id, give it one
		if((ds.hasDescription() || ds.hasPhysicalProperty() || ds.hasPhysicalDefinitionAnnotation()) && ! ds.hasMetadataID())
			semsimmodel.assignValidMetadataIDtoSemSimObject(ds.getName(), ds);
	}
	


}
