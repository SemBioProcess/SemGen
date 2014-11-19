package semgen.annotation;

import java.io.File;


import semgen.GlobalActions;
import semgen.SemGenSettings;
import semgen.annotation.workbench.AnnotatorWorkbench;
import semgen.resource.file.SemGenOpenFileChooser;

public class AnnotatorFactory {
	SemGenSettings settings;
	GlobalActions globalactions;
	public AnnotatorFactory(SemGenSettings sets, GlobalActions gacts) {
		settings = sets;
		globalactions = gacts;
	}
	
	public AnnotatorWorkbench makeWorkbench() {
		SemGenOpenFileChooser sgc = new SemGenOpenFileChooser("Select legacy code or SemSim model to annotate");
		File file = sgc.getSelectedFile();
		if (file ==null) return null;
		return createAnnotatorWorkbench(file);
	}	
	
	public AnnotatorWorkbench makeWorkbench(File existing) {
		return createAnnotatorWorkbench(existing);
	}

	public AnnotatorTab makeTab() {
		AnnotatorWorkbench workbench = makeWorkbench();
		return CreateAnnotatorTab(workbench);
	}	
	
	public AnnotatorTab makeTab(File existing) {
		AnnotatorWorkbench workbench = makeWorkbench(existing);
		return CreateAnnotatorTab(workbench);
	}
	
	private AnnotatorWorkbench createAnnotatorWorkbench(File file) {
		AnnotatorWorkbench workbench = new AnnotatorWorkbench();
		workbench.initialize(file, settings.doAutoAnnotate());
		return workbench;
	}
	
	private AnnotatorTab CreateAnnotatorTab(AnnotatorWorkbench workbench) {
		AnnotatorTab tab = new AnnotatorTab(settings, globalactions, workbench);
		tab.initialize();
		return tab;
	}
}
