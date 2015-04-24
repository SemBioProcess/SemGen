package semgen.utilities;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Hashtable;

import semsim.utilities.ResourcesManager;

public class OntologyCache {

	private File ontologyTermsAndNamesCacheFile = new File("cfg/ontologyTermsAndNamesCache.txt");
	private Hashtable<String, String[]> ontologyTermsAndNamesCache;
	
	public OntologyCache() {
		try {
			ontologyTermsAndNamesCache = ResourcesManager.createHashtableFromFile("cfg/ontologyTermsAndNamesCache.txt");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public Hashtable<String, String[]> getOntTermsandNamesCache() {
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
