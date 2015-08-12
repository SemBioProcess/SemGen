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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.sbml.libsbml.CVTerm;
import org.sbml.libsbml.Compartment;
import org.sbml.libsbml.CompartmentType;
import org.sbml.libsbml.Constraint;
import org.sbml.libsbml.Delay;
import org.sbml.libsbml.FunctionDefinition;
import org.sbml.libsbml.InitialAssignment;
import org.sbml.libsbml.KineticLaw;
import org.sbml.libsbml.ListOfFunctionDefinitions;
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
import org.sbml.libsbml.Trigger;
import org.sbml.libsbml.Unit;
import org.sbml.libsbml.UnitDefinition;
import org.sbml.libsbml.libsbml;
import org.sbml.libsbml.libsbmlConstants;
import org.semanticweb.owlapi.model.OWLException;

import semsim.SBMLconstants;
import semsim.SemSimConstants;
import semsim.SemSimLibrary;
import semsim.SemSimObject;
import semsim.annotation.Annotatable;
import semsim.annotation.Annotation;
import semsim.annotation.ReferenceOntologyAnnotation;
import semsim.annotation.SemSimRelation;
import semsim.annotation.StructuralRelation;
import semsim.annotation.CurationalMetadata.Metadata;
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
import semsim.model.physical.object.PhysicalPropertyinComposite;
import semsim.model.physical.object.ReferencePhysicalEntity;
import semsim.model.physical.object.ReferencePhysicalProcess;
import semsim.owl.SemSimOWLFactory;
import semsim.utilities.ReferenceOntologies;
import semsim.utilities.ReferenceOntologies.OntologyDomain;
import semsim.utilities.ReferenceOntologies.ReferenceOntology;

public class SBMLreader extends ModelReader{

	public Model sbmlmodel;
	private Map<String, PhysicalEntity> compartmentAndSemSimEntitiesMap = new HashMap<String, PhysicalEntity>();
	private Map<String, CompositePhysicalEntity> speciesAndSemSimEntitiesMap = new HashMap<String, CompositePhysicalEntity>();
	public Hashtable<String, String[]> ontologycache = new  Hashtable<String, String[]>();
	public Set<String> baseUnits = new HashSet<String>();
	private Submodel parametersubmodel;
	private Submodel speciessubmodel;
	private Submodel compartmentsubmodel;
	
	
	public SBMLreader(File file) {
		super(file);
	}

