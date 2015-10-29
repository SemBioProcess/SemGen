package semgen.stage.janet;
import java.io.File;











import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Files;

public class fileSystemUpdate {

 static String makeDir(String[][] string_2D_Array) {
		// TODO Auto-generated method stub
		int rows = string_2D_Array.length;
		int noduprows =0;
		String dirPath = null;
		System.out.println("string_2D_Array[2][0]= " + string_2D_Array[2][0]); // number of columns in first row
		
		for(int i =0; i<rows;i++){
			if(string_2D_Array[i][0]== null){
				noduprows = i;
				break;
			}
		}
	 
	 
	 
        for(int i =0; i<noduprows;i++){
        	String wsPath = string_2D_Array[i][0];
         	try {
			
				//wsPath = wsPath.replaceAll("[^\\x0A-\\x0D]", "");
				wsPath = wsPath.replaceAll("[\n\r]","");//REMOVE LINE FEED. Should work for win and nix
				wsPath = URLEncoder.encode(wsPath, "UTF-8");
			} catch (UnsupportedEncodingException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} 
         	
         	dirPath =  "examples/JanetModels/" + wsPath+ "/";
                 	
         	File theDir = new File(dirPath);
         	
         	try {
         		if (!theDir.exists())
         			Files.createDirectory(theDir.toPath());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
         	

         }
		 return dirPath;
	}

 
 static String FinalmakeDir(String linkurl) throws IOException {
	
	
	 	String wsPath =  parselinkURL(linkurl);
      	String dirPath =  "examples/JanetModels/" + wsPath+ "/";
              	
      	File theDir = new File(dirPath);
      	
      	try {
      		if (!theDir.exists())
      			Files.createDirectory(theDir.toPath());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
      	

     
		 return dirPath;
	}
 
 
 public static String parselinkURL(String linkurl)  {
	
	 System.out.println("linkurl = " + linkurl);
	 String[] temp = linkurl.split("/");
	 //for(int i =0; i < temp.length ; i++)
	//	    System.out.println("*** here = " + temp[i]);
	 //System.exit(0);
	 String dirname = temp[4];
	 
	 return dirname;
	 
 }
 
 
public static void downloadFiles(String[][] janetNoDupArray, String dirPath) throws IOException {
	// TODO Auto-generated method stub
	
	int rows = janetNoDupArray.length;
	int noduprows =0;
	System.out.println("string_2D_Array[2][0]= " +janetNoDupArray[2][0]); // number of columns in first row
	
	for(int i =0; i<rows;i++){
		if(janetNoDupArray[i][0]== null){
			noduprows = i;
			break;
		}
	}
	String httpURL;
	String downloadURL;
	String fsPath;
	String filename;
	String workspace;
	for(int i =0; i<noduprows;i++){
		{
			workspace = janetNoDupArray[i][0];
			filename = janetNoDupArray[i][1];
			httpURL =janetNoDupArray[i][2];
			httpURL = httpURL.replace("@@file", "rawfile");
			String[] splithttp = httpURL.split("#");
			downloadURL = splithttp[0];
			//downloadURL = URLEncoder.encode(downloadURL, "UTF-8");
			System.out.println("downloadURL= " +downloadURL);
			System.out.println("filename= " +filename);
			fsPath =dirPath +filename;
			System.out.println("fsPath= " +fsPath);
			
			
					
					
			fsPath = fsPath.replaceAll("[\n\r]","");//REMOVE LINE FEED. Should work for win and nix
			fsPath = URLEncoder.encode(fsPath, "UTF-8");
			
			System.out.println("UTF 8 Encoded fsPath= " +fsPath);
			
			URL url = new URL(downloadURL);
			
			File theDir = new File(dirPath);
			
			String absdirPath = theDir.getAbsoluteFile().getParentFile().getAbsolutePath() + "/" + workspace + "/" + filename;
			
			absdirPath = absdirPath.replaceAll("[\n\r]","");
			//absdirPath = URLEncoder.encode(absdirPath, "UTF-8");
			//System.out.println("absdirPath= " +absdirPath);
			File file = new File(absdirPath);
			//File file = new File(fsPath);
			
			
			FileUtils.copyURLToFile(url, file);
			
		}
	}
	
}//end downloadFiles method








public static void FinaldownloadFiles(String downloadURL, String dirPath) throws IOException {
	// TODO Auto-generated method stub
	
	
			
	        //downloadURL = finalurl(downloadURL); 		
	        System.out.println("downloadURL = " + downloadURL);
	        String filename = findFilename(downloadURL);
			String ws =  Finalws(downloadURL);
			
			System.out.println("ws = " + ws);
			System.out.println("filename = " + filename);
			
			
			URL url = new URL(downloadURL);
			
			File theDir = new File(dirPath);
			
			String absdirPath = theDir.getAbsoluteFile().getParentFile().getAbsolutePath() +"\\" + ws+ "\\" + filename;
			
			absdirPath = absdirPath.replaceAll("[\n\r]","");
			//absdirPath = URLEncoder.encode(absdirPath, "UTF-8");
			System.out.println("absdirPath= " +absdirPath);
			File file = new File(absdirPath);
			//File file = new File(fsPath);
			
			
			FileUtils.copyURLToFile(url, file);
			
		
	
	
}//end downloadFiles method




static String Finalws(String downloadURL) {
	
	 String[] temp = downloadURL.split("/");
	// for(int i =0; i < temp.length ; i++)
	//	    System.out.println(temp[i]);
	 String ws = temp[4];
	 
	 //System.out.println("ws = " + ws);
	 //System.exit(0);
	 
	 return ws;
	
	
}


static String finalurl(String downloadURL) {

	System.out.println("downloadURL = " + downloadURL);
	String[] temp =downloadURL.split(" ");
	return temp[temp.length-2];
}


public static String findFilename(String linkurl)  {
	
	 
	 String[] temp = linkurl.split("/");
	// for(int i =0; i < temp.length ; i++)
	//	    System.out.println(temp[i]);
	 String filename = temp[temp.length-1];
	 String[] temp2 = filename.split(" ");
	 filename = temp2[0];
	 System.out.println("filename = " + filename);
	 //System.exit(0);
	 
	 return filename;
	 
}












 
}//end class