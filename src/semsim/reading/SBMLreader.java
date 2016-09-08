package semsim.reading;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.xml.stream.XMLStreamException;

import org.jdom.Document;
import org.jdom.Element;
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

import semsim.SemSimLibrary;
import semsim.SemSimObject;
import semsim.annotation.Annotation;
import semsim.annotation.ReferenceOntologyAnnotation;
import semsim.annotation.CurationalMetadata.Metadata;
import semsim.annotation.Relation;
import semsim.definitions.RDFNamespace;
import semsim.definitions.ReferenceOntologies;
import semsim.definitions.SBMLconstants;
import semsim.definitions.SemSimRelations;
import semsim.definitions.SemSimRelations.SemSimRelation;
import semsim.definitions.SemSimRelations.StructuralRelation;
import semsim.definitions.ReferenceOntologies.OntologyDomain;
import semsim.definitions.ReferenceOntologies.ReferenceOntology;
import semsim.model.collection.SemSimModel;
import semsim.model.collection.Submodel;
import semsim.model.computational.Event.EventAssignment;
import semsim.model.computational.RelationalConstraint;
import semsim.model.computational.Event;
import semsim.model.computational.datastructures.DataStructure;
import semsim.model.computational.datastructures.Decimal;
import semsim.model.computational.units.UnitFactor;
import semsim.model.computational.units.UnitOfMeasurement;
import semsim.model.physical.PhysicalEntity;
import semsim.model.physical.PhysicalModelComponent;
import semsim.model.physical.PhysicalProcess;
import semsim.model.physical.object.CompositePhysicalEntity;
import semsim.model.physical.object.CustomPhysicalEntity;
import semsim.model.physical.object.CustomPhysicalProcess;
import semsim.model.physical.object.PhysicalProperty;
import semsim.model.physical.object.PhysicalPropertyinComposite;
import semsim.model.physical.object.ReferencePhysicalDependency;
import semsim.model.physical.object.ReferencePhysicalEntity;
import semsim.model.physical.object.ReferencePhysicalProcess;
import semsim.owl.SemSimOWLFactory;
import semsim.utilities.SemSimUtil;
import semsim.writing.SBMLwriter;

public class SBMLreader extends ModelReader{

	private SBMLDocument sbmldoc;
	private Model sbmlmodel;
	private Map<String, PhysicalEntity> compartmentAndEntitiesMap = new HashMap<String, PhysicalEntity>();
	private Map<String, CompositePhysicalEntity> speciesAndEntitiesMap = new HashMap<String, CompositePhysicalEntity>();
	private Map<String, SpeciesConservation> speciesAndConservation = new HashMap<String, SpeciesConservation>();  // associates species with the reactions they participate in
	private Set<String> baseUnits = new HashSet<String>();
	private Submodel parametersubmodel;
	private Submodel speciessubmodel;
	private Submodel compartmentsubmodel;
	
	private static final String mathMLelementStart = "<math xmlns=\"http://www.w3.org/1998/Math/MathML\">\n";
	private static final String mathMLelementEnd = "</math>";
	private String timedomainname = "t";
	public static final String reactionprefix = "Reaction_";
	private UnitOfMeasurement timeunits;
	private UnitOfMeasurement substanceunits;
	
	private SemSimRDFreader rdfreader;
	
	
	public SBMLreader(File file) {
		super(file);
	}
	
	public SBMLreader(ModelAccessor accessor){
		super(accessor);
	}

	@Override
	public SemSimModel read() throws IOException, InterruptedException,
			OWLException, XMLStreamException {
		
		// Load the SBML file into a new SBML model
		sbmldoc = new SBMLReader().readSBMLFromString(modelaccessor.getLocalModelTextAsString());
		
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
		
		// Collect function definitions. Not used in SBML level 1.
		// collectFunctionDefinitions();
		if (sbmlmodel.getListOfFunctionDefinitions().size()>0)
			addErrorToModel("SBML source model contains function definitions but these are not yet supported in SemSim.");

		//collectCompartmentTypes();  // We ignore compartment types for now. This class is not available in JSBML.
		// See http://sbml.org/Software/libSBML/5.11.4/docs/formatted/java-api/org/sbml/libsbml/CompartmentType.html

		//collectSpeciesTypes();  // Ignore these for now, too. This class is not available in JSBML.
		// See http://sbml.org/Software/libSBML/5.11.4/docs/formatted/java-api/org/sbml/libsbml/SpeciesType.html
		
		// collectInitialAssignments();
		// Sets the t=0 value for a compartment, species or parameter. The symbol field refers to the ID of the SBML element.
		// If one of these elements already has an initial value stated in its construct, the initialAssignment overwrites it.
		
		
		if (sbmlmodel.getListOfInitialAssignments().size()>0)
			addErrorToModel("SBML source model contains initial assignments but these are not yet supported in SemSim.");
		
				
		// if any errors at this point, return model
		if(semsimmodel.getErrors().size()>0) return semsimmodel;
		
		// Create submodels for compartments and species. Each reaction gets its own submodel later.
		if(sbmlmodel.getListOfParameters().size()>0) 
			parametersubmodel = semsimmodel.addSubmodel(new Submodel("Parameters"));
		
		if(sbmlmodel.getListOfCompartments().size()>0) 
			compartmentsubmodel = semsimmodel.addSubmodel(new Submodel("Compartments"));
		
		if(sbmlmodel.getListOfSpecies().size()>0)
			speciessubmodel = semsimmodel.addSubmodel(new Submodel("Species"));
		
		collectSemSimRDF();
		collectModelLevelData();
		setBaseUnits();
		collectUnits();
		setTimeDomain();
		collectCompartments();
		collectSpecies();
		collectParameters();
		collectReactions();
		setSpeciesConservationEquations();
		collectRules();
		collectConstraints();
		collectEvents();
		setComputationalDependencyNetwork();

		return semsimmodel;
	}
	
