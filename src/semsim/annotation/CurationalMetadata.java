package semsim.annotation;

import java.util.ArrayList;

public class CurationalMetadata {
	public enum Metadata {
		description("Description"),
		annotatorauthor("Annotator Name"),
		annotatorcontact("Annotator Contact"),
		modelauthor("Model Author"),
		modelcontact("Model Contact"),
		cellmlurl("CellML URL"),
		matlaburl("Matlab URL"),
		mmlurl("JSim URL"),
		sbmlurl("SBML URL");
		
		private final String text;
		private String value = "";
	
		private Metadata(final String text) {
		   this.text = text;
		}
	
		@Override
		public String toString() {
		   return text;
		}
		 
		private void setValue(String val) {
			value = val;
		 }
		 private String getValue() {
			 return value;
		 }
		 private boolean hasValue() {
			 return !value.isEmpty();
		 }
	}
	
	public String getAnnotationName(Metadata item) {
		return item.toString();
	}
	
	public String getAnnotationValue(Metadata item) {
		return item.getValue();
	}
	
	public void setAnnotationValue(Metadata item, String value) {
		 item.setValue(value);
	}
	
	public boolean hasAnnotationValue(Metadata item) {
		return item.hasValue();
	}
	
	public ArrayList<Annotation> createAnnotations() {
		ArrayList<Annotation> list = new ArrayList<Annotation>();
		
		//addAnnotation(list, SemSimConstants.ANNOTATOR_CONTACT_INFO_RELATION, annotatorcontact);
		
		return list;
	}
	
	private void addAnnotation(ArrayList<Annotation> list, SemSimRelation rel, String value) {
		if (!value.equals("") )  
			list.add(new Annotation(rel, value));
	}
}


