package semgen.webservices.KEGG;

import java.rmi.RemoteException;

import javax.xml.rpc.ServiceException;

public class KEGGclient {
	KEGGLocator  locator = new KEGGLocator();
    KEGGPortType serv;
    public KEGGclient() throws RemoteException{
		try {
			serv = locator.getKEGGPort();
			 Definition[] results = serv.list_organisms();
		     for (int i = 0; i < results.length; i++) {
	                System.out.println(results[i].getDefinition());
	        }
		} catch (ServiceException e) {
			e.printStackTrace();
		}
    }
}
