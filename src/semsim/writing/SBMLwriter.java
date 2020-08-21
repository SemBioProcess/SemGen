package semsim.writing;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
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
import org.sbml.jsbml.AbstractSBase;
import org.sbml.jsbml.CVTerm;
import org.sbml.jsbml.CVTerm.Qualifier;
import org.sbml.jsbml.Compartment;
import org.sbml.jsbml.Constraint;
import org.sbml.jsbml.ExplicitRule;
import org.sbml.jsbml.InitialAssignment;
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
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.SBMLWriter;
import org.sbml.jsbml.SBase;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;
import org.sbml.jsbml.Symbol;
import org.sbml.jsbml.Unit;
import org.sbml.jsbml.UnitDefinition;
import org.sbml.jsbml.Unit.Kind;
import org.sbml.jsbml.validator.SyntaxChecker;

import org.apache.jena.rdf.model.Resource;

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
import semsim.fileaccessors.OMEXAccessor;
import semsim.model.collection.SemSimModel;
import semsim.model.computational.Computation;
import semsim.model.computational.Event;
import semsim.model.computational.RelationalConstraint;
import semsim.model.computational.SBMLInitialAssignment;
import semsim.model.computational.EventAssignment;
import semsim.model.computational.datastructures.DataStructure;
import semsim.model.computational.datastructures.SBMLFunctionOutput;
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
import semsim.reading.SBMLreader;
import semsim.reading.ModelClassifier.ModelType;
import semsim.utilities.ErrorLog;
import semsim.utilities.SemSimUtil;

/**
 * Class for translating a SemSim model into an SBML model
 * @author mneal
 *
 */
public class SBMLwriter extends ModelWriter {
	
	private SBMLDocument sbmldoc;
	private Model sbmlmodel;
	private static final int sbmllevel = 3;
	private static final int sbmlversion = 1;
	
	private static final Namespace sbmlNS = Namespace.getNamespace(SBMLDocument.URI_NAMESPACE_L3V1Core);
	
	private static final String cellmlInlineUnitsNSdeclaration = "xmlns:cellml=\"http://www.cellml.org/cellml/1.0#\"";	
	private Map<String,String> oldAndNewUnitNameMap = new HashMap<String,String>();
	private Map<String,String> oldAndNewParameterNameMap = new HashMap<String,String>();
	
	private LinkedHashMap<CompositePhysicalEntity, Compartment> entityCompartmentMap = new LinkedHashMap<CompositePhysicalEntity, Compartment>();
	private LinkedHashMap<CompositePhysicalEntity, Species> entitySpeciesMap = new LinkedHashMap<CompositePhysicalEntity, Species>();
	private LinkedHashMap<CustomPhysicalProcess, Reaction> processReactionMap = new LinkedHashMap<CustomPhysicalProcess, Reaction>();
	
	// Data Structures whose annotations will be omitted in the SemSimRDF block.
	// This includes species and reaction elements. DataStructures representing
	// properties of species and reactions will have their annotations stored
	// in the usual SBML annotation element.
	private Set<DataStructure> DSsToOmitFromCompositesRDF = new HashSet<DataStructure>();
	
	private Set<DataStructure> candidateDSsForCompartments = new HashSet<DataStructure>();
	private Set<DataStructure> candidateDSsForSpecies = new HashSet<DataStructure>();
	private Set<DataStructure> candidateDSsForReactions = new HashSet<DataStructure>();
	private ArrayList<String> globalParameters = new ArrayList<String>();
	
	private AbstractRDFwriter rdfwriter;
	private Set<String> metaIDsUsed = new HashSet<String>();
	public static String semsimAnnotationElementName = "semsimAnnotation";
	
	public SBMLwriter(SemSimModel model) {
		super(model);
	}

	public SBMLwriter(SemSimModel model, boolean OMEXmetadata) { // Change this to include an OMEXarchiveWriter?
		super(model, OMEXmetadata);
	}

