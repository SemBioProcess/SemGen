package semgen.stage.janet;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.tree.DefaultMutableTreeNode;

import org.apache.commons.codec.binary.StringUtils;

//import semgen.visualizations.JsonString;

public class parseSearchResults
{
	
	
	
	
	
	/*Parse the links space of the raw text
	 * input: rawsearch string
	 * output: the link space string is returned*/
    public static String linksSpace(String rawsearch) 
    {
    	
        //System.out.println("rawsearch = " + rawsearch);

        String ls_name;
        
        //Pattern p = Pattern.compile("\\\"prompt\": \"(.*?)\\,");// match the term identifiers.org
        Pattern p = Pattern.compile("links\": \\[(.*?)\\]", Pattern.DOTALL);// match across whole string Start: "links": [   End: ]
        Matcher m = p.matcher(rawsearch);
        
        ls_name = matcherToString(m); 
        //ws_name = ws_name.replaceAll("\"", "\r\n");//Add carriage return before http for readability
        
        System.out.println("ls_name = " + ls_name );	
            	
       	m.reset(); // reset the matcher to 0 before returning by discarding all current state information
        return   ls_name;
            
     }
    
	/*Parse the links space of the raw text
	 * input: rawsearch string
	 * output: the variable string is returned*/
    public static String variableSpace(String rawsearch, Boolean resultBool) 
    {
    	
        //System.out.println("rawsearch = " + rawsearch);

        String vs_name = null;
        
        if( resultBool==true)
        {
        	Pattern p = Pattern.compile(".*\\#(.*?)\\\r\n");// match the term start: (last occurrence of \ ) end: #
        	Matcher m = p.matcher(rawsearch);
        
        	vs_name = matcherToString(m); 
        	
        
        	System.out.println("vs_name = " + vs_name );	
            	
        	m.reset(); // reset the matcher to 0 before returning by discarding all current state information
        }
        return   vs_name;
            
     }
	
    
    
	/*Parse the links space of the raw text
	 * input: rawsearch string
	 * output: the file string is returned*/
    public static String fileSpace(String rawsearch, Boolean resultBool) 
    {
    	
        //System.out.println("rawsearch = " + rawsearch);

        String fs_name = null;
        
        if( resultBool==true)
        {
        	Pattern p = Pattern.compile(".*\\/(.*?)\\#");// match the term start: (last occurrence of \ ) end: #
        	Matcher m = p.matcher(rawsearch);
        
        	fs_name = matcherToString(m); 
        	//fs_name = fs_name.replaceAll("\"", "\r\n");//Add carriage return before http for readability
        
        	System.out.println("fs_name = " + fs_name );	
            	
        	m.reset(); // reset the matcher to 0 before returning by discarding all current state information
        }
        return   fs_name;
            
     }
	
    
    
	/*Parse the links space of the raw text
	 * input: rawsearch string
	 * output: the workspace string is returned*/
    public static String workSpace(String rawsearch, Boolean resultBool) 
    {
    	
        //System.out.println("rawsearch = " + rawsearch);

        String ws_name = null;
        
        if( resultBool==true)
        {
        	Pattern p = Pattern.compile("\\\"prompt\": \"(.*?)\\\",");// match the term Start: "prompt" :  End: ,
        	Matcher m = p.matcher(rawsearch);
        
        	ws_name = matcherToString(m); 
        	//ws_name = ws_name.replaceAll("\"", "\r\n");//Add carriage return before http for readability
        
        	System.out.println("ws_name = " + ws_name );	
            	
        	m.reset(); // reset the matcher to 0 before returning by discarding all current state information
        }
        return   ws_name;
            
     }

	/*Parse text to see if the search query is found on identifiers.org
	 * input: rawsearch string
	 * output: boolean true or false, depending upon if the parse term was found*/
    public static boolean parseSearchStatus(String rawsearch) 
    {
    	
        //System.out.println("rawsearch = " + rawsearch);

        boolean bool_matches;
        
        Pattern p = Pattern.compile("identifiers.org");// match the term identifiers.org
        Matcher m = p.matcher(rawsearch);
        bool_matches = m.find();
        System.out.println("m.find() = " + m.find() );	
            	
       	m.reset(); // reset the matcher to 0 before returning by discarding all current state information
        return   bool_matches;
            
     }
	
	
	
