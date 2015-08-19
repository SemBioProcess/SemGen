package semgen.stage.janet;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.file.Files;

public class fileSystemUpdate {

 static void makeDir(String[][] string_2D_Array, int array_counter) {
		// TODO Auto-generated method stub
        for(int i =0; i<array_counter;i++){
        	String wsPath = string_2D_Array[i][0];
         	try {
			
				//wsPath = wsPath.replaceAll("[^\\x0A-\\x0D]", "");
				wsPath = wsPath.replaceAll("[\n\r]","");//REMOVE LINE FEED. Should work for win and nix
				wsPath = URLEncoder.encode(wsPath, "UTF-8");
			} catch (UnsupportedEncodingException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} 
         	
         	String dirPath =  "examples/JanetModels/" + wsPath;
                 	
         	File theDir = new File(dirPath);
         	
         	try {
         		if (!theDir.exists())
         			Files.createDirectory(theDir.toPath());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
         	
         	/*boolean dirCreated = false;
         	if (!theDir.exists())
         	{
         		   dirCreated = theDir.mkdir();
         		  System.out.println("theDir.canWrite() = " + theDir.canWrite());
         		  if(dirCreated==false)
         		  {
         			 theDir.setWritable(true);
         			//theDir.setReadOnly();
         			 dirCreated = theDir.mkdir();
         		  }
         		  
         		  System.out.println("Boolean flag returned to make a directory = " + dirCreated);
         	}*/
         	//File file = new File("examples/AnnotatedModels/" + modelName + ".owl");
         }
		
	}
 
}