package semsim.model.collection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import semsim.SemSimObject;
import semsim.definitions.SemSimTypes;
import semsim.model.computational.datastructures.DataStructure;
import semsim.model.computational.datastructures.SemSimInteger;
import semsim.model.physical.PhysicalEntity;
import semsim.model.physical.PhysicalModelComponent;
import semsim.model.physical.PhysicalProcess;

/**
 * A SemSimObject that contains {@link Submodel}s and {@link DataStructure}s
 * @author mneal
 *
 */
public abstract class SemSimCollection extends SemSimObject{
	
	protected ArrayList<Submodel> submodels = new ArrayList<Submodel>();
	protected ArrayList<DataStructure> dataStructures = new ArrayList<DataStructure>();

	public SemSimCollection(SemSimTypes type) {
		super(type);
	}
	
	/**
	 * Constructor specifying the type of collection and the associated {@link DataStructure}s
	 * @param type The type of collection
	 * @param dscollection The list of {@link DataStructure}s
	 */
	public SemSimCollection(SemSimTypes type, ArrayList<DataStructure> dscollection) {
		super(type);
		dataStructures.addAll(dscollection);
	}
	
	/**
	 * Constructor specifying the type of collection, the associated {@link DataStructure}s,
	 * and the associated {@link Submodel}s
	 * @param type The type of collection
	 * @param dscollection The list of {@link DataStructure}s
	 * @param smcollection The list of {@link Submodel}s
	 */
	public SemSimCollection(SemSimTypes type, ArrayList<DataStructure> dscollection, ArrayList<Submodel> smcollection) {
		super(type);
		dataStructures.addAll(dscollection);
		submodels.addAll(smcollection);
	}
	
	
	/**
	 * Copy constructor
	 * @param coltocopy The SemSimCollection to copy
	 */
	public SemSimCollection(SemSimCollection coltocopy) {
		super(coltocopy);
	}
	
	/**
	 * @return All Decimals contained in the model.
	 */
	public ArrayList<DataStructure> getDecimals(){
		ArrayList<DataStructure> list = new ArrayList<DataStructure>();
		
		for(DataStructure ds : getAssociatedDataStructures()){
			if(ds.isType(SemSimTypes.DECIMAL)) list.add(ds);
		}
		return list;
	}
	
	/**
	 * @return The MMLchoiceVariables in the model. 
	 */
	public ArrayList<DataStructure> getMMLchoiceVars(){
		ArrayList<DataStructure> list = new ArrayList<DataStructure>();
		
		for(DataStructure ds : getAssociatedDataStructures()){
			if(ds.isType(SemSimTypes.MMLCHOICE)) list.add(ds);
		}
		return list;
	}

	/**
	 * Add a {@link DataStructure} to the model. DataStructure is not added to model 
	 * if one with the same name already exists.
	 * 
	 * @param ds The DataStructure to add
	 * @return The DataStructure to add
	 */
	public DataStructure addDataStructure(DataStructure ds){
		
		if(!containsDataStructure(ds.getName())){
			dataStructures.add(ds);
		}
		else {
			System.err.println("Model already has data structure named " + ds.getName() + ". Using existing data structure.");
		}
		
		return ds;
	}
	
	/**
	 * Add a {@link Submodel} to the collection
	 * @param submodel The {@link Submodel} to add
	 * @return The {@link Submodel} added to the colleciton
	 */
	public Submodel addSubmodel(Submodel submodel){
		submodels.add(submodel);
		return submodel;
	}
	
	public void addSubmodels(Collection<Submodel> submodel){
		submodels.addAll(submodel);
	}
	
	public void setSubmodels(Collection<Submodel> submodels) {
		this.submodels = new ArrayList<Submodel>(submodels);
	}
	
	public void removeSubmodel(Submodel sub){
			submodels.remove(sub);
	}
	
	/**
	 * @return All {@link Submodel}s in the model.
	 */
	public ArrayList<Submodel> getSubmodels() {
		return submodels;
	}
	
	/**
	 * @return All {@link Submodel}s not contained in another submodel in the collection.
	 */
	public ArrayList<Submodel> getTopSubmodels() {
		ArrayList<Submodel> toplevelsms = new ArrayList<Submodel>();
		for (Submodel sm : submodels) {
			boolean ischild = false;
			for (Submodel sm2 : submodels) {
				if (sm2 == sm) continue;
				if (sm2.containsSubmodel(sm)) {
					ischild = true;
					break;
				}
			}
			if (!ischild) toplevelsms.add(sm);
		}
		return toplevelsms;
	}
	
	public ArrayList<FunctionalSubmodel> getTopFunctionalSubmodels(){
		ArrayList<FunctionalSubmodel> toplevelfsms = new ArrayList<FunctionalSubmodel>();
		for(Submodel sub : getTopSubmodels()){
			if(sub.isFunctional()) toplevelfsms.add((FunctionalSubmodel)sub);
		}
		return toplevelfsms;
	}
	
