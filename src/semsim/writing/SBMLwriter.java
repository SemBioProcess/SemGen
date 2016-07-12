package semsim.writing;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
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
import org.sbml.jsbml.Compartment;
import org.sbml.jsbml.Constraint;
import org.sbml.jsbml.ExplicitRule;
import org.sbml.jsbml.JSBML;
import org.sbml.jsbml.KineticLaw;
import org.sbml.jsbml.LocalParameter;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.ModifierSpeciesReference;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.RateRule;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLWriter;
import org.sbml.jsbml.SBase;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;
import org.sbml.jsbml.Unit;
import org.sbml.jsbml.UnitDefinition;
import org.sbml.jsbml.Unit.Kind;
import org.semanticweb.owlapi.model.OWLException;

import semsim.SemSimLibrary;
import semsim.SemSimObject;
import semsim.definitions.RDFNamespace;
import semsim.definitions.SBMLconstants;
import semsim.definitions.SemSimRelations.StructuralRelation;
import semsim.model.collection.SemSimModel;
import semsim.model.computational.Computation;
import semsim.model.computational.Event;
import semsim.model.computational.RelationalConstraint;
import semsim.model.computational.Event.EventAssignment;
import semsim.model.computational.datastructures.DataStructure;
import semsim.model.computational.units.UnitFactor;
import semsim.model.computational.units.UnitOfMeasurement;
import semsim.model.physical.PhysicalDependency;
import semsim.model.physical.object.ReferencePhysicalDependency;
import semsim.model.physical.PhysicalEntity;
import semsim.model.physical.PhysicalModelComponent;
import semsim.model.physical.PhysicalProcess;
import semsim.model.physical.object.CompositePhysicalEntity;
import semsim.model.physical.object.CustomPhysicalProcess;
import semsim.reading.SBMLreader;
import semsim.reading.SemSimRDFreader;
import semsim.utilities.SemSimUtil;

public class SBMLwriter extends ModelWriter {
	
	private SBMLDocument sbmldoc;
	private Model sbmlmodel;
	private static final int sbmllevel = 2;
	private static final int sbmlversion = 4;
	
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

	@Override
	public void writeToFile(File destination) {
				
		sbmldoc = new SBMLDocument();
		sbmlmodel = sbmldoc.createModel(semsimmodel.getName());
		
		
		// TODO: Need to work out how to store SBase names and notes in SemSim objects
		// Currently notes are set in description field and names are ignored
		
		addNotesAndMetadataID(semsimmodel, sbmlmodel);
		
		sortDataStructuresIntoSBMLgroups();
		setLevelAndVersion();
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
			
			return;
		}
	
		String outputstring =  new XMLOutputter().outputString(doc);
		SemSimUtil.writeStringToFile(outputstring, destination);
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
	
	private void setLevelAndVersion(){
		sbmldoc.setLevel(sbmllevel);
		sbmldoc.setVersion(sbmlversion);
		sbmlmodel.setLevel(sbmllevel);
		sbmlmodel.setVersion(sbmlversion);
	}
	
