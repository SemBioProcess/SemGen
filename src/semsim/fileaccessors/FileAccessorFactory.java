package semsim.fileaccessors;

import java.io.File;
import java.io.IOException;

import org.jdom.JDOMException;

import semsim.reading.ModelClassifier;
import semsim.reading.ModelClassifier.ModelType;

public class FileAccessorFactory {

	public static ModelAccessor getModelAccessor(File file) {
		return getModelAccessor(file.getPath());
	}
	
	public static ModelAccessor getModelAccessor(File file, ModelType type) {

		return new ModelAccessor(file, type);
	}
	
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
		if(location.startsWith("http://") || location.startsWith("file:")) new ModelAccessor(new File(location), type);
		
		else if (hasseperator) {

			if (type.equals(ModelType.MML_MODEL_IN_PROJ)) {
				return new JSIMProjectAccessor(new File(path), frag);
			}
			
			accessor = new ModelAccessor(new File(path), frag, type);
		}
		
		else accessor = new ModelAccessor(new File(location), type);
		
		return accessor;
	}
	
	public static OMEXAccessor getOMEXArchive(File archive, File file, ModelType type) {
		return new OMEXAccessor(archive, file, type);
	}
	
	public static JSIMProjectAccessor getJSIMProjectAccessor(File file, String fragment) {
		return new JSIMProjectAccessor(file, fragment);
	}
	
	private static OMEXAccessor getOMEXArchive(String fullpath) {
		int extindex = fullpath.lastIndexOf(".omex");
		String modelpath = fullpath.substring(extindex + 6, fullpath.length());
		ModelType type = classifyFile(modelpath);
		return getOMEXArchive(new File(fullpath.substring(0, extindex+5 )), new File(modelpath), type);
	}
	
	private static ModelType classifyFile(String standAloneFile) {
		ModelType modeltype = ModelType.UNKNOWN;
		try {
			modeltype = ModelClassifier.classify(standAloneFile);
		} catch (JDOMException | IOException e) {
			e.printStackTrace();
		}
		return modeltype;
	}
	
	private static boolean isOMEXArchive(String path) {
		return path.contains(".omex");
	}
	
}