	// JKM: generates and returns an XML document based on the SBML
	private Document writeSBMLtoXML() {
		
		sbmldoc = new SBMLDocument(sbmllevel, sbmlversion);
		String sbmlmodname = semsimmodel.getName();

		// Create a valid model name if current name is invalid
		if( ! SyntaxChecker.isValidId(sbmlmodname, sbmllevel, sbmlversion)) sbmlmodname = "default_name";

		sbmlmodel = sbmldoc.createModel(sbmlmodname);

		if(semsimmodel.hasMetadataID())
			sbmlmodel.setMetaId(semsimmodel.getMetadataID());

		// If we're writing from a model with FunctionalSubmodels, flatten model first
		if(semsimmodel.getFunctionalSubmodels().size() > 0){
			SemSimModel modelcopy = semsimmodel.clone(); // Create a temporary clone because flattening changes model contents
			semsimmodel = modelcopy;
			SemSimUtil.flattenModel(semsimmodel);
		}

		// Initialize an OMEX metadata writer, if we're writing to an OMEX file
		if(getWriteLocation() instanceof OMEXAccessor){
			rdfwriter = new OMEXmetadataWriter(semsimmodel);
			String archivefn = ((OMEXAccessor)getWriteLocation()).getArchiveFileName();
			String modfn = getWriteLocation().getFileName();
			String rdffn = modfn.replaceFirst("\\..*$", ".rdf");
			rdfwriter.setModelNamespaceInRDF(RDFNamespace.OMEX_LIBRARY.getNamespaceAsString() + archivefn + "/" + modfn);
			rdfwriter.setLocalNamespaceInRDF(RDFNamespace.OMEX_LIBRARY.getNamespaceAsString() + archivefn + "/" + rdffn);
			rdfwriter.rdf.setNsPrefix("myOMEX", RDFNamespace.OMEX_LIBRARY.getNamespaceAsString() + archivefn);
			rdfwriter.rdf.setNsPrefix("local", rdfwriter.getLocalNamespace());
			rdfwriter.setRDFforModelLevelAnnotations();
		}
		else addModelLevelCVTerms();

		// TODO: Need to work out how to store SBase names and notes in SemSim objects
		// Currently notes are set in description field and names are ignored

		addNotesAndMetadataID(semsimmodel, sbmlmodel);
		sortDataStructuresIntoSBMLgroups();
		addUnits();
		addCompartments();
		addSpecies();
		addReactions();
		addFunctionDefinitions();
		addGlobalParameters();
		addEvents();
		addSBMLinitialAssignments();
		addConstraints();

		Document doc = OMEXmetadataEnabled() ? addOMEXmetadataRDF() : addSemSimRDF();

		// If no errors, return the model.
		// First catch errors
		if(sbmldoc.getErrorCount() > 0){

			String errors = "";
			for(int i = 0; i< sbmldoc.getErrorCount(); i++){
				errors = errors + "\n\n" + sbmldoc.getError(i);
			}
			ErrorLog.addError("The resulting SBML model had the following errors:\n\n" + errors, true, false);
			return null;
		} else {
			return doc;
		}
	}

	
	@Override
	public String encodeModel() {
		return new XMLOutputter().outputString(writeSBMLtoXML());
	}
	

	/**
	 * Gets the RDF writer used by this SBMLwriter.
	 */
	public AbstractRDFwriter getRDFWriter() {
		return rdfwriter;
	}
	