	/**
	 * Collect the SBML model's model-level data such as model name, semantic annotations, etc.
	 */
	private void collectModelLevelData(){
		
		sbmlmodel.getVersion();
		collectSBaseData(sbmlmodel, semsimmodel);
		semsimmodel.setName(sbmlmodel.getId());
		semsimmodel.setModelAnnotation(Metadata.fullname, sbmlmodel.getName());
		
		// TODO: collect model-level annotations here, too.		
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
	
	// For SBML level 2, add the reserved units, if not already stated in listOfUnitDefinitions
	private void addSBMLlevel2reservedUnits() {
			
		for(String resunitname : SBMLconstants.SBML_LEVEL_2_RESERVED_UNITS_MAP.keySet()){
			
			if( ! semsimmodel.containsUnit(resunitname)){
				
				UnitOfMeasurement resuom = new UnitOfMeasurement(resunitname);
				String baseunitname = SBMLconstants.SBML_LEVEL_2_RESERVED_UNITS_MAP.get(resunitname);
				UnitOfMeasurement baseuom = null;
				
				// Add base unit, if not already in model
				if( ! semsimmodel.containsUnit(baseunitname)){
					baseuom = new UnitOfMeasurement(baseunitname);
					baseuom.setFundamental(baseUnits.contains(baseunitname));
					semsimmodel.addUnit(baseuom);
				}
				else baseuom = semsimmodel.getUnit(baseunitname);
				
				resuom.addUnitFactor(new UnitFactor(baseuom, 1, null));
				semsimmodel.addUnit(resuom);
			}
		}
		
	}
	
	/**
	 * Set the temporal solution domain for the SemSim model
	 */
	private void setTimeDomain(){
		
		boolean timenamepredefined = false;
		
		// Check if the time csymbol is used anywhere in the model's MathML.
		Document doc = getJDOMdocumentFromString(semsimmodel, modelaccessor.getLocalModelTextAsString());
		
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
		
		if(sbmlmodel.getLevel()==3.0 && sbmlmodel.isSetTimeUnits()){
			timeunits = new UnitOfMeasurement(sbmlmodel.getTimeUnits());
			semsimmodel.addUnit(timeunits);
		}
		else if(semsimmodel.containsUnit("time")) timeunits = semsimmodel.getUnit("time");
				
		timeds.setUnit(timeunits);

		PhysicalProperty timeprop = new PhysicalProperty("Time", SemSimLibrary.OPB_TIME_URI);
		semsimmodel.addPhysicalProperty(timeprop);
		timeds.setSingularAnnotation(timeprop);
		
		semsimmodel.addDataStructure(timeds);
		
		if(speciessubmodel!=null) speciessubmodel.addDataStructure(timeds);		
	}

	/**
	 *  Collect the SBML model's compartment data
	 */
	private void collectCompartments(){
		
		for(int c=0; c<sbmlmodel.getListOfCompartments().size(); c++){
			Compartment sbmlc = sbmlmodel.getCompartment(c);
			String compid = sbmlc.getId();
			
			// NOTE: we ignore the "outside" attribute for now
			
			DataStructure ds = semsimmodel.addDataStructure(new Decimal(compid));
			ds.setDeclared(true);
			compartmentsubmodel.addDataStructure(ds);
			
			String mathml = mathMLelementStart + " <apply>\n  <eq />\n  <ci>" + compid + "</ci>\n  <cn>" 
					+ sbmlc.getSize() + "</cn>\n </apply>\n" + mathMLelementEnd;
			ds.getComputation().setMathML(mathml);
			ds.getComputation().setComputationalCode(compid + " = " + Double.toString(sbmlc.getSize()));
			
			String defaultunits = null;
			PhysicalPropertyinComposite prop = null;
			String modelobjectspecifieddefaultunits = "";
			
			// Add physical property here
			if(sbmlc.getSpatialDimensions()==3.0){
				prop = new PhysicalPropertyinComposite("", URI.create(RDFNamespace.OPB.getNamespaceasString() + "OPB_00154"));
				defaultunits = "volume";
				modelobjectspecifieddefaultunits = sbmlmodel.getVolumeUnits();
			}
			else if(sbmlc.getSpatialDimensions()==2.0){
				prop = new PhysicalPropertyinComposite("", URI.create(RDFNamespace.OPB.getNamespaceasString() + "OPB_00295"));
				defaultunits = "area";
				modelobjectspecifieddefaultunits = sbmlmodel.getAreaUnits();
			}
			else if(sbmlc.getSpatialDimensions()==1.0){
				prop = new PhysicalPropertyinComposite("", URI.create(RDFNamespace.OPB.getNamespaceasString() + "OPB_01064"));

				defaultunits = "length";
				modelobjectspecifieddefaultunits = sbmlmodel.getLengthUnits();
			}
						
			// Set the units for the compartment
			if(sbmlc.isSetUnits()){
				
				if(semsimmodel.containsUnit(sbmlc.getUnits()))
					ds.setUnit(semsimmodel.getUnit(sbmlc.getUnits()));
				
				// If the units are set but weren't found in the semsim model
				// try a case-insensitive check. This is to account for an issue
				// revealed in BIOMD165 compartment "cell" where the units are
				// "litre" but JSBML converted this units name to "Litre" when
				// processing the original unit definition. The unit "Litre" is 
				// stored in the semsim model, not "litre".
				else{ 
					for(UnitOfMeasurement uom : semsimmodel.getUnits()){
						if(uom.getName().toLowerCase().equals(sbmlc.getUnits().toLowerCase()))
							ds.setUnit(uom);
					}
				}
			}
			else if(semsimmodel.containsUnit(defaultunits))
				ds.setUnit(semsimmodel.getUnit(defaultunits));
			
			// If the model is SBML Level 3 and the SBML Model object specifies the units
			// for compartments with this compartment's spatial dimension, then create the
			// unit in the SemSim model and assign it to the compartment
			else if( ! modelobjectspecifieddefaultunits.isEmpty() && sbmlmodel.getLevel()==3.0){
				UnitOfMeasurement uom = new UnitOfMeasurement(modelobjectspecifieddefaultunits);
				semsimmodel.addUnit(uom);
				ds.setUnit(uom);
			}
			
			// Otherwise the unit for the compartment is undefined
			else System.err.println("WARNING: Units for compartment " + sbmlc.getId() + " were undefined");
			
			// Collect the physical entity representation of the compartment
			PhysicalEntity compartmentent = null;

			// If the compartment is annotated with a SemSim annotation, collect it
			if(rdfreader.hasPropertyAnnotationForDataStructure(ds)){				
				rdfreader.getDataStructureAnnotations(ds);
				
				PhysicalModelComponent pmc = ds.getAssociatedPhysicalModelComponent();
				
				if(ds.hasAssociatedPhysicalComponent() && pmc instanceof PhysicalEntity)
					compartmentent = (PhysicalEntity)pmc;
				
			}
			// Otherwise we use the info in the SBML to get the physical entity 
			// representation of the compartment
			else{	
				ds.setAssociatedPhysicalProperty(prop);
				
				PhysicalEntity singlecompartmentent = (PhysicalEntity) createPhysicalComponentForSBMLobject(sbmlc);
							
				ArrayList<PhysicalEntity> entlist = new ArrayList<PhysicalEntity>();
				entlist.add(singlecompartmentent);
				ArrayList<StructuralRelation> rellist = new ArrayList<StructuralRelation>();
				
				compartmentent = semsimmodel.addCompositePhysicalEntity(entlist, rellist); // this also adds the singular physical entities to the model
				ds.setAssociatedPhysicalModelComponent(compartmentent);
			}
			
			compartmentAndEntitiesMap.put(compid, compartmentent);
			
			collectSBaseData(sbmlc, compartmentent);
		}
	}
	
	/**
	 *  Collect the SBML model's chemical species data
	 */
	private void collectSpecies(){
		
		for(int s=0; s<sbmlmodel.getListOfSpecies().size(); s++){
			Species species = sbmlmodel.getSpecies(s);
			
			String speciesid = species.getId();

			DataStructure ds = semsimmodel.addDataStructure(new Decimal(speciesid));
			
			ds.setDeclared(true);
			ds.setSolutionDomain(semsimmodel.getAssociatedDataStructure(timedomainname));
			speciessubmodel.addDataStructure(ds);
			
			boolean isConstant = species.getConstant();
			boolean isBoundaryCondition = species.getBoundaryCondition();
			boolean isSetWithRuleAssignment = sbmlmodel.getRule(speciesid) != null;
			
			if(isConstant){
				
				if(isBoundaryCondition){
					// don't apply conservation eq. set initial condition only
					// CAN be a reactant or product
				}
				else{
					// don't apply conservation eq. set IC only
					// CANNOT be a reactant or product
				}
			}
			else{
				
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
			PhysicalPropertyinComposite prop = null;
			
			/*
			From http://sbml.org/Software/libSBML/5.11.4/docs/formatted/java-api/org/sbml/libsbml/Species.html
			In SBML Level 3, if the 'substanceUnits' attribute is not set on a given
			Species object instance, then the unit of amount for that species is inherited
			from the 'substanceUnits' attribute on the enclosing Model object instance. 
			If that attribute on Model is not set either, then the unit associated with the
			species' quantity is undefined.
			*/
						
			if(sbmlmodel.getLevel()==3){
				if(species.isSetSubstanceUnits())
					substanceunits = semsimmodel.getUnit(species.getSubstanceUnits());
				
				else if(sbmlmodel.isSetSubstanceUnits())
					substanceunits = semsimmodel.getUnit(sbmlmodel.getSubstanceUnits());
				
				else
					System.err.println("WARNING: Substance units for " + species.getId() + " were undefined");
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
			else if(sbmlmodel.getLevel()==2){
				if(species.isSetSubstanceUnits())
					substanceunits = semsimmodel.getUnit(species.getSubstanceUnits());
				else 
					substanceunits = semsimmodel.getUnit("substance");
			}
			
			boolean hasonlysub = species.getHasOnlySubstanceUnits();

			UnitOfMeasurement unitforspecies = null;
			String compartmentname = species.getCompartment();

			UnitOfMeasurement compartmentunits = semsimmodel.getAssociatedDataStructure(compartmentname).getUnit();	
			
			// Deal with whether the species is expressed in substance units or not 
			if(hasonlysub && substanceunits!=null) unitforspecies = substanceunits;
			
			else if(substanceunits!=null && compartmentunits!=null){
				
				// Make unit for concentration of species
				String unitname = substanceunits.getName() + "_per_" + compartmentunits.getName();
				
				// If the substance/compartment unit was already added to the model, use it, otherwise create anew
				if(semsimmodel.containsUnit(unitname)) unitforspecies = semsimmodel.getUnit(unitname);
				
				else{
					unitforspecies = new UnitOfMeasurement(unitname);
					UnitFactor substancefactor = new UnitFactor(substanceunits, 1.0, null);
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
			
			String baseunitname = getSubstanceBaseUnits(substanceunits);
			
			// Assign OPB properties
			if(baseunitname.equals("dimensionless"))
				prop = new PhysicalPropertyinComposite(null,null);
			
			// Deal with amount/concentration units
			else if(baseunitname.equals("mole")){
				
				if(hasonlysub){
					// look up factor for unit substance in semsimmodel and determine OPB property from that.
					// but if substance not in model...(level 3) ...
					prop = new PhysicalPropertyinComposite("Chemical molar amount", SemSimLibrary.OPB_CHEMICAL_MOLAR_AMOUNT_URI);
				}
				else prop = new PhysicalPropertyinComposite("Chemical concentration", SemSimLibrary.OPB_CHEMICAL_CONCENTRATION_URI);
			}
			// Deal with particle units
			else if(baseunitname.equals("item")){
				
				if(hasonlysub)
					prop = new PhysicalPropertyinComposite("Particle count", SemSimLibrary.OPB_PARTICLE_COUNT_URI);
				
				else prop = new PhysicalPropertyinComposite("Particle concentration", SemSimLibrary.OPB_PARTICLE_CONCENTRATION_URI);
			}
			// Deal with mass/density units
			else if(baseunitname.equals("kilogram") || baseunitname.equals("gram")){
				
				if(hasonlysub)
					prop = new PhysicalPropertyinComposite("Mass of solid entity", SemSimLibrary.OPB_MASS_OF_SOLID_ENTITY_URI);
				
				else {
					double compartmentdims = sbmlmodel.getCompartment(compartmentname).getSpatialDimensions();
					
					if(compartmentdims==0.0){
						addErrorToModel("Compartment dimensions for species " + speciesid + " cannot be zero because species has mass units.");
						prop = new PhysicalPropertyinComposite(null,null);
					}
					
					else if(compartmentdims==1.0)
						prop = new PhysicalPropertyinComposite("Mass lineal density", SemSimLibrary.OPB_MASS_LINEAL_DENSITY_URI);
					
					else if(compartmentdims==2.0)
						prop = new PhysicalPropertyinComposite("Mass areal density", SemSimLibrary.OPB_MASS_AREAL_DENSITY_URI);
					
					else if(compartmentdims==3.0)
						prop = new PhysicalPropertyinComposite("Mass volumetric density", SemSimLibrary.OPB_MASS_VOLUMETRIC_DENSITY_URI);
					
				}
			}
			else prop = new PhysicalPropertyinComposite(null,null);

						
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
			semsimmodel.addAssociatePhysicalProperty(prop);
			ds.setAssociatedPhysicalProperty(prop);
			
			PhysicalEntity compartmentent = null;
			
			if(compartmentAndEntitiesMap.containsKey(species.getCompartment()))
				compartmentent = compartmentAndEntitiesMap.get(species.getCompartment());
			
			else System.err.println("WARNING: unknown compartment " + species.getCompartment() + " for species " + species.getId());
			
			ArrayList<PhysicalEntity> entlist = new ArrayList<PhysicalEntity>();
			ArrayList<StructuralRelation> rellist = new ArrayList<StructuralRelation>();
			rellist.add(StructuralRelation.PART_OF);


			PhysicalEntity speciesent = (PhysicalEntity) createPhysicalComponentForSBMLobject(species);
			entlist.add(speciesent);
			
			if(compartmentent instanceof CompositePhysicalEntity){
				entlist.addAll(((CompositePhysicalEntity) compartmentent).getArrayListOfEntities());
				rellist.addAll(((CompositePhysicalEntity) compartmentent).getArrayListOfStructuralRelations());
			}
			else entlist.add(compartmentent);
			
			
			CompositePhysicalEntity compositeent = semsimmodel.addCompositePhysicalEntity(entlist, rellist); // this also adds the singular physical entities to the model
			ds.setAssociatedPhysicalModelComponent(compositeent);
			speciesAndEntitiesMap.put(species.getId(), compositeent);
						
			collectSBaseData(species, compositeent);
		}
	}
	
	/**
	 *  Collect the SBML model's parameters
	 */
	private void collectParameters(){
		for(int p=0; p<sbmlmodel.getListOfParameters().size(); p++){
			Parameter sbmlpar = sbmlmodel.getParameter(p);

			DataStructure pards = addParameter(sbmlpar, null);
			parametersubmodel.addDataStructure(pards);
		}
	}
	
	/**
	 *  Collect the SBML model's rules
	 */
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
					mathmlstring = addLHStoMathML(mathmlstring, varname, sbmlrule.isRate());
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
			else{}  // don't do anything if the Rule is a non-Assignment rule
		}
	}
	
	/**
	 * Collect the SBML model's constraints
	 */
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
	
	/**
	 * Collect the SBML model's discrete events
	 */
	private void collectEvents(){
		
		try{
			for(int e=0; e<sbmlmodel.getListOfEvents().size(); e++){
				org.sbml.jsbml.Event sbmlevent = sbmlmodel.getEvent(e);
				
				org.sbml.jsbml.Trigger sbmltrigger = sbmlevent.getTrigger();
				String triggermathml = JSBML.writeMathMLToString(sbmltrigger.getMath());
				triggermathml = stripXMLheader(triggermathml);
				
				Event ssevent = new Event();
				ssevent.setName(sbmlevent.getId());
				ssevent.setMetadataID(sbmlevent.getMetaId());
				
				ssevent.setTriggerMathML(triggermathml);			
				
				// Process event assignments
				for(int a=0; a<sbmlevent.getListOfEventAssignments().size(); a++){
					org.sbml.jsbml.EventAssignment ea = sbmlevent.getEventAssignment(a);
					String varname = ea.getVariable();
					EventAssignment ssea = ssevent.new EventAssignment();
					
					String assignmentmathmlstring = JSBML.writeMathMLToString(ea.getMath());
					assignmentmathmlstring = stripXMLheader(assignmentmathmlstring);
					assignmentmathmlstring = addLHStoMathML(assignmentmathmlstring, varname, false);
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
				
				semsimmodel.addEvent(ssevent);
			}
		} catch(SBMLException | XMLStreamException e){
			e.printStackTrace();
		}
	}
	
	/**
	 *  Collect the SBML model's reaction data
	 */
	private void collectReactions(){
		
		// If there are no species defined in model or no reactions defined, return
		if(sbmlmodel.getNumSpecies()==0 || sbmlmodel.getNumReactions()==0) return;
		
		// We assume that SBML Kinetic Laws are defined in units of substance/time.
		// First add units to model
		UnitOfMeasurement subpertimeuom = new UnitOfMeasurement("substance_per_time");
				
		UnitFactor substancefactor = new UnitFactor(substanceunits, 1.0, null);
		UnitFactor timefactor = new UnitFactor(timeunits, -1.0, null);
		subpertimeuom.addUnitFactor(substancefactor);
		subpertimeuom.addUnitFactor(timefactor);
				
		semsimmodel.addUnit(subpertimeuom);
		
		// Assign OPB properties based on units
		String basesubstanceunitsname = getSubstanceBaseUnits(substanceunits);
		PhysicalPropertyinComposite prop = null;

		if(basesubstanceunitsname.equals("dimensionless"))
			prop = new PhysicalPropertyinComposite(null,null);
		
		else if(basesubstanceunitsname.equals("mole"))
			prop = new PhysicalPropertyinComposite("Chemical molar flow rate", SemSimLibrary.OPB_CHEMICAL_MOLAR_FLOW_RATE_URI);
		
		else if(basesubstanceunitsname.equals("item"))
			prop = new PhysicalPropertyinComposite("Particle flow rate", SemSimLibrary.OPB_PARTICLE_FLOW_RATE_URI);
		
		else if(basesubstanceunitsname.equals("kilogram") || basesubstanceunitsname.equals("gram"))
			prop = new PhysicalPropertyinComposite("Material flow rate", SemSimLibrary.OPB_MATERIAL_FLOW_RATE_URI);
		
		else prop = new PhysicalPropertyinComposite(null,null);
		
		semsimmodel.addAssociatePhysicalProperty(prop);
				
		// Iterate through reactions
		for(int r=0; r<sbmlmodel.getListOfReactions().size(); r++){
			Reaction reaction = sbmlmodel.getReaction(r);
			String reactionID = reaction.getId();
			
			DataStructure rateds = semsimmodel.addDataStructure(new Decimal(reactionID));
			
			rateds.setDeclared(true);
			String thereactionprefix = reactionprefix + reactionID;
			
			Submodel rxnsubmodel = new Submodel(thereactionprefix);
			semsimmodel.addSubmodel(rxnsubmodel);
			rxnsubmodel.addDataStructure(rateds);
			
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
						mathmlstring = addLHStoMathML(mathmlstring, reactionID, false);
									
						for(int l=0; l<kineticlaw.getListOfLocalParameters().size(); l++){
							LocalParameter lp = kineticlaw.getLocalParameter(l);
							DataStructure localds = addParameter(lp, thereactionprefix);
							mathmlstring = mathmlstring.replaceAll("<ci>\\s*" + lp.getId() + "\\s*</ci>", "<ci>" + localds.getName() + "</ci>");
							rxnsubmodel.addDataStructure(localds);
						}
			
						rateds.getComputation().setMathML(mathmlstring);
						rateds.getComputation().setComputationalCode(reactionID + " = " + reaction.getKineticLaw().getMath().toFormula());
					}
				} catch (SBMLException | XMLStreamException e) {
					e.printStackTrace();
				}
				
				rateds.setAssociatedPhysicalProperty(prop);
			}
			
			PhysicalProcess process = (PhysicalProcess) createPhysicalComponentForSBMLobject(reaction);
						
			// Set sources (reactants)
			for(int s=0; s<reaction.getNumReactants(); s++){
				String reactantname = reaction.getReactant(s).getSpecies();
				double stoich = reaction.getReactant(s).getStoichiometry();
				PhysicalEntity reactantent = speciesAndEntitiesMap.get(reactantname);
				process.addSource(reactantent, stoich);
									
				// Store info about species conservation for use in outputting species equations
				if(speciesAndConservation.containsKey(reactantname)){
					SpeciesConservation sc = speciesAndConservation.get(reactantname);
				
					if(sc.setWithConservationEquation) sc.consumedby.add(reactionID);
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
				
					if(sc.setWithConservationEquation) sc.producedby.add(reactionID);
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
	
	/**
	 * Create the conservation equations for the species in the model
	 */
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
			
			eqmathml = mathMLelementStart + makeLHSforStateVariable(speciesid);
			
			SpeciesConservation speccon = speciesAndConservation.get(speciesid);
			
			
			// If the species is set as a boundary condition, set RHS to zero
			if(sbmlspecies.getBoundaryCondition()==true || sbmlspecies.getConstant()==true){
				eqmathml = eqmathml + "  <cn>0</cn>\n </apply>\n" + mathMLelementEnd;
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
				
				for(String reactionid : speccon.producedby){
					Double stoich = semsimmodel.getCustomPhysicalProcessByName(reactionid).getSinkStoichiometry(speciesent);
					
					if(stoich==1){
						eqmathml = eqmathml + "\n" + ws +" <ci>"+ reactionid + "</ci>";
						eqstring = eqstring + " + " + reactionid;
					}
					else{
						eqmathml = eqmathml + "\n" + ws + " <apply>\n" + ws + "  <times/>\n" + ws + "  <cn>" + stoich + "</cn>\n" 
								+ ws + "  <ci>" + reactionid + "</ci>\n" + ws + " </apply>";
						eqstring = eqstring + " + (" + stoich + "*" + reactionid + ")";
	
					}
				}
				
				for(String reactionid : speccon.consumedby){
					Double stoich = semsimmodel.getCustomPhysicalProcessByName(reactionid).getSourceStoichiometry(speciesent);
					
					if(stoich==1){
						eqmathml = eqmathml + "\n" + ws + " <apply>\n" + ws + "  <times/>\n" + ws + "  <cn>-1</cn>\n" + ws 
								+ "  <ci>" + reactionid + "</ci>\n" + ws + " </apply>";					
						eqstring = eqstring + " - " + reactionid;
					}
					else{
						eqmathml = eqmathml + "\n" + ws + " <apply>\n" + ws + "  <times/>\n" + ws + "  <cn>-" + stoich + "</cn>\n" + ws 
								+ "  <ci>" + reactionid + "</ci>\n" + ws + " </apply>";	
						eqstring = eqstring + " - (" + stoich + "*" + reactionid + ")";
					}
				}
				
				String eqmathmlend = subunits ? "" : "   <ci>" + compartmentid + "</ci>\n  </apply>\n"; // if concentration units, include the divide operation closer
				eqmathml = eqmathml + "\n" + ws + "</apply>\n" + eqmathmlend + " </apply>\n" + mathMLelementEnd;  // end plus operation, end eq operation
				
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
			
			String prefix = null;
			
			// If we are looking at a reaction rate data structure, use reaction
			// prefix so we can ID local parameters as inputs
			if(sbmlmodel.getReaction(ds.getName())!=null)
				prefix = reactionprefix + ds.getName();
			
			SemSimUtil.setComputationInputsForDataStructure(semsimmodel, ds, prefix);
		}
	}
	
	/**
	 * Collect the SemSim annotations for data structures in the model.
	 * This excludes data structures corresponding to species and reactions in the model.
	 */
	private void collectSemSimRDF(){
		
		Document projdoc = getJDOMdocumentFromString(semsimmodel, modelaccessor.getLocalModelTextAsString());
		
		// Collect namespace b/c JSBML always seems to reutrn null for getNamespace() fxns in Model and SBMLDocument 
		Namespace sbmlmodelns = projdoc.getRootElement().getNamespace();
	
		Namespace semsimns = RDFNamespace.SEMSIM.createJdomNamespace();
		String rdfstring = null;
		
		//TODO: should change this so that it only reads in the semsim annotation into the JDOM document (saves time)
		
		if(projdoc.hasRootElement()){
			Element modelel = projdoc.getRootElement().getChild("model", sbmlmodelns);
				
			if(modelel != null){
				Element modelannel = modelel.getChild("annotation", sbmlmodelns);
						
				if(modelannel != null){	
					Element modelannssel = modelannel.getChild(SBMLwriter.semsimAnnotationElementName, semsimns);
								
					if(modelannssel != null){
						Element rdfel = modelannssel.getChild("RDF", RDFNamespace.RDF.createJdomNamespace());
						XMLOutputter xmloutputter = new XMLOutputter();
						rdfstring = xmloutputter.outputString(rdfel);
					}
				}
			}
		}
		
		rdfreader = new SemSimRDFreader(modelaccessor, semsimmodel, rdfstring, null);
		
		// Get the semsim namespace of the model, if present, according to the rdf block
		String modelnamespace = rdfreader.getModelRDFnamespace();
		
		if(modelnamespace == null )
			modelnamespace = semsimmodel.generateNamespaceFromDateAndTime();
		
		semsimmodel.setNamespace(modelnamespace);
	}
	
	
	/**
	 *  Collect all data common to an SBase object and copy it into a specified SemSimObject
	 * @param sbmlobject
	 * @param semsimobject
	 */
	private void collectSBaseData(SBase sbmlobject, SemSimObject semsimobject){
		
		addNotes(sbmlobject, semsimobject);
		addMetadataID(sbmlobject, semsimobject);
		
		// need to collect SBO terms here?
	}
	
	/**
	 *  Copy the notes attached to an SBML element and into the Description field of a SemSimObject 
	 * @param sbmlobject
	 * @param semsimobject
	 */
	private void addNotes(SBase sbmlobject, SemSimObject semsimobject){
				
		if(sbmlobject.isSetNotes()){
				
			try {
				semsimobject.setDescription(sbmlobject.getNotesString());
			} catch (XMLStreamException e) {
//				e.printStackTrace();  // commented out b/c JSBML keeps throwing exceptions in stack trace 
			}
		}
	}
	
	/**
	 * Copy the metadataID from an SBase object to a SemSimObject
	 * @param sbmlobject
	 * @param semsimobject
	 */
	private void addMetadataID(SBase sbmlobject, SemSimObject semsimobject){
		if(sbmlobject.getMetaId()!=null && ! sbmlobject.getMetaId().equals(""))
			semsimobject.setMetadataID(sbmlobject.getMetaId());
	}
	
	
	/**
	 * Collects all biological qualifier annotations for a given SBase object and 
	 * converts them into a set of ReferenceOntologyAnnotations. If more than one
	 * identity annotation is applied (BQBiol:is), as is common in SBML models, only the first
	 * annotation that uses a term from a SemSim preferred knowledge resource is collected.
	 * 
	 * @param sbmlobject
	 * @return The set of ReferenceOntologyAnnotations associated with the SBase object
	 */
	// Get biological qualifier annotations
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
				if(SemSimRelations.getBiologicalQualifierRelation(q)!= SemSimRelation.UNKNOWN){
					
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
										SemSimRelation.HAS_PHYSICAL_DEFINITION : SemSimRelations.getBiologicalQualifierRelation(q);
								
								// If we're looking at an identity relation...
								if(relation==SemSimRelation.HAS_PHYSICAL_DEFINITION){
									
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
	 * 
	 * @param sbmlobject
	 * @return The set of ReferenceOntologyAnnotations associated with the SBase object
	 */	private Set<ReferenceOntologyAnnotation> getModelQualifierAnnotations(SBase sbmlobject){
		
		Set<ReferenceOntologyAnnotation> anns = new HashSet<ReferenceOntologyAnnotation>();
		
		// If the CV term is used with a model qualifier
		for(int i=0; i<sbmlobject.getNumCVTerms();i++){
			CVTerm term = sbmlobject.getCVTerm(i);
			
			if(term.getQualifierType()==CVTerm.Type.MODEL_QUALIFIER){
				Qualifier q = term.getModelQualifierType();

				if(SemSimRelations.getModelQualifierRelation(q)!= SemSimRelation.UNKNOWN){
					
					for(int h=0; h<term.getNumResources(); h++){
						String uri = term.getResourceURI(h);
						Relation relation = (q==Qualifier.BQM_IS) ? SemSimRelation.HAS_PHYSICAL_DEFINITION : SemSimRelations.getModelQualifierRelation(q);
						anns.add(new ReferenceOntologyAnnotation(relation, URI.create(uri), uri, sslib));

					}
				}
			}
		}
		return anns;
	}
	
	// Assign a semsim physical entity object to an sbml model element
	private PhysicalModelComponent createPhysicalComponentForSBMLobject(SBase sbmlobject){
		
		String id = getIDforSBaseObject(sbmlobject);
		boolean isentity = isEntity(sbmlobject);
		
		PhysicalModelComponent pmc = isentity ? new CustomPhysicalEntity(id, "") : new CustomPhysicalProcess(id, "");
	
		Set<ReferenceOntologyAnnotation> tempanns = new HashSet<ReferenceOntologyAnnotation>();
		tempanns.addAll(getBiologicalQualifierAnnotations(sbmlobject));
		
		for(ReferenceOntologyAnnotation ann : getBiologicalQualifierAnnotations(sbmlobject)){
			
			// If there is a physical definition annotation, create reference physical component
			if(ann.getRelation().equals(SemSimRelation.HAS_PHYSICAL_DEFINITION)){
				// if entity, use reference term, but don't otherwise
				if(isentity)
					pmc = semsimmodel.addReferencePhysicalEntity(
							new ReferencePhysicalEntity(ann.getReferenceURI(), ann.getValueDescription())); 
				
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
	 * 
	 * @param sbmlel An SBase object
	 * @return Whether a given SBML element represents a physical entity
	 */
	private boolean isEntity(SBase sbmlel){
		
		return (sbmlel instanceof Compartment || sbmlel instanceof Species);
	}
	
	/**
	 * 
	 * @param sbmlobject
	 * @return The SBML ID for the object
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
	 * Add an SBML parameter to the model. This can also be used for SBML LocalParameters.
	 * @param p The SBML parameter to add to the SemSim model.
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
		String mathmlstring = mathMLelementStart + " <apply>\n  <eq />\n  <ci>" 
				+ ID + "</ci>\n  <cn>" + qwu.getValue() + "</cn>\n </apply>\n" + mathMLelementEnd;
		ds.getComputation().setMathML(mathmlstring);

		// Collect annotations
		if(rdfreader.hasPropertyAnnotationForDataStructure(ds))
			rdfreader.getDataStructureAnnotations(ds);
		
		collectSBaseData(qwu, ds);
		
		return ds;
	}
	
	
	/**
	 *  Get the base unit name for the model's "substance units"
	 * @param substanceunits The "substance" units in the SemSim model
	 * @return The name of the base unit for the "substance" unit in the SemSim model
	 */
	private String getSubstanceBaseUnits(UnitOfMeasurement substanceunits){
		String val = "mole";
		
		if(substanceunits!=null){
			if(substanceunits.getUnitFactors().size()==1){
				
				for(UnitFactor uf : substanceunits.getUnitFactors())
					val = uf.getBaseUnit().getName();
							
			}
		}
		return val;
	}
	
	/**
	 *  Select appropriate set of base units based on SBML level/version
	 */
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
	 * Add the left-hand side of a MathML equation
	 * @param mathmlstring The right-hand side of a MathML equation
	 * @param varname The name of the solved variable
	 * @param isODE Whether the variable is solved with an ODE
	 * @return The MathML equation containing both the left- and right-hand side
	 */
	private String addLHStoMathML(String mathmlstring, String varname, boolean isODE){
		String LHSstart = null;
		if(isODE) 
			LHSstart = makeLHSforStateVariable(varname);
		else LHSstart = " <apply>\n  <eq />\n  <ci>" + varname + "  </ci>\n";
		String LHSend = "</apply>\n";
		mathmlstring = mathmlstring.replace(mathMLelementStart, mathMLelementStart + LHSstart);
		mathmlstring = mathmlstring.replace(mathMLelementEnd, LHSend + mathMLelementEnd);
		return mathmlstring;
	}
	
	/**
	 * Create the MathML left-hand side for a variable that is solved using an ODE
	 * @param varname
	 * @return
	 */
	private String makeLHSforStateVariable(String varname){
		return " <apply>\n <eq/>\n  <apply>\n  <diff/>\n   <bvar>\n    <ci>" 
				+ timedomainname + "</ci>\n   </bvar>\n   <ci>" + varname + "</ci>\n  </apply>\n  ";
	}
	
	
	private class SpeciesConservation{
		public boolean setWithConservationEquation;
		public ArrayList<String> consumedby;
		public ArrayList<String> producedby;
		
		public SpeciesConservation(){
			setWithConservationEquation = true;
			consumedby = new ArrayList<String>();
			producedby = new ArrayList<String>();
		}
	}
}
