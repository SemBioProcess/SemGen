package semsim.model.collection;

import java.net.URI;

import semsim.annotation.ReferenceTerm;
import semsim.definitions.SemSimTypes;
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
		super(SemSimTypes.SUBMODEL);
		setName(name);
	}
	
	public Submodel(Submodel ssmtocopy) {
		super(ssmtocopy);
		isImported = ssmtocopy.isImported;
		functional = ssmtocopy.functional;
		parentImport = ssmtocopy.parentImport;
		singularterm = ssmtocopy.singularterm;
		if (ssmtocopy.hrefValue!=null) {
			hrefValue = new String(ssmtocopy.hrefValue);
		}
		if (ssmtocopy.referencedName!=null) {
			referencedName = new String(ssmtocopy.referencedName);
		}
		if (ssmtocopy.localName!=null) {
			localName = new String(ssmtocopy.localName);
		}

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
	
	public void setSingularAnnotation(ReferenceTerm refterm) {
		singularterm = refterm;
	}
	
	public void removeSingularAnnotation() {
		singularterm = null;
	}
		
	public URI getPhysicalDefinitionURI() {
		return singularterm.getPhysicalDefinitionURI();
	}
	
	
	public ReferenceTerm getReferenceTerm() {
		return singularterm;
	}
	
	public boolean isFunctional() {
		return functional;
	}

}
