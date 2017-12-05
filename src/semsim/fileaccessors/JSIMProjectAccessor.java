package semsim.fileaccessors;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import org.jdom.Document;

import semsim.model.collection.SemSimModel;
import semsim.reading.JSimProjectFileReader;
import semsim.reading.ModelClassifier.ModelType;
import semsim.writing.JSimProjectFileWriter;
import semsim.writing.ModelWriter;

public class JSIMProjectAccessor extends ModelAccessor {
	
	// Use this constructor for models that are stored within JSim Project files
	protected JSIMProjectAccessor(File archiveFile, String modelNameInArchive){
			super(archiveFile, modelNameInArchive, ModelType.MML_MODEL_IN_PROJ);

	}
	
	// Copy constructor
	public JSIMProjectAccessor(JSIMProjectAccessor matocopy) {
		super(matocopy);
		fragment = matocopy.fragment;
	}

	public InputStream modelInStream() throws IOException{
		
		if(modelIsOnline()) return null;
		Document projdoc = getJDOMDocument();
		
		return JSimProjectFileReader.getModelSourceCode(projdoc, fragment);
	}
	
	@Override
	protected ModelWriter makeWriter(SemSimModel model) {
		return new JSimProjectFileWriter(model, this);
	}
	

	@Override
	public String getFullPath() {
		return new String(filepath + separator + fragment);
	}
	
	
	// If the model is in a standalone file, the name of the file is returned
	// otherwise a string with format [name of archive] > [name of model] is returned
	public String getShortLocation(){
		return fragment + '>'  + getFileName();
	}
	
	public String getModelName() {
		return fragment;
	}

	public ModelType getModelType() {
		return ModelType.MML_MODEL_IN_PROJ;
	}
}
