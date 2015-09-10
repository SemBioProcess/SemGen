package semsim.utilities.webservices;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import semsim.SemSimConstants;

public class WebserviceTester {
	
	public static Boolean testBioPortalWebservice(){
		SAXBuilder builder = new SAXBuilder();
		Document doc = null;
		try {
			URL url = new URL(
					"http://data.bioontology.org/ontologies/OPB?q&format=xml&apikey=" + SemSimConstants.BIOPORTAL_API_KEY);
			System.out.println("Testing: " + url);
			URLConnection yc = url.openConnection();
			yc.setRequestProperty("Accept", "application/xml");
			yc.setReadTimeout(60000); // Tiemout after a minute
			BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream()));
			doc = builder.build(in);
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} catch (JDOMException e) {
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
