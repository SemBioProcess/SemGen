package semsim.annotation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import semsim.SemSimConstants;

public class PublishingMetadata {
	private String annotatorauthor = "";
	private String annotatorcontact = "";
	private String modelauthor = "";
	private String modelcontact = "";
	private Set<String> publicationcitations = new HashSet<String>();
	
	private String matlaburl = "";
	private String mmlurl = "";
	private String sbmlurl = "";
	private String cellmlurl = "";
	
	public PublishingMetadata() {}

	public String getAnnotatorauthor() {
		return annotatorauthor;
	}

	public void setAnnotatorauthor(String annotatorauthor) {
		this.annotatorauthor = annotatorauthor;
	}

	public String getAnnotatorcontact() {
		return annotatorcontact;
	}

	public void setAnnotatorcontact(String annotatorcontact) {
		this.annotatorcontact = annotatorcontact;
	}

	public String getModelauthor() {
		return modelauthor;
	}

	public void setModelauthor(String modelauthor) {
		this.modelauthor = modelauthor;
	}

	public String getModelcontact() {
		return modelcontact;
	}

	public void setModelcontact(String modelcontact) {
		this.modelcontact = modelcontact;
	}

	public void addPublicationcitation(String publicationcitation) {
		publicationcitations.add(publicationcitation);
	}
	
	public Set<String> getPublicationcitations() {
		return publicationcitations;
	}

	public void setPublicationcitations(Set<String> publicationcitations) {
		this.publicationcitations = publicationcitations;
	}

	public String getMatlaburl() {
		return matlaburl;
	}

	public void setMatlaburl(String matlaburl) {
		this.matlaburl = matlaburl;
	}

	public String getMmlurl() {
		return mmlurl;
	}

	public void setMmlurl(String mmlurl) {
		this.mmlurl = mmlurl;
	}

	public String getSbmlurl() {
		return sbmlurl;
	}

	public void setSbmlurl(String sbmlurl) {
		this.sbmlurl = sbmlurl;
	}

	public String getCellmlurl() {
		return cellmlurl;
	}

	public void setCellmlurl(String cellmlurl) {
		this.cellmlurl = cellmlurl;
	}

	public ArrayList<Annotation> createAnnotations() {
		ArrayList<Annotation> list = new ArrayList<Annotation>();
		
		addAnnotation(list, SemSimConstants.ANNOTATOR_CONTACT_INFO_RELATION, annotatorcontact);
		
		return list;
	}
	
	private void addAnnotation(ArrayList<Annotation> list, SemSimRelation rel, String value) {
		if (!value.equals("") )  
			list.add(new Annotation(rel, value));
	}
}


