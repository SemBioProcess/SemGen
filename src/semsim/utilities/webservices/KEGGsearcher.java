package semsim.utilities.webservices;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Scanner;

/**
 * Class for sending queries to KEGG webservices
 * @author mneal
 *
 */
public class KEGGsearcher {

	/**
	 * Use KEGG webservice to find a human-readable name for a KEGG ID
	 * @param ID A KEGG ID
	 * @return The human-readable name associated with the ID
	 * @throws IOException
	 */
	public static String getNameForID(String ID) throws IOException{
		String name = null;
		URL url = new URL("http://rest.kegg.jp/get/" + ID);
		System.out.println(url);
		URLConnection yc = url.openConnection();
		yc.setReadTimeout(60000); // Tiemout after a minute
		try{
			Scanner scanner = new Scanner(new InputStreamReader(yc.getInputStream()));
			Boolean cont = true;
			while(scanner.hasNext() && cont){
				String line = scanner.nextLine();
				if(line.startsWith("NAME")){
					name = line.replace("NAME", "");
					name = name.trim();
					if(name.contains(";")) name = name.replaceAll(";","");
					cont = false;
				}
			}
			scanner.close();
		}
		catch(FileNotFoundException e){e.printStackTrace();}
		return name;
	}
}
