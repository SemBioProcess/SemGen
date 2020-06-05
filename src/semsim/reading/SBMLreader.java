package semsim.reading;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipException;

import javax.xml.stream.XMLStreamException;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.tuple.Pair;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.filter.ElementFilter;
import org.jdom.output.XMLOutputter;
import org.sbml.jsbml.CVTerm;
import org.sbml.jsbml.CVTerm.Qualifier;
import org.sbml.jsbml.Compartment;
import org.sbml.jsbml.Constraint;
import org.sbml.jsbml.Delay;
import org.sbml.jsbml.KineticLaw;
import org.sbml.jsbml.LocalParameter;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.Priority;
import org.sbml.jsbml.QuantityWithUnit;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.ExplicitRule;
import org.sbml.jsbml.InitialAssignment;
import org.sbml.jsbml.Rule;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.SBMLReader;
import org.sbml.jsbml.SBase;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.Unit;
import org.sbml.jsbml.UnitDefinition;
import org.sbml.jsbml.JSBML;
import org.semanticweb.owlapi.model.OWLException;

import org.apache.jena.rdf.model.NodeIterator;
import org.apache.jena.rdf.model.Resource;

import semsim.SemSimLibrary;
import semsim.SemSimObject;
import semsim.annotation.Annotation;
import semsim.annotation.ReferenceOntologyAnnotation;
import semsim.annotation.Relation;
import semsim.definitions.RDFNamespace;
import semsim.definitions.ReferenceOntologies;
import semsim.definitions.SBMLconstants;
import semsim.definitions.SemSimRelations;
import semsim.definitions.SemSimTypes;
import semsim.definitions.SemSimRelations.SemSimRelation;
import semsim.definitions.SemSimRelations.StructuralRelation;
import semsim.fileaccessors.ModelAccessor;
import semsim.fileaccessors.OMEXAccessor;
import semsim.definitions.ReferenceOntologies.OntologyDomain;
import semsim.definitions.ReferenceOntologies.ReferenceOntology;
import semsim.model.collection.SemSimModel;
import semsim.model.computational.EventAssignment;
import semsim.model.computational.RelationalConstraint;
import semsim.model.computational.SBMLInitialAssignment;
import semsim.model.computational.Event;
import semsim.model.computational.datastructures.DataStructure;
import semsim.model.computational.datastructures.Decimal;
import semsim.model.computational.datastructures.SBMLFunctionOutput;
import semsim.model.computational.units.UnitFactor;
import semsim.model.computational.units.UnitOfMeasurement;
import semsim.model.physical.PhysicalEntity;
import semsim.model.physical.PhysicalModelComponent;
import semsim.model.physical.PhysicalProcess;
import semsim.model.physical.object.CompositePhysicalEntity;
import semsim.model.physical.object.CustomPhysicalEntity;
import semsim.model.physical.object.CustomPhysicalProcess;
import semsim.model.physical.object.PhysicalProperty;
import semsim.model.physical.object.PhysicalPropertyInComposite;
import semsim.model.physical.object.ReferencePhysicalDependency;
import semsim.model.physical.object.ReferencePhysicalEntity;
import semsim.model.physical.object.ReferencePhysicalProcess;
import semsim.owl.SemSimOWLFactory;
import semsim.utilities.SemSimUtil;
import semsim.writing.SBMLwriter;

/**
 * Class for converting an SBML document into the SemSim format
 * @author mneal
 *
 */
public class SBMLreader extends ModelReader{

	private SBMLDocument sbmldoc;
	private Model sbmlmodel;
	private Map<String, PhysicalEntity> compartmentAndEntitiesMap = new HashMap<String, PhysicalEntity>();
	private Map<String, CompositePhysicalEntity> speciesAndEntitiesMap = new HashMap<String, CompositePhysicalEntity>();
	private Map<String, SpeciesConservation> speciesAndConservation = new HashMap<String, SpeciesConservation>();  // associates species with the reactions they participate in
	private Set<String> baseUnits = new HashSet<String>();
	private String timedomainname = "time";
	public static final String REACTION_PREFIX = "Reaction_";
	public static final String FUNCTION_PREFIX = "Function_";
	private UnitOfMeasurement modeltimeunits;
	private UnitOfMeasurement modelsubstanceunits;
	private AbstractRDFreader rdfreader;
	
	
	public SBMLreader(ModelAccessor accessor){
		super(accessor);
	}
	

	@Override
	public SemSimModel read() throws IOException, InterruptedException,
			OWLException, XMLStreamException {
		
		// Load the SBML file into a new SBML model
		InputStream instream = modelaccessor.modelInStream();
		sbmldoc = new SBMLReader().readSBMLFromStream(instream);
		instream.close();
		
		if (sbmldoc.getNumErrors()>0){
		      System.err.println("Encountered the following SBML errors:");
		      sbmldoc.printErrors(System.err);
		      semsimmodel.addError("Source SBML model contained errors");
		      return semsimmodel;
		}
		else sbmlmodel = sbmldoc.getModel();

		// If model is SBML level 1, add error and return. This level not yet supported.
		if (sbmlmodel.getLevel()==1){
			addErrorToModel("SBML-to-SemSim conversion for SBML level 1 models not yet supported");
			return semsimmodel;
		}
		
		
		semsimmodel.setSemSimVersion(SemSimLibrary.SEMSIM_VERSION);		
		semsimmodel.setSourceFileLocation(modelaccessor);
		
		//collectCompartmentTypes();  // We ignore compartment types for now. This class is not available in JSBML.
		// See http://sbml.org/Software/libSBML/5.11.4/docs/formatted/java-api/org/sbml/libsbml/CompartmentType.html

		//collectSpeciesTypes();  // Ignore these for now, too. This class is not available in JSBML.
		// See http://sbml.org/Software/libSBML/5.11.4/docs/formatted/java-api/org/sbml/libsbml/SpeciesType.html
		
				
		// if any errors at this point, return model
		if(semsimmodel.getErrors().size()>0) return semsimmodel;
		
		// If model is from an archive, read in the annotations on the SBML physical components using
		// getAnnotationsForSBMLphysicalComponents() in CASAreader. Composites on parameters are read in 
		// in the collectReactions (for local reaction-specific parameters) and collectParameters functions.
		try {
			
			if(modelaccessor instanceof OMEXAccessor){
				rdfreader = modelaccessor.createRDFreaderForModel(semsimmodel, null, sslib);
				if (rdfreader instanceof OMEXmetadataReader) {
					((OMEXmetadataReader)rdfreader).getAnnotationsForPhysicalComponents(sbmlmodel);
				}
			}
			else{
				String existingrdf = collectSemSimRDF(); // For SemSim-specific RDF annotations
				
				// If a standalone SBML model, modelaccessor will just be a regular ModelAccessor and will return a SemSimRDFreader
				rdfreader = modelaccessor.createRDFreaderForModel(semsimmodel, existingrdf, sslib);
			}
			
		} catch (JDOMException e) {
			e.printStackTrace();
		}
		
		collectModelLevelData();
		setBaseUnits();
		collectUnits();
		setSubstanceUnits();
		setTimeDomain();
		collectCompartments();
		collectSpecies();
		collectParameters();
		collectFunctionDefinitions(); // NOTE: Function definitions not used in SBML Level 1
		collectReactions();
		setSpeciesConservationEquations();
		collectRules();
		collectConstraints();
		collectEvents();
		collectInitialAssignments();
		setComputationalDependencyNetwork();
	
		return semsimmodel;
	}
	
