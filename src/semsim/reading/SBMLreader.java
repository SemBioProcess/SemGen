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

import org.jdom.Content;
import org.jdom.Element;
import org.sbml.libsbml.CVTerm;
import org.sbml.libsbml.Compartment;
import org.sbml.libsbml.Constraint;
import org.sbml.libsbml.FunctionDefinition;
import org.sbml.libsbml.InitialAssignment;
import org.sbml.libsbml.KineticLaw;
import org.sbml.libsbml.ListOfFunctionDefinitions;
import org.sbml.libsbml.LocalParameter;
import org.sbml.libsbml.Model;
import org.sbml.libsbml.Parameter;
import org.sbml.libsbml.Reaction;
import org.sbml.libsbml.Rule;
import org.sbml.libsbml.SBMLDocument;
import org.sbml.libsbml.SBMLReader;
import org.sbml.libsbml.SBase;
import org.sbml.libsbml.Species;
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
import semsim.model.collection.SemSimModel;
import semsim.model.computational.RelationalConstraint;
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
import semsim.writing.CellMLwriter;

public class SBMLreader extends ModelReader{

	public Model sbmlmodel;
	private Map<String, PhysicalEntity> compartmentAndSemSimEntitiesMap = new HashMap<String, PhysicalEntity>();
	private Map<String, PhysicalEntity> speciesAndSemSimEntitiesMap = new HashMap<String, PhysicalEntity>();
	public Hashtable<String, String[]> ontologycache = new  Hashtable<String, String[]>();
	public Set<String> baseUnits = new HashSet<String>();
	
	
	
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
		
		setBaseUnits();
		
		// Collect function definitions. Not used in SBML level 1.
		// collectFunctionDefinitions();
		if (sbmlmodel.getListOfFunctionDefinitions().size()>0){
			addErrorToModel("SBML source model contains function definitions but these are not yet supported in SemSim.");
			return semsimmodel;
		}

		//collectCompartmentTypes();
		if (sbmlmodel.getListOfCompartmentTypes().size()>0){
			addErrorToModel("SBML source model contains compartment types but these are not yet supported in SemSim.");
			return semsimmodel;
		}
		
		/*
		 * A compartment type in SBML is a grouping construct used to establish a relationship between multiple 
		 * Compartment objects. In SBML Level 2 Versions 2, 3 and 4, a compartment type only has an identity, and
		 *  this identity can only be used to indicate that particular compartments belong to this type. This may 
		 *  be useful for conveying a modeling intention, such as when a model contains many similar compartments,
		 *   either by their biological function or the reactions they carry. Without a compartment type construct,
		 *    it would be impossible in the language of SBML to indicate that all of the compartments share an 
		 *    underlying conceptual relationship because each SBML compartment must be given a unique and separate 
		 *    identity. Compartment types have no mathematical meaning in SBML Level 2—they have no effect on a model's
		 *     mathematical interpretation. Simulators and other numerical analysis software may ignore CompartmentType
		 *      definitions and references to them in a model.
		
		There is no mechanism in SBML for representing hierarchies of compartment types. One CompartmentType instance cannot
		 be the subtype of another CompartmentType instance; SBML provides no means of defining such relationships.
		As with other major structures in SBML, CompartmentType has a mandatory attribute, 'id', used to give the compartment
		 type an identifier. The identifier must be a text string conforming to the identifer syntax permitted in SBML. 
		 CompartmentType also has an optional 'name' attribute, of type string. The 'id' and 'name' must be used according
		  to the guidelines described in the SBML specification (e.g., Section 3.3 in the Level 2 Version 4 specification).
		CompartmentType was introduced in SBML Level 2 Version 2. It is not available in earlier versions of Level 2 nor 
		in any version of Level 1.
		 */
		
//		collectSpeciesTypes();
// Same deal as with compartment types, just with species
		if (sbmlmodel.getListOfCompartmentTypes().size()>0){
			addErrorToModel("SBML source model contains species types but these are not yet supported in SemSim.");
			return semsimmodel;
		}
		
		
//		collectInitialAssignments();
		if (sbmlmodel.getListOfCompartmentTypes().size()>0){
			addErrorToModel("SBML source model contains initial assignments but these are not yet supported in SemSim.");
			return semsimmodel;
		}
		// Set the t=0 value for a compartment, species or parameter. The symbol field refers to the ID of the SBML element.
		// If one of these elements already has an initial value stated in its construct, the initialAssignment overwrites it.
		
		
//		collectEvents();
		if (sbmlmodel.getListOfCompartmentTypes().size()>0){
			addErrorToModel("SBML source model contains events but these are not yet supported in SemSim.");
			return semsimmodel;
		}
				
