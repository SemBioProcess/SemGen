package semsim.model;

import java.util.Set;

import semsim.model.computational.datastructures.DataStructure;
import semsim.model.physical.Submodel;

public interface SemSimCollection {
	
	public Set<DataStructure> getAssociatedDataStructures();
	public DataStructure addDataStructure(DataStructure ds);
	public Set<Submodel> getSubmodels();
	public DataStructure getAssociatedDataStructure(String name);
	
}