	/**
	 * Collect the SBML model's model-level data such as model name, semantic annotations, etc.
	 */
	private void collectModelLevelData(){
		
		collectSBaseData(sbmlmodel, semsimmodel);
		semsimmodel.setName(sbmlmodel.getId());
		rdfreader.getModelLevelAnnotations();	// This retrieves any model-level info in SemGen-generated RDF within the SBML file
		
		// Retrieve model-level annotations not encoded in the SemGen-generated RDF (e.g. curational annotations from BioModels)
		for(int m=0;m<sbmlmodel.getNumCVTerms();m++){
			CVTerm cv = sbmlmodel.getCVTerm(m);
			
			for(int i=0; i<cv.getNumResources(); i++){
				String obj = cv.getResourceURI(i);
				Qualifier qual = cv.getQualifier();
				Relation rel = qual.isBiologicalQualifier() ? SemSimRelations.getRelationFromBiologicalQualifier(qual) :
					SemSimRelations.getRelationFromModelQualifier(qual);
				semsimmodel.addReferenceOntologyAnnotation(rel, URI.create(obj), "", sslib);
			}
		}
	}
	
	
	/**
	 * Collect the model's units
	 */
	private void collectUnits(){

		// For more info about SBML units, see 
		// http://sbml.org/Software/libSBML/5.11.4/docs/formatted/java-api/org/sbml/libsbml/Unit.html
		
		for(int u=0; u<sbmlmodel.getListOfUnitDefinitions().size(); u++){
			
			UnitDefinition sbmlunitdef = sbmlmodel.getUnitDefinition(u);
			UnitOfMeasurement semsimunit = new UnitOfMeasurement(sbmlunitdef.getId());
												
			for(int v=0; v<sbmlunitdef.getListOfUnits().size(); v++){
				
				Unit sbmlunit = sbmlunitdef.getUnit(v);
				String unitfactorname = sbmlunit.getKind().getName(); //org.sbml.jsbml.util.UnitKind_toString();

				UnitOfMeasurement baseunit = null;
				
				// If the base unit for the unit factor was already added to model, retrieve it. Otherwise create anew.
				if(semsimmodel.containsUnit(unitfactorname)) baseunit = semsimmodel.getUnit(unitfactorname);
				
				else if( ! unitfactorname.equals("dimensionless")){  // don't add factor if it's dimensionless
					baseunit = new UnitOfMeasurement(unitfactorname);
					baseunit.setFundamental(baseUnits.contains(unitfactorname));
					collectSBaseData(sbmlunit, baseunit);
					semsimmodel.addUnit(baseunit);
				}
				else continue;
				
				UnitFactor unitfactor = new UnitFactor(baseunit, sbmlunit.getExponent(), null);
				
				// Set the unit factor prefix based on scale value
				for(String prefix : sslib.getUnitPrefixesAndPowersMap().keySet()){
					if(sslib.getUnitPrefixesAndPowersMap().get(prefix).intValue()==sbmlunit.getScale())
						unitfactor.setPrefix(prefix);
				}
				
				unitfactor.setMultiplier(sbmlunit.getMultiplier());
				semsimunit.addUnitFactor(unitfactor);
			}
			
			collectSBaseData(sbmlunitdef, semsimunit);
			semsimmodel.addUnit(semsimunit);
		}
		
		if(sbmlmodel.getLevel()==2) addSBMLlevel2reservedUnits();
	}
	
	
	/** For SBML level 2, add the reserved units, if not already stated in listOfUnitDefinitions */
	private void addSBMLlevel2reservedUnits() {
			
		for(String resunitname : SBMLconstants.SBML_LEVEL_2_RESERVED_UNITS_MAP.keySet()){
			
			if( ! semsimmodel.containsUnit(resunitname))
				addReservedUnit(resunitname, SBMLconstants.SBML_LEVEL_2_RESERVED_UNITS_MAP.get(resunitname));
		}
	}
	
