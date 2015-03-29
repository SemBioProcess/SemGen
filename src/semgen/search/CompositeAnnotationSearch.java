package semgen.search;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import semgen.visualizations.JsonString;

//import semgen.utilities.file.LoadSemSimModel;
//import semsim.model.SemSimModel;
//import semsim.model.computational.datastructures.DataStructure;

public class CompositeAnnotationSearch {
	// Given a keyword, do a string search on data structures from a set of annotated SemSim models.
	// Return a list of SemSim models containing the keyword.
	
	public static JsonString compositeAnnotationSearch(String keyword) {
		// Set<SemSimModel> searchFrom = new HashSet<SemSimModel>();
		Set<String> searchResults = new HashSet<String>();
		
		// Turn "keyword" into regex
		String queryString = "";
		String queryArray[];
		queryArray = keyword.toLowerCase().split(" ");
		for(String str : queryArray) {
			String newStr = "(?=.*" + str + ")";
			queryString += newStr;
		}
		Pattern p = Pattern.compile(queryString);
		//System.out.println(queryString);
		
		// LOADING EACH SEMSIM MODEL TOOK TOO LONG.
		// INSTEAD WE HAVE A TEXT FILE OF ALL COMPOSITE ANNOTATIONS AND FREETEXT DESCRIPTIONS TO SEARCH FROM
		// Ex. some annotation; SemSimModelName
		//     ...
		
		// Get a list of annotated SemSim models to search from
//		File sourceDir = new File("examples/AnnotatedModels");
//		File[] directoryListing = sourceDir.listFiles();
//		for(File file : directoryListing) {
//			if(file.getName().endsWith(".owl")) {
//				SemSimModel semsimmodel = LoadSemSimModel.loadSemSimModelFromFile(file, false);
//				searchFrom.add(semsimmodel);
//			}
//		}
//		
//		for(SemSimModel semsimmodel : searchFrom) {
//			Boolean found = false;
//			Set<DataStructure> dsSet = new HashSet<DataStructure>(semsimmodel.getDataStructures());
//			for(DataStructure ds : dsSet) {
//				if(ds.getPhysicalProperty().getPhysicalPropertyOf()!=null) { //Ignore DataStructures without physical entities
//					String compAnn = ds.getCompositeAnnotationAsString(false);
//					String freeText = ds.getDescription();
//					System.out.println(compAnn + "; " + semsimmodel.getName());
//					System.out.println(freeText + "; " + semsimmodel.getName());
//					Matcher m = p.matcher(compAnn);
//					found = m.find();
//					if(found) {
//						searchResults.add(semsimmodel);
//						break;
//					}
//				}
//			}			
//		}
		
		try {
			Scanner annotationFile = new Scanner(new File("examples/AnnotatedModels/Annotations_FULL_LIST.txt"));
			if(annotationFile.hasNext()) {
				String wholeline = "";
				String modelName = "";
				Boolean found = false;
				int delimIndex = 0;
				while(annotationFile.hasNext()) {
					wholeline = annotationFile.nextLine();
					Matcher m = p.matcher(wholeline.toLowerCase());
					found = m.find();
					if(found) {
						delimIndex = wholeline.indexOf(";");
						modelName = wholeline.substring(delimIndex+2, wholeline.length());
						searchResults.add(modelName);
					}
				}
			}
			annotationFile.close();
		} catch (FileNotFoundException e) {
			System.out.println("Annotation file not found!");
			e.printStackTrace();
		}
		
		// Turn the set into a json array
		return new JsonString("[\"" + StringUtils.join(searchResults, "\",\"") + "\"]");
	}

}
