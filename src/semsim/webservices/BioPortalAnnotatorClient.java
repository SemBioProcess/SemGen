package semsim.webservices;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Set;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;


public class BioPortalAnnotatorClient { 
	
	// NCBO annotator settings
	public Set<String> ontologyids;
	public String longestOnly;
	public String wholeWordOnly;
	public String filterNumber;
	public String withDefaultStopWords;
	public String isStopWordsCaseSensitive;
	public String mintermSize;
	public String scored;
	public String withSynonyms; 
	public String ontologiesToKeepInResult; 
	public String isVirtualOntologyId; 
	public String levelMax;
	public String mappingTypes;
	public Hashtable<String,String[]> annotatorsettings = new Hashtable<String,String[]>();
	
		/* NEEDS TO BE REWRITTEN FOR NEW BIOPORTAL REST SERVICES */
		public static final String annotatorUrl = "http://rest.bioontology.org/obs/annotator";
		public String text;
	    public BioPortalAnnotatorClient(String text, Set<String> ontologyids, Hashtable<String,String[]> annotatorsettings) throws FileNotFoundException {
	    	this.text = text;
	    	
	    	this.annotatorsettings = annotatorsettings;  //ResourcesManager.createHashtableFromFile("cfg/NCBOAnnotatorSettings.txt");
			longestOnly = annotatorsettings.get("longestOnly")[0];
			wholeWordOnly = annotatorsettings.get("wholeWordOnly")[0];
			filterNumber = annotatorsettings.get("filterNumber")[0];
			withDefaultStopWords = annotatorsettings.get("withDefaultStopWords")[0];
			isStopWordsCaseSensitive = annotatorsettings.get("isStopWordsCaseSensitive")[0];
			mintermSize = annotatorsettings.get("minTermSize")[0];
			scored = annotatorsettings.get("scored")[0];
			withSynonyms = annotatorsettings.get("withSynonyms")[0];
			
			isVirtualOntologyId = annotatorsettings.get("isVirtualOntologyId")[0];
			levelMax = annotatorsettings.get("levelMax")[0];
			mappingTypes = annotatorsettings.get("mappingTypes")[0];
			
			String temp = "";
			String[] onts = ontologyids.toArray(new String[]{}); 
			for(int x=0; x<onts.length; x++){
				temp = temp + "," + onts[x];
			}
			temp = temp.substring(1,temp.length());
			ontologiesToKeepInResult = temp;
	    	System.out.println("Annotating with NCBO Annotator...");
	    }
	    public String annotate() throws HttpException, IOException{
	    	String contents = "";
            HttpClient client = new HttpClient();
            client.getParams().setParameter(
            HttpMethodParams.USER_AGENT,"Annotator Client Example - Annotator");  //Set this string for your application 
            
            PostMethod method = new PostMethod(annotatorUrl);
            
            // Configure the form parameters
            method.addParameter("longestOnly",longestOnly);  // "breast cancer" vs. "breast," "cancer," and "breast cancer"
            method.addParameter("wholeWordOnly",wholeWordOnly);
            method.addParameter("filterNumber", filterNumber);
            method.addParameter("stopWords","lead,can,growth,cell,point,left,right,center,line,binding,role,disease,oxide,formation");
            method.addParameter("withDefaultStopWords",withDefaultStopWords);
            method.addParameter("isStopWordsCaseSensitive",isStopWordsCaseSensitive);
            method.addParameter("mintermSize",mintermSize);
            method.addParameter("scored", scored);
            method.addParameter("withSynonyms",withSynonyms); 
            method.addParameter("ontologiesToExpand", "");
            method.addParameter("ontologiesToKeepInResult", ontologiesToKeepInResult); 
            method.addParameter("isVirtualOntologyId", isVirtualOntologyId); 
            method.addParameter("semanticTypes", ""); 
            method.addParameter("levelMax", levelMax);
            method.addParameter("mappingTypes", mappingTypes); //null, Automatic 
            method.addParameter("textToAnnotate", text);
            method.addParameter("format", "xml"); //Options are 'text', 'xml', 'tabDelimited'   
            method.addParameter("apikey", "c4192e4b-88a8-4002-ad08-b4636c88df1a");

            // Execute the POST method
            int statusCode = client.executeMethod(method);
            
            if( statusCode != -1 ) {
                try {
                contents = method.getResponseBodyAsString();
                System.out.println(contents);
                method.releaseConnection();
                }
                catch( Exception e ) {
                    e.printStackTrace();
                }
            }
	        return contents;
	    }
	}