	/**
	 * Add one reserved unit
	 * @param resunitname The reserved unit's name
	 * @param baseunitname Base unit of the reserved unit
	 * @return {@link UnitOfMeasurement} instance for the input reserved unit
	 */
	private UnitOfMeasurement addReservedUnit(String resunitname, String baseunitname){
		
		UnitOfMeasurement resuom = new UnitOfMeasurement(resunitname);
		UnitOfMeasurement baseuom = null;
		
		// Add base unit, if not already in model
		if( ! semsimmodel.containsUnit(baseunitname)){
			baseuom = new UnitOfMeasurement(baseunitname);
			baseuom.setFundamental(baseUnits.contains(baseunitname));
			semsimmodel.addUnit(baseuom);
		}
		else baseuom = semsimmodel.getUnit(baseunitname);
		
		UnitFactor uf = new UnitFactor(baseuom, 1, null);		
		resuom.addUnitFactor(uf);
		semsimmodel.addUnit(resuom);
				
		return resuom;
	}
		
		
	/** Get the default substance units for the model */
	private void setSubstanceUnits(){
		/*
		From http://sbml.org/Software/libSBML/5.11.4/docs/formatted/java-api/org/sbml/libsbml/Species.html
		In SBML Level 3, if the 'substanceUnits' attribute is not set on a given
		Species object instance, then the unit of amount for that species is inherited
		from the 'substanceUnits' attribute on the enclosing Model object instance. 
		If that attribute on Model is not set either, then the unit associated with the
		species' quantity is undefined.
		*/
					
		if(sbmlmodel.getLevel()==3){
						
			// If already added as an explicit UnitDefinition, retrieve UnitOfMeasurement from SemSim model
			if(semsimmodel.containsUnit("substance"))
				modelsubstanceunits = semsimmodel.getUnit("substance");
			
			// Otherwise use default substance units
			else modelsubstanceunits = addReservedUnit("substance", SBMLconstants.SBML_LEVEL_2_RESERVED_UNITS_MAP.get("substance"));
		}
		
		
		/*
		In SBML Level 2, if the 'substanceUnits' attribute is not set on a given Species object instance,
		 then the unit of amount for that species is taken from the predefined SBML unit identifier 
		 'substance'. The value assigned to 'substanceUnits' must be chosen from one of the following 
		 possibilities: one of the base unit identifiers defined in SBML, the built-in unit identifier 
		 'substance', or the identifier of a new unit defined in the list of unit definitions in the 
		 enclosing Model object. The chosen units for 'substanceUnits' must be be 'dimensionless', 
		 'mole', 'item', 'kilogram', 'gram', or units derived from these.
		 */
		else if(sbmlmodel.getLevel()==2){  // "substance" unit should have already been added as part of Level 2 reserved units
			String subid = sbmlmodel.getPredefinedUnitDefinition("substance").getId();
			
			if(semsimmodel.containsUnit(subid)) modelsubstanceunits = semsimmodel.getUnit(subid);
		}
		
			
		if(modelsubstanceunits == null) System.err.println("WARNING: Could not determine default substance units for model");
	}
	
	
	/** Set the temporal solution domain for the SemSim model  
	 * @throws IOException*/
	private void setTimeDomain() throws IOException{
		
		//NOTE: default time units for SBML level 2 models is seconds.
		boolean timenamepredefined = false;
		
		Document doc = modelaccessor.getJDOMDocument();
		
		Iterator<?> descit = doc.getRootElement().getDescendants(new ElementFilter());
		
		while(descit.hasNext()){
			Element el = (Element)descit.next();
			
			if(el.getName().equals("csymbol")){
				
				if(el.getAttributeValue("definitionURL").equals("http://www.sbml.org/sbml/symbols/time")){
					timedomainname = el.getText().trim();
					timenamepredefined = true;
					break;
				}
			}
		}
		
		
		// If cysmbol time not found, go through model compartments, species, and global parameters to see if we
		// can use the default name for our time domain data structure
		if( ! timenamepredefined){
			Set<String> usedids = new HashSet<String>();
			
			for(Compartment c : sbmlmodel.getListOfCompartments())
				usedids.add(c.getId());
			
			for(Species s : sbmlmodel.getListOfSpecies())
				usedids.add(s.getId());
			
			for(Parameter p : sbmlmodel.getListOfParameters())
				usedids.add(p.getId());
			
			Integer x = 0;
			
			while(usedids.contains(timedomainname)){
				timedomainname = timedomainname + x;
				x++;
			}
		}
		
		// Create a data structure that represents the temporal solution domain
		DataStructure timeds = new Decimal(timedomainname);
		timeds.setDeclared(true);
		timeds.setDescription("Temporal solution domain");
		timeds.setIsSolutionDomain(true);
		
		
		if(semsimmodel.containsUnit("time")){  // Time unit should have already been added for level 2 models
			modeltimeunits = semsimmodel.getUnit("time");
		}
		else{
			if(sbmlmodel.isSetTimeUnits()){
				modeltimeunits = new UnitOfMeasurement(sbmlmodel.getTimeUnits());
				semsimmodel.addUnit(modeltimeunits);
			}
			else modeltimeunits = addReservedUnit("time", SBMLconstants.SBML_LEVEL_2_RESERVED_UNITS_MAP.get("time"));
		}
		
		timeds.setUnit(modeltimeunits);

		PhysicalProperty timeprop = new PhysicalProperty("Time", SemSimLibrary.OPB_TIME_URI);
		semsimmodel.addPhysicalProperty(timeprop);
		timeds.setSingularAnnotation(timeprop);
		
		semsimmodel.addDataStructure(timeds);
	}

	
	/** Collect the SBML model's compartment data */
	private void collectCompartments(){
		
		for(int c=0; c<sbmlmodel.getListOfCompartments().size(); c++){
			Compartment sbmlc = sbmlmodel.getCompartment(c);
			String compid = sbmlc.getId();
			
			// NOTE: we ignore the "outside" attribute for now
			
			DataStructure ds = semsimmodel.addDataStructure(new Decimal(compid));
			ds.setDeclared(true);
			
			String mathml = SemSimUtil.mathMLelementStart + " <apply>\n  <eq />\n  <ci>" + compid + "</ci>\n  <cn>" 
					+ sbmlc.getSize() + "</cn>\n </apply>\n" + SemSimUtil.mathMLelementEnd;
			ds.getComputation().setMathML(mathml);
			ds.getComputation().setComputationalCode(compid + " = " + Double.toString(sbmlc.getSize()));
			
			String reservedunits = null;
			PhysicalPropertyInComposite prop = null;
			String modelobjectspecifieddefaultunits = "";
			
			// Add physical property
			if(sbmlc.getSpatialDimensions()==3.0 || ! sbmlc.isSetSpatialDimensions()){  // For compartments where the spatial dimension isn't specified, we assume 3
				prop = new PhysicalPropertyInComposite("Fluid volume", SemSimLibrary.OPB_FLUID_VOLUME_URI);
				reservedunits = "volume";
				modelobjectspecifieddefaultunits = sbmlmodel.getVolumeUnits();
			}
			else if(sbmlc.getSpatialDimensions()==2.0){
				prop = new PhysicalPropertyInComposite("Area of spatial entity", SemSimLibrary.OPB_AREA_OF_SPATIAL_ENTITY_URI);
				reservedunits = "area";
				modelobjectspecifieddefaultunits = sbmlmodel.getAreaUnits();
			}
			else if(sbmlc.getSpatialDimensions()==1.0){
				prop = new PhysicalPropertyInComposite("Span of spatial entity", SemSimLibrary.OPB_SPAN_OF_SPATIAL_ENTITY_URI);
				reservedunits = "length";
				modelobjectspecifieddefaultunits = sbmlmodel.getLengthUnits();
			}
						
			// Set the units for the compartment
			UnitOfMeasurement compuom = null;
			
			if(sbmlc.isSetUnits()){
				
				if(semsimmodel.containsUnit(sbmlc.getUnits()))
					compuom = semsimmodel.getUnit(sbmlc.getUnits());
				
				// If the units are set but weren't found in the semsim model
				// try a case-insensitive check. This is to account for an issue
				// revealed in BIOMD165 compartment "cell" where the units are
				// "litre" but JSBML converted this units name to "Litre" when
				// processing the original unit definition. The unit "Litre" is 
				// stored in the semsim model, not "litre".
				else{ 
					for(UnitOfMeasurement uom : semsimmodel.getUnits()){
						
						if(uom.getName().toLowerCase().equals(sbmlc.getUnits().toLowerCase()))
							compuom = uom;
					}
				}
			}
			// If the model is SBML Level 3 and the SBML Model object specifies the units
			// for compartments with this compartment's spatial dimension, then create the
			// unit in the SemSim model and assign it to the compartment
			else if(sbmlmodel.getLevel() == 3){
								
				String unitname = ( ! modelobjectspecifieddefaultunits.isEmpty()) ? modelobjectspecifieddefaultunits : reservedunits;

				if(semsimmodel.containsUnit(unitname)) compuom = semsimmodel.getUnit(unitname);
				else{
					if(SBMLconstants.SBML_LEVEL_2_RESERVED_UNITS_MAP.containsKey(unitname))
						compuom = addReservedUnit(unitname, SBMLconstants.SBML_LEVEL_2_RESERVED_UNITS_MAP.get(unitname));
				}
			}
			else{
				if(semsimmodel.containsUnit(reservedunits)) 
					compuom = semsimmodel.getUnit(reservedunits);
			}
			
			// If we've got the unit, associate it with the data structure and make sure it's added to the model
			if(compuom != null){
				ds.setUnit(compuom);
				
				if( ! semsimmodel.containsUnit(compuom.getName())) 
					semsimmodel.addUnit(compuom);
			}
			
			else if(sbmlc.getUnits().equals("dimensionless")){} // Don't assign units. Don't issue warning.
			
			// Otherwise the unit for the compartment is undefined
			else System.err.println("WARNING: Units for compartment " + sbmlc.getId() + " were undefined");
			
			// Collect the physical entity representation of the compartment
			PhysicalEntity compartmentent = null;

			boolean setEntityFromSBMLsource = false;
			
			// If we're reading in from an OMEX file, see if the CASA file contains any composite physical entity annotations 
			// for the compartment. Single physical entity annotations should have already been picked up and attached
			// as CV terms.
			if(modelaccessor instanceof OMEXAccessor && sbmlc.getCVTermCount()==0){
				
				ds.setAssociatedPhysicalProperty(prop);
				
				// TODO: collecting the RDF resource duplicates code in CASA reader. Put in one function.
				String metaid = sbmlc.getMetaId(); // TODO: what if no metaid assigned? Just do nothing?
				String ns = semsimmodel.getLegacyCodeLocation().getFileName();
				Resource res = rdfreader.rdf.getResource(ns + "#" + metaid); // Don't use "./" + ns
								
				// See if the compartment has an IS annotation that links to a composite physical entity
				NodeIterator nodeit = rdfreader.rdf.listObjectsOfProperty(res, SemSimRelation.BQB_IS.getRDFproperty());
				
				if(nodeit.hasNext()){
					Resource entityres = nodeit.next().asResource();
					
					if(entityres.getURI().contains(ns + "#")){
						compartmentent = (PhysicalEntity) rdfreader.getPMCfromRDFresourceAndAnnotate(entityres);
						ds.setAssociatedPhysicalModelComponent(compartmentent);
					}
				}
				else setEntityFromSBMLsource = true;
			}
			// If the compartment is annotated with a SemSim annotation in a standalone SBML model, collect it
			else if(rdfreader.hasPropertyAnnotationForDataStructure(ds)){
				rdfreader.getDataStructureAnnotations(ds);
				
				PhysicalModelComponent pmc = ds.getAssociatedPhysicalModelComponent();
				
				if(ds.hasAssociatedPhysicalComponent() && pmc instanceof PhysicalEntity)
					compartmentent = (PhysicalEntity)pmc;
				
			}
			// Otherwise we use the info in the SBML to get the physical entity 
			// representation of the compartment
			else setEntityFromSBMLsource = true;
			
			if(setEntityFromSBMLsource){
				ds.setAssociatedPhysicalProperty(prop);
				
				PhysicalEntity singlecompartmentent = (PhysicalEntity) createSingularPhysicalComponentForSBMLobject(sbmlc);
							
				ArrayList<PhysicalEntity> entlist = new ArrayList<PhysicalEntity>();
				entlist.add(singlecompartmentent);
				ArrayList<StructuralRelation> rellist = new ArrayList<StructuralRelation>();
				
				compartmentent = semsimmodel.addCompositePhysicalEntity(entlist, rellist); // this also adds the singular physical entities to the model
				ds.setAssociatedPhysicalModelComponent(compartmentent);
				
				collectSBaseData(sbmlc, singlecompartmentent); // Note that the compartment meta ID gets assigned to the singular physical entity here, not the composite
			}
			else collectSBaseData(sbmlc, compartmentent); // Metadata ID gets assigned to singleton composite physical entity here (this might not be needed)

			compartmentAndEntitiesMap.put(compid, compartmentent);
		}
	}
	
	
	/** Collect the SBML model's chemical species data */
	private void collectSpecies(){
		
		for(int s=0; s<sbmlmodel.getListOfSpecies().size(); s++){
			Species species = sbmlmodel.getSpecies(s);
			
			String speciesid = species.getId();

			DataStructure ds = semsimmodel.addDataStructure(new Decimal(speciesid));
			
			ds.setDeclared(true);
			ds.setSolutionDomain(semsimmodel.getAssociatedDataStructure(timedomainname));
			
			boolean isConstant = species.getConstant();
			boolean isBoundaryCondition = species.getBoundaryCondition();
			boolean isSetWithRuleAssignment = sbmlmodel.getRule(speciesid) != null;
			
//			if(isConstant){
//				
//				if(isBoundaryCondition){
//					// don't apply conservation eq. set initial condition only. CAN be a reactant or product
//				}
//				else{
//					// don't apply conservation eq. set IC only. CANNOT be a reactant or product
//				}
//			}
			if( ! isConstant){
				
				SpeciesConservation nsc = new SpeciesConservation();
				speciesAndConservation.put(speciesid, nsc);
				
				if(isBoundaryCondition){
					// Can change by rules or events but not reactions
					nsc.setWithConservationEquation = false;
					// CAN be a reactant or product
				}
				else{
					// Can change by reactions or rules (but not both at the same time), and events
					nsc.setWithConservationEquation = ! isSetWithRuleAssignment;
					// CAN be a reactant or product
				}
			}
			
					
			// Deal with equations for species concentration/amount here
			PhysicalPropertyInComposite prop = null;
			
			UnitOfMeasurement speciessubstanceunits = null;
			
			if(species.isSetSubstanceUnits())
				speciessubstanceunits = semsimmodel.getUnit(species.getSubstanceUnits());
			
			else if(modelsubstanceunits != null) speciessubstanceunits = modelsubstanceunits;
			
			else System.err.println("WARNING: Substance units for " + species.getId() + " were undefined");;
			
			// Deal with whether the species is expressed in substance units or not 
			boolean hasonlysub = species.getHasOnlySubstanceUnits();

			UnitOfMeasurement unitforspecies = null;
			String compartmentname = species.getCompartment();

			UnitOfMeasurement compartmentunits = semsimmodel.getAssociatedDataStructure(compartmentname).getUnit();	
			
			if(hasonlysub && speciessubstanceunits!=null) unitforspecies = speciessubstanceunits;
			else if(speciessubstanceunits != null && compartmentunits != null){
				
				// Make unit for concentration of species
				String unitname = speciessubstanceunits.getName() + "_per_" + compartmentunits.getName();
				
				// If the substance/compartment unit was already added to the model, use it, otherwise create anew
				unitforspecies = semsimmodel.getUnit(unitname);
				
				if(unitforspecies==null){
					unitforspecies = new UnitOfMeasurement(unitname);
					UnitFactor substancefactor = new UnitFactor(speciessubstanceunits, 1.0, null);
					unitforspecies.addUnitFactor(substancefactor);
					UnitFactor compartmentfactor = new UnitFactor(compartmentunits, -1.0, null);
					unitforspecies.addUnitFactor(compartmentfactor);
					semsimmodel.addUnit(unitforspecies);
				}				
			}
			
			if(unitforspecies!=null) ds.setUnit(unitforspecies);
			
			// The OPB properties assigned here need to account for the different possible units for 
			// substance units: 'dimensionless', 'mole', 'item', kilogram','gram', etc. as above.
			// Will need base unit breakdown to assign appropriate OPB terms. Using the follow if-else
			// statements in the meantime. Currently assuming that if hasOnlySubstanceUnits is false, 
			// that the same OPB term can be used regardless of the compartment dimensionality.
			
			String baseunitname = getSubstanceBaseUnits(speciessubstanceunits);
			
			// Assign OPB properties
			if(baseunitname.equals("dimensionless"))
				prop = new PhysicalPropertyInComposite(null,null);
			
			// Deal with amount/concentration units
			else if(baseunitname.equals("mole")){
				
				if(hasonlysub){
					// look up factor for unit substance in semsimmodel and determine OPB property from that.
					// but if substance not in model...(level 3) ...

					prop = new PhysicalPropertyInComposite("Chemical molar amount", SemSimLibrary.OPB_CHEMICAL_MOLAR_AMOUNT_URI);
				}
				else prop = new PhysicalPropertyInComposite("Chemical concentration", SemSimLibrary.OPB_CHEMICAL_CONCENTRATION_URI);
			}
			// Deal with particle units
			else if(baseunitname.equals("item")){
				
				if(hasonlysub)
					prop = new PhysicalPropertyInComposite("Particle count", SemSimLibrary.OPB_PARTICLE_COUNT_URI);
				else prop = new PhysicalPropertyInComposite("Particle concentration", SemSimLibrary.OPB_PARTICLE_CONCENTRATION_URI);
			}
			// Deal with mass/density units
			else if(baseunitname.equals("kilogram") || baseunitname.equals("gram")){
				
				if(hasonlysub)
					prop = new PhysicalPropertyInComposite("Mass of solid entity", SemSimLibrary.OPB_MASS_OF_SOLID_ENTITY_URI);				
				else {
					double compartmentdims = sbmlmodel.getCompartment(compartmentname).getSpatialDimensions();
					
					if(compartmentdims==0.0){
						addErrorToModel("Compartment dimensions for species " + speciesid + " cannot be zero because species has mass units.");
						prop = new PhysicalPropertyInComposite(null,null);
					}
					
					else if(compartmentdims==1.0)
						prop = new PhysicalPropertyInComposite("Mass lineal density", SemSimLibrary.OPB_MASS_LINEAL_DENSITY_URI);					
					else if(compartmentdims==2.0)
						prop = new PhysicalPropertyInComposite("Mass areal density", SemSimLibrary.OPB_MASS_AREAL_DENSITY_URI);					
					else if(compartmentdims==3.0)
						prop = new PhysicalPropertyInComposite("Mass volumetric density", SemSimLibrary.OPB_MASS_VOLUMETRIC_DENSITY_URI);					
				}
			}
			else prop = new PhysicalPropertyInComposite(null,null);

						
			// Set initial condition
			if(species.isSetInitialAmount())
				
				if(hasonlysub)
					ds.setStartValue(Double.toString(species.getInitialAmount()));
				else{
					double compartmentsize = sbmlmodel.getCompartment(compartmentname).getSize();
					ds.setStartValue(Double.toString(species.getInitialAmount()/compartmentsize));
				}
			
			else if(species.isSetInitialConcentration()){
				
				if(species.getHasOnlySubstanceUnits()){
					double compartmentsize = sbmlmodel.getCompartment(compartmentname).getSize();
					ds.setStartValue(Double.toString(species.getInitialConcentration()*compartmentsize));
				}
				else
					ds.setStartValue(Double.toString(species.getInitialConcentration()));
			}

			// Set physical property annotation
			semsimmodel.addPhysicalPropertyForComposite(prop);
			ds.setAssociatedPhysicalProperty(prop);
			
			PhysicalEntity compartmentent = null;
			
			if(compartmentAndEntitiesMap.containsKey(species.getCompartment()))
				compartmentent = compartmentAndEntitiesMap.get(species.getCompartment());
			
			else System.err.println("WARNING: unknown compartment " + species.getCompartment() + " for species " + species.getId());
			
			ArrayList<PhysicalEntity> entlist = new ArrayList<PhysicalEntity>();
			ArrayList<StructuralRelation> rellist = new ArrayList<StructuralRelation>();

			PhysicalEntity speciesent = (PhysicalEntity) createSingularPhysicalComponentForSBMLobject(species);
			entlist.add(speciesent);
			
			if(compartmentent instanceof CompositePhysicalEntity){
				rellist.add(StructuralRelation.PART_OF);
				entlist.addAll(((CompositePhysicalEntity) compartmentent).getArrayListOfEntities());
				rellist.addAll(((CompositePhysicalEntity) compartmentent).getArrayListOfStructuralRelations());
			}
			else if (compartmentent!=null) { 
				rellist.add(StructuralRelation.PART_OF);
				entlist.add(compartmentent); 
			}
			
			CompositePhysicalEntity compositeent = semsimmodel.addCompositePhysicalEntity(entlist, rellist); // this also adds the singular physical entities to the model
			ds.setAssociatedPhysicalModelComponent(compositeent);
			speciesAndEntitiesMap.put(species.getId(), compositeent);
			
			if(speciesent instanceof ReferencePhysicalEntity)
				// We don't add the notes here, because if the species is defined against a reference term, 
				// the description of the reference term should be used
				addMetadataID(species, speciesent);
			else collectSBaseData(species, speciesent);
		}
	}
	