	public boolean containsSubmodel(Submodel sm) {
		return submodels.contains(sm);
	}
	
	public boolean containsSubmodels() {
		return !submodels.isEmpty();
	}
	
	/**
	 * @param name The name of a {@link Submodel} to retrieve
	 * @return The Submodel with the specified name or null if no Submodel found with that name. 
	 */
	public Submodel getSubmodel(String name){
		Submodel sub = null;
		
		for(Submodel sub1 : getSubmodels()){
			
			if(sub1.getName().equals(name)){
				sub = sub1;
				break;
			}
		}
		return sub;
	}
	
	/**
	 * Get a DataStructure in the model by its name.
	 * @param name The name to search for.
	 * @return The DataStructure with the specified name or null if not found.
	 */
	public DataStructure getAssociatedDataStructure(String name){
		
		for(DataStructure ds : getAssociatedDataStructures()){
			
			if(ds.getName().contentEquals(name)) return ds;
		}
		return null;
	}
	
	public void setAssociatedDataStructures(Collection<DataStructure> dsset) {
		dataStructures = new ArrayList<DataStructure>(dsset);
	}
	
	/**
	 * @return The set of all {@link DataStructure}s in the model.
	 */
	public ArrayList<DataStructure> getAssociatedDataStructures(){
		return dataStructures;
	}
	
	/**
	 * @return A set of all the names of DataStructures contained in the model.
	 */
	public Set<String> getDataStructureNames(){
		Set<String> set = new HashSet<String>();
		
		for(DataStructure ds : getAssociatedDataStructures()){
			set.add(ds.getName());
		}
		
		return set;
	}

	/**
	 * @return All DataStructures that are explicitly declared in the model.
	 * Some DataStructures may not be explicitly declared. For example, in MML code one can use
	 * x:t in the RHS of an equation. This can instantiate a variable in the model called "x:t"
	 * without an explicit declaration.
	 */
	public Set<DataStructure> getDeclaredDataStructures(){
		Set<DataStructure> dsset = new HashSet<DataStructure>();
		
		for(DataStructure ds : getAssociatedDataStructures()){
			if(ds.isDeclared()) dsset.add(ds);
		}
		
		return dsset;
	}
	
	/**
	 * @return The set of DataStructures that are not included in any Submodel.
	 */
	public ArrayList<DataStructure> getUngroupedDataStructures(){
		ArrayList<DataStructure> returnset = new ArrayList<DataStructure>();
		ArrayList<DataStructure> allds = getAssociatedDataStructures();		returnset.addAll(allds);
		
		for(Submodel sub : getSubmodels()){
			
			for(DataStructure ds : sub.getAssociatedDataStructures())
				returnset.remove(ds);
		}
		
		return returnset;
		
	}
		
	/**
	 * @return True if the model contains a DataStructure with the specified name, otherwise false.
	 */
	public boolean containsDataStructure(String name){
		return getAssociatedDataStructure(name)!=null;
	}
	
	/**
	 * @return All DataStructures that are associated with {@link FunctionalSubmodel}s.
	 */
	public Set<DataStructure> getDataStructuresFromFunctionalSubmodels(){
		Set<DataStructure> dss = new HashSet<DataStructure>();
		
		for(FunctionalSubmodel submodel : getFunctionalSubmodels()){
			dss.addAll(submodel.getAssociatedDataStructures());
		}
		
		return dss;
	}
	
	/**
	 * @return All {@link FunctionalSubmodel}s in the model.
	 */
	public Set<FunctionalSubmodel> getFunctionalSubmodels(){
		Set<FunctionalSubmodel> fxnalsubs = new HashSet<FunctionalSubmodel>();
		
		for(Submodel sub : getSubmodels()){
			if(sub.isFunctional()) fxnalsubs.add((FunctionalSubmodel) sub);
		}
		
		return fxnalsubs;
	}
	
	public boolean containsFunctionalSubmodels() {
		for (Submodel sub : submodels) {
			if (sub.isFunctional()) return true;
		}
		return false;
	}
	
	/**
	 * @return All {@link SemSimInteger}s in the model.
	 */
	public ArrayList<DataStructure> getIntegers(){
		ArrayList<DataStructure> list = new ArrayList<DataStructure>();
		
		for(DataStructure ds : getAssociatedDataStructures()){
			
			if(ds instanceof SemSimInteger) list.add(ds);
		}
		return list;
	}
	
	/**
	 * @return All Decimals, Integers and MMLchoiceVariables in the model.
	 */
	public Set<DataStructure> getReals(){
		Set<DataStructure> reals = new HashSet<DataStructure>();		reals.addAll(getDecimals());
		reals.addAll(getIntegers());
		reals.addAll(getMMLchoiceVars());
		return reals;
	}
	
