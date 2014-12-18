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
	
	public static enum ModelChangeEnum {SOURCECHANGED};
	
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
	
	public ArrayList<Pair<String, Boolean>> getModelAnnotationFilledPairs() {
		ArrayList<Pair<String, Boolean>> list = new ArrayList<Pair<String, Boolean>>();
		
		for (Metadata m : metadatalist) {
			list.add(new Pair<String, Boolean>(metadata.getAnnotationName(m), metadata.hasAnnotationValue(m)));
		}
		
		return list;
	}
}
