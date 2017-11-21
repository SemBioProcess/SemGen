package semgen.utilities.file;

import semgen.SemGen;
import semsim.model.collection.SemSimModel;
import semsim.reading.ModelAccessor;
import semsim.reading.ModelClassifier.ModelType;
import semsim.writing.CellMLwriter;
import semsim.writing.JSimProjectFileWriter;
import semsim.writing.MMLwriter;
import semsim.writing.OMEXArchiveWriter;
import semsim.writing.SBMLwriter;
import semsim.writing.SemSimOWLwriter;

public class SaveSemSimModel {

	public static void writeToFile(SemSimModel semsimmodel,
			ModelAccessor modelaccessor) {
		
			ModelType modeltype = modelaccessor.getFileType();
		try {
			if(modeltype==ModelType.SEMSIM_MODEL) {
				new SemSimOWLwriter(semsimmodel).writeToFile(modelaccessor);
			}
			else if(modeltype==ModelType.SBML_MODEL){
				new SBMLwriter(semsimmodel).writeToFile(modelaccessor);
			}
			else if(modeltype==ModelType.CELLML_MODEL){
				new CellMLwriter(semsimmodel).writeToFile(modelaccessor);
			}
			else if(modeltype==ModelType.MML_MODEL_IN_PROJ){
				new JSimProjectFileWriter(modelaccessor, semsimmodel).writeToFile(modelaccessor);
			}
			else if(modeltype==ModelType.MML_MODEL){
				new MMLwriter(semsimmodel).writeToFile(modelaccessor);
			}
			else if (modeltype==ModelType.OMEX_ARCHIVE) {
				new OMEXArchiveWriter(null);
			}
			
			SemGen.logfilewriter.println(modelaccessor.getShortLocation() + " was saved");

		} 
		catch (Exception e) {
			e.printStackTrace();
		}
		
	}

}
