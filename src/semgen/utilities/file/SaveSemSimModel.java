package semgen.utilities.file;

import java.io.File;

import javax.swing.filechooser.FileFilter;

import semgen.SemGen;
import semsim.model.collection.SemSimModel;
import semsim.reading.ModelAccessor;
import semsim.reading.ModelClassifier;
import semsim.writing.CellMLwriter;
import semsim.writing.JSimProjectFileWriter;
import semsim.writing.SemSimOWLwriter;

public class SaveSemSimModel {

	public static void writeToFile(SemSimModel semsimmodel,
			ModelAccessor modelaccessor, File file, int modelformat) {
		
		try {
			if(modelformat==ModelClassifier.SEMSIM_MODEL) {
				new SemSimOWLwriter(semsimmodel).writeToFile(file);
			}
			else if(modelformat==ModelClassifier.CELLML_MODEL){
				new CellMLwriter(semsimmodel).writeToFile(file);
			}
			else if(modelformat==ModelClassifier.MML_MODEL_IN_PROJ){
				new JSimProjectFileWriter(modelaccessor, semsimmodel).writeToFile(file);
			}
			
			SemGen.logfilewriter.println(modelaccessor.getShortLocation() + " was saved");

		} 
		catch (Exception e) {e.printStackTrace();
		}
		
	}
	
	public static void writeToFile(SemSimModel semsimmodel,
			ModelAccessor modelaccessor, File file, FileFilter filter){
		
		int format = -1;
		if(filter == SemGenFileChooser.projfilter){
			format = ModelClassifier.MML_MODEL_IN_PROJ;
		}
		else if(filter == SemGenFileChooser.owlfilter){
			format = ModelClassifier.SEMSIM_MODEL;
		}
		else if(filter == SemGenFileChooser.cellmlfilter){
			format = ModelClassifier.CELLML_MODEL;
		}
		
		writeToFile(semsimmodel, modelaccessor, file, format);
		
	}

}
