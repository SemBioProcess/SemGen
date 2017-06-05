package semgen.utilities.file;

import java.io.File;

import javax.swing.filechooser.FileFilter;

import semgen.SemGen;
import semsim.model.collection.SemSimModel;
import semsim.reading.ModelAccessor;
import semsim.reading.ModelClassifier.ModelType;
import semsim.writing.CellMLwriter;
import semsim.writing.JSimProjectFileWriter;
import semsim.writing.MMLwriter;
import semsim.writing.SBMLwriter;
import semsim.writing.SemSimOWLwriter;

public class SaveSemSimModel {

	public static void writeToFile(SemSimModel semsimmodel,
			ModelAccessor modelaccessor, File outputfile, ModelType modeltype) {
		
		try {
			if(modeltype==ModelType.SEMSIM_MODEL) {
				new SemSimOWLwriter(semsimmodel).writeToFile(outputfile);
			}
			else if(modeltype==ModelType.SBML_MODEL){
				new SBMLwriter(semsimmodel).writeToFile(outputfile);
			}
			else if(modeltype==ModelType.CELLML_MODEL){
				new CellMLwriter(semsimmodel).writeToFile(outputfile);
			}
			else if(modeltype==ModelType.MML_MODEL_IN_PROJ){
				new JSimProjectFileWriter(modelaccessor, semsimmodel).writeToFile(outputfile);
			}
			else if(modeltype==ModelType.MML_MODEL){
				new MMLwriter(semsimmodel).writeToFile(outputfile);
			}
			
			SemGen.logfilewriter.println(modelaccessor.getShortLocation() + " was saved");

		} 
		catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public static void writeToFile(SemSimModel semsimmodel,
			ModelAccessor modelaccessor, File outputfile, FileFilter filter){
		
		ModelType format = ModelType.UNKNOWN;
		
		if(filter == SemGenFileChooser.projfilter){
			format = ModelType.MML_MODEL_IN_PROJ;
		}
		else if(filter == SemGenFileChooser.owlfilter){
			format = ModelType.SEMSIM_MODEL;
		}
		else if(filter == SemGenFileChooser.cellmlfilter){
			format = ModelType.CELLML_MODEL;
		}
		else if(filter == SemGenFileChooser.sbmlfilter){
			format = ModelType.SBML_MODEL;
		}
		else if(filter == SemGenFileChooser.mmlfilter){
			format = ModelType.MML_MODEL;
		}
		
		writeToFile(semsimmodel, modelaccessor, outputfile, format);
		
	}

}
