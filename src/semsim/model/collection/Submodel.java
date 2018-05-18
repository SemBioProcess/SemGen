package semsim.model.collection;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import semsim.definitions.SemSimTypes;
import semsim.model.Importable;
import semsim.model.computational.datastructures.DataStructure;

/**
 * A SemSim Submodel is a collection of {@link DataStructure}s and other 
 * Submodels within a SemSim model.
 * 
 * @author mneal
 */
public class Submodel extends SemSimCollection implements Importable {
	
	private boolean isImported = false;
	private String hrefValue;
	private String referencedName;
	private String localName;
	private Importable parentImport;
	protected boolean functional = false;
	
	/**
	 * Constructor specifying submodel name
	 * @param name The submodel's name
	 */
	public Submodel(String name){ 
		super(SemSimTypes.SUBMODEL);
		setName(name);
	}
	
	
	/**
	 * Constructor specifying the submodel name and the {@link DataStructure}s it contains
	 * @param name The name of the Submodel
	 * @param dscollection The list of {@link DataStructure}s contained in the Submodel
	 */
	public Submodel(String name, ArrayList<DataStructure> dscollection){ 
		super(SemSimTypes.SUBMODEL, dscollection);
		setName(name);
	}
	
	
	/**
	 * Constructor specifying the name of the Submodel, the {@link DataStructure}s it contains
	 * as well as the other Submodels it contains
	 * @param name The name of the Submodel
	 * @param dscollection The {@link DataStructure}s the Submodel contains
	 * @param smcollection The Submodels contained in the Submodel
	 */
	public Submodel(String name, ArrayList<DataStructure> dscollection, ArrayList<Submodel> smcollection){ 
		super(SemSimTypes.SUBMODEL, dscollection, smcollection);
		setName(name);
	}
	
	
	/**
	 * Copy constructor
	 * @param ssmtocopy The Submodel to copy
	 */
	public Submodel(Submodel ssmtocopy) {
		super(ssmtocopy);
		isImported = ssmtocopy.isImported;
		functional = ssmtocopy.functional;
		parentImport = ssmtocopy.parentImport;
		
		if (ssmtocopy.hrefValue!=null) {
			hrefValue = new String(ssmtocopy.hrefValue);
		}
		if (ssmtocopy.referencedName!=null) {
			referencedName = new String(ssmtocopy.referencedName);
		}
		if (ssmtocopy.localName!=null) {
			localName = new String(ssmtocopy.localName);
		}
		submodels.addAll(ssmtocopy.submodels);
		dataStructures.addAll(ssmtocopy.dataStructures);

	}
	
	/**
	 * List the {@link DataStructure}s contained by a submodel tree
	 * @param sub The root submodel to check
	 * @return All the {@link DataStructure}s contained by a submodel tree
	 */
	public static Set<DataStructure> getCodewordsAssociatedWithNestedSubmodels(Submodel sub) {
		Set<Submodel> associatedcomponents = traverseNestedComponentTreeForDataStructures(sub);
		Set<DataStructure> newcdwds = new HashSet<DataStructure>();
		for(Submodel submod : associatedcomponents){
			newcdwds.addAll(submod.getAssociatedDataStructures());
		}
		return newcdwds;
	}
	
	
	/**
	 * List the submodels contained within a root submodel's hierarchy
	 * @param sub The root submodel
	 * @return List of submodels contained within a root submodel's hierarchy
	 */
	private static Set<Submodel> traverseNestedComponentTreeForDataStructures(Submodel sub) {
	    // traverse all nodes that belong to the parent
		Set<Submodel> nodes  = new HashSet<Submodel>();
		ArrayList<Submodel> set = sub.getSubmodels();
	    for(Submodel node : set){
		    // store node information
		    nodes.add(node);
		    // traverse children recursively
		    traverseNestedComponentTreeForDataStructures(node);
	    }
	    return nodes;
	}
	
	
	// For Importable interface
	@Override
	public void setImported(boolean isImported) {
		this.isImported = isImported;
	}

	@Override
	public boolean isImported() {
		return isImported;
	}

	@Override
	public void setHrefValue(String hrefValue) {
		this.hrefValue = hrefValue;
		if(hrefValue!=null) setImported(true);
	}

	@Override
	public String getHrefValue() {
		return hrefValue;
	}

	@Override
	public void setReferencedName(String name) {
		this.referencedName = name;
		if(name!=null) this.setImported(true);
	}

	@Override
	public String getReferencedName() {
		return referencedName;
	}

	@Override
	public String getLocalName() {
		return localName;
	}

	@Override
	public void setLocalName(String name) {
		localName = name;
	}

	@Override
	public Importable getParentImport() {
		return parentImport;
	}

	@Override
	public void setParentImport(Importable parent) {
		this.parentImport = parent;
	}
	
	
	public Submodel clone() {
		return new Submodel(this);
	}
	
	
	/** @return Whether the Submodel is a CellML-style component (i.e. {@link FunctionalSubmodel} */
	public boolean isFunctional() {
		return functional;
	}

}
