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

/**
 * Class containing functions for accessing the model contents
 * within a JSim project file
 * @author mneal
 *
 */
public class JSimProjectAccessor extends ModelAccessor {
	
	// Use this constructor for models that are stored within JSim Project files
	/**
	 * Constructor for models that are stored within JSim project files
	 * @param archiveFile Location of project file
	 * @param modelNameInArchive Name of model in the project file
	 */
	protected JSimProjectAccessor(File archiveFile, String modelNameInArchive){
			super(archiveFile, modelNameInArchive, ModelType.MML_MODEL_IN_PROJ);

	}
	
	/**
	 * Copy contructor
	 * @param matocopy The JSimProjectAccessor to copy
	 */
	public JSimProjectAccessor(JSimProjectAccessor matocopy) {
		super(matocopy);
		fragment = matocopy.fragment;
	}

	/** Creates an InputStream for the model in the project file */
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
	public String getFullPathToModel() {
		return new String(filepath + separator + fragment);
	}
	
	/**
	 * @return If the model is in a standalone file, the name of the file is returned
	 * otherwise a string with format [name of archive] &gt; [name of model] is returned
	 */
	public String getShortLocation(){
		return fragment + '>'  + getFileName();
	}
	
	/** @return The name of the model in the project file that the JSimProjectAccessor points to */
	public String getModelName() {
		return fragment;
	}

	/** @return The type of model pointed to by the JSimProjectAccessor */
	public ModelType getModelType() {
		return ModelType.MML_MODEL_IN_PROJ;
	}
}
