package semgen.search;

import semgen.SemGen;
import semgen.stage.serialization.SearchResultSet;
import semsim.owl.SemSimOWLFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.*;

import javax.swing.JOptionPane;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class BioModelsSearch {
    public static final String SourceName = "BioModels";

    public static SearchResultSet bioModelsSearch(String searchString) {
        
        Set<String> results = new HashSet<String>();

		try {
	        
	        URL url = new URL("https://www.ebi.ac.uk/biomodels/search?query=" + 
	        		URLEncoder.encode(searchString, "UTF-8") +  
	        		"&sort=id-asc&format=xml");
	        
			System.out.println("Searching BioModels: " + url.toString());
			
	        SAXBuilder saxBuilder = new SAXBuilder();
	        Document xmldoc;
			xmldoc = saxBuilder.build(url);
			
			if(xmldoc.getRootElement().getChild("models") != null) {
	        	Element modelsel = xmldoc.getRootElement().getChild("models");
	        	
	        	// Go through model hits and collect BioModels IDs
	        	if(modelsel.getChildren("modelSummary").size()>0) {
	        		
	        		@SuppressWarnings("unchecked")
					Iterator<Element> modelsumit = modelsel.getChildren("modelSummary").iterator();
	        		
	        		while(modelsumit.hasNext()) {
	        			
	        			Element sumel = modelsumit.next();
	        			String id = sumel.getChild("id").getText();
	        			
	        			if(id!=null && ! id.isEmpty() && id.startsWith("BIOMD")) results.add(id); // Only include curated models
	        		}
	        	}
	        }
		} catch (JDOMException | IOException e) {
			JOptionPane.showMessageDialog(SemGen.getSemGenGUI(), "An error occurred searching BioModels\n" + 
					e.getClass() + "\n" + e.getMessage(),
							null, JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
        
        return new SearchResultSet(SourceName, results.toArray(new String[] {}));
    }

    
    @SuppressWarnings("hiding")
	public static <String> void addAllIfNotNull(Set<String> list, String[] c) {
        if (c != null) {
            list.addAll(Arrays.asList(c));
        }
    }

    
    // Download the SBML code
    public static String getModelSBMLById(String id) {
    	
            String result = "";
			try {
				
	            URL url = new URL("https://www.ebi.ac.uk/biomodels/model/download/" +
	            		id + "?filename=" + id + "_url.xml");
	            
	    		System.out.println("Downloading SBML: " + url.toString());

	    		URLConnection yc = url.openConnection();
                yc.setReadTimeout(60000); // Time out after a minute
                StringBuilder stringBuilder = new StringBuilder();
                BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream()));
                String inputLine;
                while ((inputLine = in.readLine()) != null)
                    stringBuilder.append(inputLine + '\n');
                result = stringBuilder.toString();
                in.close();
			} catch (IOException e) {
				JOptionPane.showMessageDialog(SemGen.getSemGenGUI(), "An error occurred downloading " + id + "\n" + 
						e.getClass() + "\n" + e.getMessage(),
								null, JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
			}
            
           return result;
    }
    

    // Look up PubMed ID associated with model, use NCBI efetch to get abstract
    public static String findPubmedAbstract(String modelId) {
        
        String abstr = "Could not retrieve abstract for " + modelId;
    	
    	try {
        	String restreply = "";

	    	URL url = new URL("https://www.ebi.ac.uk/biomodels/" + modelId + "?format=json");
	    	
	    	URLConnection yc = url.openConnection();
	        yc.setReadTimeout(60000); // Time out after a minute
	        StringBuilder stringBuilder = new StringBuilder();
	        BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream()));
	        String inputLine;
	        
	        while ((inputLine = in.readLine()) != null)
	            stringBuilder.append(inputLine + '\n');
	        
	        restreply = stringBuilder.toString();
	        in.close();
	        
	        // XML format doesn't seem to be working on BioModels' end, so we just Json here
	        byte[] jsonData = restreply.getBytes();
	    	ObjectMapper omapper = new ObjectMapper();
	        JsonNode rootNode = omapper.readTree(jsonData);
	        JsonNode linkNode = rootNode.path("publication").path("link");
	        
	        String pubmedlink = linkNode.asText();
	        String pubmedid = SemSimOWLFactory.getIRIfragment(pubmedlink);
	        
	        // Use efetch to get publication info
	        // Sometimes BioModelsWS will return non-PubmedIDs
	        if (!pubmedid.equals("") && pubmedid.matches("[0-9]+")) {
	        	
                URL url2 = new URL("https://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?db=pubmed&id=" + pubmedid + "&retmode=text&rettype=abstract");
                URLConnection yc2 = url2.openConnection();
                yc2.setReadTimeout(60000); // Tiemout after a minute
                StringBuilder stringBuilder2 = new StringBuilder();
                BufferedReader in2 = new BufferedReader(new InputStreamReader(yc2.getInputStream()));
                String inputLine2;
                while ((inputLine2 = in2.readLine()) != null)
                	stringBuilder2.append(inputLine2 + '\n');
                abstr = stringBuilder2.toString();
                in.close();
	        }

    	}
    	catch(IOException e) {
    		JOptionPane.showMessageDialog(SemGen.getSemGenGUI(), "An error occurred getting abstract for " + modelId + "\n" + 
					e.getClass() + "\n" + e.getMessage(),
							null, JOptionPane.ERROR_MESSAGE);
    		e.printStackTrace();
    	}
    	
        return abstr;
    }
}
