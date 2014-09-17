package semgen.annotation.workbench;

import java.util.Observable;

import semgen.annotation.dialog.LegacyCodeChooser;
import semsim.SemSimConstants;
import semsim.model.SemSimModel;
import semsim.model.annotation.Annotation;
import semsim.model.annotation.SemSimRelation;

public class ModelAnnotations extends Observable {
	public static Integer CODEREFRESH = 300;
	SemSimModel semsimmodel;
	
	public ModelAnnotations(SemSimModel ssm) {
		semsimmodel = ssm;
	}
	
	public void addModelAnnotation(SemSimRelation rel, String ann) {
		semsimmodel.addAnnotation(new Annotation(rel, ann));
	}
	
	public void setModelSourceFile(String loc) {
		addModelAnnotation(SemSimConstants.LEGACY_CODE_LOCATION_RELATION, loc);
		notifyObservers(CODEREFRESH);
	}
	
	public void changeModelSourceFile() {
		LegacyCodeChooser lc = new LegacyCodeChooser();
		String loc = lc.getCodeLocation();
		if (loc != null && !loc.equals("")) {
			setModelSourceFile(loc);
		}
	}
}