		collectModelLevelData();
		collectUnits();
		collectCompartments();
		collectSpecies();
		collectParameters();
		collectRules();
		collectReactions();
		collectConstraints();
		
		return semsimmodel;
	}
	
	/**
	 * Collect the SBML model's model-level data such as model name, semantic annotations, etc.
	 */
	private void collectModelLevelData(){
		
		sbmlmodel.getVersion();
		collectSBaseData(sbmlmodel, semsimmodel);
		semsimmodel.setName(sbmlmodel.getId());
		// Need to collect annotations here, too.

		// What to do about sbml model name field???
		
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
	 * Collect the model's function definitions
	 */
	private void collectUnits(){

		/*
		The attribute 'id' in UnitDefinition cannot be given simply any value, and the precise
		details of the values permitted differ slightly between Levels of SBML:
		The 'id' of a UnitDefinition must not contain a value from the list of SBML's predefined
		base unit names (i.e., the strings gram, litre, etc.). In SBML Level 3, this list
		consists of the following:
		
			ampere	farad	joule	lux	radian	volt
			avogadro	gram	katal	metre	second	watt
			becquerel	gray	kelvin	mole	siemens	weber
			candela	henry	kilogram	newton	sievert
			coulomb	hertz	litre	ohm	steradian
			dimensionless	item	lumen	pascal	tesla
			
		This list of predefined base units is nearly identical in SBML Level 2 Version 4, 
		the exception being that Level 2 does not define avogadro. SBML Level 2 Version 1 
		(and only this Level+Version combination) provides an additional predefined unit name, 
		Celsius, not available in Level 3. Finally, SBML Level 1 Versions 2–3 provide two 
		more additional predefined unit names, meter and liter. This is explained in somewhat 
		greater detail in the description of the Unit class.
		
		In SBML Level 2 (all Versions), there is an additional set of reserved identifiers:
		substance, volume, area, length, and time. Using one of these values for the attribute
		'id' of a UnitDefinition has the effect of redefining the model-wide default units
		for the corresponding quantities. The list of special unit names in SBML Level 2 is
		given in the table below:
		
		Identifier	Possible scalable units						Default units
		------------------------------------------------------------------------------
		substance	mole, item, gram, kilogram, dimensionless	mole
		volume		litre, cubic metre, dimensionless			litre
		area		square metre, dimensionless					square metre
		length		metre, dimensionless, 						metre
		time		second, dimensionless						second
		
		Also, SBML Level 2 imposes two limitations on redefining the predefined unit substance,
		 volume, area, length, and time: (1) The UnitDefinition of a predefined SBML unit can 
		 only contain a single Unit object within it. (2) The value of the 'kind' attribute in
		  a Unit instance must be drawn from one of the values in the second column of the table
		   above.
		The special unit names substance, volume, area, length, and time are not defined by SBML
		 Level 3, which uses a different approach to setting model-wide inherited units.
		*/
		
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
			this.collectSBaseData(sbmlunitdef, semsimunit);
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
			// Compartment type?
			// Constant?
			// Derived unit definition
			// Outside?
			// spatial dimensions
			// type code
			// units 
			// volume
			
			DataStructure ds = semsimmodel.addDataStructure(new Decimal(compid));
			String mathml = "<cn>" + sbmlc.getSize() + "</cn>";
			ds.getComputation().setMathML(mathml);
			ds.getComputation().setComputationalCode(compid + " = " + Double.toString(sbmlc.getSize()));
						
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
			PhysicalEntity compartmentent = (PhysicalEntity) getPhysicalComponentForSBMLobject(sbmlc);
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
		
		for(int s=0; s<sbmlmodel.getListOfSpecies().size(); s++){
			Species species = sbmlmodel.getSpecies(s);
			
//			System.out.println("SUB: " + sbmlmodel.getSubstanceUnits());
////			System.out.println(species.getC)
//			System.out.println("VOL: " + sbmlmodel.getVolumeUnits());
//			System.out.println("AREA: " + sbmlmodel.getAreaUnits());
//			System.out.println("LENGTH: " + sbmlmodel.getLengthUnits());
//			System.out.println("TIME: " + sbmlmodel.getTimeUnits());
//			System.out.println("EXTENT: " + sbmlmodel.getExtentUnits());
			
			
			DataStructure ds = semsimmodel.addDataStructure(new Decimal(species.getId()));
			
			// Deal with equations for species concentration/amount here
			PhysicalPropertyinComposite prop = null;
			
			/*
			From http://sbml.org/Software/libSBML/5.11.4/docs/formatted/java-api/
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
				if(species.isSetSubstanceUnits()){
					ds.setUnit(semsimmodel.getUnit(species.getSubstanceUnits()));
				}
				else{
					ds.setUnit(semsimmodel.getUnit("substance"));
				}
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
			
			PhysicalEntity speciesent = (PhysicalEntity) getPhysicalComponentForSBMLobject(species);
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
			addParameter(sbmlpar, null);
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
			
			ds.getComputation().setComputationalCode(varname + " = " + sbmlrule.getFormula());
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
	 *  Collect the SBML model's reaction data
	 */
	private void collectReactions(){
		
		for(int r=0; r<sbmlmodel.getListOfReactions().size(); r++){
			Reaction reaction = sbmlmodel.getReaction(r);
			DataStructure ds = semsimmodel.addDataStructure(new Decimal(reaction.getId()));
			
			KineticLaw kineticlaw = reaction.getKineticLaw();
			
			// Deal with kinetic law (need to collect local parameters)
			String mathmlstring = libsbml.writeMathMLToString(kineticlaw.getMath());
			ds.getComputation().setMathML(mathmlstring);
			ds.getComputation().setComputationalCode(reaction.getId() + " = " + reaction.getKineticLaw().getFormula());
			
			for(int l=0; l<kineticlaw.getListOfLocalParameters().size(); l++){
				LocalParameter lp = kineticlaw.getLocalParameter(l);
				addParameter(lp, reaction.getId());
			}
			
			// This might be unnecessary for some more recent versions of SBML models (listOfParameters might have been deprecated)
			for(int p=0; p<kineticlaw.getListOfParameters().size(); p++){
				Parameter par = kineticlaw.getParameter(p);
				addParameter(par, reaction.getId());
			}
			
			//SBML Reaction objects are defined in units of substance/time.
			
			String unitid = kineticlaw.getDerivedUnitDefinition().getId();
			UnitOfMeasurement uom = semsimmodel.getUnit(unitid);
			ds.setUnit(uom);

			PhysicalPropertyinComposite prop = null;
			
			// Add physical property here
			prop = new PhysicalPropertyinComposite("Chemical molar flow rate", URI.create(SemSimConstants.OPB_NAMESPACE + "OPB_00592"));
			ds.setAssociatedPhysicalProperty(prop);
			
			PhysicalProcess process = (PhysicalProcess) getPhysicalComponentForSBMLobject(reaction);
			collectSBaseData(reaction, process);
			
			// Set sources (reactants)
			for(int s=0; s<reaction.getNumReactants(); s++){
				String reactantname = reaction.getReactant(s).getSpecies();
				double stoich = reaction.getReactant(s).getStoichiometry();
				PhysicalEntity reactantent = speciesAndSemSimEntitiesMap.get(reactantname);
				process.addSource(reactantent, stoich);
			}
			
			// Set sinks (products)
			for(int p=0; p<reaction.getNumProducts(); p++){
				String productname = reaction.getProduct(p).getSpecies();
				double stoich = reaction.getProduct(p).getStoichiometry();
				PhysicalEntity productent = speciesAndSemSimEntitiesMap.get(productname);
				process.addSink(productent, stoich);
			}
			
			// Set mediators (modifiers)
			for(int m=0; m<reaction.getNumModifiers(); m++){
				String productname = reaction.getModifier(m).getSpecies();
				PhysicalEntity mediatorent = speciesAndSemSimEntitiesMap.get(productname);
				process.addMediator(mediatorent);
			}
			
			ds.setAssociatedPhysicalModelComponent(process);

			// Assert computational dependencies
			Content mathmlcontent = CellMLwriter.makeXMLContentFromString(mathmlstring);
			
			// Maybe solution is to first test whether an input is a local par, if so use prefix, if not don't
			CellMLreader.whiteBoxFunctionalSubmodelEquations((Element) mathmlcontent, reaction.getId(), semsimmodel, ds);
			
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
	
	// Copy ID of SBML element and use it as SemSimObject name
//	private void addIDasName(SBase sbmlclass, SemSimObject semsimclass){
//		if(sbmlclass.getId()!=null && ! sbmlclass.getId().equals(""))
//			semsimclass.setName(sbmlclass.getId());
//	}
	
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
	
	// Get biological qualifier annotations
	private Set<ReferenceOntologyAnnotation> getBiologicalQualifierAnnotations(SBase sbmlobject){
		
		Set<ReferenceOntologyAnnotation> anns = new HashSet<ReferenceOntologyAnnotation>();
		
		// Get CV terms
		for(int i=0; i<sbmlobject.getNumCVTerms();i++){
			CVTerm term = sbmlobject.getCVTerm(i);
			
			// If the CV term is used with a biological qualifier
			if(term.getQualifierType()==1){
				Integer t = Integer.valueOf(term.getBiologicalQualifierType());
				
				if(SemSimConstants.BIOLOGICAL_QUALIFIER_TYPES_AND_RELATIONS.containsKey(t)){
					
					for(int j=0; j<term.getNumResources(); j++){
						String uri = term.getResourceURI(j);
						SemSimRelation relation = (t==0) ? SemSimConstants.REFERS_TO_RELATION : SemSimConstants.BIOLOGICAL_QUALIFIER_TYPES_AND_RELATIONS.get(t);
						anns.add(new ReferenceOntologyAnnotation(relation, URI.create(uri), uri));
					}
				}
			}
		}
		return anns;
	}
	
	// Get model qualifier annotations
	private Set<ReferenceOntologyAnnotation> getModelQualifierAnnotations(SBase sbmlobject){
		
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
	private PhysicalModelComponent getPhysicalComponentForSBMLobject(SBase sbmlobject){
		
		String id = null;
		boolean isentity = true;
		if(sbmlobject instanceof Compartment) id = ((Compartment)sbmlobject).getId();
		if(sbmlobject instanceof Species) id = ((Species)sbmlobject).getId();
		if(sbmlobject instanceof Reaction){
			id = ((Reaction)sbmlobject).getId();
			isentity = false;
		}
		
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
	 * Add an SBML parameter to the model. This can also be used for SBML LocalParameters.
	 * @param p The SBML parameter to add to the SemSim model.
	 */
			
	private void addParameter(Parameter p, String prefix){

		String ID = (prefix==null || prefix.equals("")) ? p.getId() : prefix + "." + p.getId();
		
		if(semsimmodel.containsDataStructure(ID)){
			addErrorToModel("Multiple data structures with name " + ID);
		}
		DataStructure ds = semsimmodel.addDataStructure(new Decimal(ID));
		
		UnitOfMeasurement unitforpar = semsimmodel.getUnit(p.getUnits());
		ds.setUnit(unitforpar);
		
		ds.getComputation().setMathML("<cn>" + p.getValue() + "</cn>");
		ds.getComputation().setComputationalCode(ID + " = " + Double.toString(p.getValue()));
		
		// Annotations, too?
		collectSBaseData(p, ds);
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