	@Override
	public SemSimModel readFromFile() throws IOException, InterruptedException,
			OWLException, CloneNotSupportedException {
		
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
		
		// Create submodels for compartments and species. Each reaction gets its own submodel later.
		if(sbmlmodel.getListOfParameters().size()>0) 
			parametersubmodel = semsimmodel.addSubmodel(new Submodel("parameters"));
		
		if(sbmlmodel.getListOfCompartments().size()>0) 
			compartmentsubmodel = semsimmodel.addSubmodel(new Submodel("compartments"));
		
		if(sbmlmodel.getListOfSpecies().size()>0)
			speciessubmodel = semsimmodel.addSubmodel(new Submodel("species"));
				
		// if any errors at this point, return model
		if(semsimmodel.getErrors().size()>0) return semsimmodel;
		
		setBaseUnits();
		collectModelLevelData();
		collectUnits();
		collectCompartments();
		collectSpecies();
		collectParameters();
		collectRules();
		collectReactions();
		collectConstraints();
		collectEvents();

		
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
	private void collectFunctionDefinitions(){
				
		for(int f=0; f<sbmlmodel.getListOfFunctionDefinitions().size(); f++){
//			FunctionDefinition fd = sbmlmodel.getFunctionDefinition(f);
			//... not sure how to deal with SBML functions yet. Use functional submodels?
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
				String unitfactorname = libsbml.UnitKind_toString(sbmlunit.getKind());

				UnitOfMeasurement baseunit = null;
				
				// If the base unit for the unit factor was already added to model, retrieve it. Otherwise create anew.
				if(semsimmodel.containsUnit(unitfactorname)) baseunit = semsimmodel.getUnit(unitfactorname);
				else{
					baseunit = new UnitOfMeasurement(unitfactorname);
					baseunit.setFundamental(baseUnits.contains(unitfactorname));
					collectSBaseData(sbmlunit, baseunit);
					semsimmodel.addUnit(baseunit);
				}
				
				UnitFactor unitfactor = new UnitFactor(baseunit, sbmlunit.getExponentAsDouble(), unitfactorname);
				
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
			
			String mathml = "<cn>" + sbmlc.getSize() + "</cn>";
			ds.getComputation().setMathML(mathml);
			//ds.getComputation().setComputationalCode(compid + " = " + Double.toString(sbmlc.getSize()));
						
			if(sbmlc.isSetUnits()) 
				ds.setUnit(semsimmodel.getUnit(sbmlc.getUnits()));
			
			PhysicalPropertyinComposite prop = null;
						
			// Add physical property here
			if(sbmlc.getSpatialDimensionsAsDouble()==3.0){
				prop = new PhysicalPropertyinComposite("", URI.create(SemSimConstants.OPB_NAMESPACE + "OPB_00154"));
			}
			else if(sbmlc.getSpatialDimensionsAsDouble()==2.0){
				prop = new PhysicalPropertyinComposite("", URI.create(SemSimConstants.OPB_NAMESPACE + "OPB_00295"));
			}
			else if(sbmlc.getSpatialDimensionsAsDouble()==1.0){
				prop = new PhysicalPropertyinComposite("", URI.create(SemSimConstants.OPB_NAMESPACE + "OPB_01064"));
			}
			else{}  // what to do if zero?
			
			ds.setAssociatedPhysicalProperty(prop);
			
			// Set the physical entity for the compartment
			PhysicalEntity compartmentent = (PhysicalEntity) createPhysicalComponentForSBMLobject(sbmlc);
			compartmentAndSemSimEntitiesMap.put(compid, compartmentent);
						
			ArrayList<PhysicalEntity> entlist = new ArrayList<PhysicalEntity>();
			entlist.add(compartmentent);
			ArrayList<StructuralRelation> rellist = new ArrayList<StructuralRelation>();
			
			CompositePhysicalEntity compositeent = new CompositePhysicalEntity(entlist, rellist);
			semsimmodel.addCompositePhysicalEntity(compositeent); // this also adds the singular physical entity to the model
			ds.setAssociatedPhysicalModelComponent(compositeent);

			collectSBaseData(sbmlc, compartmentent);
		}
	}
	
	/**
	 *  Collect the SBML model's chemical species data
	 */
	private void collectSpecies(){
		
		// For info on dealing with species, espeica
		for(int s=0; s<sbmlmodel.getListOfSpecies().size(); s++){
			Species species = sbmlmodel.getSpecies(s);
			
			DataStructure ds = semsimmodel.addDataStructure(new Decimal(species.getId()));
			speciessubmodel.addDataStructure(ds);
			
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
					ds.setUnit(semsimmodel.getUnit(species.getSubstanceUnits()));
				}
				else{
					if(sbmlmodel.isSetSubstanceUnits()){
						ds.setUnit(semsimmodel.getUnit(sbmlmodel.getSubstanceUnits()));
					}
					else{}
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
					ds.setUnit(semsimmodel.getUnit(species.getSubstanceUnits()));
				else 
					ds.setUnit(semsimmodel.getUnit("substance"));
			}
			
			// The OPB properties assigned here need to account for the different possible units for 
			// substance units: 'dimensionless', 'mole', 'item', kilogram','gram', etc. as above.
			// Will need base unit breakdown to assign appropriate OPB terms
			
			if(species.getHasOnlySubstanceUnits()){
				// look up factor for unit substance in semsimmodel and determine OPB property from that.
				// but if substance not in model...(level 3) ...
				
				prop = new PhysicalPropertyinComposite("Chemical molar amount", URI.create(SemSimConstants.OPB_NAMESPACE + "OPB_00425")); // Chemical molar amount
				ds.setStartValue(Double.toString(species.getInitialAmount()));
			}
			else{
				prop = new PhysicalPropertyinComposite("Chemical concentration", URI.create(SemSimConstants.OPB_NAMESPACE + "OPB_00340")); // Chemical concentration
				ds.setStartValue(Double.toString(species.getInitialConcentration()));
			}

			ds.setAssociatedPhysicalProperty(prop);
			
			PhysicalEntity speciesent = (PhysicalEntity) createPhysicalComponentForSBMLobject(species);
			
			PhysicalEntity compartmentent = null;
			
			if(compartmentAndSemSimEntitiesMap.containsKey(species.getCompartment()))
				compartmentent = compartmentAndSemSimEntitiesMap.get(species.getCompartment());
			else
				System.err.println("WARNING: unknown compartment " + species.getCompartment() + " for species " + species.getId());
			
			
			ArrayList<PhysicalEntity> entlist = new ArrayList<PhysicalEntity>();
			entlist.add(speciesent);
			entlist.add(compartmentent);
			ArrayList<StructuralRelation> rellist = new ArrayList<StructuralRelation>();
			rellist.add(SemSimConstants.PART_OF_RELATION);
			
			CompositePhysicalEntity compositeent = new CompositePhysicalEntity(entlist, rellist);
						
			semsimmodel.addCompositePhysicalEntity(compositeent); // this also adds the singular physical entity to the model
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
	private void collectInitialAssignments(){
		for(int i=0; i<sbmlmodel.getListOfInitialAssignments().size(); i++){
			InitialAssignment ia = sbmlmodel.getInitialAssignment(i);
			String symbol = ia.getSymbol();
			DataStructure ds = semsimmodel.getAssociatedDataStructure(symbol);;
			//ds.setStartValue(val);
			//...
		}
	}
	
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
			else ds = semsimmodel.addDataStructure(new Decimal(varname));
			
			//ds.getComputation().setComputationalCode(varname + " = " + sbmlrule.getFormula());
			String mathmlstring = libsbml.writeMathMLToString(sbmlrule.getMath());
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
			
			Event ssevent = new Event();
			ssevent.getTrigger().setMathML(triggermathml);
			
			// collect inputs for trigger here
			
			// Process event assignments
			for(int a=0; a<sbmlevent.getListOfEventAssignments().size(); a++){
				org.sbml.libsbml.EventAssignment ea = sbmlevent.getEventAssignment(a);
				EventAssignment ssea = ssevent.new EventAssignment();
				ssea.setMathML(libsbml.writeMathMLToString(ea.getMath()));
				ssea.setOutput(semsimmodel.getAssociatedDataStructure(ea.getVariable()));
				
				// set inputs
				
				
				ssevent.addEventAssignment(ssea);
				
			}
			
			// Collect the delay info
			if(sbmlevent.isSetDelay()){
				Delay delay = sbmlevent.getDelay();
				ssevent.setDelayMathML(libsbml.writeMathMLToString(delay.getMath()));
			}
			
			// Collect priority (SBML level 3)
			if(sbmlmodel.getLevel()==3 && sbmlevent.isSetPriority()){
				Priority priority = sbmlevent.getPriority();
				ssevent.setPriorityMathML(libsbml.writeMathMLToString(priority.getMath()));
			}
			
			// Set the time units (SBML level 2 version 2 or version 1)
			if(sbmlmodel.getLevel()==3 && sbmlmodel.getVersion()<3 && sbmlevent.isSetTimeUnits()){
				String timeunitsname = sbmlevent.getTimeUnits();
				ssevent.setTimeUnit(semsimmodel.getUnit(timeunitsname));
			}
		}
	}
	
