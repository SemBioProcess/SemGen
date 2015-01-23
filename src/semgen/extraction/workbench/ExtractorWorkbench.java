package semgen.extraction.workbench;

import java.io.File;

import semgen.utilities.Workbench;
import semsim.model.SemSimModel;

public class ExtractorWorkbench extends Workbench {
	File sourcefile;
	SemSimModel semsimmodel;
	
	public ExtractorWorkbench(File file, SemSimModel model) {
		sourcefile = file;
		semsimmodel = model;
	}
	
	@Override
	public void initialize() {

	}

	@Override
	public void setModelSaved(boolean val) {
		
	}

	@Override
	public String getCurrentModelName() {
		return semsimmodel.getName();
	}

	@Override
	public String getModelSourceFile() {
		return semsimmodel.getLegacyCodeLocation();
	}

	@Override
	public File saveModel() {
		return null;
	}

	@Override
	public File saveModelAs() {
		return null;
	}

	public File getSourceFile() {
		return sourcefile;
	}
	
	public SemSimModel getSourceModel() {
		return semsimmodel;
	}

}
