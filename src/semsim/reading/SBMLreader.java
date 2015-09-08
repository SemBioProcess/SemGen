package semsim.reading;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import org.sbml.libsbml.CVTerm;
import org.sbml.libsbml.Compartment;
import org.sbml.libsbml.CompartmentType;
import org.sbml.libsbml.Constraint;
import org.sbml.libsbml.Delay;
import org.sbml.libsbml.KineticLaw;
import org.sbml.libsbml.LocalParameter;
import org.sbml.libsbml.Model;
import org.sbml.libsbml.Parameter;
import org.sbml.libsbml.Priority;
import org.sbml.libsbml.Reaction;
import org.sbml.libsbml.Rule;
import org.sbml.libsbml.SBMLDocument;
import org.sbml.libsbml.SBMLReader;
import org.sbml.libsbml.SBase;
import org.sbml.libsbml.Species;
import org.sbml.libsbml.SpeciesType;
import org.sbml.libsbml.Unit;
import org.sbml.libsbml.UnitDefinition;
import org.sbml.libsbml.libsbml;
import org.semanticweb.owlapi.model.OWLException;

import semsim.SBMLconstants;
import semsim.SemSimConstants;
import semsim.SemSimLibrary;
import semsim.SemSimObject;
import semsim.annotation.Annotation;
import semsim.annotation.ReferenceOntologies;
import semsim.annotation.ReferenceOntologyAnnotation;
import semsim.annotation.SemSimRelation;
import semsim.annotation.StructuralRelation;
import semsim.annotation.CurationalMetadata.Metadata;
import semsim.annotation.ReferenceOntologies.OntologyDomain;
import semsim.annotation.ReferenceOntologies.ReferenceOntology;
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
import semsim.model.physical.object.ReferencePhysicalEntity;
import semsim.model.physical.object.ReferencePhysicalProcess;
import semsim.owl.SemSimOWLFactory;
import semsim.utilities.SemSimUtil;

public class SBMLreader extends ModelReader{

	public Model sbmlmodel;
	private Map<String, PhysicalEntity> compartmentAndSemSimEntitiesMap = new HashMap<String, PhysicalEntity>();
	private Map<String, CompositePhysicalEntity> speciesAndSemSimEntitiesMap = new HashMap<String, CompositePhysicalEntity>();
	private Map<String, SpeciesConservation> speciesAndConservation = new HashMap<String, SpeciesConservation>();  // associates species with the reactions they participate in
	public Hashtable<String, String[]> ontologycache = new  Hashtable<String, String[]>();
	public Set<String> baseUnits = new HashSet<String>();
	private Submodel parametersubmodel;
	private Submodel speciessubmodel;
	private Submodel compartmentsubmodel;
//	private Submodel reactionssubmodel;
	
	private static final String mathMLelementStart = "<math xmlns=\"http://www.w3.org/1998/Math/MathML\">\n";
	private static final String mathMLelementEnd = "</math>";
	private static final String timedomainname = "t";
	private UnitOfMeasurement timeunits;
	private UnitOfMeasurement substanceunits;
	
	
	public SBMLreader(File file) {
		super(file);
	}

	@Override
	public SemSimModel readFromFile() throws IOException, InterruptedException,
			OWLException {
		
		// Load the SBML file into a new SBML model
		SBMLDocument sbmldoc = new SBMLReader().readSBMLFromFile(srcfile.getAbsolutePath());
		
		if (sbmldoc.getNumErrors()>0){
		      System.err.println("Encountered the following SBML errors:");
		      sbmldoc.printErrors();
		      semsimmodel.addError("Source SBML model contained errors");
		      return semsimmodel;
		}
		else sbmlmodel = sbmldoc.getModel();

		// If model is SBML level 1, add error and return. This level not yet supported.
		if (sbmlmodel.getLevel()==1){
			addErrorToModel("SBML-to-SemSim conversion for SBML level 1 models not yet supported");
			return semsimmodel;
		}
		
		semsimmodel.setSemsimversion(SemSimLibrary.SEMSIM_VERSION);
		semsimmodel.setSourceFileLocation(srcfile.getAbsolutePath());
		
		// Collect function definitions. Not used in SBML level 1.
		// collectFunctionDefinitions();
		if (sbmlmodel.getListOfFunctionDefinitions().size()>0)
			addErrorToModel("SBML source model contains function definitions but these are not yet supported in SemSim.");

		//collectCompartmentTypes();  // We ignore compartment types for now. 
		// See http://sbml.org/Software/libSBML/5.11.4/docs/formatted/java-api/org/sbml/libsbml/CompartmentType.html

		//collectSpeciesTypes();  // Ignore these for now, too.
		// See http://sbml.org/Software/libSBML/5.11.4/docs/formatted/java-api/org/sbml/libsbml/SpeciesType.html
		
		
		// Set the t=0 value for a compartment, species or parameter. The symbol field refers to the ID of the SBML element.
		// If one of these elements already has an initial value stated in its construct, the initialAssignment overwrites it.
//		collectInitialAssignments();
		
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
		
//		if(sbmlmodel.getListOfReactions().size()>0)
//			reactionssubmodel = semsimmodel.addSubmodel(new Submodel("Reactions"));
		
		collectModelLevelData();
		setBaseUnits();
		collectUnits();
		setTimeDomain();
		collectCompartments();
		collectSpecies();
		collectParameters();
		collectRules();
		collectReactions();
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
		semsimmodel.setModelAnnotation(Metadata.fullname, sbmlmodel.getId());
		
		// Need to collect annotations here, too.		
	}
	
