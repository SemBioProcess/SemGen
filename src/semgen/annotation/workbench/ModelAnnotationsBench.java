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
	Set<Annotation> annotations;
	
	public static enum ModelChangeEnum {SOURCECHANGED};
	
	public ModelAnnotationsBench(SemSimModel ssm) {
		model = ssm;
		annotations = ssm.getAnnotations();
		metadata = ssm.getCurationalMetadata();
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
		
		for (Metadata m : Metadata.values()) {
			list.add(new Pair<String, Boolean>(metadata.getAnnotationName(m), metadata.hasAnnotationValue(m)));
		}
		
		return list;
	}
}
