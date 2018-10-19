package semsim.utilities.webservices;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Scanner;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import org.jdom.Content;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;

/**
 * Class for sending queries to UniProt webservices
 * @author mneal
 *
 */
public class UniProtSearcher {
	public static Namespace ns = Namespace.getNamespace("xs", "http://uniprot.org/uniprot");
	
	/**
	 * Perform a String search over reviewed proteins in UniProt
	 * @param thestring Search string
	 * @return A map that links the names of proteins matching the search
	 * criteria to their UniProt URI (in identifiers.org format). 
	 * @throws JDOMException
	 * @throws IOException
	 */
	public HashMap<String,String> search(String thestring) throws JDOMException, IOException{
		HashMap<String,String> idnamemap = new HashMap<String,String>();
		thestring = thestring.replace(" ", "%20");
		URL url = new URL("http://www.uniprot.org/uniprot/?query=reviewed:yes+AND+name:" + thestring + "*&format=tab&columns=id,protein%20names");
		System.out.println(url.toString());
		// Use +AND+created:[current TO *] ??? (created in the current UniProtKB/Swiss-Prot release)
		
		InputStream is = getInputStreamFromURL(url);
		Scanner s = new Scanner(is);
		s.useDelimiter("\\A");
		while(s.hasNext()){
			String line = s.nextLine();
			String id = line.substring(0,line.indexOf("\t"));
			String name = line.substring(line.indexOf("\t"),line.length());
			
			// append name with ID because sometimes UNIPROT names aren't unique
			name = name + " (" + id + ")";
			name = name.trim();
			
			String uristring = "https://identifiers.org/uniprot/" + id;
			idnamemap.put(name, uristring);
		}
		s.close();
		return idnamemap;
	}
	
	
	/**
	 * Retrieve the preferred name for a protein
	 * @param ID UniProt ID of the protein
	 * @return The preferred name of the protein
	 * @throws IOException
	 * @throws JDOMException
	 */
	public static String getPreferredNameForID(String ID) throws IOException, JDOMException{
		String name = null;
		URL url = new URL("http://www.uniprot.org/uniprot/" + ID + ".xml");
		BufferedReader in = new BufferedReader(new InputStreamReader(getInputStreamFromURL(url)));
		Document doc = new SAXBuilder().build(in);
		in.close();
		
		// Process XML results from REST service
		if (doc!=null) {
			Iterator<?> x = doc.getDescendants();
			
			while(x.hasNext()){
				Content con = (Content) x.next();
				if(con instanceof Element){
					Element el = (Element)con;
					if(el.getName().equals("recommendedName")){
						// If we find the recommended name, use it
						// Otherwise the submittedName, if found, will be used
						return el.getChildText("fullName",ns);
					}
					if(el.getName().equals("submittedName")){
						name = el.getChildText("fullName", ns);
					}
				}
			}
		}
		return name;
	}
	
	
	/**
	 * Create an InputStream object for connecting to a URL
	 * @param url The URL
	 * @return The InputStream object delivering the contents of the URL
	 * @throws JDOMException
	 * @throws IOException
	 */
	public static InputStream getInputStreamFromURL(URL url) throws JDOMException, IOException{
		URLConnection yc = url.openConnection();
		yc.setReadTimeout(60000); // Tiemout after a minute
		return yc.getInputStream();
	}
}