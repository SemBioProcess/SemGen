package semsim.fileaccessors;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import org.jdom.JDOMException;

import semsim.reading.ModelClassifier;
import semsim.reading.ModelClassifier.ModelType;

/**
 * Class for creating {@link ModelAccessor} instances from model files
 * @author mneal
 *
 */
public class FileAccessorFactory {

	/**
	 * Create a {@link ModelAccessor} from a File
	 * @param file The model file
	 * @return A {@link ModelAccessor} linked to the file
	 */
	public static ModelAccessor getModelAccessor(File file) {
		return getModelAccessor(file.getPath());
	}
	
	/**
	 * Create a {@link ModelAccessor} from a File and specify the {@link ModelType} as well
	 * @param file The model file
	 * @param type The type of model
	 * @return A {@link ModelAccessor} linked to the file
	 */
	public static ModelAccessor getModelAccessor(File file, ModelType type) {

		return new ModelAccessor(file, type);
	}
	
	/**
	 * Create a {@link ModelAccessor} from a file path or web location
	 * @param location The location of the model file
	 * @return A {@link ModelAccessor} linked to the file
	 */
	public static ModelAccessor getModelAccessor(String location) {
		String path = location;
		if (isOMEXArchive(location)) {
			return getOMEXArchive(location);
		}
		
		boolean hasseperator = location.contains(ModelAccessor.separator);
		String frag = null;
		if(hasseperator){
			path = location.substring(0, location.indexOf(ModelAccessor.separator));
			frag = location.substring(location.indexOf(ModelAccessor.separator) + 1, location.length());
		}
		
		ModelAccessor accessor = null;
		ModelType type = classifyFile(path);
		if(location.startsWith("http"))
			return new ModelAccessor(URI.create(location), type);
		
		else if(location.startsWith("file:"))
			return new ModelAccessor(new File(location), type);
		
		else if (hasseperator) {

			if (type.equals(ModelType.MML_MODEL_IN_PROJ)) {
				return new JSimProjectAccessor(new File(path), frag);
			}
			
			accessor = new ModelAccessor(new File(path), frag, type);
		}
		
		else accessor = new ModelAccessor(new File(location), type);
		
		return accessor;
	}
	
	/**
	 * Create an {@link OMEXAccessor} instance for a model in a COMBINE archive
	 * @param archive The archive file
	 * @param file The model file within the archive
	 * @param type The model type
	 * @return An {@link OMEXAccessor} linked to the model
	 */
	public static OMEXAccessor getOMEXArchive(File archive, File file, ModelType type) {
		return new OMEXAccessor(archive, file, type);
	}
	
	/**
	 * Create a {@link JSimProjectAccessor} from a give file
	 * @param file A project file
	 * @param fragment The name of the model within the project file
	 * @return A {@link JSIMprojectAcccessor} linked to the project/model
	 */
	public static JSimProjectAccessor getJSIMprojectAccessor(File file, String fragment) {
		return new JSimProjectAccessor(file, fragment);
	}
	
	/**
	 * Create an {@link OMEXAccessor} from a path to the model within an archive
	 * @param fullpath The path to the model in the archive
	 * @return An {@link OMEXAccessor} linked to the model
	 */
	private static OMEXAccessor getOMEXArchive(String fullpath) {
		int extindex = fullpath.lastIndexOf(".omex");
		String modelpath = fullpath.substring(extindex + 6, fullpath.length());
		ModelType type = classifyFile(modelpath);
		return getOMEXArchive(new File(fullpath.substring(0, extindex+5 )), new File(modelpath), type);
	}
	
	/**
	 * Determine the type of model indicated from its file path
	 * @param standAloneFile Path to the model
	 * @return The model type
	 */
	private static ModelType classifyFile(String standAloneFile) {
		ModelType modeltype = ModelType.UNKNOWN;
		try {
			modeltype = ModelClassifier.classify(standAloneFile);
		} catch (JDOMException | IOException e) {
			e.printStackTrace();
		}
		return modeltype;
	}
	
	/**
	 * Determines whether a given file path is for an {@link OMEXArchive}
	 * @param path File path
	 * @return Whether the file path points to an {@link OMEXArchive}
	 */
	private static boolean isOMEXArchive(String path) {
		return path.contains(".omex");
	}
	
}
