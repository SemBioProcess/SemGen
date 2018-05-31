package semsim.utilities.webservices;

import java.util.ArrayList;
import java.util.Scanner;
import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import javax.xml.namespace.QName;

/**
 * Class for sending queries to BRENDA webservices
 * @author mneal
 *
 */
public class BRENDAwebservice {
	
	public static ArrayList<String> getGOxrefsFromID(String eznum){
		ArrayList<String> GOxrefs = new ArrayList<String>();
		try {
			ArrayList<String> recnames = getRecommendedNameAndGOxrefFromID(eznum);
			for(String recname : recnames){
				GOxrefs.add(recname.substring(recname.lastIndexOf("*")+1,recname.lastIndexOf("#")));
			}
		} catch (Exception e) {e.printStackTrace();} 
		
		return GOxrefs;
	}
	
	
	/**
	 * Collect recommended names from an enzyme ID
	 * @param eznum The enzyme ecNumber
	 * @return An ordered list of recommended names for the enzyme
	 */
	public static ArrayList<String> getRecommendedNamesFromID(String eznum){
		ArrayList<String> names = new ArrayList<String>();
		ArrayList<String> recnames = getRecommendedNameAndGOxrefFromID(eznum);
		for(String recname : recnames){
			recname = recname.substring(0,recname.lastIndexOf("#goNumber"));
			names.add(recname.substring(recname.lastIndexOf("*"),recname.length()));
		}
		return names;
	}
	
	
    /**
     * Get the recommended names and Gene Ontology cross-refs for an enzyme
     * @param eznum The ecNumber of the enzyme
     * @return Ordered list of Strings that contain recommended names and Gene Ontology cross-refs
     */
	public static ArrayList<String> getRecommendedNameAndGOxrefFromID(String eznum) {
		System.out.println("Calling BRENDA webservice");
		ArrayList<String> result = new ArrayList<String>();
		
		Service service = new Service();

		try {

			Call call = (Call) service.createCall();

			String endpoint = "http://www.brenda-enzymes.org/soap2//brenda_server.php";

			call.setTargetEndpointAddress( new java.net.URL(endpoint) );

			call.setOperationName(new QName("http://soapinterop.org/", "getRecommendedName"));

			String resultstring = (String) call.invoke( new Object[] {"ecNumber*" + eznum} );
				
			Scanner scanner = new Scanner(resultstring);
			scanner.useDelimiter("!");
			while(scanner.hasNext()){
			  result.add(scanner.next());
			}
			scanner.close();
		} catch (Exception e) {e.printStackTrace();}
		return result;
   }
}