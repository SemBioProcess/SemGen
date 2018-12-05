package semsim.utilities.webservices;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import semsim.SemSimLibrary;
import semsim.annotation.Ontology;

/**
 * Class for connecting to the BioPortal search service
 * and performing string searches over ontology classes.
 * @author mneal
 *
 */
public class BioPortalSearcher {
	private static final ObjectMapper JSON_OBJECT_MAPPER = new ObjectMapper();
	private static final String BIOPORTAL_API_KEY = "c4192e4b-88a8-4002-ad08-b4636c88df1a";
	
	
	/**
	 * Execute a string search over an ontology on BioPortal
	 * @param lib A SemSimLibrary instance
	 * @param text The search string
	 * @param bioportalNickName BioPortal nickname of ontology to search
	 * @param exactmatch Only retrieve exact matches to string
	 * @return A map that links the human-readable name of a class that matched
	 * the search string to its URI
	 * @throws IOException
	 * @throws JDOMException
	 */
	public HashMap<String,String> search(SemSimLibrary lib, String text, String bioportalNickName, int exactmatch) throws IOException, JDOMException{
		text = text.replace(" ", "+");
		
		boolean exactmatchbool = exactmatch==1; 
		
		URL url;
		if (exactmatch==2) {
			url = new URL(
					"http://data.bioontology.org/search?q=" + bioportalNickName + ":"
							+ text + "&ontologies="
							+ bioportalNickName + "&format=xml" + "&include=prefLabel,synonym,definition,notation,cui,semanticType,properties"
							+ "&apikey=" + BIOPORTAL_API_KEY + "&also_search_properties=true");

		}
		else {
					url = new URL(
				"http://data.bioontology.org/search?q="
						+ text + "&ontologies="
						+ bioportalNickName + "&format=xml" + "&exact_match=" + exactmatchbool
						+ "&apikey=" + BIOPORTAL_API_KEY);

		}
		
		System.out.println(url);
		URLConnection yc = url.openConnection();
		yc.setRequestProperty("Accept", "application/xml");
		yc.setReadTimeout(60000); // Timeout after a minute
		BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream()));
		Document doc = new SAXBuilder().build(in);
		in.close();
		
		HashMap<String,String> rdflabelsanduris = new HashMap<String,String>();
		// Process XML results from BioPortal REST service
		if (doc!=null) {
			if(doc.getRootElement().getName().equals("nilClass"))
				System.out.println("No matches found for " + text);
			else{
				List<?> resultlist = (List<?>)doc.getRootElement().getChild("collection").getChildren("class");
				Iterator<?> resultsiterator = resultlist.iterator();

				while (resultsiterator.hasNext()) {
					Element nextel = (Element) resultsiterator.next();
					String preferredLabel = nextel.getChildText("prefLabel");
					String uri = nextel.getChildText("id");
	
					// Only collect terms from the queried ontology; don't show terms imported from other ontologies
					Ontology ont = lib.getOntologyfromTermURI(uri);
					
					if(ont.getNickName().equals(bioportalNickName) && preferredLabel != null){
						rdflabelsanduris.put(preferredLabel, uri);
					}

				}
			}
		}
		return rdflabelsanduris;
	}
	
	
	/**
	 * Look up the human-readable class name for a given URI
	 * @param encodeduri Class URI as a URL-encoded String
	 * @param bioportalontID BioPortal ID of the ontology containing the URI 
	 * @return The human-readable name of the class represented by the input URI
	 */
	public static String getRDFLabelUsingBioPortal(String encodeduri, String bioportalontID){
		String label = null;

		try {
			System.out.println("Looking up " + encodeduri);
			URL url = new URL(
					"http://data.bioontology.org/ontologies/" + bioportalontID + "/classes/" + encodeduri);
			System.out.println(url);
	        HttpURLConnection conn;
	        BufferedReader rd;
	        String line;
	        String result = "";
	        conn = (HttpURLConnection) url.openConnection();
	        conn.setRequestMethod("GET");
	        conn.setRequestProperty("Authorization", "apikey token=" + BIOPORTAL_API_KEY);
	        conn.setRequestProperty("Accept", "application/json");
			conn.setReadTimeout(60000); // Timeout after a minute
			
	        rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
	        while ((line = rd.readLine()) != null) {
	            result += line;
	        }
	        rd.close();
	        
	        // process resulting input stream
            JsonNode root = JSON_OBJECT_MAPPER.readTree(result);
            JsonNode labelnode = root.get("prefLabel");
            
            if(labelnode != null) label = labelnode.asText();
		}
        catch (IOException e) {
			e.printStackTrace();
		}
		
		return label;
	}
	
	
	/** @return Whether BioPortal responded to a test query */
	public static Boolean testBioPortalWebservice(){
		SAXBuilder builder = new SAXBuilder();
		Document doc = null;
		try {
			URL url = new URL(
					"http://data.bioontology.org/ontologies/OPB?q&format=xml&apikey=" + BIOPORTAL_API_KEY);
			System.out.println("Testing: " + url);
			URLConnection yc = url.openConnection();
			yc.setRequestProperty("Accept", "application/xml");
			yc.setReadTimeout(60000); // Timeout after a minute
			BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream()));
			doc = builder.build(in);
			in.close();
		} catch (IOException | JDOMException e) {
			e.printStackTrace();
			return false;
		}

		// Process XML results from BioPortal REST service to see if we're online
		if (doc!=null) {
			if(doc.getRootElement()!=null){
				if(doc.getRootElement().getName().equals("ontology")){
					System.out.println("Received response from BioPortal");
					return true;
				}
			}
		}
		return false;
	}
}
