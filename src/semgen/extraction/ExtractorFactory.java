package semgen.extraction;

import java.io.File;

import org.semanticweb.owlapi.model.OWLException;

import semgen.GlobalActions;
import semgen.SemGenSettings;
import semgen.resource.file.SemGenOpenFileChooser;

public class ExtractorFactory {
	SemGenSettings settings;
	GlobalActions globalactions;
	public ExtractorFactory(SemGenSettings sets, GlobalActions gacts) {
		settings = sets;
		globalactions = gacts;
	}
	
	public ExtractorTab makeTab() {
		final SemGenOpenFileChooser sgc =  new SemGenOpenFileChooser("Extractor - Select source SemSim model",
				new String[]{"owl"} );
		File file = sgc.getSelectedFile();
		if (file == null) return null;
		
		return CreateExtractorTab(sgc.getSelectedFile());
	}	
	
	public ExtractorTab makeTab(File existing) {
		return CreateExtractorTab(existing);
	}

	private ExtractorTab CreateExtractorTab(File file) {
		ExtractorTab tab = null;
		try {
			tab = new ExtractorTab(file, settings, globalactions);
		} catch (OWLException e) {
			e.printStackTrace();
		}
		if (!tab.initialize()) return null;
		return tab;
	}
}
