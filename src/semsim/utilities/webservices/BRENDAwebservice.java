package semsim.utilities.webservices;

import java.util.ArrayList;
import java.util.Scanner;
import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import javax.xml.namespace.QName;

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
	
	
	public static ArrayList<String> getRecommendedNamesFromID(String eznum){
		ArrayList<String> names = new ArrayList<String>();
		ArrayList<String> recnames = getRecommendedNameAndGOxrefFromID(eznum);
		for(String recname : recnames){
			recname = recname.substring(0,recname.lastIndexOf("#goNumber"));
			names.add(recname.substring(recname.lastIndexOf("*"),recname.length()));
		}
		return names;
	}
	
	
       
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