package semgen.webservices.KEGG;

import java.rmi.RemoteException;

import javax.xml.rpc.ServiceException;

public class KEGGclient {
	KEGGLocator  locator = new KEGGLocator();
    KEGGPortType serv;
    public KEGGclient() throws RemoteException{
		try {
			serv = locator.getKEGGPort();
			 String   query   = "phospho";
		     Definition[] results = serv.list_organisms(); //get_genes_by_pathway(query);
		     for (int i = 0; i < results.length; i++) {
	                System.out.println(results[i].getDefinition());
	        }
		} catch (ServiceException e) {
			e.printStackTrace();
		}
    }
}
