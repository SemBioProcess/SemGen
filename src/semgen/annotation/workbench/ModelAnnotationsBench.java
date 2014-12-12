package semgen.annotation.workbench;

import java.util.Observable;
import java.util.Set;

import semgen.annotation.dialog.modelanns.LegacyCodeChooser;
import semsim.SemSimConstants;
import semsim.annotation.Annotation;
import semsim.annotation.CurationalMetadata;
import semsim.annotation.SemSimRelation;
import semsim.model.SemSimModel;

public class ModelAnnotationsBench extends Observable {
	CurationalMetadata metadata;	
	Set<Annotation> annotations;
	public static enum ModelChangeEnum {SOURCECHANGED};
	
	public ModelAnnotationsBench(SemSimModel ssm) {
		annotations = ssm.getAnnotations();
		metadata = ssm.getCurationalMetadata();
	}
	
	public void addModelAnnotation(SemSimRelation rel, String ann) {
		annotations.add(new Annotation(rel, ann));
	}
	
	public void setModelSourceFile(String loc) {
		addModelAnnotation(SemSimConstants.LEGACY_CODE_LOCATION_RELATION, loc);
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
}
