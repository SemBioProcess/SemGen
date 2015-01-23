package semgen.annotation.workbench;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Set;

import org.openjena.atlas.lib.Pair;

import semgen.annotation.dialog.modelanns.LegacyCodeChooser;
import semsim.annotation.Annotation;
import semsim.annotation.CurationalMetadata;
import semsim.annotation.CurationalMetadata.Metadata;
import semsim.annotation.SemSimRelation;
import semsim.model.SemSimModel;

public class ModelAnnotationsBench extends Observable {
	SemSimModel model;
	CurationalMetadata metadata;
	ArrayList<Metadata> metadatalist = new ArrayList<Metadata>();
	Set<Annotation> annotations;
	int focusindex = -1;
		
	public static enum ModelChangeEnum {SOURCECHANGED, METADATACHANGED, METADATASELECTED};
	
	public ModelAnnotationsBench(SemSimModel ssm) {
		model = ssm;
		annotations = ssm.getAnnotations();
		for (Metadata m : Metadata.values()) {
			metadatalist.add(m);
		}
		metadata = ssm.getCurationalMetadata();
	}
	
	public String getFullModelName() {
		return metadata.getAnnotationValue(Metadata.fullname);
	}
	
	public void addModelAnnotation(SemSimRelation rel, String ann) {
		annotations.add(new Annotation(rel, ann));
	}
	
	public void setModelSourceFile(String loc) {
		model.setSourcefilelocation(loc);
		setChanged();
		notifyObservers(ModelChangeEnum.SOURCECHANGED);
	}
	
	public void changeModelSourceFile() {
		LegacyCodeChooser lc = new LegacyCodeChooser();
		String loc = lc.getCodeLocation();
		if (loc != null && !loc.equals("")) {
			setModelSourceFile(loc);
		}
	}
	
	public ArrayList<String[]> getAllMetadataInformation() {
		ArrayList<String[]> list = new ArrayList<String[]>();
		for (Metadata mdata : metadatalist) {
			list.add(new String[]{
					metadata.getAnnotationName(mdata),
					metadata.getAnnotationValue(mdata)
					});
		}
		
		return list;
	}
	
	public String getMetadataNamebyIndex(int index) {
		return metadata.getAnnotationName(metadatalist.get(index));
	}
	
	public String getMetadataValuebyIndex(int index) {
		return metadata.getAnnotationValue(metadatalist.get(index));
	}
	
	public void setMetadataValuebyIndex(int index, String value) {
		metadata.setAnnotationValue(metadatalist.get(index), value);
		setChanged();
		notifyObservers(ModelChangeEnum.METADATACHANGED);
	}
	
	public ArrayList<Pair<String, Boolean>> getModelAnnotationFilledPairs() {
		ArrayList<Pair<String, Boolean>> list = new ArrayList<Pair<String, Boolean>>();
		
		for (Metadata m : metadatalist) {
			list.add(new Pair<String, Boolean>(metadata.getAnnotationName(m), metadata.hasAnnotationValue(m)));
		}
		
		return list;
	}
	
	public void notifyOberserversofMetadataSelection(int index) {
		focusindex = index;
		setChanged();
		notifyObservers(ModelChangeEnum.METADATASELECTED);
	}
	
	public int getFocusIndex() {
		return focusindex;
	}
	
	public boolean hasFocus() {
		return focusindex != -1;
	}
	
	public boolean focusHasValue() {
		return metadata.hasAnnotationValue(metadatalist.get(focusindex));
	}
}