	/** Collect the SBML model's parameters */
	private void collectParameters(){
		for(int p=0; p<sbmlmodel.getListOfParameters().size(); p++){
			Parameter sbmlpar = sbmlmodel.getParameter(p);
			addParameter(sbmlpar, null);
		}
	}
	
	/** Collect the SBML model's rules */
	private void collectRules(){
		
		for(int r=0; r<sbmlmodel.getListOfRules().size(); r++){
			Rule sbmlrule = sbmlmodel.getRule(r);
			
			if(((ExplicitRule)sbmlrule).isSetVariable()){
				String varname = ((ExplicitRule) sbmlrule).getVariable();
								
				DataStructure ds = null;
				
				if(semsimmodel.containsDataStructure(varname)) 
					ds = semsimmodel.getAssociatedDataStructure(varname);
				else {
					ds = new Decimal(varname); 
					ds.setDeclared(true);
					semsimmodel.addDataStructure(ds);
				}
				
				String LHScodestart = sbmlrule.isRate() ? "d(" + varname + ")/d(" + timedomainname + ")" : varname;
				ds.getComputation().setComputationalCode(LHScodestart + " = " + sbmlrule.getMath().toFormula());
				
				String mathmlstring = null;
				
				try {
					mathmlstring = JSBML.writeMathMLToString(sbmlrule.getMath());
					mathmlstring = stripXMLheader(mathmlstring);
					mathmlstring = SemSimUtil.addLHStoMathML(mathmlstring, varname, sbmlrule.isRate(), timedomainname);
					ds.getComputation().setMathML(mathmlstring);
					
				} catch (SBMLException | XMLStreamException e) {
					e.printStackTrace();
				}
				
				// Remove start value if we are overwriting the computation for a species
				if(sbmlmodel.getSpecies(varname) != null && ! sbmlrule.isRate()) ds.setStartValue(null);
				
				// If we're assigning a rate rule to a parameter, use it's value attribute as the initial condition
				if(sbmlmodel.getParameter(varname) != null && sbmlrule.isRate()){
					Parameter par = sbmlmodel.getParameter(varname);
					ds.setStartValue(Double.toString(par.getValue()));
				}
				
				collectSBaseData(sbmlrule, ds.getComputation());
			}
			// don't do anything if the Rule is a non-Assignment rule
		}
	}
	
