package semgen.utilities;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import semsim.utilities.ResourcesManager;

public class OntologyCache {

	private File ontologyTermsAndNamesCacheFile = new File("cfg/ontologyTermsAndNamesCache.txt");
	private HashMap<String, String[]> ontologyTermsAndNamesCache;
	
	public OntologyCache() {
		try {
			ontologyTermsAndNamesCache = ResourcesManager.createHashMapFromFile("cfg/ontologyTermsAndNamesCache.txt");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public HashMap<String, String[]> getOntTermsandNamesCache() {
		return ontologyTermsAndNamesCache;
	}
	
	public void storeCachedOntologyTerms(){
		try {
			PrintWriter writer = new PrintWriter(new FileWriter(ontologyTermsAndNamesCacheFile));
			for(String key : ontologyTermsAndNamesCache.keySet()){
				writer.println(key + "; " + ontologyTermsAndNamesCache.get(key)[0]);
			}
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
