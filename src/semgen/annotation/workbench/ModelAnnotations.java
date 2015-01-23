package semgen.annotation.workbench;

import java.util.Observable;
import java.util.Set;

import semgen.annotation.dialog.LegacyCodeChooser;
import semsim.SemSimConstants;
import semsim.model.SemSimModel;
import semsim.model.annotation.Annotation;
import semsim.model.annotation.SemSimRelation;

public class ModelAnnotations extends Observable {
	Set<Annotation> annotations;
	public static enum ModelChangeEnum {SOURCECHANGED};
	
	public ModelAnnotations(SemSimModel ssm) {
		annotations = ssm.getAnnotations();
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
