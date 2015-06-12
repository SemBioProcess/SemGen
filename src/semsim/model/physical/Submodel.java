package semsim.model.physical;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import semgen.SemGen;
import semsim.SemSimConstants;
import semsim.annotation.ReferenceOntologyAnnotation;
import semsim.annotation.ReferenceTerm;
import semsim.model.Importable;
import semsim.model.SemSimCollection;
import semsim.model.SemSimComponent;
import semsim.model.computational.datastructures.DataStructure;

public class Submodel extends SemSimComponent implements Cloneable, Importable, SemSimCollection, ReferenceTerm {
	
	private Set<DataStructure> associatedDataStructures = new HashSet<DataStructure>();
	private Set<Submodel> submodels = new HashSet<Submodel>();
	private boolean isImported = false;
	private ReferenceTerm singularterm;
	private String hrefValue;
	private String referencedName;
	private String localName;
	private Importable parentImport;
	protected boolean functional = false;
	
	public Submodel(String name){ 
		setName(name);
	}
	
	public DataStructure addDataStructure(DataStructure ds){
		associatedDataStructures.add(ds);
		return ds;
	}
	
	public void addSubmodel(Submodel submodel){
		submodels.add(submodel);
	}
	
	public DataStructure getAssociatedDataStructure(String name){
		for(DataStructure ds : getAssociatedDataStructures()){
			if(ds.getName().equals(name)) return ds;
		}
		return null;
	}
	
	public Set<DataStructure> getAssociatedDataStructures() {
		return associatedDataStructures;
	}
	
	public void setAssociatedDataStructures(Set<DataStructure> associatedDataStructures) {
		this.associatedDataStructures = associatedDataStructures;
	}

	public void setSubmodels(Set<Submodel> submodels) {
		this.submodels.addAll(submodels);
	}

	public Set<Submodel> getSubmodels() {
		return submodels;
	}
	
	public void removeSubmodel(Submodel sub){
			submodels.remove(sub);
	}
	
	public Submodel clone() throws CloneNotSupportedException {
        return (Submodel) super.clone();
	}
	
	// For Importable interface
	public void setImported(boolean isImported) {
		this.isImported = isImported;
	}

	public boolean isImported() {
		return isImported;
	}

	public void setHrefValue(String hrefValue) {
		this.hrefValue = hrefValue;
		if(hrefValue!=null) setImported(true);
	}

	public String getHrefValue() {
		return hrefValue;
	}

	public void setReferencedName(String name) {
		this.referencedName = name;
		if(name!=null) this.setImported(true);
	}

	public String getReferencedName() {
		return referencedName;
	}

	public String getLocalName() {
		return localName;
	}

	public void setLocalName(String name) {
		localName = name;
	}

	public Importable getParentImport() {
		return parentImport;
	}

	public void setParentImport(Importable parent) {
		this.parentImport = parent;
	}

	@Override
	public Boolean hasRefersToAnnotation() {
		return singularterm != null;
	}
	
	public void setSingularAnnotation(ReferenceTerm refterm) {
		singularterm = refterm;
	}
	
	public void removeSingularAnnotation() {
		singularterm = null;
	}
	
	public ReferenceOntologyAnnotation getRefersToReferenceOntologyAnnotation(){
		if(hasRefersToAnnotation()){
			return singularterm.getRefersToReferenceOntologyAnnotation();
		}
		return null;
	}
	
	public URI getReferstoURI() {
		return singularterm.getReferstoURI();
	}
	
	
	public ReferenceTerm getReferenceTerm() {
		return singularterm;
	}
	
	/**
	 * @return The name of the knowledge base that contains the URI used as the annotation value
	 */
	public String getNamewithOntologyAbreviation() {
		return singularterm.getName() + " (" + SemGen.semsimlib.getReferenceOntologyAbbreviation(singularterm.getReferstoURI()) + ")";
	}
	
	@Override
	public URI getSemSimClassURI() {
		return SemSimConstants.SUBMODEL_CLASS_URI;
	}
	
	public boolean isFunctional() {
		return functional;
	}

}
