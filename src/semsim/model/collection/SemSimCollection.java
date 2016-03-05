package semsim.model.collection;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import semsim.SemSimObject;
import semsim.definitions.SemSimTypes;
import semsim.model.computational.datastructures.DataStructure;
import semsim.model.computational.datastructures.Decimal;
import semsim.model.computational.datastructures.MMLchoice;
import semsim.model.computational.datastructures.SemSimInteger;
import semsim.model.physical.PhysicalProcess;
import semsim.model.physical.object.CompositePhysicalEntity;

public abstract class SemSimCollection extends SemSimObject{
	
	protected ArrayList<Submodel> submodels = new ArrayList<Submodel>();
	protected ArrayList<DataStructure> dataStructures = new ArrayList<DataStructure>();

	public SemSimCollection(SemSimTypes type) {
		super(type);
	}
	
	public SemSimCollection(SemSimCollection coltocopy) {
		super(coltocopy);
		submodels.addAll(coltocopy.submodels);
		dataStructures.addAll(coltocopy.dataStructures);
	}
	
	/**
	 * @return All Decimals contained in the model.
	 */
	public ArrayList<DataStructure> getDecimals(){
		ArrayList<DataStructure> list = new ArrayList<DataStructure>();
		
		for(DataStructure ds : getAssociatedDataStructures()){
			if(ds instanceof Decimal) list.add(ds);
		}
		return list;
	}
	
	/**
	 * @return The MMLchoiceVariables in the model. 
	 */
	public ArrayList<DataStructure> getMMLchoiceVars(){
		ArrayList<DataStructure> list = new ArrayList<DataStructure>();
		
		for(DataStructure ds : getAssociatedDataStructures()){
			if(ds instanceof MMLchoice) list.add(ds);
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
		else System.err.println("Model already has data structure named " + ds.getName() + ". Using existing data structure.");
		
		return ds;
	}
	
	public Submodel addSubmodel(Submodel submodel){
		submodels.add(submodel);
		return submodel;
	}
	
	public void setSubmodels(ArrayList<Submodel> submodels) {
		this.submodels.clear();
		this.submodels.addAll(submodels);
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
			
			if(ds.getName().equals(name)) return ds;
		}
		return null;
	}
	
	public void setAssociatedDataStructures(ArrayList<DataStructure> dsset) {
		dataStructures.clear();
		dataStructures.addAll(dsset);
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
		Set<DataStructure> reals = new HashSet<DataStructure>();
		reals.addAll(getDecimals());
		reals.addAll(getIntegers());
		reals.addAll(getMMLchoiceVars());
		return reals;
	}
	
	/**
	 * @return The set of {@link DataStructure}s with composite entities in the model.
	 */
	public Set<DataStructure> getDataStructureswithCompositesEntities(){
		Set<DataStructure> dswcpes = new HashSet<DataStructure>();
		for (DataStructure ds : dataStructures) {
			if (ds.hasAssociatedPhysicalComponent()) {
				if (ds.getAssociatedPhysicalModelComponent() instanceof CompositePhysicalEntity) {
					dswcpes.add(ds);
				}
			}
		}
		return dswcpes;
	}
	
	/**
	 * @return The set of {@link DataStructure}s with physical properties in the model.
	 */
	public Set<DataStructure> getDataStructureswithPhysicalProcesses(){
		Set<DataStructure> dswprocs = new HashSet<DataStructure>();
		for (DataStructure ds : dataStructures) {
			if (ds.hasAssociatedPhysicalComponent()) {
				if (ds.getAssociatedPhysicalModelComponent() instanceof PhysicalProcess) {
					dswprocs.add(ds);
				}
			}
		}
		return dswprocs;
	}
	
	/**
	 * @return The set of {@link DataStructure}s with physical properties in the model.
	 */
	public Set<DataStructure> getDataStructureswithoutAssociatedPhysicalComponents(){
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
}