	/** Collect the SBML model's constraints */
	private void collectConstraints(){
		
		for(int c=0; c<sbmlmodel.getListOfConstraints().size(); c++){
			Constraint cons = sbmlmodel.getConstraint(c);
			String mathml;
			try {
				mathml = JSBML.writeMathMLToString(cons.getMath());
				mathml = stripXMLheader(mathml);
				RelationalConstraint rc = new RelationalConstraint("", mathml, cons.getMessageString());
				semsimmodel.addRelationalConstraint(rc);
			} catch (SBMLException | XMLStreamException e) {
				e.printStackTrace();
			}
		}
	}
	
	/** Collect the SBML model's discrete events */
	private void collectEvents(){
		
		try{
			for(int e=0; e<sbmlmodel.getListOfEvents().size(); e++){
				org.sbml.jsbml.Event sbmlevent = sbmlmodel.getEvent(e);
				
				org.sbml.jsbml.Trigger sbmltrigger = sbmlevent.getTrigger();
				String triggermathml = JSBML.writeMathMLToString(sbmltrigger.getMath());
				triggermathml = stripXMLheader(triggermathml);
				
				Event ssevent = new Event();
				ssevent.setName(sbmlevent.getId());
				ssevent.setTriggerMathML(triggermathml);			
				
				// Process event assignments
				for(int a=0; a<sbmlevent.getListOfEventAssignments().size(); a++){
					org.sbml.jsbml.EventAssignment ea = sbmlevent.getEventAssignment(a);
					String varname = ea.getVariable();
					EventAssignment ssea = new EventAssignment();
					
					String assignmentmathmlstring = JSBML.writeMathMLToString(ea.getMath());
					assignmentmathmlstring = stripXMLheader(assignmentmathmlstring);
					assignmentmathmlstring = SemSimUtil.addLHStoMathML(assignmentmathmlstring, varname, false, timedomainname);
					ssea.setMathML(assignmentmathmlstring);
					
					DataStructure outputds = semsimmodel.getAssociatedDataStructure(varname);
					ssea.setOutput(outputds);
					
					ssevent.addEventAssignment(ssea);
					
					// add Event to the output Data Structure's list of Events
					outputds.getComputation().addEvent(ssevent);
				}
				
				// Collect the delay info
				if(sbmlevent.isSetDelay()){
					Delay delay = sbmlevent.getDelay();
					String delaymathml = JSBML.writeMathMLToString(delay.getMath());
					delaymathml = stripXMLheader(delaymathml);
					ssevent.setDelayMathML(delaymathml);
				}
				
				// Collect priority (SBML level 3)
				if(sbmlmodel.getLevel()==3 && sbmlevent.isSetPriority()){
					Priority priority = sbmlevent.getPriority();
					String prioritymathml;
					prioritymathml = JSBML.writeMathMLToString(priority.getMath());
					prioritymathml = stripXMLheader(prioritymathml);
					ssevent.setPriorityMathML(prioritymathml);
				}
				
				// Set the time units (SBML level 2, versions 2 or 1)
				if(sbmlmodel.getLevel()==3 && sbmlmodel.getVersion()<3 && sbmlevent.isSetTimeUnits()){
					String timeunitsname = sbmlevent.getTimeUnits();
					ssevent.setTimeUnit(semsimmodel.getUnit(timeunitsname));
				}
				
				collectSBaseData(sbmlevent, ssevent);
				semsimmodel.addEvent(ssevent);
			}
		} catch(SBMLException | XMLStreamException e){
			e.printStackTrace();
		}
	}
	
	/** Collect specific assignments made at the beginning of the simulation  */
	private void collectInitialAssignments(){
		// Sets the t=0 value for a compartment, species or parameter. The symbol field refers to the ID of the SBML element.
		// If one of these elements already has an initial value stated in its construct, the initialAssignment overwrites it.
		for(int i=0; i<sbmlmodel.getListOfInitialAssignments().size();i++){
			InitialAssignment sbmlia = sbmlmodel.getInitialAssignment(i);
			SBMLInitialAssignment semsimia = new SBMLInitialAssignment();
			
			
			String variableID = sbmlia.getVariable();
			DataStructure output = semsimmodel.getAssociatedDataStructure(variableID);
			semsimia.setOutput(output);
			output.getComputation().addSBMLinitialAssignment(semsimia);
			
			try {
				String assignmentmathmlstring = JSBML.writeMathMLToString(sbmlia.getMath());
				assignmentmathmlstring = stripXMLheader(assignmentmathmlstring);
				assignmentmathmlstring = SemSimUtil.addLHStoMathML(assignmentmathmlstring, variableID, false, timedomainname);
				semsimia.setMathML(assignmentmathmlstring);
			} catch (SBMLException | XMLStreamException e) {
				e.printStackTrace();
			}
			
			collectSBaseData(sbmlia, semsimia);
		}
	}
	
