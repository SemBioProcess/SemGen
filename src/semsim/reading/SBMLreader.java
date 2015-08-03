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
import org.sbml.libsbml.KineticLaw;
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
import org.semanticweb.owlapi.model.OWLException;

import semsim.SemSimConstants;
import semsim.SemSimLibrary;
import semsim.SemSimObject;
import semsim.annotation.Annotatable;
import semsim.annotation.Annotation;
import semsim.annotation.ReferenceOntologyAnnotation;
import semsim.annotation.SemSimRelation;
import semsim.annotation.StructuralRelation;
import semsim.model.collection.SemSimModel;
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

public class SBMLreader extends ModelReader{

	public Model sbmlmodel;
	private Map<String, PhysicalEntity> compartmentAndSemSimEntitiesMap = new HashMap<String, PhysicalEntity>();
	private Map<String, PhysicalEntity> speciesAndSemSimEntitiesMap = new HashMap<String, PhysicalEntity>();
	private Map<String, UnitOfMeasurement> unitDefintionsAndSemSimUnitsMap = new HashMap<String, UnitOfMeasurement>();
	public Hashtable<String, String[]> ontologycache = new  Hashtable<String, String[]>();
	
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
		      return semsimmodel;
		}
		else {
			sbmlmodel = sbmldoc.getModel();
		}
		
		semsimmodel.setSemsimversion(SemSimLibrary.SEMSIM_VERSION);
		semsimmodel.setSourceFileLocation(srcfile.getAbsolutePath());
		
		// Collect model-level information
		collectModelLevelData();
		
		// Collect function definitions. Not used in SBML level 1.
		if (sbmlmodel.getListOfFunctionDefinitions().size()>0){
			System.err.println("CANNOT READ MODEL: SBML source model contains function definitions but these are not yet supported in SemSim:");
			return semsimmodel;
		}

		// Collect unit definitions
		collectUnits();
		
		// Collect compartment types
//		collectCompartmentTypes();
		
		// Collect species types
//		collectSpeciesTypes();
		
		// Collect compartment info
		collectCompartments();
		
		// Collect species info
		collectSpecies();
		
		// Collect parameters
		collectParameters();
		
		// Collect initial assignments
//		collectInitialAssignmentsData();
		
		// Collect rules
		collectRules();
		
		// Collect constraints
//		collectConstraints();
		
		// Collect reactions
		collectReactions();
		
		// Collect events