	/**
	 * Add model-level annotations as CVTerms within the SBML code. This should not be used 
	 * when writing to an OMEX file.
	 */
	private void addModelLevelCVTerms(){
		
		for(ReferenceOntologyAnnotation ann : semsimmodel.getReferenceOntologyAnnotations()){
			
			CVTerm cv = new CVTerm();
			
			Relation rel = ann.getRelation();
			
			Qualifier qual = SemSimRelations.getBiologicalQualifierFromRelation(rel);
			if(qual==null) qual = SemSimRelations.getModelQualifierFromRelation(rel);
			if(qual==null) continue;
			
			cv.setQualifier(qual);
			cv.addResource(ann.getReferenceURI().toString());
			
			sbmlmodel.addCVTerm(cv);
		}
	}
	
	
	/**
	 *  Determine which data structures potentially simulate properties of SBML compartments,
	 *  species, and reactions
	 */
	private void sortDataStructuresIntoSBMLgroups(){

		Set<String> existingnames = semsimmodel.getDataStructureNames();

		for(DataStructure ds : semsimmodel.getAssociatedDataStructures()){
			
			if(ds.hasPhysicalProperty()){
				
				URI propphysdefuri = ds.getPhysicalProperty().getPhysicalDefinitionURI();
				
				if(SBMLconstants.OPB_PROPERTIES_FOR_COMPARTMENTS.contains(propphysdefuri))
					candidateDSsForCompartments.add(ds);
				
				else if(SBMLconstants.OPB_PROPERTIES_FOR_SPECIES.contains(propphysdefuri))
					candidateDSsForSpecies.add(ds);
				
				else if(SBMLconstants.OPB_PROPERTIES_FOR_REACTIONS.contains(propphysdefuri))
					candidateDSsForReactions.add(ds);
				
				else globalParameters.add(ds.getName());
				
			}
			else if ( ! ds.isSolutionDomain() && ! (ds instanceof SBMLFunctionOutput) && ds.isDeclared()) 
				globalParameters.add(ds.getName());
			
			if(ds.getName().contains("."))
				makeDataStructureNameValid(ds, existingnames);
		}
	}
	
	
	/** Collect units for SBML model */
	private void addUnits(){

		// First rename any units that don't have SBML-compliant names and create a map
		// relating the old names to the new
		SemSimUtil.createUnitNameMap(semsimmodel,oldAndNewUnitNameMap);

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
				String unitname = uom.getName();
				if(oldAndNewUnitNameMap.containsKey(unitname)) unitname = oldAndNewUnitNameMap.get(unitname);
				UnitDefinition ud = sbmlmodel.createUnitDefinition(unitname);
				
				addNotesAndMetadataID(uom, ud);
				
				Set<UnitFactor> ufset = SemSimUtil.recurseBaseUnits(uom, 1.0, ModelWriter.sslib);
				
				for(UnitFactor uf : ufset){
					
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
						else sbmluf.setScale(0);
						
						ud.addUnit(sbmluf);
					}
					else ErrorLog.addError("Error adding unit factor " + uf.getBaseUnit().getName() + " to declaration for " + uom.getName() + ": " + uf.getBaseUnit().getName() + " is not a valid SBML unit Kind", true, false);
				}
			}
		}
	}
	
	
	/** Collect compartments for SBML model */
	private void addCompartments(){
		
		for(DataStructure ds : candidateDSsForCompartments){
			
			PhysicalModelComponent pmc = ds.getAssociatedPhysicalModelComponent();
			
			URI propphysdefuri = ds.getPhysicalProperty().getPhysicalDefinitionURI();
			
			// Go to next data structure if there isn't a physical property associated with the current one
			// or if there is a multi-entity composite physical entity used in the data structure's
			// composite annotation (even if there's a valid physical property, the full composite
			// needs to be stored in either the SemSim RDF block or the OMEX metadata file)
			if(propphysdefuri == null || ! (pmc instanceof CompositePhysicalEntity))
				continue; // TODO: add to global parameters?
			
			int compdim = -1;
			
			Compartment comp = null;
			
			if(propphysdefuri.equals(SemSimLibrary.OPB_FLUID_VOLUME_URI))
				compdim = 3;
			
			// Only write out spatialDimensions attribute if compartment is not 3D
			else if(propphysdefuri.equals(SemSimLibrary.OPB_AREA_OF_SPATIAL_ENTITY_URI))
				compdim = 2;
			
			else if(propphysdefuri.equals(SemSimLibrary.OPB_SPAN_OF_SPATIAL_ENTITY_URI))
				compdim = 1;
			
			
			if(compdim!=-1 && ((CompositePhysicalEntity)pmc).getArrayListOfEntities().size()==1){
				comp = sbmlmodel.createCompartment(ds.getName());
				comp.setSpatialDimensions(compdim);
			}
			// Go to next data structure if we didn't find an appropriate OPB property
			// or if there's a composite physical entity
			else{
				globalParameters.add(ds.getName());
				continue;
			}
			
			// A Compartment object must have the required attributes 
			// 'id' and 'constant', and may have the optional attributes 
			// 'metaid', 'sboTerm', 'name', 'spatialDimensions', 'size' and 'units'. 
			comp.setConstant(ds.getComputationInputs().size()==0);
						
			entityCompartmentMap.put((CompositePhysicalEntity)pmc, comp);

			comp.setName(pmc.getName().replace("\"", ""));
			
			CompositePhysicalEntity pmcAsCPE = (CompositePhysicalEntity)pmc;
			boolean oneentity =  pmcAsCPE.getArrayListOfEntities().size() == 1;
			PhysicalEntity indexent = pmcAsCPE.getArrayListOfEntities().get(0);
			boolean onerefentity = oneentity && indexent.hasPhysicalDefinitionAnnotation();			
			
			// Store annotation for compartment
			// If it's a singular entity and writing to standalone, annotate with CVTerm
			// If it's a singular entity and writing to OMEX, annotate in the metadata file. 
			// Link metaid's of compartment and the entity with the annotations (the singular
			// entity in the composite entity)
			
			// If it's a composite entity then we capture the semantics using a full composite in
			// the RDF block or OMEX metadata file when adding parameter info
			if( onerefentity ){
				
				addRDFannotationForPhysicalSBMLelement(pmc, comp);
				
				if( ! OMEXmetadataEnabled()) {
					DSsToOmitFromCompositesRDF.add(ds);
				}
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
			
			if(oneentity) addNotesAndMetadataID(pmcAsCPE.getArrayListOfEntities().get(0), comp); // Put metadataID from physical entity within composite physical entity
			else addNotesAndMetadataID(pmc, comp);
		}
	}
	
	/** Collect chemical species for SBML model */
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
					
					if(compartmentDefined){  // If there was a compartment defined in the annotation, map the SemSim and SBML representations
						
						// Because we are creating a new compartment that isn't explicitly asserted as a physical entity
						// in the SemSim model, we have to add it. Otherwise, when we can end up duplicating meta ID's
						// on SBML objects (not allowed) when we create the annotation statements that link compartments
						// to reference terms. This is because when assigning new meta ID's, we look at the existing meta ID's
						// on SemSim model components and choose one that's not taken. We have to make sure the new compartment's
						// meta ID is part of that existing list.
						// If an equivalent CompositePhysicalEntity is already in the model, we use it from here on
						compcpe = semsimmodel.addCompositePhysicalEntity(compcpe); 
						entityCompartmentMap.put(compcpe, cptmt);
						addRDFannotationForPhysicalSBMLelement(compcpe, cptmt);
					}
				}
									
				// If the index entity in the composite entity is a custom term, use
				// the name of the custom term and be sure to store its annotations
				
				PhysicalEntity indexent = fullcpe.getArrayListOfEntities().get(0);
				
				Species species = sbmlmodel.createSpecies(ds.getName(), cptmt);
				
				if(indexent instanceof CustomPhysicalEntity)
					species.setName(indexent.getName());
				
				// Do not store the annotation for this data structure in the SemSim RDF
				// Preserve semantics in <species> element, unless writing to OMEX metadata file
				if( ! OMEXmetadataEnabled() ) DSsToOmitFromCompositesRDF.add(ds);
				
				addRDFannotationForPhysicalSBMLelement(indexent, species);
					
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
				addNotesAndMetadataID(indexent, species);
			}
			
			// Otherwise the data structure is not associated with a physical entity and we 
			// treat it as a global parameter
			else globalParameters.add(ds.getName());
		}
	}
	
	
	/** Collect chemical reactions for SBML model */
	private void addReactions(){
		
		for(DataStructure ds : candidateDSsForReactions){
			
			PhysicalModelComponent pmc = ds.getAssociatedPhysicalModelComponent();
			
			if(pmc instanceof CustomPhysicalProcess){
				
				CustomPhysicalProcess process = (CustomPhysicalProcess)pmc;
				
				if( ! entitySpeciesMap.keySet().containsAll(process.getParticipants())){ // Make sure that all participants in reaction were asserted successfully as SBML species
					globalParameters.add(ds.getName());
					continue;
				}
				
				Reaction rxn = sbmlmodel.createReaction(ds.getName());
				
				processReactionMap.put(process, rxn);
				
				// A <reaction> object must have the required attributes 'id',
				// 'reversible' and 'fast', and may have the optional attributes
				// 'metaid', 'sboTerm', 'name' and 'compartment'.
				rxn.setReversible(true); // TODO: extend SemSim object model to include this "reversible" attribute somewhere
				rxn.setFast(false); // TODO: extend SemSim object model to include this "fast" attribute somewhere
				
				if( ! OMEXmetadataEnabled() ) DSsToOmitFromCompositesRDF.add(ds);
				
				addRDFannotationForPhysicalSBMLelement(process, rxn);

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
				for(int s=0; s<globalParameters.size();s++){
					
					String fullnm = globalParameters.get(s);
					
					if(fullnm.startsWith(SBMLreader.REACTION_PREFIX + ds.getName() + ".")){
						globalParameters.set(s, "");
						String localnm = fullnm.replace(SBMLreader.REACTION_PREFIX + ds.getName() + ".", "");
						mathml = mathml.replaceAll("<ci>\\s*" + fullnm + "\\s*</ci>", "<ci>" + localnm + "</ci>");

						LocalParameter lp = kl.createLocalParameter(localnm);
						DataStructure assocds = semsimmodel.getAssociatedDataStructure(fullnm);
						String formula = getFormulaFromRHSofMathML(assocds.getComputation().getMathML(), fullnm);
						lp.setValue(Double.parseDouble(formula));
						
						// Add units, if needed
						// Deal with units on compartments if needed
						setUnitsForModelComponent(lp, assocds);
						assignMetaIDtoParameterIfAnnotated(assocds);
						addNotesAndMetadataID(assocds, lp);	
					}
				}
				
				// It's invalid to specify an empty kinetic law, reactant, product or mediator in SBML.
				// So skip adding the law to the reaction if there's no math (sometimes happens in extractions)
				if( ! mathml.isEmpty() && mathml!=null){ 
					kl.setMath(getASTNodeFromRHSofMathML(mathml, ds.getName()));
					rxn.setKineticLaw(kl);
				}
				
				addNotesAndMetadataID(process, rxn);	
			}
			
			// Otherwise the data structure is not associated with a process
			else globalParameters.add(ds.getName());

		}
	}
	
	
	/** Collect discrete events for SBML model */
	private void addEvents(){
		for(Event e : semsimmodel.getEvents()){
			
			org.sbml.jsbml.Event sbmle = sbmlmodel.createEvent();
			
			// Store delay info
			if(e.hasDelayMathML()){
				String origdelaymathml = e.getDelayMathML();
				String newdelaymathml = updateParameterNamesInMathML(origdelaymathml);
				sbmle.createDelay(JSBML.readMathMLFromString(newdelaymathml));
			}
				
			// Store priority info
			if(e.hasPriorityMathML()){
				String origprioritymathml = e.getPriorityMathML();
				String newprioritymathml = updateParameterNamesInMathML(origprioritymathml);
				sbmle.createPriority(JSBML.readMathMLFromString(newprioritymathml));
			}
			
			// Store name in "id" attribute
			if(e.hasName())
				sbmle.setId(e.getName());
				
			// Store description in "name" attribute
			if(e.hasDescription())
				sbmle.setName(e.getDescription());
			
			if(e.hasMetadataID())
				sbmle.setMetaId(e.getMetadataID());
								
			// Store trigger info
			String oldtriggermathml = e.getTriggerMathML();
			String newtriggermathml = updateParameterNamesInMathML(oldtriggermathml);
			sbmle.createTrigger(JSBML.readMathMLFromString(newtriggermathml));
			
			// Store each event assignment
			for(EventAssignment ea : e.getEventAssignments()){
				String outputname = ea.getOutput().getName();
				
				if(oldAndNewParameterNameMap.containsKey(outputname))
					outputname = oldAndNewParameterNameMap.get(outputname);
				
				String origassignmathml = ea.getMathML();
				String newassignmathml = updateParameterNamesInMathML(origassignmathml);
				
				sbmle.createEventAssignment(outputname, getASTNodeFromRHSofMathML(newassignmathml, outputname));
			}
			
			addNotesAndMetadataID(e, sbmle);
		}
	}
	
	
	/** Add in the SBML-style initial assignments  */
	private void addSBMLinitialAssignments(){
		
		for(SBMLInitialAssignment ssia : semsimmodel.getSBMLInitialAssignments()){
			
			InitialAssignment sbmlia = sbmlmodel.createInitialAssignment();
			
			DataStructure outputds = ssia.getOutput();
			String variableID = outputds.getName();
			String origassignmathml = ssia.getMathML();
			String newassignmathml = updateParameterNamesInMathML(origassignmathml);
			
			sbmlia.setMath(getASTNodeFromRHSofMathML(newassignmathml, variableID));
			sbmlia.setVariable(variableID);
			
			addNotesAndMetadataID(ssia, sbmlia);
		}
	}

	
	/**
	 * Update any parameter names in a MathML block that need to be changed before writing out
	 * @param mathml MathML XML content
	 * @return Updated MathML XML content containing replacement parameter names
	 */
	private String updateParameterNamesInMathML(String mathml){
		for(String oldname : oldAndNewParameterNameMap.keySet())
			mathml = SemSimUtil.replaceCodewordsInString(mathml, oldAndNewParameterNameMap.get(oldname), oldname);
		
		return mathml;
	}
		
	
	/** Add function definitions to SBML model */
	private void addFunctionDefinitions(){
		
		for(SBMLFunctionOutput fd : semsimmodel.getSBMLFunctionOutputs()){
			org.sbml.jsbml.FunctionDefinition sbmlfd = sbmlmodel.createFunctionDefinition(fd.getName());
			sbmlfd.setMath(getASTNodeFromRHSofMathML(fd.getComputation().getMathML(), ""));
			addNotesAndMetadataID(fd, sbmlfd);
		}
	}
	
	
	/** Collect global parameters for SBML model */
	private void addGlobalParameters(){
		
		// TODO: t.delta, etc. are getting renamed to t_delta but this change isn't being reflected in the model's MathML.
		// e.g. t.delta is used to compute EDVLV but doesn't look like EDVLV's MathML is being updated
		for(String dsname : globalParameters){
			
			if(dsname.equals("")) continue;
			
			DataStructure ds = semsimmodel.getAssociatedDataStructure(dsname);
			
			// Don't add parameter if it's not a declared element
			if( ! ds.isDeclared())	continue;
			
			String mathml = ds.getComputation().getMathML();

			if(oldAndNewParameterNameMap.containsKey(dsname)){
				dsname = oldAndNewParameterNameMap.get(dsname);
				
				Set<DataStructure> dssettoedit = new HashSet<DataStructure>();
				dssettoedit.addAll(ds.getUsedToCompute());
				dssettoedit.add(ds);
				
				// Go through all data structures that are dependent on the one we are renaming and replace occurrences of old name in equations
				for(DataStructure depds : dssettoedit){ 
					
					if(depds.hasComputation()){
						Computation depcomp = depds.getComputation();
						String oldmathml = depcomp.getMathML();
						String newmathml = SemSimUtil.replaceCodewordsInString(oldmathml, dsname, ds.getName());
						depcomp.setMathML(newmathml);
					}
				}
			}
			
			Parameter par = sbmlmodel.createParameter(dsname);
			
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
	
	
	/** Add the relational constraints to the SBML model */
	private void addConstraints(){
		
		for(RelationalConstraint rc : semsimmodel.getRelationalConstraints()){
			Constraint c = sbmlmodel.createConstraint();
			c.setMath(getASTNodeFromRHSofMathML(rc.getMathML(), ""));
		}
	}
	
	
	/**
	 * If storing the SBML model in an OMEX archive, write out all full
	 * composite annotations in the OMEX metadata file
	 * @return JDOM Document object representing the SBML model
	 */
	private Document addOMEXmetadataRDF(){
		
		writeFullCompositesInRDF();

		return makeXMLdocFromSBMLdoc();
	}
	
	
	
	
	/**
	 *  If writing to a standalone SBML model, create the SemSim-structured annotations for the model elements
	 */ 
	private Document addSemSimRDF(){
		
		rdfwriter = new SemSimRDFwriter(semsimmodel, ModelType.SBML_MODEL);
		
		rdfwriter.setRDFforModelLevelAnnotations(); // This just makes an RDF resource for the model. Model-level info is stored within SBML <model> element, not SemSim RDF block.
		
		// NOTE: SemSim-style submodels are not currently preserved on SBML export
		
		writeFullCompositesInRDF();
		
		Document doc = makeXMLdocFromSBMLdoc();

		// Add the RDF metadata to the appropriate element in the SBML file
		if( ! rdfwriter.rdf.isEmpty()){
			
			String rawrdf = AbstractRDFwriter.getRDFmodelAsString(rdfwriter.rdf,"RDF/XML-ABBREV");			
			Content newrdf = ModelWriter.makeXMLContentFromString(rawrdf);
			
			Element modelel = doc.getRootElement().getChild("model", sbmlNS);
			
			if(modelel == null){
				ErrorLog.addError("SBML writer error - no 'model' element found in XML doc", true, false);
				return doc;
			}
			
			Element modelannel = modelel.getChild("annotation", sbmlNS);
			
			if(modelannel == null){
				modelannel = new Element("annotation", sbmlNS);
				modelel.addContent(modelannel);
			}
			
			Element ssannel = modelannel.getChild(semsimAnnotationElementName, sbmlNS);
			
			if(ssannel == null){
				ssannel = new Element(semsimAnnotationElementName, sbmlNS); 
				modelannel.addContent(ssannel);
			}
			// Remove old RDF if present
			else modelannel.removeContent(ssannel);
			
			// Add the SemSim RDF
			if(newrdf !=null) ssannel.addContent(newrdf);
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
			RHS = RHS.replaceFirst("xmlns=\"" + RDFNamespace.MATHML.getNamespaceAsString() + "\"",
					"xmlns=\"" + RDFNamespace.MATHML.getNamespaceAsString() + "\"" + " "
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
	 * @return The right-hand-side of the expression
	 */
	private String getFormulaFromRHSofMathML(String mathml, String localname){
		ASTNode node = getASTNodeFromRHSofMathML(mathml, localname);
		return JSBML.formulaToString(node);	
	}
	
	
	/**
	 * Look up an SBML component in one of the LinkedHashMaps used to 
	 * related SemSim physical model components to SBML elements
	 * @param pmc A SemSim {@link PhysicalModelComponent}
	 * @param map Mapping between a SemSim {@link PhysicalModelComponent} and an SBML element
	 * @return The SBML element corresponding to the input SemSim physical model component
	 */
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
	private void addNotesAndMetadataID(SemSimObject sso, AbstractSBase sbo){
		
		if(sso.hasDescription() && !OMEXmetadataEnabled()){ // Don't store model description in notes if writing to OMEX file. It will be stored in OMEX metadata file.
			try {
				String desc = sso.getDescription();
				byte[] chars = desc.getBytes("UTF-8"); // Make sure to use UTF-8 formatting (the Le Novere problem)
				String temp = "<notes>\n  <body xmlns=\"http://www.w3.org/1999/xhtml\">\n    " + new String(chars) + "\n  </body>\n</notes>";
				temp = temp.replace("&", "&amp;");
				sbo.setNotes(temp);
			} catch (XMLStreamException e) {
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
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
	
	
	/**
	 * Assign physical units to an SBML element
	 * @param qwu The SBML element
	 * @param ds The data structure corresponding to the SBML element
	 * and whose units will be applied to the element
	 */
	private void setUnitsForModelComponent(QuantityWithUnit qwu, DataStructure ds){
		
		if(ds.hasUnits()){
			String uomname = ds.getUnit().getName();
			if(oldAndNewUnitNameMap.containsKey(uomname)) uomname = oldAndNewUnitNameMap.get(uomname);
			
			if( sbmlmodel.containsUnitDefinition(uomname)) qwu.setUnits(uomname);
			
		}
	}
	
	
	/**
	 * Add a rule to the SBML model
	 * @param ds The {@link DataStructure} whose values are computed by the rule
	 * @param sbmlobj The SBML Symbol corresponding to the {@link DataStructure}
	 */
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
	
	
	/**
	 * Add semantic annotations on an SBML compartment, species or reaction element encoded as RDF
	 * @param pmc {@link PhysicalModelComponent} corresponding to the SBML element
	 * @param sbmlobj The SBML element to annotate
	 */
	private void addRDFannotationForPhysicalSBMLelement(PhysicalModelComponent pmc, SBase sbmlobj){
		
		// If neither the SBML object nor the SemSim physical model component have a metadata ID, 
		// assign them both the same one
		if( ! sbmlobj.isSetMetaId() && ! pmc.hasMetadataID()){
			String metaid = semsimmodel.assignValidMetadataIDtoSemSimObject("metaid0", pmc);
			sbmlobj.setMetaId(metaid);
		}
		
		// Check if the physical model component is a composite physical entity
		// containing only one physical entity component
		boolean oneent = false;
		
		if(pmc instanceof CompositePhysicalEntity)
			oneent = ((CompositePhysicalEntity)pmc).getArrayListOfEntities().size()==1;
		
		// This is used when writing RDF-based semantic annotations within a OMEX metadata file 
		// that is linked to the SBML file in a COMBINE archive
		if(OMEXmetadataEnabled()){
			
			// When the pmc is a one-entity composite physical entity, use the
			// metadata ID on the pmc to link it to the SBML entity (this is used for compartments)
			if(oneent){
				String metaidtouse = pmc.getMetadataID();
				pmc = ((CompositePhysicalEntity)pmc).getArrayListOfEntities().get(0);
				((OMEXmetadataWriter)rdfwriter).setAnnotationsForSBMLphysicalComponent(metaidtouse, pmc); // uses model-specific namespace (not local RDF namespace) when creating RDF resources here
			}
			else ((OMEXmetadataWriter)rdfwriter).setAnnotationsForSBMLphysicalComponent(pmc); // uses model-specific namespace (not local RDF namespace) when creating RDF resources here
			return;
		}
		
		if(oneent) pmc = ((CompositePhysicalEntity)pmc).getArrayListOfEntities().get(0);
		
		// This is used to add singular annotations to SBML elements
		// when writing RDF-based semantic annotations within a standalone SBML file
		if( ! OMEXmetadataEnabled()) {
			
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
	}
	
	
	/**
	 * For data structures that are set to constant values in the SBML code,
	 * get the contant value as a Double
	 * @param ds The DataStructure with constant value
	 * @return Double representation of the value
	 */
	private Double getConstantValueForPropertyOfEntity(DataStructure ds){
		Double formulaAsDouble = null;
		
		if(ds.getComputation().hasMathML()){
			String mathml = ds.getComputation().getMathML();
			String formula = getFormulaFromRHSofMathML(mathml, ds.getName());			
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
	
	
	/**
	 * If an SBML parameter is annotated and does not have an assigned metaID,
	 * assign it a unique metaID
	 * @param ds The {@link DataStructure} that will be written out as an SBML parameter
	 */
	private void assignMetaIDtoParameterIfAnnotated(DataStructure ds){
		// If the parameter is annotated, and doesn't have a meta id, give it one
		if((ds.hasDescription() || ds.hasPhysicalProperty() || ds.hasPhysicalDefinitionAnnotation()) && ! ds.hasMetadataID())
			semsimmodel.assignValidMetadataIDtoSemSimObject(ds.getName(), ds);
	}

	/**
	 * For data structures that don't have valid IDs, rename them and store the 
	 * old-to-new name mapping 
	 * @param ds A {@link DataStructure}
	 */
	// in the MathML of dependent data structures
	private void makeDataStructureNameValid(DataStructure ds, Set<String> existingnames){
		String oldname = ds.getName();
		String newname = oldname.replaceAll("\\.", "_");
			
		while(existingnames.contains(newname)){
			newname = newname + "_";
		}
		
		oldAndNewParameterNameMap.put(oldname, newname);
	}
	
	
	@Override
	public AbstractRDFwriter getRDFwriter(){
		return rdfwriter;
	}
	
	
	/** @return The SBML model translated into a JDOM Document object */
	private Document makeXMLdocFromSBMLdoc(){
		try {
			String sbmlstring = new SBMLWriter().writeSBMLToString(sbmldoc);
			SAXBuilder builder = new SAXBuilder();
			InputStream is = new ByteArrayInputStream(sbmlstring.getBytes("UTF-8"));
			return builder.build(is);
		} catch (JDOMException | IOException | SBMLException | XMLStreamException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	
	/** Write out composite annotations in the SemSim RDF associated with the model */
	private void writeFullCompositesInRDF(){
		
		Map<String,SemSimObject> metamap = semsimmodel.getMetadataIDcomponentMap();
		Set<String> usedmetaids = new HashSet<String>(); 
		usedmetaids.addAll(metamap.keySet());
		
		if(semsimmodel.hasMetadataID()) usedmetaids.add(semsimmodel.getMetadataID());
		
		for(DataStructure ds : semsimmodel.getAssociatedDataStructures()){
			// Only the info for parameters are stored (i.e. not compartments, species or reactions)
			// because the <species> and <reaction> elements already contain the needed info, and 
			// the <compartments> refer to physical entities (which can be composite), not properties of entities
			
			if(DSsToOmitFromCompositesRDF.contains(ds) || ds.isSolutionDomain()) continue;
			else{
				String metadataID = null;
				
				// We use custom code here to make this function go faster (previously used the AbstractRDFwriter.assignMetaIDandCreateResourceForDataStructure()) routine
				// but it can be time-consuming for larger models
				if(ds.hasMetadataID()) metadataID = ds.getMetadataID();
				else{
					metadataID = semsimmodel.generateUniqueMetadataID("metaid_0", usedmetaids);
					usedmetaids.add(metadataID);
				}
				
				String resuri = rdfwriter.getModelNamespace() + "#" + metadataID;
				resuri = resuri.replaceAll("##", "#"); // In case model namespace ends in #
				Resource ares = rdfwriter.rdf.createResource(resuri);
				
								
				rdfwriter.setFreeTextAnnotationForObject(ds, ares);
				rdfwriter.setSingularAnnotationForDataStructure(ds, ares);
				rdfwriter.setDataStructurePropertyAndPropertyOfAnnotations(ds, ares);
			}
		}		
	}
}