	/**
	 *  Collect units for SBML model
	 */
	private void addUnits(){
		
		for(UnitOfMeasurement uom : semsimmodel.getUnits()){	
			
			if(Kind.isValidUnitKindString(uom.getName(), sbmllevel, sbmlversion)) 
				continue;
			
			else if( ! SBMLconstants.SBML_LEVEL_2_RESERVED_UNITS_MAP.containsKey(uom.getName())
					&& ! SBMLconstants.SBML_LEVEL_2_RESERVED_UNITS_MAP.containsValue(uom.getName())){
				
				UnitDefinition ud = sbmlmodel.createUnitDefinition(uom.getName());
				
				addNotesAndMetadataID(uom, ud);
				
				for(UnitFactor uf : uom.getUnitFactors()){
					
					if(uf.getBaseUnit()==null){
						System.err.println("Found a null base unit for a unit factor in unit " + uom.getName());
						continue;
					}
					
					String factorbasename = uf.getBaseUnit().getName();
					
					if(Kind.isValidUnitKindString(factorbasename, sbmllevel, sbmlversion)){
						
						Unit sbmluf = new Unit(sbmllevel, sbmlversion);
						sbmluf.setKind(Kind.valueOf(factorbasename.toUpperCase()));
						sbmluf.setExponent(uf.getExponent());
						sbmluf.setMultiplier(uf.getMultiplier());
						
						if(uf.getPrefix() != null && ! uf.getPrefix().equals("")){
							Integer power = sslib.getUnitPrefixesAndPowersMap().get(uf.getPrefix());
							
							if(power!=null) sbmluf.setScale(power);
							else System.err.println("Couldn't find power for prefix " + uf.getPrefix());
						}
						
						ud.addUnit(sbmluf);
					}
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
			
			// Go to next data structure if we didn't find an appropriate OPB property
			// or if the associated physical entity is NOT a composite physical entity
			if(comp == null || ! (pmc instanceof CompositePhysicalEntity)) continue;
			
			entityCompartmentMap.put((CompositePhysicalEntity)pmc, comp);
			
			// TODO: if composite physical entity is just one entity, store annotation in <compartment> block
			// and omit writing SemSim annotation
			
			String mathml = ds.getComputation().getMathML();
			
			boolean hasinputs = ds.getComputationInputs().isEmpty();
			boolean hasmathml = mathml != null;
			
			// TODO: if size of compartment is variable, need to create an assignment
			if( ! hasinputs && hasmathml){
				
				//TODO: if mappable variable, need to use local name of datastructure as second parameter here
				String formula = getFormulaFromRHSofMathML(mathml, ds.getName());
				comp.setSize(Double.parseDouble(formula));
			}
			else if(hasinputs && hasmathml) addRuleToModel(ds);
			
			// TODO: if composite entity for compartment only has one entity, exclude from 
			// SemSim annotations and instead write it as an annotation on the <comp> element
			addNotesAndMetadataID(pmc, comp);
		}
	}
	
	/**
	 *  Collect chemical species for SBML model
	 */
	private void addSpecies(){
		
		int c = 0; // index number for any compartments that we add anew
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
				compentlist.remove(0);
				
				ArrayList<StructuralRelation> comprellist = new ArrayList<StructuralRelation>();
				comprellist.addAll(fullcpe.getArrayListOfStructuralRelations());
				comprellist.remove(0);
				
				CompositePhysicalEntity compcpe = new CompositePhysicalEntity(compentlist, comprellist);
				
				Compartment cptmt = null;
				
				SBase temp = lookupSBaseComponentInEntityMap(compcpe, entityCompartmentMap);
				
				if(temp != null && (temp instanceof Compartment))
					cptmt = (Compartment)temp;
				
				
				// If we don't have a compartment for the species, create a new one and add to entity-compartment map
				if(cptmt == null){
					c = c + 1;
					cptmt = sbmlmodel.createCompartment("compartment_" + c);
					entityCompartmentMap.put(compcpe, cptmt);
				}
												
				Species species = sbmlmodel.createSpecies(ds.getName(), cptmt);
				
				// Do not store the annotation for this data structure in the SemSim RDF
				// Preserve semantics in <species> element
				DSsToOmitFromSemSimRDF.add(ds);
					
				// In SBML Level 3 the hasSubstanceUnitsOnly must be set either way. In Level 2 the default is false.
				URI physdefprop = ds.getPhysicalProperty().getPhysicalDefinitionURI();
				
				boolean substanceonly = (physdefprop.equals(SemSimLibrary.OPB_CHEMICAL_MOLAR_AMOUNT_URI)
						|| physdefprop.equals(SemSimLibrary.OPB_PARTICLE_COUNT_URI)
						|| physdefprop.equals(SemSimLibrary.OPB_MASS_OF_SOLID_ENTITY_URI));
				
				species.setHasOnlySubstanceUnits(substanceonly);
				
				
				boolean solvedwithconservation = false;
				
				if( dscomputation.hasPhysicalDependency()){
					PhysicalDependency dep = dscomputation.getPhysicalDependency();
					
					if(dep.hasPhysicalDefinitionAnnotation()){
						URI uri = ((ReferencePhysicalDependency)dep).getPhysicalDefinitionURI();
						
						if(uri.equals(SemSimLibrary.OPB_DERIVATIVE_CONSTRAINT_URI)) 
							solvedwithconservation = true;
					}
				}
				
				boolean hasinputs = ds.getComputationInputs().size()>0;
				
				// See if the species is a source or sink in a process
				boolean isourceorsink = false;
				
				for(PhysicalProcess proc : semsimmodel.getPhysicalProcesses()){
					if(proc.getSourcePhysicalEntities().contains(fullcpe)
							|| proc.getSinkPhysicalEntities().contains(fullcpe)){
						isourceorsink = true;
						break;
					}
				}
								
				// Set isConstant and isBoundaryCondition values
				species.setConstant( ! hasinputs);

				if( ! hasinputs) species.setBoundaryCondition(isourceorsink);
				else species.setBoundaryCondition( ! solvedwithconservation);
				
				if( ! solvedwithconservation && ! species.getConstant()) addRuleToModel(ds);
				
				// Set start value, if present.
				// TODO: We assumed the start value is a constant double. Eventually need to accommodate
				// expressions as initial conditions.
				if(ds.hasStartValue()){
					Double init = Double.parseDouble(ds.getStartValue());
					
					if(substanceonly) species.setInitialAmount(init);
					else species.setInitialConcentration(init);
				}
				
				entitySpeciesMap.put(fullcpe, species);
				
				addNotesAndMetadataID(fullcpe, species);
			}
			
			// Otherwise the data structure is not associated with a physical entity
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
						specref.setStoichiometry(process.getSourceStoichiometry(source));
						rxn.addReactant(specref);
					}
				}
				
