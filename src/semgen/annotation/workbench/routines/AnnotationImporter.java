package semgen.annotation.workbench.routines;

import semgen.annotation.workbench.SemSimTermLibrary;
import semsim.model.SemSimModel;

public class AnnotationImporter {
	SemSimModel importedmodel;
	SemSimModel importingmodel;
	SemSimTermLibrary library;
	
	public AnnotationImporter(SemSimTermLibrary lib, SemSimModel model) {
		library = lib;
		importingmodel = model;
	}


		
}