	/** Collect the SBML model's FunctionDefinitions */
	private void collectFunctionDefinitions(){
		
		for(int i=0; i<sbmlmodel.getListOfFunctionDefinitions().size(); i++){
			org.sbml.jsbml.FunctionDefinition sbmlfd = sbmlmodel.getFunctionDefinition(i);
			semsim.model.computational.datastructures.SBMLFunctionOutput fd = new SBMLFunctionOutput(sbmlfd.getId());
			
			semsimmodel.addSBMLFunctionOutput(fd);
			collectSBaseData(sbmlfd, fd);
			
			String mathml = sbmlfd.getMathMLString();
			fd.getComputation().setMathML(mathml);
			
			Map<String,String> inputs = SemSimUtil.getInputNamesFromMathML(mathml, Pair.of(FUNCTION_PREFIX + sbmlfd.getId(), "_"));
			
			// Add parameters local to function
			for(String input : inputs.keySet()){
				String internalparname = inputs.get(input);
				Decimal internalpar = new Decimal(internalparname, SemSimTypes.DECIMAL);
				internalpar.setDeclared(false);
				semsimmodel.addDataStructure(internalpar); // Use prefixed name to create unique Decimal in SemSim model
			}
		}
	}
	
	
	/** Collect the SBML model's reaction data */
	private void collectReactions(){
		
		// If there are no species defined in model or no reactions defined, return
		if(sbmlmodel.getNumSpecies()==0 || sbmlmodel.getNumReactions()==0) return;
		
		// We assume that SBML Kinetic Laws are defined in units of substance/time.
		// First add units to model
		UnitOfMeasurement subpertimeuom = new UnitOfMeasurement("substance_per_time");
				
		UnitFactor substancefactor = new UnitFactor(modelsubstanceunits, 1.0, null);
		UnitFactor timefactor = new UnitFactor(modeltimeunits, -1.0, null);
		subpertimeuom.addUnitFactor(substancefactor);
		subpertimeuom.addUnitFactor(timefactor);
				
		semsimmodel.addUnit(subpertimeuom);
		
		// Assign OPB properties based on units
		String basesubstanceunitsname = getSubstanceBaseUnits(modelsubstanceunits);
		PhysicalPropertyInComposite prop = null;

		if(basesubstanceunitsname.equals("dimensionless"))
			prop = new PhysicalPropertyInComposite(null,null);
		
		else if(basesubstanceunitsname.equals("mole"))
			prop = new PhysicalPropertyInComposite("Chemical molar flow rate", SemSimLibrary.OPB_CHEMICAL_MOLAR_FLOW_RATE_URI);
		
		else if(basesubstanceunitsname.equals("item"))
			prop = new PhysicalPropertyInComposite("Particle flow rate", SemSimLibrary.OPB_PARTICLE_FLOW_RATE_URI);		
		else if(basesubstanceunitsname.equals("kilogram") || basesubstanceunitsname.equals("gram"))
			prop = new PhysicalPropertyInComposite("Material flow rate", SemSimLibrary.OPB_MATERIAL_FLOW_RATE_URI);		
		else prop = new PhysicalPropertyInComposite(null,null);
		
		semsimmodel.addPhysicalPropertyForComposite(prop);
				
		// Iterate through reactions
		for(int r=0; r<sbmlmodel.getListOfReactions().size(); r++){
			
			Reaction reaction = sbmlmodel.getReaction(r);
			String reactionID = reaction.getId();
			
			DataStructure rateds = semsimmodel.addDataStructure(new Decimal(reactionID)); 
			rateds.setAssociatedPhysicalProperty(prop);

			rateds.setDeclared(true);
			String thereactionprefix = REACTION_PREFIX + reactionID;
			
			if(reaction.isSetKineticLaw()){
				KineticLaw kineticlaw = reaction.getKineticLaw();
				
				rateds.setUnit(subpertimeuom);
				
				// Deal with kinetic law
				String mathmlstring = null;
				try {
					
					if(kineticlaw.isSetMath()){
						mathmlstring = JSBML.writeMathMLToString(kineticlaw.getMath());
					
						// For some reason the mathml string output for kinetic laws has <?xml version="1.0"...> at the head. Strip it.
						mathmlstring = stripXMLheader(mathmlstring);
						mathmlstring = SemSimUtil.addLHStoMathML(mathmlstring, reactionID, false, timedomainname);
									
						for(int l=0; l<kineticlaw.getListOfLocalParameters().size(); l++){
							LocalParameter lp = kineticlaw.getLocalParameter(l);
							DataStructure localds = addParameter(lp, thereactionprefix);
							mathmlstring = mathmlstring.replaceAll("<ci>\\s*" + lp.getId() + "\\s*</ci>", "<ci>" + localds.getName() + "</ci>");
						}
			
						rateds.getComputation().setMathML(mathmlstring);
						rateds.getComputation().setComputationalCode(reactionID + " = " + reaction.getKineticLaw().getMath().toFormula());
					}
				} catch (SBMLException | XMLStreamException e) {
					e.printStackTrace();
				}				
			}
						
			PhysicalProcess process = (PhysicalProcess) createSingularPhysicalComponentForSBMLobject(reaction);
			
			if(modelaccessor instanceof OMEXAccessor) {
				process.setIsFromSBMLinOMEXarchive(true);
			}
						
			// Set sources (reactants)
			for(int s=0; s<reaction.getNumReactants(); s++){
				String reactantname = reaction.getReactant(s).getSpecies();
				double stoich = reaction.getReactant(s).getStoichiometry();
				PhysicalEntity reactantent = speciesAndEntitiesMap.get(reactantname);
				process.addSource(reactantent, stoich);
									
				// Store info about species conservation for use in outputting species equations
				if(speciesAndConservation.containsKey(reactantname)){
					SpeciesConservation sc = speciesAndConservation.get(reactantname);
				
					if(sc.setWithConservationEquation) sc.consumedby.add(Pair.of(process, reaction)); // Use the name field on SemSim objects for species conservation info
				}
			}
			
			// Set sinks (products)
			for(int p=0; p<reaction.getNumProducts(); p++){
				String productname = reaction.getProduct(p).getSpecies();
				double stoich = reaction.getProduct(p).getStoichiometry();
				PhysicalEntity productent = speciesAndEntitiesMap.get(productname);
				process.addSink(productent, stoich);
				
				// Store info about species conservation for use in outputting species equations
				if(speciesAndConservation.containsKey(productname)){
					SpeciesConservation sc = speciesAndConservation.get(productname);
				
					if(sc.setWithConservationEquation) sc.producedby.add(Pair.of(process, reaction));
				}
			}
			
			// Set mediators (modifiers)
			for(int m=0; m<reaction.getNumModifiers(); m++){
				String mediatorname = reaction.getModifier(m).getSpecies();
				PhysicalEntity mediatorent = speciesAndEntitiesMap.get(mediatorname);
				process.addMediator(mediatorent);
			}
			
			rateds.setAssociatedPhysicalModelComponent(process);
						
			// Add process to model
			if(process instanceof ReferencePhysicalProcess) 
				semsimmodel.addReferencePhysicalProcess((ReferencePhysicalProcess) process);
			else 
				semsimmodel.addCustomPhysicalProcess((CustomPhysicalProcess) process);
			
			collectSBaseData(reaction, process);
		}
	}
	
	/** Create the conservation equations for the species in the model */
	private void setSpeciesConservationEquations(){
		
		for(String speciesid : speciesAndConservation.keySet()){
	      
			DataStructure speciesds = semsimmodel.getAssociatedDataStructure(speciesid);

			// The attribute hasOnlySubstanceUnits takes on a boolean value. 
			// In SBML Level 3, the attribute has no default value and must always
			// be set in a model; in SBML Level 2, it has a default value of false.
			Species sbmlspecies = sbmlmodel.getSpecies(speciesid);
				
			boolean subunits = false;

			if(sbmlspecies.isSetHasOnlySubstanceUnits()){
				subunits = sbmlspecies.getHasOnlySubstanceUnits();
			}
			else if(sbmlmodel.getLevel()==3.0){
				addErrorToModel("Required SBML level 3.0 attribute 'hasOnlySubstanceUnits' is unspecified for species " + speciesid + ".");
				return;
			}
			
			String compartmentid = sbmlmodel.getSpecies(speciesid).getCompartment();
			
			String eqstring = "";
			String eqmathml = "";
			String ws = subunits ? "  " : "   ";
			
			String LHS = "d(" + speciesid + ")/d(" + timedomainname + ")";
			
			eqmathml = SemSimUtil.mathMLelementStart + SemSimUtil.makeLHSforStateVariable(speciesid, timedomainname);
			
			SpeciesConservation speccon = speciesAndConservation.get(speciesid);
			
			
			// If the species is set as a boundary condition, set RHS to zero
			if(sbmlspecies.getBoundaryCondition()==true || sbmlspecies.getConstant()==true){
				eqmathml = eqmathml + "  <cn>0</cn>\n </apply>\n" + SemSimUtil.mathMLelementEnd;
				eqstring = "0";
			}
			
			// Otherwise create the RHS of the ODE for the conservation equation
			// but only if the species is actually a reactant or product one of the reactions.
			// If not a reactant or product, do not create a new equation for its solution.
			else if (speccon.consumedby.size()>0 || speccon.producedby.size()>0){
				String RHSstart = subunits ? "" : "  <divide/>\n   <apply>\n";
				eqmathml = eqmathml + "  <apply>\n" + RHSstart + ws + "<plus/>";
			

				// When a Species is to be treated in terms of concentrations or density, the units of the 
				// spatial size portion of the concentration value (i.e., the denominator in the units formula substance/ size)
				// are those indicated by the value of the 'units' attribute on the compartment in which the species is located.
				
				PhysicalEntity speciesent = speciesAndEntitiesMap.get(speciesid);
				
				for(Pair<PhysicalProcess,Reaction> reactionpair : speccon.producedby){
					Double stoich = reactionpair.getLeft().getSinkStoichiometry(speciesent);
					String reactionsbmlid = reactionpair.getRight().getId();
					
					if(stoich==1){
						eqmathml = eqmathml + "\n" + ws +" <ci>"+ reactionsbmlid + "</ci>";
						eqstring = eqstring + " + " + reactionsbmlid;
					}
					else{
						eqmathml = eqmathml + "\n" + ws + " <apply>\n" + ws + "  <times/>\n" + ws + "  <cn>" + stoich + "</cn>\n" 
								+ ws + "  <ci>" + reactionsbmlid + "</ci>\n" + ws + " </apply>";
						eqstring = eqstring + " + (" + stoich + "*" + reactionsbmlid + ")";
	
					}
				}
				
				for(Pair<PhysicalProcess,Reaction> reactionpair : speccon.consumedby){
					Double stoich = reactionpair.getLeft().getSourceStoichiometry(speciesent);
					String reactionsbmlid = reactionpair.getRight().getId();

					if(stoich==1){
						eqmathml = eqmathml + "\n" + ws + " <apply>\n" + ws + "  <times/>\n" + ws + "  <cn>-1</cn>\n" + ws 
								+ "  <ci>" + reactionsbmlid + "</ci>\n" + ws + " </apply>";					
						eqstring = eqstring + " - " + reactionsbmlid;
					}
					else{
						eqmathml = eqmathml + "\n" + ws + " <apply>\n" + ws + "  <times/>\n" + ws + "  <cn>-" + stoich + "</cn>\n" + ws 
								+ "  <ci>" + reactionsbmlid + "</ci>\n" + ws + " </apply>";	
						eqstring = eqstring + " - (" + stoich + "*" + reactionsbmlid + ")";
					}
				}
				
				String eqmathmlend = subunits ? "" : "   <ci>" + compartmentid + "</ci>\n  </apply>\n"; // if concentration units, include the divide operation closer
				eqmathml = eqmathml + "\n" + ws + "</apply>\n" + eqmathmlend + " </apply>\n" + SemSimUtil.mathMLelementEnd;  // end plus operation, end eq operation
				
				// Annotate the data structure's computation as an OPB Derivative Constraint
				ReferencePhysicalDependency rpd = semsimmodel.addReferencePhysicalDependency(
						new ReferencePhysicalDependency(SemSimLibrary.OPB_DERIVATIVE_CONSTRAINT_URI, "Derivative constraint"));
				speciesds.getComputation().setPhysicalDependency(rpd);
			}
			
			// Store the equations
			if(eqstring.length()>0){
				
				// Strip first + operator if present
				if(eqstring.trim().startsWith("+"))
					eqstring = eqstring.substring(3, eqstring.length()); 
				
				//Add compartment divisor if needed to computational code
				eqstring = subunits ? eqstring : "(" + eqstring + ")/" + compartmentid; // add compartment divisor if species in conc. units
				eqstring = LHS + " = " + eqstring; // add LHS to computational code string
				
				speciesds.getComputation().setComputationalCode(eqstring);
				speciesds.getComputation().setMathML(eqmathml);
			}
		}
	}
	