				// Sinks next...
				for(PhysicalEntity sink : process.getSinkPhysicalEntities()){
					SBase tempsink = lookupSBaseComponentInEntityMap(sink, entitySpeciesMap);
					
					if( tempsink != null && (tempsink instanceof Species)){
						SpeciesReference specref = new SpeciesReference((Species)tempsink);
						specref.setStoichiometry(process.getSinkStoichiometry(sink));
						rxn.addProduct(specref);
					}
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
					}
				}
				
				kl.setMath(getASTNodeFromRHSofMathML(mathml, ds.getName()));	
				rxn.setKineticLaw(kl);
				
				addNotesAndMetadataID(process, rxn);
				
				// TODO: set units?
			}
			
			// Otherwise the data structure isn't associated with a process
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
					}
					else{
						// TODO: probably need to get local name for mappableVariables here.
						String formula = getFormulaFromRHSofMathML(mathml, ds.getName());
						par.setValue(Double.parseDouble(formula));
					}
				}
			}
			else if(hasinputs){
				par.setConstant(false);
				addRuleToModel(ds);
			}
			else if(hasmathml){
				// TODO: probably need to get local name for mappableVariables here.
				String formula = getFormulaFromRHSofMathML(mathml, ds.getName());
				try {
		            Double d = Double.parseDouble(formula);
					par.setValue(d);
		        } catch (NumberFormatException e) {
		        	// If we're here we have some equation that just uses numbers, not variables
		        	addRuleToModel(ds);
		        }
			}
			
			// TODO: we assume no 0 = f(p) type rules (i.e. SBML algebraic rules). Need to eventually account for them
			addNotesAndMetadataID(ds, par);
			
			// TODO: set units
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
		
		rdfblock = new SemSimRDFwriter(semsimmodel);
		rdfblock.setRDFforModelLevelAnnotations();
		
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
				
				String rawrdf = SemSimRDFreader.getRDFmodelAsString(rdfblock.rdf);			
				Content newrdf = ModelWriter.makeXMLContentFromString(rawrdf);
				Namespace sbmlmodNS = Namespace.getNamespace("http://www.sbml.org/sbml/level" + sbmlmodel.getLevel() + "/version" + sbmlmodel.getVersion());
				
				Element modelel = doc.getRootElement().getChild("model", sbmlmodNS);
				
				if(modelel == null){
					System.err.println("SBML writer error - no 'model' element found in XML doc");
					return doc;
				}
				
				Element modelannel = modelel.getChild("annotation", sbmlmodNS);
				
				if(modelannel == null){
					modelannel = new Element("annotation", sbmlmodNS);
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
		
		if(sso.hasDescription())
			try {
				sbo.setNotes(sso.getDescription());
			} catch (XMLStreamException e) {
				e.printStackTrace();
			}
		
		if(sso.hasMetadataID()){
			
			if( metaIDsUsed.contains(sso.getMetadataID()))
				System.err.println("Warning: attempt to assign metadataID " + sso.getMetadataID() + " to two or more SBase objects: ");
			
			else{
				sbo.setMetaId(sso.getMetadataID());
				metaIDsUsed.add(sso.getMetadataID());
			}
		}
	}
	
	private void addRuleToModel(DataStructure ds){
		ExplicitRule rule = null;
		String mathml = ds.getComputation().getMathML();
		
		if(ds.hasStartValue()) rule = sbmlmodel.createRateRule();
		else rule = sbmlmodel.createAssignmentRule();
		
		rule.setMath(getASTNodeFromRHSofMathML(mathml, ds.getName()));
		rule.setVariable(ds.getName());
	}
	
	
	@Override
	public void writeToFile(URI uri) throws OWLException {
		writeToFile(new File(uri));

	}

}
