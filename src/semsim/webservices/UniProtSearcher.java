package semsim.webservices;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
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


public class UniProtSearcher {
	public static Namespace ns = Namespace.getNamespace("xs", "http://uniprot.org/uniprot");
	public Hashtable<String,String> classnamesanduris = new Hashtable<String,String>();
	public Hashtable<String,String> rdflabelsanduris = new Hashtable<String,String>();
	
	public Map<String,String> search(String thestring) throws JDOMException, IOException{
		Map<String,String> idnamemap = new HashMap<String,String>();
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
			
			String uristring = "http://identifiers.org/uniprot/" + id;
			classnamesanduris.put(name, uristring);
			rdflabelsanduris.put(name, uristring);
		}
		s.close();
		return idnamemap;
	}
	
	
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
	
	public static InputStream getInputStreamFromURL(URL url) throws JDOMException, IOException{
		URLConnection yc = url.openConnection();
		yc.setReadTimeout(60000); // Tiemout after a minute
		return yc.getInputStream();
	}
	
	
}