	/**
	 * Collect the SBML model's function definitions
	 */
//	private void collectFunctionDefinitions(){
//				
//		for(int f=0; f<sbmlmodel.getListOfFunctionDefinitions().size(); f++){
////			FunctionDefinition fd = sbmlmodel.getFunctionDefinition(f);
//			//... not sure how to deal with SBML functions yet. Use functional submodels?
//		}
//	}
	
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
				String unitfactorname = libsbml.UnitKind_toString(sbmlunit.getKind());

				UnitOfMeasurement baseunit = null;
				
				// If the base unit for the unit factor was already added to model, retrieve it. Otherwise create anew.
				
				if(semsimmodel.containsUnit(unitfactorname)) baseunit = semsimmodel.getUnit(unitfactorname);
				
				else if(! unitfactorname.equals("dimensionless")){  // don't add factor if it's dimensionless
					baseunit = new UnitOfMeasurement(unitfactorname);
					baseunit.setFundamental(baseUnits.contains(unitfactorname));
					collectSBaseData(sbmlunit, baseunit);
					semsimmodel.addUnit(baseunit);
				}
				else continue;

				
				UnitFactor unitfactor = new UnitFactor(baseunit, sbmlunit.getExponentAsDouble(), null);
				
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
		
		addSBMLlevel2reservedUnits();
	}
	
	// For SBML level 2, add the reserved units, if not already stated in listOfUnitDefinitions
	private void addSBMLlevel2reservedUnits() {
		if(sbmlmodel.getLevel()==2){
			
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
	}
	
	private void setTimeDomain(){
		// Create a data structure that represents the temporal solution domain
		DataStructure timeds = new Decimal(timedomainname);
		timeds.setDescription("Temporal solution domain");
		timeds.setIsSolutionDomain(true);
		
		
		if(sbmlmodel.getLevel()==3.0 && sbmlmodel.isSetTimeUnits()){
			timeunits = new UnitOfMeasurement(sbmlmodel.getTimeUnits());
			semsimmodel.addUnit(timeunits);
		}
		else if(semsimmodel.containsUnit("time")){
			timeunits = semsimmodel.getUnit("time");
		}
		else{}
				
		timeds.setUnit(timeunits);
		
		PhysicalProperty timeprop = new PhysicalProperty("Time", URI.create(SemSimConstants.OPB_NAMESPACE + "OPB_01023"));
		semsimmodel.addPhysicalProperty(timeprop);
		timeds.setSingularAnnotation(timeprop);
		
		semsimmodel.addDataStructure(timeds);
		speciessubmodel.addDataStructure(timeds);		
	}

	/**
	 *  Collect the SBML model's compartment data
	 */
	private void collectCompartments(){
		
		for(int c=0; c<sbmlmodel.getListOfCompartments().size(); c++){
			Compartment sbmlc = sbmlmodel.getCompartment(c);
			String compid = sbmlc.getId();
			
			// What to do with name?
			// Constant?
			// Outside?
			// spatial dimensions
			// type code
			// units 
			// volume
			
			DataStructure ds = semsimmodel.addDataStructure(new Decimal(compid));
			compartmentsubmodel.addDataStructure(ds);
			
			String mathml = mathMLelementStart + " <apply>\n  <eq />\n  <ci>" + compid + "</ci>\n  <cn>" 
					+ sbmlc.getSize() + "</cn>\n </apply>\n" + mathMLelementEnd;
			ds.getComputation().setMathML(mathml);
			ds.getComputation().setComputationalCode(compid + " = " + Double.toString(sbmlc.getSize()));
			
			String defaultunits = null;
			PhysicalPropertyinComposite prop = null;
						
			// Add physical property here
			if(sbmlc.getSpatialDimensionsAsDouble()==3.0){
				prop = new PhysicalPropertyinComposite("", URI.create(SemSimConstants.OPB_NAMESPACE + "OPB_00154"));
				defaultunits = "volume";
			}
			else if(sbmlc.getSpatialDimensionsAsDouble()==2.0){
				prop = new PhysicalPropertyinComposite("", URI.create(SemSimConstants.OPB_NAMESPACE + "OPB_00295"));
				defaultunits = "area";
			}
			else if(sbmlc.getSpatialDimensionsAsDouble()==1.0){
				prop = new PhysicalPropertyinComposite("", URI.create(SemSimConstants.OPB_NAMESPACE + "OPB_01064"));
				defaultunits = "length";
			}
			else{}  // what to do if a point?
			
			// Set the units for the compartment
			if(sbmlc.isSetUnits()) 
				ds.setUnit(semsimmodel.getUnit(sbmlc.getUnits()));
			else ds.setUnit(semsimmodel.getUnit(defaultunits));
			
			ds.setAssociatedPhysicalProperty(prop);
			
			// Set the physical entity for the compartment
			PhysicalEntity compartmentent = (PhysicalEntity) createPhysicalComponentForSBMLobject(sbmlc);
			compartmentAndSemSimEntitiesMap.put(compid, compartmentent);
						
			ArrayList<PhysicalEntity> entlist = new ArrayList<PhysicalEntity>();
			entlist.add(compartmentent);
			ArrayList<StructuralRelation> rellist = new ArrayList<StructuralRelation>();
			
			CompositePhysicalEntity compositeent = new CompositePhysicalEntity(entlist, rellist);
			compositeent = semsimmodel.addCompositePhysicalEntity(compositeent); // this also adds the singular physical entities to the model
			ds.setAssociatedPhysicalModelComponent(compositeent);

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
			speciessubmodel.addDataStructure(ds);
			
			speciesAndConservation.put(speciesid, new SpeciesConservation());
					
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
				if(species.isSetSubstanceUnits()){
					substanceunits = semsimmodel.getUnit(species.getSubstanceUnits());
				}
				else{
					if(sbmlmodel.isSetSubstanceUnits()){
						substanceunits = semsimmodel.getUnit(sbmlmodel.getSubstanceUnits());
					}
				}
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
			
			// Deal with whether the species is expressed in substance units or not 
			if(hasonlysub) unitforspecies = substanceunits;
			else{
				// Make unit for concentration of species
				UnitOfMeasurement compartmentunits = semsimmodel.getAssociatedDataStructure(compartmentname).getUnit();				
				
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
			
			ds.setUnit(unitforspecies);
			
			
			// The OPB properties assigned here need to account for the different possible units for 
			// substance units: 'dimensionless', 'mole', 'item', kilogram','gram', etc. as above.
			// Will need base unit breakdown to assign appropriate OPB terms. Using the follow if-else
			// statements in the meantime. Currently assuming that if hasOnlySubstanceUnits is false, 
			// that the same OPB term can be used regardless of the compartment dimensionality.
			
			String baseunitname = getSubstanceBaseUnits(substanceunits);
			
			// Assign OPB properties
			if(baseunitname.equals("dimensionless")){
				prop = new PhysicalPropertyinComposite(null,null);
			}
			
			// Deal with amount/concentration units
			else if(baseunitname.equals("mole")){
				
				if(hasonlysub){
					// look up factor for unit substance in semsimmodel and determine OPB property from that.
					// but if substance not in model...(level 3) ...
					prop = new PhysicalPropertyinComposite("Chemical molar amount", URI.create(SemSimConstants.OPB_NAMESPACE + "OPB_00425"));
				}
				else prop = new PhysicalPropertyinComposite("Chemical concentration", URI.create(SemSimConstants.OPB_NAMESPACE + "OPB_00340"));
			}
			// Deal with particle units
			else if(baseunitname.equals("item")){
				
				if(hasonlysub)
					prop = new PhysicalPropertyinComposite("Particle count", URI.create(SemSimConstants.OPB_NAMESPACE + "OPB_01001"));
				
				else prop = new PhysicalPropertyinComposite("Particle concentration", URI.create(SemSimConstants.OPB_NAMESPACE + "OPB_01000"));
			}
			// Deal with mass/density units
			else if(baseunitname.equals("kilogram") || baseunitname.equals("gram")){
				
				if(hasonlysub)
					prop = new PhysicalPropertyinComposite("Mass of solid entity", URI.create(SemSimConstants.OPB_NAMESPACE + "OPB_01226"));
				
				else {
					double compartmentdims = sbmlmodel.getCompartment(compartmentname).getSpatialDimensionsAsDouble();
					
					if(compartmentdims==0.0){
						addErrorToModel("Compartment dimensions for species " + speciesid + " cannot be zero because species has mass units.");
						prop = new PhysicalPropertyinComposite(null,null);
					}
					
					else if(compartmentdims==1.0)
						prop = new PhysicalPropertyinComposite("Mass lineal density", URI.create(SemSimConstants.OPB_NAMESPACE + "OPB_00190"));
					
					else if(compartmentdims==2.0)
						prop = new PhysicalPropertyinComposite("Mass areal density", URI.create(SemSimConstants.OPB_NAMESPACE + "OPB_00258"));
					
					else if(compartmentdims==3.0)
						prop = new PhysicalPropertyinComposite("Mass volumetric density", URI.create(SemSimConstants.OPB_NAMESPACE + "OPB_00101"));
					
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
			
			if(compartmentAndSemSimEntitiesMap.containsKey(species.getCompartment()))
				compartmentent = compartmentAndSemSimEntitiesMap.get(species.getCompartment());
			else
				System.err.println("WARNING: unknown compartment " + species.getCompartment() + " for species " + species.getId());
			
			
			ArrayList<PhysicalEntity> entlist = new ArrayList<PhysicalEntity>();
			PhysicalEntity speciesent = (PhysicalEntity) createPhysicalComponentForSBMLobject(species);
			entlist.add(speciesent);
			entlist.add(compartmentent);
			ArrayList<StructuralRelation> rellist = new ArrayList<StructuralRelation>();
			rellist.add(SemSimConstants.PART_OF_RELATION);
			
			CompositePhysicalEntity compositeent = new CompositePhysicalEntity(entlist, rellist);
						
			compositeent = semsimmodel.addCompositePhysicalEntity(compositeent); // this also adds the singular physical entities to the model
			ds.setAssociatedPhysicalModelComponent(compositeent);
			speciesAndSemSimEntitiesMap.put(species.getId(), compositeent);
						
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
	 * Collect the SBML mode's initial assignments.
	 * These are expressions that can be used set the t=0 values of compartments, species and parameters.
	 * They override any initial values asserted in the declaration of these SBML components. 
	 */
//	private void collectInitialAssignments(){
//		for(int i=0; i<sbmlmodel.getListOfInitialAssignments().size(); i++){
//			InitialAssignment ia = sbmlmodel.getInitialAssignment(i);
//			String symbol = ia.getSymbol();
//			DataStructure ds = semsimmodel.getAssociatedDataStructure(symbol);;
//			//ds.setStartValue(val);
//			//...
//		}
//	}
	
	/**
	 *  Collect the SBML model's rules
	 */
	private void collectRules(){
		
		for(int r=0; r<sbmlmodel.getListOfRules().size(); r++){
			Rule sbmlrule = sbmlmodel.getRule(r);
			String varname = sbmlrule.getVariable();
//			libsbmlConstants.RULE_TYPE_RATE;
//			sbmlrule.getVariable()
			
			// assignmentrule for setting value of variable to some formula output
			// raterule for ODEs
			// algebraic for all other types????
			
			DataStructure ds = null;
			if(semsimmodel.containsDataStructure(varname)) 
				ds = semsimmodel.getAssociatedDataStructure(varname);
			else {
				ds = new Decimal(varname); 
				semsimmodel.addDataStructure(ds);
			}
			
			ds.getComputation().setComputationalCode(varname + " = " + sbmlrule.getFormula());
			String mathmlstring = libsbml.writeMathMLToString(sbmlrule.getMath());
			mathmlstring = stripXMLheader(mathmlstring);
			mathmlstring = addLHStoMathML(mathmlstring, varname);
			ds.getComputation().setMathML(mathmlstring);
			
			collectSBaseData(sbmlrule, ds.getComputation());
		}
	}
	
	/**
	 * Collect the SBML model's constraints
	 */
	private void collectConstraints(){
		
		for(int c=0; c<sbmlmodel.getListOfConstraints().size(); c++){
			Constraint cons = sbmlmodel.getConstraint(c);
			String mathml = libsbml.writeMathMLToString(cons.getMath());
			mathml = stripXMLheader(mathml);
			RelationalConstraint rc = new RelationalConstraint("", mathml, cons.getMessageString());
			semsimmodel.addRelationalConstraint(rc);
		}
	}
	
	/**
	 * Collect the SBML model's discrete events
	 */
	private void collectEvents(){
		
		for(int e=0; e<sbmlmodel.getListOfEvents().size(); e++){
			org.sbml.libsbml.Event sbmlevent = sbmlmodel.getEvent(e);
			
			org.sbml.libsbml.Trigger sbmltrigger = sbmlevent.getTrigger();
			String triggermathml = libsbml.writeMathMLToString(sbmltrigger.getMath());
			triggermathml = stripXMLheader(triggermathml);
			
			Event ssevent = new Event();
			ssevent.setName(sbmlevent.getId());
			
			ssevent.setTriggerMathML(triggermathml);			
			
			// Process event assignments
			for(int a=0; a<sbmlevent.getListOfEventAssignments().size(); a++){
				org.sbml.libsbml.EventAssignment ea = sbmlevent.getEventAssignment(a);
				String varname = ea.getVariable();
				EventAssignment ssea = ssevent.new EventAssignment();
				
				String assignmentmathmlstring = libsbml.writeMathMLToString(ea.getMath());
				assignmentmathmlstring = stripXMLheader(assignmentmathmlstring);
				assignmentmathmlstring = addLHStoMathML(assignmentmathmlstring, varname);
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
				String delaymathml = libsbml.writeMathMLToString(delay.getMath());
				delaymathml = stripXMLheader(delaymathml);
				ssevent.setDelayMathML(delaymathml);
			}
			
			// Collect priority (SBML level 3)
			if(sbmlmodel.getLevel()==3 && sbmlevent.isSetPriority()){
				Priority priority = sbmlevent.getPriority();
				String prioritymathml = libsbml.writeMathMLToString(priority.getMath());
				prioritymathml = stripXMLheader(prioritymathml);
				ssevent.setPriorityMathML(prioritymathml);
			}
			
			// Set the time units (SBML level 2 version 2 or version 1)
			if(sbmlmodel.getLevel()==3 && sbmlmodel.getVersion()<3 && sbmlevent.isSetTimeUnits()){
				String timeunitsname = sbmlevent.getTimeUnits();
				ssevent.setTimeUnit(semsimmodel.getUnit(timeunitsname));
			}
			
			semsimmodel.addEvent(ssevent);
		}
	}
	
	/**
	 *  Collect the SBML model's reaction data
	 */
	private void collectReactions(){
		
		// We assume that SBML Kinetic Laws are defined in units of substance/time.
		// Add units to model
		UnitOfMeasurement subpertimeuom = new UnitOfMeasurement("substance_per_time");
				
		UnitFactor substancefactor = new UnitFactor(substanceunits, 1.0, null);
		UnitFactor timefactor = new UnitFactor(timeunits, -1.0, null);
		subpertimeuom.addUnitFactor(substancefactor);
		subpertimeuom.addUnitFactor(timefactor);	
		
		semsimmodel.addUnit(subpertimeuom);
		
		for(int r=0; r<sbmlmodel.getListOfReactions().size(); r++){
			Reaction reaction = sbmlmodel.getReaction(r);
			String reactionID = reaction.getId();
			
			DataStructure ds = semsimmodel.addDataStructure(new Decimal(reactionID));
			Submodel rxnsubmodel = new Submodel(reactionID);
			semsimmodel.addSubmodel(rxnsubmodel);
			//reactionssubmodel.addSubmodel(rxnsubmodel);
			rxnsubmodel.addDataStructure(ds);
			
			KineticLaw kineticlaw = reaction.getKineticLaw();
			
			ds.setUnit(subpertimeuom);
			
			// Deal with kinetic law
			String mathmlstring = libsbml.writeMathMLToString(kineticlaw.getMath());
			
			// For some reason the mathml string output for kinetic laws has <?xml version="1.0"...> at the head. Strip it.
			mathmlstring = stripXMLheader(mathmlstring);
			mathmlstring = addLHStoMathML(mathmlstring, reactionID);
						
			for(int l=0; l<kineticlaw.getListOfLocalParameters().size(); l++){
				LocalParameter lp = kineticlaw.getLocalParameter(l);
				DataStructure localds = addParameter(lp, reaction.getId());
				mathmlstring = mathmlstring.replaceAll("<ci>\\s*" + lp.getId() + "\\s*</ci>", "<ci>" + localds.getName() + "</ci>");
				rxnsubmodel.addDataStructure(localds);
			}
				
			// This might be unnecessary for some more recent versions of SBML models (listOfParameters might have been deprecated)
			for(int p=0; p<kineticlaw.getListOfParameters().size(); p++){
				Parameter par = kineticlaw.getParameter(p);
				DataStructure localds = addParameter(par, reaction.getId());
				mathmlstring = mathmlstring.replaceAll("<ci>\\s*" + par.getId() + "\\s*</ci>", "<ci>" + localds.getName() + "</ci>");
				rxnsubmodel.addDataStructure(localds);
			}

			ds.getComputation().setMathML(mathmlstring);
			ds.getComputation().setComputationalCode(reactionID + " = " + reaction.getKineticLaw().getFormula());
		
			
			PhysicalPropertyinComposite prop = null;
			String baseunitname = getSubstanceBaseUnits(substanceunits);
			
			// Assign OPB properties
			if(baseunitname.equals("dimensionless")){
				prop = new PhysicalPropertyinComposite(null,null);
			}
			else if(baseunitname.equals("mole")){
				prop = new PhysicalPropertyinComposite("Chemical molar flow rate", URI.create(SemSimConstants.OPB_NAMESPACE + "OPB_00592"));
			}
			else if(baseunitname.equals("item")){
				prop = new PhysicalPropertyinComposite("Particle flow rate", URI.create(SemSimConstants.OPB_NAMESPACE + "OPB_00544"));
			}
			else if(baseunitname.equals("kilogram") || baseunitname.equals("gram")){
				prop = new PhysicalPropertyinComposite("Material flow rate", URI.create(SemSimConstants.OPB_NAMESPACE + "OPB_01220"));
			}
			else{
				prop = new PhysicalPropertyinComposite(null,null);
			}
						
						
			ds.setAssociatedPhysicalProperty(prop);
			
			PhysicalProcess process = (PhysicalProcess) createPhysicalComponentForSBMLobject(reaction);
						
			// Set sources (reactants)
			for(int s=0; s<reaction.getNumReactants(); s++){
				String reactantname = reaction.getReactant(s).getSpecies();
				double stoich = reaction.getReactant(s).getStoichiometry();
				PhysicalEntity reactantent = speciesAndSemSimEntitiesMap.get(reactantname);
				process.addSource(reactantent, stoich);
									
				// Store info about species conservation for use in outputting species equations
				speciesAndConservation.get(reactantname).consumedby.add(reactionID);
			}
			
			// Set sinks (products)
			for(int p=0; p<reaction.getNumProducts(); p++){
				String productname = reaction.getProduct(p).getSpecies();
				double stoich = reaction.getProduct(p).getStoichiometry();
				PhysicalEntity productent = speciesAndSemSimEntitiesMap.get(productname);
				process.addSink(productent, stoich);
				
				// Store info about species conservation for use in outputting species equations
				speciesAndConservation.get(productname).producedby.add(reactionID);
			}
			
			// Set mediators (modifiers)
			for(int m=0; m<reaction.getNumModifiers(); m++){
				String mediatorname = reaction.getModifier(m).getSpecies();
				PhysicalEntity mediatorent = speciesAndSemSimEntitiesMap.get(mediatorname);
				process.addMediator(mediatorent);
			}
			
			ds.setAssociatedPhysicalModelComponent(process);
						
			// Add process to model
			if(process instanceof ReferencePhysicalProcess) 
				semsimmodel.addReferencePhysicalProcess((ReferencePhysicalProcess) process);
			else 
				semsimmodel.addCustomPhysicalProcess((CustomPhysicalProcess) process);
			
			collectSBaseData(reaction, process);
		}
		
		setSpeciesConservationEquations();
	}
	
	/**
	 * Create the conservation equations for the species in the model
	 */
	private void setSpeciesConservationEquations(){
		
		for(String speciesid : speciesAndConservation.keySet()){
	      
			// The attribute hasOnlySubstanceUnits takes on a boolean value. 
			// In SBML Level 3, the attribute has no default value and must always
			// be set in a model; in SBML Level 2, it has a default value of false.
			Species sbmlspecies = sbmlmodel.getSpecies(speciesid);
				
			boolean subunits = false;

			if(sbmlspecies.isSetHasOnlySubstanceUnits()){
				subunits = sbmlspecies.getHasOnlySubstanceUnits();
			}
			else if(sbmlmodel.getVersion()==3.0){
				addErrorToModel("Required SBML level 3.0 attribute 'hasOnlySubstanceUnits' is unspecified for species " + speciesid + ".");
				return;
			}
			
			String compartmentid = sbmlmodel.getSpecies(speciesid).getCompartment();
			
			String eqstring = "";
			String eqmathml = "";
			String ws = subunits ? "  " : "   ";
			
			String LHS = "d(" + speciesid + ")/d(" + timedomainname + ")";
			
			eqmathml = mathMLelementStart + " <apply>\n <eq/>\n  <apply>\n  <diff/>\n   <bvar>\n    <ci>" 
						+ timedomainname + "</ci>\n   </bvar>\n   <ci>" + speciesid + "</ci>\n  </apply>\n  ";
			
			// If the species is set as a boundary condition, set RHS to zero
			if(sbmlspecies.getBoundaryCondition()==true || sbmlspecies.getConstant()==true){
				eqmathml = eqmathml + "  <cn>0</cn>\n </apply>\n" + mathMLelementEnd;
				eqstring = "0";
			}
			
			// Otherwise create the RHS of the ODE
			else{
				String RHSstart = subunits ? "" : "  <divide/>\n   <apply>\n";
				eqmathml = eqmathml + "  <apply>\n" + RHSstart + ws + "<plus/>";
			

				// When a Species is to be treated in terms of concentrations or density, the units of the 
				// spatial size portion of the concentration value (i.e., the denominator in the units formula substance/ size)
				// are those indicated by the value of the 'units' attribute on the compartment in which the species is located.
				
				PhysicalEntity speciesent = speciesAndSemSimEntitiesMap.get(speciesid);
				
				for(String reactionid : speciesAndConservation.get(speciesid).producedby){
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
				
				for(String reactionid : speciesAndConservation.get(speciesid).consumedby){
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
			}
			
			// Store the equations
			if(eqstring.length()>0){
				
				// Strip first + or - operator if present, add compartment divisor if needed to computational code
				if(eqstring.trim().startsWith("+") || eqstring.trim().startsWith("-")){
					eqstring = eqstring.substring(3, eqstring.length()); 
					eqstring = subunits ? eqstring : "(" + eqstring + ")/" + compartmentid; // add compartment divisor if species in conc. units
				}
				
				eqstring = LHS + " = " + eqstring; // add LHS to computational code string
				
				DataStructure speciesds = semsimmodel.getAssociatedDataStructure(speciesid);
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
			
			if(sbmlmodel.getReaction(ds.getName())!=null)
				prefix = ds.getName();
			
			SemSimUtil.setComputationInputsForDataStructure(semsimmodel, ds, prefix);
		}
	}
	
	
	/**
	 *  Collect all data common to an SBase object and copy it into a specified SemSimObject
	 * @param sbmlobject
	 * @param semsimobject
	 */
	private void collectSBaseData(SBase sbmlobject, SemSimObject semsimobject){
		
		addNotes(sbmlobject, semsimobject);
		addMetadataID(sbmlobject, semsimobject);
		
		//if(semsimobject instanceof Annotatable) addAnnotations(sbmlobject, (Annotatable)semsimobject);
		// need to collect SBO terms here?
	}
	
	/**
	 *  Copy the notes attached to an SBML element and into the Description field of a SemSimObject 
	 * @param sbmlobject
	 * @param semsimobject
	 */
	private void addNotes(SBase sbmlobject, SemSimObject semsimobject){
		if(sbmlobject.getNotesString()!=null && ! sbmlobject.getNotesString().equals(""))
			semsimobject.setDescription(sbmlobject.getNotesString());
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
	
	// Copy annotations from SBML model elements to SemSim objects
//	private void addAnnotations(SBase sbmlobject, Annotatable semsimobject){
//		
//		Set<ReferenceOntologyAnnotation> allanns = new HashSet<ReferenceOntologyAnnotation>();
//		allanns.addAll(getBiologicalQualifierAnnotations(sbmlobject));
//		allanns.addAll(getModelQualifierAnnotations(sbmlobject));
//		
//		for(ReferenceOntologyAnnotation ann : allanns) semsimobject.addAnnotation(ann);
//	}
	
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
			if(term.getQualifierType()==1){
				Integer t = Integer.valueOf(term.getBiologicalQualifierType());
				
				// If we know the relation
				if(SemSimConstants.BIOLOGICAL_QUALIFIER_TYPES_AND_RELATIONS.containsKey(t)){
					
					int numidentityanns = 0;
					
					for(int j=0; j<term.getNumResources(); j++){
						String uristring = term.getResourceURI(j);
						String namespace = SemSimOWLFactory.getNamespaceFromIRI(uristring);
						
						// If we can look up the knowledge resource given the namespace of the CV term
						if(ReferenceOntologies.getReferenceOntologybyNamespace(namespace)!=null){
							ReferenceOntology refont = ReferenceOntologies.getReferenceOntologybyNamespace(namespace);
							
							// If the knowledge resource is part of the limited set used for SemSim annotation 
							if(ontdomain.domainhasReferenceOntology(refont)){
								SemSimRelation relation = (t==0) ? 
										SemSimConstants.REFERS_TO_RELATION : SemSimConstants.BIOLOGICAL_QUALIFIER_TYPES_AND_RELATIONS.get(t);
								
								// If we're looking at an identity relation...
								if(relation==SemSimConstants.REFERS_TO_RELATION){
									
									// And we haven't added one yet, add it
									if(numidentityanns==0){
										anns.add(new ReferenceOntologyAnnotation(relation, URI.create(uristring), uristring));
										numidentityanns++;
									}
									// Otherwise skip the identity annotation
									else System.err.println("WARNING: Multiple reference annotations for " + 
												getIDforSBaseObject(sbmlobject) + ". Ignoring annotation against " + uristring);
								}
								// Otherwise add the non-identity annotation
								else anns.add(new ReferenceOntologyAnnotation(relation, URI.create(uristring), uristring));
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
			
			if(term.getQualifierType()==0){
				Integer t = Integer.valueOf(term.getModelQualifierType());
				
				if(SemSimConstants.MODEL_QUALIFIER_TYPES_AND_RELATIONS.containsKey(t)){
					
					for(int h=0; h<term.getNumResources(); h++){
						String uri = term.getResourceURI(h);
						SemSimRelation relation = (t==0) ? SemSimConstants.REFERS_TO_RELATION : SemSimConstants.MODEL_QUALIFIER_TYPES_AND_RELATIONS.get(t);
						anns.add(new ReferenceOntologyAnnotation(relation, URI.create(uri), uri));
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
			if(ann.getRelation().equals(SemSimConstants.REFERS_TO_RELATION)){
				// if entity, use reference term, but don't otherwise
				pmc = isentity ? new ReferencePhysicalEntity(ann.getReferenceURI(), ann.getValueDescription()) : pmc; 
				tempanns.remove(ann);
				break;
			}
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
	 * @param sbmlel
	 * @return Whether a given SBML element represents a physical entity
	 */
	private boolean isEntity(SBase sbmlel){
		
		return (sbmlel instanceof Compartment || sbmlel instanceof CompartmentType
			|| sbmlel instanceof Species || sbmlel instanceof SpeciesType);
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
	private DataStructure addParameter(Parameter p, String prefix){

		String ID = (prefix==null || prefix.equals("")) ? p.getId() : prefix + "." + p.getId();
		
		if(semsimmodel.containsDataStructure(ID)){
			addErrorToModel("Multiple data structures with name " + ID);
			return null;
		}
		
		DataStructure ds = semsimmodel.addDataStructure(new Decimal(ID));
		
		UnitOfMeasurement unitforpar = semsimmodel.getUnit(p.getUnits());
		ds.setUnit(unitforpar);
		
		ds.getComputation().setComputationalCode(ID + " = " + Double.toString(p.getValue()));
		String mathmlstring = mathMLelementStart + " <apply>\n  <eq />\n  <ci>" 
				+ ID + "</ci>\n  <cn>" + p.getValue() + "</cn>\n </apply>\n" + mathMLelementEnd;
		ds.getComputation().setMathML(mathmlstring);
		
		// Annotations, too?
		collectSBaseData(p, ds);
		
		return ds;
	}
	
	
	// Get the base unit name for the model's "substance units"
	private String getSubstanceBaseUnits(UnitOfMeasurement substanceunits){
		String val = "mole";
		
		if(substanceunits.getUnitFactors().size()==1){
			
			for(UnitFactor uf : substanceunits.getUnitFactors())
				val = uf.getBaseUnit().getName();
						
		}
		return val;
	}
	
	// Select appropriate set of base units by SBML level and version number
	private void setBaseUnits(){
		baseUnits.clear();
		
		if( sbmlmodel.getLevel()==3) baseUnits.addAll(SBMLconstants.SBML_LEVEL_3_BASE_UNITS);
		else if( sbmlmodel.getLevel()==2 && sbmlmodel.getVersion()==4) baseUnits.addAll(SBMLconstants.SBML_LEVEL_2_VERSION_4_BASE_UNITS);
		else if( sbmlmodel.getLevel()==2 && sbmlmodel.getVersion()==1) baseUnits.addAll(SBMLconstants.SBML_LEVEL_2_VERSION_1_BASE_UNITS);
	}
	
	private void addErrorToModel(String description){
		String errmsg = "SBML-TO-SEMSIM CONVERSION ERROR:\n" + description;
		System.err.println(errmsg);
		semsimmodel.addError(errmsg);
	}
	
	private String stripXMLheader(String mathmlstring){
		return mathmlstring.replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n", "");
	}
	
	private String addLHStoMathML(String mathmlstring, String varname){
		String LHSstart = "<apply>\n <eq />\n <ci>" + varname + " </ci>\n";
		String LHSend = "</apply>\n";
		mathmlstring = mathmlstring.replace(mathMLelementStart, mathMLelementStart + LHSstart);
		mathmlstring = mathmlstring.replace(mathMLelementEnd, LHSend + mathMLelementEnd);
		return mathmlstring;
	}
	
	
	private class SpeciesConservation{
		public ArrayList<String> consumedby;
		public ArrayList<String> producedby;
		
		public SpeciesConservation(){
			consumedby = new ArrayList<String>();
			producedby = new ArrayList<String>();
		}
	}
}