//		collectEvents();

		
		
		return semsimmodel;
	}
	
	private void collectModelLevelData(){
		
		sbmlmodel.getVersion();
		collectSBaseData(sbmlmodel, semsimmodel);
		semsimmodel.setName(sbmlmodel.getId());

		// What to do about sbml model name field???
		
	}
	
	private void collectFunctionDefinitions(){
				
		for(int f=0; f<sbmlmodel.getListOfFunctionDefinitions().size(); f++){
//			FunctionDefinition fd = sbmlmodel.getFunctionDefinition(f);
			//... not sure how to deal with SBML functions yet. Use functional submodels?
		}
	}
	
	private void collectUnits(){
		
		for(int u=0; u<sbmlmodel.getListOfUnitDefinitions().size(); u++){
			UnitDefinition sbmlunitdef = sbmlmodel.getUnitDefinition(u);
			UnitOfMeasurement semsimunit = new UnitOfMeasurement(sbmlunitdef.getId());
			
			for(int v=0; v<sbmlunitdef.getListOfUnits().size(); v++){
				
				Unit sbmlunit = sbmlunitdef.getUnit(v);
				String unitfactorname = libsbml.UnitKind_toString(sbmlunit.getKind());

				UnitOfMeasurement baseunit = null;
				
				// If the base unit for the unit factor was already added to model, retrieve it otherwise create anew
				if(semsimmodel.containsUnit(unitfactorname)) baseunit = semsimmodel.getUnit(unitfactorname);
				else{
					baseunit = new UnitOfMeasurement(unitfactorname);
					baseunit.setFundamental(true);
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
			
			semsimunit.setFundamental(false);
			semsimmodel.addUnit(semsimunit);
			unitDefintionsAndSemSimUnitsMap.put(sbmlunitdef.getId(), semsimunit);
		}
	}
	
	// Collect model compartments
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
						
			PhysicalPropertyinComposite prop = null;
						
			// Add physical property here
			if(sbmlc.getSpatialDimensionsAsDouble()==3.0){
				prop = new PhysicalPropertyinComposite("", URI.create(SemSimConstants.OPB_NAMESPACE + "OPB_00154"));
			}
			else if(sbmlc.getSpatialDimensionsAsDouble()==2.0){
				prop = new PhysicalPropertyinComposite("", URI.create(SemSimConstants.OPB_NAMESPACE + "OPB_00295"));
			}
			
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
	
	// Collect chemical species in model
	private void collectSpecies(){
		
		for(int s=0; s<sbmlmodel.getListOfSpecies().size(); s++){
			Species species = sbmlmodel.getSpecies(s);
			DataStructure ds = semsimmodel.addDataStructure(new Decimal(species.getId()));
			
			// Deal with equations for species concentration/amount here
			

			PhysicalPropertyinComposite prop = null;
			
			// Add physical property here
			if(species.getHasOnlySubstanceUnits()){
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
				System.err.println("ERROR: unknown compartment " + species.getCompartment() + " for species " + species.getId());
			
			
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
	
	// Collect model parameters
	private void collectParameters(){
		
		for(int p=0; p<sbmlmodel.getListOfParameters().size(); p++){
			Parameter sbmlpar = sbmlmodel.getParameter(p);
			DataStructure ds = semsimmodel.addDataStructure(new Decimal(sbmlpar.getId()));
			
			UnitOfMeasurement unitforpar = unitDefintionsAndSemSimUnitsMap.get(sbmlpar.getUnits());
			ds.setUnit(unitforpar);
			
			ds.getComputation().setMathML("<cn>" + sbmlpar.getValue() + "</cn>");
			ds.getComputation().setComputationalCode(sbmlpar.getId() + " = " + Double.toString(sbmlpar.getValue()));
			
			// Annotations, too? Is this the right thing to do?
			collectSBaseData(sbmlpar, ds);
		}
	}
	
	// Collect model rules
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
			String mathmlstring = libsbml.formulaToString(sbmlrule.getMath());
			ds.getComputation().setMathML(mathmlstring);

			collectSBaseData(sbmlrule, ds.getComputation());
		}
	}
	
	// Collect all reactions
	private void collectReactions(){
		
		for(int r=0; r<sbmlmodel.getListOfReactions().size(); r++){
			Reaction reaction = sbmlmodel.getReaction(r);
			DataStructure ds = semsimmodel.addDataStructure(new Decimal(reaction.getId()));
			
			KineticLaw kineticlaw = reaction.getKineticLaw();
			
			// Deal with kinetic law (need to collect local parameters)
			String mathmlstring = libsbml.formulaToString(kineticlaw.getMath());
			ds.getComputation().setMathML(mathmlstring);
			ds.getComputation().setComputationalCode(reaction.getId() + " = " + reaction.getKineticLaw().getFormula());
			
			String unitid = kineticlaw.getDerivedUnitDefinition().getId();
			UnitOfMeasurement uom = unitDefintionsAndSemSimUnitsMap.get(unitid);
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

			// Add process to model
			if(process instanceof ReferencePhysicalProcess) 
				semsimmodel.addReferencePhysicalProcess((ReferencePhysicalProcess) process);
			else 
				semsimmodel.addCustomPhysicalProcess((CustomPhysicalProcess) process);
			
			collectSBaseData(reaction, process);
		}
	}
	
	
	// Collect all data common to an SBase object and copy it into a SemSimObject
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
	
	// Copy the notes attached to an SBML element and into the Description field of a SemSimObject 
	private void addNotes(SBase sbmlobject, SemSimObject semsimobject){
		if(sbmlobject.getNotesString()!=null && ! sbmlobject.getNotesString().equals(""))
			semsimobject.setDescription(sbmlobject.getNotesString());
	}
	
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

}