	/*Parse text to see if the search query (Start: "href": "  followed by    END: ",) is found and collect them if they exist
	 * input: rawsearch string
	 * output: All substrings matching the criteria are concatenated and returned as a string
     * (arg 1 = string passed, arg 2 = boolean value of existence of search result)*/
    public static  String parseHttp(String rawsearch, Boolean resultBool)
    {
    	
        System.out.println("rawsearch = " + rawsearch);
        

        String receivedString = null;
        
        if(resultBool==true)//if the search query was found
        {
        
        	Pattern p = Pattern.compile("\\\"href\": \"(.*?)\\\",");// match start term is "href": " and match end term is ",
        	//Pattern p = Pattern.compile("\\[(.*?)\\]");
        	Matcher m = p.matcher(rawsearch);
        
        	receivedString = matcherToString(m); 
        	//receivedString = receivedString.replaceAll("http", "\r\n http");//Add carriage return before http for readability
        	//System.out.println("receivedString = " + receivedString);
        	
        	m.reset(); // reset the matcher to 0 before returning by discarding all current state information
        	//m.group(1) is string we want
        	return    receivedString;
        }
        else
        	return "No Result Found...";
            
     }
    
    
    /*Convert Matcher to String*/
    public static String matcherToString(Matcher m) 
    {
    	StringBuilder sb = new StringBuilder();
    	String matchString = null;
    
    	while (m.find()) 
    	{
    		sb.append(m.group(1));
    		sb.append("\r\n");
        
    	}

    	//System.out.println("sb = " + sb.toString());
    	matchString = sb.toString();
		return matchString;
    }
    
    
    /*Count number of String*/
    public static int countNumStrings(String str) 
    {
    
    	int occurrences = 0;
    	for(char c : str.toCharArray())
    	{
    	   if(c == '\r')
    	      occurrences++;
    	}

    	System.out.println("occurrences = " + occurrences);
		return occurrences;
    }
    
    /*Split a String*/
    public static String[] splitString(String str, int MAXVAL, Boolean resultBool) 
    {
        
        String temp[];
        temp = new String[MAXVAL] ;
        int counter = 0;
        String concatStr = "";
        
        if(resultBool)
        {
        
        
            char[] t = str.toCharArray(); 

            for(char c : t)
            {
                
              concatStr+=c;
                
                if(c == '\n')
                {
                    temp[counter] = concatStr;
                    concatStr = "";
                    counter++;
                }
                
                //System.out.println(c);
         }
              
            //for(int i =0; i<MAXVAL;i++)
             //System.out.println("*******"+temp[i]);
          
            //System.out.println("temp.lengt = " + temp.length); 
        }
         
        return temp; 
    }     
    
    /*repack parsed String[] and put it into a String[][] and return it*/
    public static String[][] repack(String [] workspaceArray, String[] fileNameSpaceArray, String [] variableSpaceArray, int row)
     {
      
         int col = 3;//wsname,filename,varname
         String[][] string_2D_Array = new String [row+1][col+1];
         for(int i =0; i<row;i++)
         {
             string_2D_Array[i][0] = "[W] "+workspaceArray[i];
             string_2D_Array[i][1] = "[M] "+fileNameSpaceArray[i];
             string_2D_Array[i][2] = "[V] "+variableSpaceArray[i];
          }   
                 
         for(int i =0; i<row;i++)
         {
             for(int j =0; j<col;j++)         
             {
                 
                System.out.println(" " + string_2D_Array[i][j]);
             }
              System.out.println("\n");
         }
         return  string_2D_Array;
     }
    
    /*repack for SemGen parsed String[] and put it into a String[][] and return it*/
    public static String[][] removeDupForSemGen(String [] workspaceArray, String[] fileNameSpaceArray, String [] variableSpaceArray, String[] httpTextSpaceArray, int dupSearchItemsFound)
     {
      
         int col = 3;//wsname,filename,HTTPurl
         int nodupSearchItemsFound=0;
         String[][] string_2D_Array = new String [dupSearchItemsFound+1][col+1];
         String janet_searched_models_for_semgen = "";
         
         //remove duplictes
         for(int i =0; i<dupSearchItemsFound;i++){
        	 int dupflag = 0;
        	 for(int j=i+1;j<dupSearchItemsFound;j++){
        		 if(workspaceArray[i].equals(workspaceArray[j])  && fileNameSpaceArray[i].equals(fileNameSpaceArray[j]) )
        		 	 dupflag=1;
        	 }
        	 if(dupflag==0){
        		 string_2D_Array[nodupSearchItemsFound][0] = workspaceArray[i];
                 string_2D_Array[nodupSearchItemsFound][1] = fileNameSpaceArray[i];
                 string_2D_Array[nodupSearchItemsFound][2] = httpTextSpaceArray[i]; 
                 nodupSearchItemsFound+=1;
        	 }
         }	 
         
         for(int i =0; i<dupSearchItemsFound;i++){
         	System.out.print("WorkSpacearray["+i+"]=" + string_2D_Array[i][0]);
         	System.out.print("FileSpacearray["+i+"]="+ string_2D_Array[i][1]);
         	System.out.print("HTTPSpacearray["+i+"]=" + string_2D_Array[i][2]);
         	System.out.println("");
         }
         
         
         
        
         return  string_2D_Array;
        // return  janet_searched_models_for_semgen;
     }
    
    
	static String packReturnStringforSemGen(String[][] string_2D_Array) {
	
		int rows = string_2D_Array.length;
		int noduprows =0;
		String janet_searched_models_for_semgen = "";
		System.out.println("string_2D_Array[2][0]= " + string_2D_Array[2][0]); // number of columns in first row
		
		for(int i =0; i<rows;i++){
			if(string_2D_Array[i][0]== null){
				noduprows = i;
				break;
			}
		}
			
		 
		System.out.println(" noduprows = " +  noduprows);
        for(int i =0; i< noduprows;i++){
            if(i< noduprows-1)
           	 janet_searched_models_for_semgen += "W:" +string_2D_Array[i][0].replaceAll("\\s+","") + " "  + "M:" + string_2D_Array[i][1].replaceAll("\\s+","")  +",";
            else
           	 janet_searched_models_for_semgen += "W:" +string_2D_Array[i][0].replaceAll("\\s+","") + " "  + "M:" + string_2D_Array[i][1].replaceAll("\\s+","");
         }   
        janet_searched_models_for_semgen.replaceAll("\\s+","");
        return janet_searched_models_for_semgen;
	}

