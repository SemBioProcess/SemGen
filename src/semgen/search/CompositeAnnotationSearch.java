package semgen.search;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import semgen.stage.serialization.SearchResultSet;


public class CompositeAnnotationSearch {
	public static final String SourceName = "Examples";

	// Given a keyword, do a string search on data structures from a set of annotated SemSim models.
	// Return a list of SemSim model names containing the keyword.
	public static String[] compositeAnnotationSearch(String searchString) throws FileNotFoundException {
		List<Set<String>> compareResults = new ArrayList<Set<String>>(); 
		String queryArray[];
		queryArray = searchString.toLowerCase().split(" ");

		for(String keyword : queryArray) {
			Set<String> searchResults = new HashSet<String>();
			Scanner annotationFile = new Scanner(new File("examples/AnnotatedModels/Annotations_FULL_LIST.txt"));
			if(annotationFile.hasNext()) {
				String wholeline = "";
				String modelName = "";
				Boolean found = false;
				int delimIndex = 0;
				while(annotationFile.hasNext()) {
					wholeline = annotationFile.nextLine();
					found = wholeline.toLowerCase().contains(keyword);
					if(found) {
						delimIndex = wholeline.indexOf(";");
						modelName = wholeline.substring(delimIndex+2, wholeline.length());
						searchResults.add(modelName);
					}
				}
			}
			// Store the list of models found for each keyword
			compareResults.add(searchResults);
			annotationFile.close();
		}
		// Find the intersection of the results for each keyword
		Set<String> finalResults = new HashSet<String>();
		for(Set<String> resultSet : compareResults) {
			finalResults = compareResults.get(0);
			finalResults.retainAll(resultSet);
		}

		return finalResults.toArray(new String[finalResults.size()]);
	}
}