	/**
	 * @return The set of {@link DataStructure}s with composite entities in the model.
	 */
	public Set<DataStructure> getDataStructuresWithCompositesEntities(){
		Set<DataStructure> dswcpes = new HashSet<DataStructure>();
		for (DataStructure ds : dataStructures) {
			if (ds.hasAssociatedPhysicalComponent()) {
				if (ds.getAssociatedPhysicalModelComponent().isType(SemSimTypes.COMPOSITE_PHYSICAL_ENTITY)) {
					dswcpes.add(ds);
				}
			}
		}
		return dswcpes;
	}
	
	/**
	 * @return The set of {@link DataStructure}s with physical properties in the model.
	 */
	public Set<DataStructure> getDataStructuresWithPhysicalProcesses(){
		Set<DataStructure> dswprocs = new HashSet<DataStructure>();
		for (DataStructure ds : dataStructures) {
			if (ds.hasAssociatedPhysicalComponent()) {
				if (ds.getAssociatedPhysicalModelComponent().isType(SemSimTypes.CUSTOM_PHYSICAL_PROCESS) 
						|| ds.getAssociatedPhysicalModelComponent().isType(SemSimTypes.REFERENCE_PHYSICAL_PROCESS)) {
					dswprocs.add(ds);
				}
			}
		}
		return dswprocs;
	}
	
	public Set<DataStructure> getDataStructuresWithProcessesandParticipants() {
		Set<DataStructure> dsphysio = getDataStructuresWithPhysicalProcesses();
		
		Set<PhysicalEntity> pes = new HashSet<PhysicalEntity>();
		for (DataStructure ds : dsphysio) {
			pes.addAll(((PhysicalProcess)ds.getAssociatedPhysicalModelComponent()).getParticipants());
		}
		
		for (PhysicalEntity part : pes) {
			dsphysio.addAll(gatherDatastructuresWithPhysicalComponent(part));
		}
		
		return dsphysio;
	}
	
	public HashSet<DataStructure> gatherDatastructuresWithPhysicalComponent(PhysicalModelComponent pmc) {
		HashSet<DataStructure> dsswithpmc = new HashSet<DataStructure>();
		
		for (DataStructure ds : getAssociatedDataStructures()) {
			if (ds.getAssociatedPhysicalModelComponent()==null) continue;
			if (ds.getAssociatedPhysicalModelComponent().equals(pmc)) {
				dsswithpmc.add(ds);
			}
		}
		
		return dsswithpmc;
	}
	
	/**
	 * @return The set of {@link DataStructure}s with physical properties in the model.
	 */
	public Set<DataStructure> getDataStructuresWithoutAssociatedPhysicalComponents(){
		Set<DataStructure> dswprocs = new HashSet<DataStructure>();
		for (DataStructure ds : dataStructures) {
			if (!ds.hasAssociatedPhysicalComponent()) {
				dswprocs.add(ds);
			}
		}
		return dswprocs;
	}
	
	/**
	 * @return The solution domain DataStructures used in the model.
	 * These are the DataStructures that specify the domain in which the model is solved. 
	 * Popular examples include time and space.
	 */
	public Set<DataStructure> getSolutionDomains(){
		Set<DataStructure> sdset = new HashSet<DataStructure>();
		for(DataStructure ds : getAssociatedDataStructures()){
			if(ds.isSolutionDomain()) sdset.add(ds);
		}
		return sdset;
	}
	
	public void replaceSubmodels(HashMap<Submodel, Submodel> smmap) {
		ArrayList<Submodel> replacements = new ArrayList<Submodel>();
		for (Submodel original : submodels) {
			Submodel replacer = smmap.get(original);
			if (replacer!=null) {
				replacements.add(replacer);
			}
		}
		submodels = replacements;
	}
	
	public void replaceDataStructures(HashMap<DataStructure, DataStructure> dsmap) {
		ArrayList<DataStructure> replacements = new ArrayList<DataStructure>();
		for (DataStructure original : dataStructures) {
			DataStructure replacer = dsmap.get(original);
			if (replacer!=null) {
				replacements.add(replacer);
			}
		}
		dataStructures = replacements;
		
	}

	public void replaceDataStructure(DataStructure replacee, DataStructure replacer) {
		if (dataStructures.contains(replacee)) {
			dataStructures.set(dataStructures.indexOf(replacee), replacer);
		}
		for (DataStructure original : dataStructures) {
			original.replaceDataStructureReference(replacer, replacee);
		}
		for (Submodel sm : submodels) {
			sm.replaceDataStructure(replacee, replacer);
		}
		
	}
	
	public abstract SemSimCollection clone();
}