	/*Enumerate the BFS tree and help add nodes to the tree by stopping at the correct child node*/
     public static DefaultMutableTreeNode BFS_Enumerate_Jtree( DefaultMutableTreeNode root, String toSearch_depth01, String toSearch_depth02)
     {
     
          Enumeration pathgen = root.breadthFirstEnumeration();
          String enumString = null;
          boolean while_level_01_flag = false;
          boolean while_level_02_flag = false;
          DefaultMutableTreeNode L1 = null, L2 = null;
          //System.out.println("The Enumerated Nodes are: \n");
          
          while(pathgen.hasMoreElements())
          {
                 DefaultMutableTreeNode tmpNode = (DefaultMutableTreeNode) pathgen.nextElement();
              
                 enumString = Arrays.toString(tmpNode.getPath());
                 //System.out.println("Inside Enumeration........." + enumString);
                 if(enumString.equals(toSearch_depth01))
                 {
                     //System.out.println(" Depth 01 String Found********");
                     while_level_01_flag =true;//the searched node exists at level 1
                     L1 = tmpNode;
                 }
               
                 
                 if(while_level_01_flag) 
                 {       
                    if(enumString.equals(toSearch_depth02))
                     {
                         //System.out.println(" Depth 02 String Found********");
                         while_level_02_flag = true;//the searched node exists at level 2
                         L2 = tmpNode;
                     }
                 }
                     
          }//end while
             //System.out.println("while_level_01_flag = " + while_level_01_flag);
             //System.out.println("while_level_02_flag = " + while_level_02_flag);
          if(while_level_01_flag == false)//add it to the root
              return null;
          else  if(while_level_01_flag == true && while_level_02_flag == false)//add it to L1
          { 
              //System.out.println("L1.getLevel() = " + L1.getLevel());
              return L1;
          
          
          }
          else if(while_level_01_flag == true && while_level_02_flag == true)//add it to L2
          {
              //System.out.println("L2.getLevel() = " + L2.getLevel());
              return L2;
          }
        
          return null;
         
     }

    public static String parseStrForListDisplay(String pathstr) 
    {
        
        //Pattern p = Pattern.compile("\\[(.*?)\\]", Pattern.DOTALL);// match the term start: (last occurrence of \ ) end: #
        //Matcher m = p.matcher(pathstr);
        
      	//String tuple = matcherToString(m); 
        //System.out.println("tuple = " + tuple);
        String tuple = null;        
        String[] tokens = pathstr.split(",");
       // System.out.println("tokens= " + tokens[0] + " " + tokens[1] + " " + tokens[2] + " " + tokens[3]  );
        String[] ws = tokens[1].split("] ");//ws is workspace
       //	System.out.println("ws[1] = " + ws[1] );	
        
        String[] ms = tokens[2].split("]+"); // ms  is modelspace
       	System.out.println("ms[1] = " + ms[1] );
        
        tuple = ms[1]+" [" + ws[1] + "]";
        
       	//m.reset(); // reset the matcher to 0 before returning by discarding all current state information
        //System.out.println("tuple = "  + tuple);
        
        //tuple = tokens[2] + "," + tokens[3] + " " + "[" + tokens[1] + "]";
        
        return tuple;
        
    }

	public static String modelnameToFilePath(String modelName) {
		// TODO Auto-generated method stub
		
		
		String returnfilepath;
		String part1;
		String part2;

		//part1 = modelName.substring(modelName.indexOf(":") + 1);
		part1 = modelName.substring(0, modelName.indexOf("M"));
		//System.out.println("modelName = " + modelName);
		//part1="W:"+ part1;
		System.out.println("PART1 = " + part1);
		
		part2 =modelName.replace(part1, "");
		System.out.println("PART2 AFTER REPLACE= " + part2);
		
		 part1 =  part1.substring(modelName.indexOf(":") + 1);
		 part2 =  part2.substring(modelName.indexOf(":")+1);
		System.out.println("modelName = " + modelName);
		System.out.println("PART1 = " + part1);
		System.out.println("PART2 = " + part2);
		part1 =part1.replaceAll("\\s+","");
		part2 =part2.replaceAll("\\s+","");
		
		returnfilepath= part1 +"/" +part2;
		
		return returnfilepath;
	}


     
    	
}


