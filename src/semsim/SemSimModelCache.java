package semsim;


import java.util.HashMap;
import java.util.Map;

import semsim.model.SemSimModel;

public class SemSimModelCache {
	
	// Local cache of semsim models
	private Map<String,SemSimModel> filePathsAndSemSimModels = new HashMap<String, SemSimModel>();
	
	public SemSimModel getCachedSemSimModelFromFilePath(String filepath){
		return filePathsAndSemSimModels.containsKey(filepath) ? 
				filePathsAndSemSimModels.get(filepath) : null;
	}
	
	public void cacheSemSimModelAndAssociatedFilePath(SemSimModel semsimmodel, String filepath){
		filePathsAndSemSimModels.put(filepath, semsimmodel);
		System.out.println();
	}
	
	public Map<String,SemSimModel> getFilePathToSemSimModelMap(){
		return filePathsAndSemSimModels;
	}
}
