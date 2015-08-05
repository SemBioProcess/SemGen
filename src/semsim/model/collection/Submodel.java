package semsim.model.collection;

import java.net.URI;
import semgen.SemGen;
import semsim.SemSimConstants;
import semsim.annotation.ReferenceOntologyAnnotation;
import semsim.annotation.ReferenceTerm;
import semsim.model.Importable;

public class Submodel extends SemSimCollection implements Importable {
	
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
	
	protected Submodel(Submodel ssmtocopy) {
		super(ssmtocopy);
		isImported = ssmtocopy.isImported;
		functional = ssmtocopy.functional;
		parentImport = ssmtocopy.parentImport;
		singularterm = ssmtocopy.singularterm;
		hrefValue = new String(ssmtocopy.hrefValue);
		referencedName = new String(ssmtocopy.referencedName);
		localName = new String(ssmtocopy.localName);
		submodels.addAll(ssmtocopy.submodels);
		dataStructures.addAll(ssmtocopy.dataStructures);
	}
	
	public Submodel clone() {
        return new Submodel(this);
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
