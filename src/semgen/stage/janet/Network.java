package semgen.stage.janet;

import java.net.*;
import java.util.regex.Matcher;
import java.io.*;

import org.json.simple.*;
import org.json.simple.parser.JSONParser;
import org.apache.commons.codec.binary.StringUtils;
import org.apache.commons.io.IOUtils;


import org.apache.http.HttpConnection;
import org.apache.http.HttpEntity;
//import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;


public class Network {

    @SuppressWarnings("deprecation")
	
    public static String fetchedData(String args) throws Exception
    {
    	
    	
    	  HttpConnection c=null;
    	  HttpClient httpClient = HttpClientBuilder.create().build(); //Use this instead 
    	  
    	  String json = "";
    
    	  Boolean resultBool;
    	  
    	  String linksArea = null;
    	  String httpText = null;

    	    try {
    	    	System.out.println("Inside 1 ");
    	    	//System.out.println("args =  " + args);
    	    	//System.exit(0);
    	        //HttpPost request = new HttpPost("http://yoururl");
    	        HttpPost request = new HttpPost("http://staging.physiomeproject.org/pmr2_ricordo/query");
    	       // request.addHeader("Accept", "application/vnd.physiome.pmr2.json.0");
    	        request.addHeader("Accept", "application/vnd.physiome.pmr2.json.1");
    	        
    	        //StringEntity params =new StringEntity("details={\"name\":\"myname\",\"age\":\"20\"} ");
    	        
    	        
    	        //StringEntity params =new StringEntity("{\"fields\":\"simple_query\":\"FMA_9611\", \"actions\": \"search\":\"1\"\} ");
    	         //below works
    	         //StringEntity params =new StringEntity("{\"fields\": {\"simple_query\": \"FMA_9611\"}, \"actions\": {\"search\": \"1\"}}");
    	         //CHEBI_37153
    	        String stringToRemote = "{\"template\": {\"data\": [{\"name\": \"form.widgets.simple_query\", \"value\": \"" + args + "\"}, {\"name\": \"form.buttons.search\", \"value\": 1}]}}";
    	        // StringEntity params =new StringEntity("{\"template\": {\"data\": [{\"name\": \"form.widgets.simple_query\", \"value\": \"OPB_01023\"}, {\"name\": \"form.buttons.search\", \"value\": 1}]}}" );
    	        StringEntity params =new StringEntity(stringToRemote);
    	         
    	        
    	        
    	        //params.setContentType("application/vnd.physiome.pmr2.json.0");
    	        request.setEntity(params);
    	        HttpResponse response = httpClient.execute(request);
    	        
    	        HttpEntity httpEntity = response.getEntity();
    	        
   

    	        if (httpEntity != null) 
    	        {
    	        	System.out.println("Inside 2 ");
    	            InputStream is = httpEntity.getContent();
    	            //result = StringUtils.convertStreamToString(is);
    	            //////is = c.openInputStream();

    	            StringBuffer buffer = new StringBuffer();
    	            int ch = 0;
    	            while (ch != -1) {
    	                ch = is.read();
    	                buffer.append((char) ch);
    	            }

    	         
    	           json = buffer.toString(); //eaw data returned by search
    	           /* System.out.println("before = ");
    	           
    	            parsedString = parseSearchResults.parseHttp(json);
    	            System.out.println("after1 = ");
    	            */
    	            resultBool = parseSearchResults.parseSearchStatus(json); //Search result exists or nor
    	          
    	            linksArea = parseSearchResults.linksSpace(json);
    	            System.out.println("linksText = " + linksArea);
    	            
    	            System.out.println("before = ");
     	           
    	            httpText = parseSearchResults.parseHttp(linksArea, resultBool);
    	            System.out.println("after1 = ");
    	            
    	            parseSearchResults.workSpace(linksArea, resultBool);
    	            
    	            System.out.println("after2 = ");
    	            
    	            parseSearchResults.fileSpace(httpText, resultBool);
    	          
    	            
    	            System.out.println("In Network");
    	            parseSearchResults.variableSpace(httpText, resultBool);
    	            
    	            parseSearchResults.countNumStrings(httpText);
    
    	            
    	        }
    	     

    	        // handle response here...
    	    }catch (Exception ex) {
    	        // handle exception here
    	    } finally {
    	    	httpClient.getConnectionManager().shutdown();
    	    }
                       
    	    //System.out.println("json =" + json);
    	    return json;
			//return httpText;
			//return parsedString;
			
    	
    	
    	
    	
    	
    	
    	
    	
    	
    }
}