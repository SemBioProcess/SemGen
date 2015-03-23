package semgen.search;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import semgen.utilities.file.LoadSemSimModel;
import semsim.model.SemSimModel;
import semsim.model.computational.datastructures.DataStructure;

public class CompositeAnnotationSearch {
	// Given a keyword, do a string search on data structures from a set of annotated SemSim models.
	// Return a list of SemSim models containing the keyword.
	
	public static Set<SemSimModel> compositeAnnotationSearch(String keyword) {
		Set<SemSimModel> searchFrom = new HashSet<SemSimModel>();
		Set<SemSimModel> searchResults = new HashSet<SemSimModel>();
		
		// Turn "keyword" into regex
		String queryString = "";
		String queryArray[];
		queryArray = keyword.split(" ");
		for(String str : queryArray) {
			String newStr = "(?=.*" + str + ")";
			queryString += newStr;
		}
		Pattern p = Pattern.compile(queryString);
		
		System.out.println(queryString);
		
		// Get a list of annotated SemSim models to search from
		File sourceDir = new File("examples/AnnotatedModels");
		File[] directoryListing = sourceDir.listFiles();
		for(File file : directoryListing) {
			SemSimModel semsimmodel = LoadSemSimModel.loadSemSimModelFromFile(file, false);
			searchFrom.add(semsimmodel);
		}
		
		for(SemSimModel semsimmodel : searchFrom) {
			Boolean found = false;
			Set<DataStructure> dsSet = new HashSet<DataStructure>(semsimmodel.getDataStructures());
			for(DataStructure ds : dsSet) {
				if(ds.getPhysicalProperty().getPhysicalPropertyOf()!=null) { //Ignore DataStructures without physical entities
					String compAnn = ds.getCompositeAnnotationAsString(false);
					Matcher m = p.matcher(compAnn); // Still buggy for some reason. E.g., calcium && troponin.
					found = m.find();
					if(found) {
						System.out.println(compAnn);
						System.out.println(semsimmodel.getName());
						searchResults.add(semsimmodel);
						break;
					}
				}
			}			
		}
		return searchResults;
	}

}