	/**
	 * Set the computational dependency network for the semsim model.
	 * For DataStructures that represent reactions, use the reaction name
	 * as the prefix for local parameters.
	 */
	public void setComputationalDependencyNetwork(){
		for(DataStructure ds : semsimmodel.getAssociatedDataStructures()){
			
			Pair<String,String> prefixanddelimiter = null;
			
			// If we are looking at a reaction rate data structure, use reaction
			// prefix so we can ID local parameters as inputs
			if(sbmlmodel.getReaction(ds.getName())!=null)
				prefixanddelimiter = Pair.of(REACTION_PREFIX + ds.getName(),".");
			else if(sbmlmodel.getFunctionDefinition(ds.getName())!=null)
				prefixanddelimiter = Pair.of(FUNCTION_PREFIX + ds.getName(),"_");
			
			SemSimUtil.setComputationInputsForDataStructure(semsimmodel, ds, prefixanddelimiter);
		}
	}
	
	/**
	 * Collect the SemSim annotations for data structures in the model.
	 * This excludes data structures corresponding to species and reactions in the model.
	 * @return RDF content as a String
	 * @throws IOException 
	 * @throws ZipException 
	 */
	private String collectSemSimRDF() throws ZipException, IOException{
		Document projdoc = modelaccessor.getJDOMDocument();
		
		// Collect namespace b/c JSBML always seems to reutrn null for getNamespace() fxns in Model and SBMLDocument 
		Namespace sbmlmodelns = projdoc.getRootElement().getNamespace();	
		String rdfstring = null;
		
		//TODO: should change this so that it only reads in the semsim annotation into the JDOM document (saves time)
		
		if(projdoc.hasRootElement()){
			Element modelel = projdoc.getRootElement().getChild("model", sbmlmodelns);
				
			if(modelel != null){
				Element modelannel = modelel.getChild("annotation", sbmlmodelns);
						
				if(modelannel != null){	
					Element modelannssel = modelannel.getChild(SBMLwriter.semsimAnnotationElementName, sbmlmodelns);
								
					if(modelannssel != null){
						Element rdfel = modelannssel.getChild("RDF", RDFNamespace.RDF.createJdomNamespace());
						
						if(rdfel != null){
							XMLOutputter xmloutputter = new XMLOutputter();
							rdfstring = xmloutputter.outputString(rdfel);
						}
					}
				}
			}
		}

		return rdfstring;
	}
	
	
	/**
	 * Collect all data common to an SBase object and copy it into a specified SemSimObject
	 * @param sbmlobject An SBase SBML object
	 * @param semsimobject A SemSimObject representing the SBML object
	 */
	private void collectSBaseData(SBase sbmlobject, SemSimObject semsimobject){
		
		addNotes(sbmlobject, semsimobject);
		addMetadataID(sbmlobject, semsimobject);
		
		// need to collect SBO terms here?
	}
	
	/**
	 *  Copy the notes attached to an SBML element and into the Description field of a SemSimObject 
	 * @param sbmlobject An SBase SBML object
	 * @param semsimobject The SemSimObject representing the SBase object
	 */
	protected void addNotes(SBase sbmlobject, SemSimObject semsimobject){
				
		if(sbmlobject.isSetNotes()){
				
			try {
				String desc = sbmlobject.getNotesString();
				desc = desc.replaceAll("<notes>\\s*<body xmlns=\"http://www.w3.org/1999/xhtml\">","");
				desc = desc.replaceAll("</body>\\s*</notes>","");
				desc = desc.replaceAll("<notes>\\s*<p xmlns=\"http://www.w3.org/1999/xhtml\">","");
				desc = desc.replaceAll("</p>\\s*</notes>","");

				desc = StringEscapeUtils.unescapeXml(desc.trim());				
				semsimobject.setDescription(desc);
			} catch (XMLStreamException e) {
//				e.printStackTrace();  // commented out b/c JSBML keeps throwing exceptions in stack trace 
			}
		}
	}
	
	/**
	 * Copy the metadataID from an SBase object to a SemSimObject
	 * @param sbmlobject An SBase SBML object
	 * @param semsimobject A SemSimObject representing the SBase object
	 */
	protected void addMetadataID(SBase sbmlobject, SemSimObject semsimobject){
		semsimmodel.assignValidMetadataIDtoSemSimObject(sbmlobject.getMetaId(),semsimobject);
	}
	
	
	/**
	 * Collects all biological qualifier annotations for a given SBase object and 
	 * converts them into a set of ReferenceOntologyAnnotations. If more than one
	 * identity annotation is applied (BQBiol:is), as is common in SBML models, only the first
	 * annotation that uses a term from a SemSim preferred knowledge resource is collected.
	 * @param sbmlobject An SBase SBML object
	 * @return The set of ReferenceOntologyAnnotations associated with the SBase object
	 */
	private Set<ReferenceOntologyAnnotation> getBiologicalQualifierAnnotations(SBase sbmlobject){
		
		OntologyDomain ontdomain = ReferenceOntologies.OntologyDomain.PhysicalEntity; 
		if (! isEntity(sbmlobject)) ontdomain = ReferenceOntologies.OntologyDomain.PhysicalProcess;
		
		Set<ReferenceOntologyAnnotation> anns = new HashSet<ReferenceOntologyAnnotation>();
		
		// Get CV terms
		for(int i=0; i<sbmlobject.getNumCVTerms();i++){
			CVTerm term = sbmlobject.getCVTerm(i);
			
			// If the CV term is used with a biological qualifier
			if(term.getQualifierType()==CVTerm.Type.BIOLOGICAL_QUALIFIER){
				
				Qualifier q = term.getBiologicalQualifierType();
				
				// If we know the relation
				if(SemSimRelations.getRelationFromBiologicalQualifier(q)!= SemSimRelation.UNKNOWN){
					
					int numidentityanns = 0;
					
					for(int j=0; j<term.getNumResources(); j++){
						String uristring = term.getResourceURI(j);
						String namespace = SemSimOWLFactory.getNamespaceFromIRI(uristring);
						
						// If we can look up the knowledge resource given the namespace of the CV term
						if(ReferenceOntologies.getReferenceOntologyByNamespace(namespace)!=null){
							ReferenceOntology refont = ReferenceOntologies.getReferenceOntologyByNamespace(namespace);
							
							// If the knowledge resource is part of the limited set used for SemSim annotation 
							if(ontdomain.domainHasReferenceOntology(refont)){
								Relation relation = (q==Qualifier.BQB_IS) ? 
										SemSimRelation.HAS_PHYSICAL_DEFINITION : SemSimRelations.getRelationFromBiologicalQualifier(q);
								
								// If we're looking at an identity relation...
								if(relation==SemSimRelation.HAS_PHYSICAL_DEFINITION || relation==SemSimRelation.BQB_IS){
									
									// And we haven't added one yet, add it
									if(numidentityanns==0){
										anns.add(new ReferenceOntologyAnnotation(relation, URI.create(uristring), uristring, sslib));
										numidentityanns++;
									}
									// Otherwise skip the identity annotation
									else System.err.println("WARNING: Multiple reference annotations for " + 
												getIDforSBaseObject(sbmlobject) + ". Ignoring annotation against " + uristring);
								}
								// Otherwise add the non-identity annotation
								else if(relation != null) anns.add(new ReferenceOntologyAnnotation(relation, URI.create(uristring), uristring, sslib));
							}
						}
					}
				}
			}
		}
		return anns;
	}
	
	/**
	 * Collects all model qualifier annotations for a given SBase object and 
	 * converts them into a set of ReferenceOntologyAnnotations.
	 * @param sbmlobject An SBase SBML object
	 * @return The set of ReferenceOntologyAnnotations associated with the SBase object
	 */	private Set<ReferenceOntologyAnnotation> getModelQualifierAnnotations(SBase sbmlobject){
		
		Set<ReferenceOntologyAnnotation> anns = new HashSet<ReferenceOntologyAnnotation>();
		
		// If the CV term is used with a model qualifier
		for(int i=0; i<sbmlobject.getNumCVTerms();i++){
			CVTerm term = sbmlobject.getCVTerm(i);
			
			if(term.getQualifierType()==CVTerm.Type.MODEL_QUALIFIER){
				Qualifier q = term.getModelQualifierType();

				if(SemSimRelations.getRelationFromModelQualifier(q)!= SemSimRelation.UNKNOWN){
					
					for(int h=0; h<term.getNumResources(); h++){
						String uri = term.getResourceURI(h);
						Relation relation = (q==Qualifier.BQM_IS) ? SemSimRelation.HAS_PHYSICAL_DEFINITION : SemSimRelations.getRelationFromModelQualifier(q);
						anns.add(new ReferenceOntologyAnnotation(relation, URI.create(uri), uri, sslib));

					}
				}
			}
		}
		return anns;
	}
	

