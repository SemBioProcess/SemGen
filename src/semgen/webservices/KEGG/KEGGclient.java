package semgen.webservices.KEGG;

import java.rmi.RemoteException;

import javax.xml.rpc.ServiceException;

public class KEGGclient {
	KEGGLocator  locator = new KEGGLocator();
    KEGGPortType serv;
    public KEGGclient() throws RemoteException{
		try {
			serv = locator.getKEGGPort();
<<<<<<< HEAD
			 Definition[] results = serv.list_organisms();
=======
			 String   query   = "phospho";
		     Definition[] results = serv.list_organisms(); //get_genes_by_pathway(query);
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
		     for (int i = 0; i < results.length; i++) {
	                System.out.println(results[i].getDefinition());
	        }
		} catch (ServiceException e) {
			e.printStackTrace();
		}
    }
}