	/**
	 *  Collect the SBML model's reaction data
	 */
	private void collectReactions(){
		
		for(int r=0; r<sbmlmodel.getListOfReactions().size(); r++){
			Reaction reaction = sbmlmodel.getReaction(r);
			String reactionID = reaction.getId();
			
			DataStructure ds = semsimmodel.addDataStructure(new Decimal(reactionID));
			Submodel rxnsubmodel = new Submodel(reactionID);
			semsimmodel.addSubmodel(rxnsubmodel);
			rxnsubmodel.addDataStructure(ds);
			
			KineticLaw kineticlaw = reaction.getKineticLaw();
			
			// Deal with kinetic law (need to collect local parameters)
			String mathmlstring = libsbml.writeMathMLToString(kineticlaw.getMath());
			ds.getComputation().setMathML(mathmlstring);
			//ds.getComputation().setComputationalCode(reaction.getId() + " = " + reaction.getKineticLaw().getFormula());
			
			for(int l=0; l<kineticlaw.getListOfLocalParameters().size(); l++){
				LocalParameter lp = kineticlaw.getLocalParameter(l);
				DataStructure localds = addParameter(lp, reaction.getId());
				rxnsubmodel.addDataStructure(localds);
			}
			
			// This might be unnecessary for some more recent versions of SBML models (listOfParameters might have been deprecated)
			for(int p=0; p<kineticlaw.getListOfParameters().size(); p++){
				Parameter par = kineticlaw.getParameter(p);
				DataStructure localds = addParameter(par, reaction.getId());
				rxnsubmodel.addDataStructure(localds);
			}
			
			//SBML Reaction objects are defined in units of substance/time. CHECK THIS.
			
			UnitDefinition ud = kineticlaw.getDerivedUnitDefinition();
			
			if(ud!=null){
				
				if(ud.isSetId()){
					String unitid = kineticlaw.getDerivedUnitDefinition().getId();
					UnitOfMeasurement uom = semsimmodel.getUnit(unitid);
					ds.setUnit(uom);
				}
			}
			

			PhysicalPropertyinComposite prop = null;
			
			// Add physical property here
			prop = new PhysicalPropertyinComposite("Chemical molar flow rate", URI.create(SemSimConstants.OPB_NAMESPACE + "OPB_00592"));
			ds.setAssociatedPhysicalProperty(prop);
			
			PhysicalProcess process = (PhysicalProcess) createPhysicalComponentForSBMLobject(reaction);
			collectSBaseData(reaction, process);
			
			// Set sources (reactants)
			for(int s=0; s<reaction.getNumReactants(); s++){
				String reactantname = reaction.getReactant(s).getSpecies();
				double stoich = reaction.getReactant(s).getStoichiometry();
				PhysicalEntity reactantent = speciesAndSemSimEntitiesMap.get(reactantname);
				process.addSource(reactantent, stoich);
								
				// Assert that the computation for the reactant depends on the rate of this reaction
				semsimmodel.getAssociatedDataStructure(reactantname).getComputation().addInput(ds);
			}
			
			// Set sinks (products)
			for(int p=0; p<reaction.getNumProducts(); p++){
				String productname = reaction.getProduct(p).getSpecies();
				double stoich = reaction.getProduct(p).getStoichiometry();
				PhysicalEntity productent = speciesAndSemSimEntitiesMap.get(productname);
				process.addSink(productent, stoich);
				
				// Assert that the computation for the product depends on the rate of this reaction
				semsimmodel.getAssociatedDataStructure(productname).getComputation().addInput(ds);
			}
			
			// Set mediators (modifiers)
			for(int m=0; m<reaction.getNumModifiers(); m++){
				String mediatorname = reaction.getModifier(m).getSpecies();
				PhysicalEntity mediatorent = speciesAndSemSimEntitiesMap.get(mediatorname);
				process.addMediator(mediatorent);
			}
			
			ds.setAssociatedPhysicalModelComponent(process);
			
			// Set the computational inputs for the reaction
			setComputationalInputsFromMathML(ds, mathmlstring);
						
			// Add process to model
			if(process instanceof ReferencePhysicalProcess) 
				semsimmodel.addReferencePhysicalProcess((ReferencePhysicalProcess) process);
			else 
				semsimmodel.addCustomPhysicalProcess((CustomPhysicalProcess) process);
			
			collectSBaseData(reaction, process);
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
	private void addAnnotations(SBase sbmlobject, Annotatable semsimobject){
		
		Set<ReferenceOntologyAnnotation> allanns = new HashSet<ReferenceOntologyAnnotation>();
		allanns.addAll(getBiologicalQualifierAnnotations(sbmlobject));
		allanns.addAll(getModelQualifierAnnotations(sbmlobject));
		
		for(ReferenceOntologyAnnotation ann : allanns) semsimobject.addAnnotation(ann);
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
			if(term.getQualifierType()==1){
				Integer t = Integer.valueOf(term.getBiologicalQualifierType());
				
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
								SemSimRelation relation = (t==0) ? SemSimConstants.REFERS_TO_RELATION : SemSimConstants.BIOLOGICAL_QUALIFIER_TYPES_AND_RELATIONS.get(t);
								
								// If we haven't already applied an identity annotation for this sbml component
								if(numidentityanns==0){
									anns.add(new ReferenceOntologyAnnotation(relation, URI.create(uristring), uristring));
									numidentityanns++;
								}
								else System.err.println("WARNING: Multiple reference annotations for " + 
											getIDforSBaseObject(sbmlobject) + ". Ignoring annotation against " + uristring);
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
				pmc = isentity? new ReferencePhysicalEntity(ann.getReferenceURI(), ann.getValueDescription()) :
					new ReferencePhysicalProcess(ann.getReferenceURI(), ann.getValueDescription());
				tempanns.remove(ann);
				break;
			}
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
		
		if(sbmlel instanceof Compartment || sbmlel instanceof CompartmentType
			|| sbmlel instanceof Species || sbmlel instanceof SpeciesType) return true;
		else return false;
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
		
		ds.getComputation().setMathML("<cn>" + p.getValue() + "</cn>");
		//ds.getComputation().setComputationalCode(ID + " = " + Double.toString(p.getValue()));
		
		// Annotations, too?
		collectSBaseData(p, ds);
		
		return ds;
	}
	
	
	// Set all the variables in a block of mathml as inputs to a particular data structure's computation
	private void setComputationalInputsFromMathML(DataStructure outputds, String mathmlstring){

		Pattern p = Pattern.compile("<ci>.+</ci>");
		Matcher m = p.matcher(mathmlstring);
		boolean result = m.find();
		
		while(result){
			
			String inputname = mathmlstring.substring(m.start()+4, m.end()-5).trim();
			String inputnamelocalfmt = outputds.getName() + "." + inputname;
			String inputnametouse = inputname;
						
			if(! semsimmodel.containsDataStructure(inputname)){
				if(! semsimmodel.containsDataStructure(inputnamelocalfmt)){
					addErrorToModel("Could not set inputs for variable " + outputds.getName() + " because input called " + inputname + " was not in model.");
					return;
				}
				else inputnametouse = inputnamelocalfmt;
			}
			
			DataStructure inputds = semsimmodel.getAssociatedDataStructure(inputnametouse);
			outputds.getComputation().addInput(inputds);
			result = m.find();
		}
	}
	
	
	// Select appropriate set of base units by SBML level and version number
	private void setBaseUnits(){
		baseUnits.clear();
		
		if( sbmlmodel.getLevel()==3) baseUnits.addAll(SBMLconstants.SBML_LEVEL_3_BASE_UNITS);
		else if( sbmlmodel.getLevel()==2 && sbmlmodel.getVersion()==4) baseUnits.addAll(SBMLconstants.SBML_LEVEL_2_VERSION_4_BASE_UNITS);
		else if( sbmlmodel.getLevel()==2 && sbmlmodel.getVersion()==1) baseUnits.addAll(SBMLconstants.SBML_LEVEL_2_VERSION_1_BASE_UNITS);
		else{}
	}
	
	private void addErrorToModel(String description){
		String errmsg = "SBML-TO-SEMSIM CONVERSION ERROR:\n" + description;
		System.err.println(errmsg);
		semsimmodel.addError(errmsg);
	}
}