	 /**
	 * Assign a singular SemSim physical entity object to an SBML model element. 
	 * Make sure that if a custom entity or process is created, that it has a unique name.
	 * @param sbmlobject An SBase SBML object
	 * @return The singular SemSim physical entity object assigned to 
	 */
	 private PhysicalModelComponent createSingularPhysicalComponentForSBMLobject(SBase sbmlobject){
		
		String id = getIDforSBaseObject(sbmlobject);
		String name = getNameforSBaseObject(sbmlobject);
		
		boolean isentity = isEntity(sbmlobject);

		// If there is a name specified for the physical component, use that for the name of the semsim representation (as long as it's not already taken), otherwise use the id
		boolean nameAlreadyTaken = isentity ? semsimmodel.getCustomPhysicalEntityByName(name)!=null : semsimmodel.getCustomPhysicalProcessByName(name)!=null;
		
		String semsimname = (name!=null && ! name.isEmpty() && ! nameAlreadyTaken) ? name : id;
		
		PhysicalModelComponent pmc = isentity ? new CustomPhysicalEntity(semsimname, "") : new CustomPhysicalProcess(semsimname, "");
	
		Set<ReferenceOntologyAnnotation> tempanns = new HashSet<ReferenceOntologyAnnotation>();
		tempanns.addAll(getBiologicalQualifierAnnotations(sbmlobject));
		
		for(ReferenceOntologyAnnotation ann : getBiologicalQualifierAnnotations(sbmlobject)){
			
			Relation annrelation = ann.getRelation();
			
			// If there is a physical identity annotation, create reference physical component
			if(annrelation.equals(SemSimRelation.HAS_PHYSICAL_DEFINITION) || annrelation.equals(SemSimRelation.BQB_IS)){
				// if entity, use reference term, but don't otherwise
				if(isentity){
					pmc = semsimmodel.addReferencePhysicalEntity(
							new ReferencePhysicalEntity(ann.getReferenceURI(), ann.getValueDescription())); 
				}
				
				tempanns.remove(ann);
				break;
			}
			// If the annotation is not a physical definition, add the reference terms from the annotation to the model but keep
			// pmc a custom term
			else if(isentity)
				semsimmodel.addReferencePhysicalEntity(new ReferencePhysicalEntity(ann.getReferenceURI(), ann.getValueDescription()));
			else
				semsimmodel.addReferencePhysicalProcess(new ReferencePhysicalProcess(ann.getReferenceURI(), ann.getValueDescription()));
		}
		
		tempanns.addAll(getModelQualifierAnnotations(sbmlobject));
		for(Annotation ann : tempanns) pmc.addAnnotation(ann);
		
		return pmc;
	}
	
	
	/**
	 * @param sbmlel An SBase object
	 * @return Whether a given SBML element represents a physical entity
	 */
	private boolean isEntity(SBase sbmlel){
		
		return (sbmlel instanceof Compartment || sbmlel instanceof Species);
	}
	
	/**
	 * @param sbmlobject An SBase object
	 * @return The SBML ID for the input object
	 */
	private String getIDforSBaseObject(SBase sbmlobject){
		
		String id = null;
		boolean isentity = isEntity(sbmlobject);
		
		if(! isentity) id = ((Reaction)sbmlobject).getId();
		else if(sbmlobject instanceof Compartment) id = ((Compartment)sbmlobject).getId();
		else if(sbmlobject instanceof Species) id = ((Species)sbmlobject).getId();
		return id;
	}
	
	/**
	 * @param sbmlobject An SBase object
	 * @return The SBML name for the object
	 */
	private String getNameforSBaseObject(SBase sbmlobject){
		
		String name = null;
		boolean isentity = isEntity(sbmlobject);
		
		if(! isentity) name = ((Reaction)sbmlobject).getName();
		else if(sbmlobject instanceof Compartment) name = ((Compartment)sbmlobject).getName();
		else if(sbmlobject instanceof Species) name = ((Species)sbmlobject).getName();
		return name;
	}
	
	/**
	 * Add an SBML parameter to the SemSim model. This can also be used for SBML LocalParameters.
	 * @param qwu The SBML parameter to add to the SemSim model.
	 * @param prefix Prefix to use when creating the ID for the parameter
	 * @return {@link DataStructure} representing the SBML parameter
	 */	
	private DataStructure addParameter(QuantityWithUnit qwu, String prefix){

		String ID = (prefix==null || prefix.equals("")) ? qwu.getId() : prefix + "." + qwu.getId();
		
		if(semsimmodel.containsDataStructure(ID)){
			addErrorToModel("Multiple data structures with name " + ID);
			return null;
		}
		
		DataStructure ds = semsimmodel.addDataStructure(new Decimal(ID));
		ds.setDeclared(true);
		
		UnitOfMeasurement unitforpar = semsimmodel.getUnit(qwu.getUnits());
		ds.setUnit(unitforpar);
		
		ds.getComputation().setComputationalCode(ID + " = " + Double.toString(qwu.getValue()));
		String mathmlstring = SemSimUtil.mathMLelementStart + " <apply>\n  <eq />\n  <ci>" 
				+ ID + "</ci>\n  <cn>" + qwu.getValue() + "</cn>\n </apply>\n" + SemSimUtil.mathMLelementEnd;
		ds.getComputation().setMathML(mathmlstring);

		collectSBaseData(qwu, ds);
		
		// Collect annotations on the parameter		
		rdfreader.getDataStructureAnnotations(ds);
		
		return ds;
	}
	
	
	/**
	 *  Get the base unit name for the model's "substance units"
	 * @param substanceunits The "substance" units in the SemSim model
	 * @return The name of the base unit for the "substance" unit in the SemSim model
	 */
	private String getSubstanceBaseUnits(UnitOfMeasurement substanceunits){
		String val = "mole";
		
		if(substanceunits != null){
			
			if(substanceunits.getUnitFactors().size()==1){
				
				for(UnitFactor uf : substanceunits.getUnitFactors())
					val = uf.getBaseUnit().getName();
							
			}
		}
		return val;
	}
	
	/** Select appropriate set of base units based on SBML level/version */
	private void setBaseUnits(){
		baseUnits.clear();
		
		if( sbmlmodel.getLevel()==3) 
			baseUnits.addAll(SBMLconstants.SBML_LEVEL_3_BASE_UNITS);
		
		else if( sbmlmodel.getLevel()==2 && sbmlmodel.getVersion()==1) 
			baseUnits.addAll(SBMLconstants.SBML_LEVEL_2_VERSION_1_BASE_UNITS);
		
		else if( sbmlmodel.getLevel()==2 && sbmlmodel.getVersion()>1) 
			baseUnits.addAll(SBMLconstants.SBML_LEVEL_2_VERSION_2_BASE_UNITS);
	}
	
	/**
	 * Add an error to the SemSim model
	 * @param description A textual description of the error
	 */
	private void addErrorToModel(String description){
		String errmsg = "SBML-TO-SEMSIM CONVERSION ERROR:\n" + description;
		System.err.println(errmsg);
		semsimmodel.addError(errmsg);
	}
	
	/**
	 * Strip the XML declaration header from a MathML expression
	 * @param mathmlstring The MathML to process
	 * @return The MathML string stripped of the XML declaration header
	 */
	private String stripXMLheader(String mathmlstring){
		mathmlstring = mathmlstring.replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n", "");
		mathmlstring = mathmlstring.replace("<?xml version=\'1.0\' encoding=\'UTF-8\'?>\n", ""); // For single quotes
		return mathmlstring;
	}
	
	/**
	 * Stores info relevant for formulating the species conservation 
	 * equations in an SBML model. 
	 * @author mneal
	 *
	 */
	private class SpeciesConservation{
		public boolean setWithConservationEquation;
		public ArrayList<Pair<PhysicalProcess,Reaction>> consumedby;
		public ArrayList<Pair<PhysicalProcess,Reaction>> producedby;
		
		public SpeciesConservation(){
			setWithConservationEquation = true;
			consumedby = new ArrayList<Pair<PhysicalProcess,Reaction>>();
			producedby = new ArrayList<Pair<PhysicalProcess,Reaction>>();
		}
	}
}
