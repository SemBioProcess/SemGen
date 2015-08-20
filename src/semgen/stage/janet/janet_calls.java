package semgen.stage.janet;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import semgen.stage.serialization.SearchResultSet;



public class janet_calls
{
	
	
	public static SearchResultSet TrimParsedJanetData(String searchString) {
		
		
		
		String janetResults = null;
		String janetSearchedStr = null;
		String janetNoDupArray[][] = null;
		String dirPath= null;
		
			//janetResults = Network.fetchedData("OPB_01023");
		try {
			
			
			janetResults = Network.fetchedData(searchString);
		
			boolean resultBool = parseSearchResults.parseSearchStatus(janetResults); //Search result exists or no
			//if(resultBool)
			//{
	
				String linksArea = parseSearchResults.linksSpace(janetResults);
			
				String httpText = parseSearchResults.parseHttp(linksArea, resultBool);
				System.out.println("httpText = " + httpText);
				String workSpaceText = parseSearchResults.workSpace(linksArea, resultBool);
				System.out.println("workSpaceText = " + workSpaceText);
				String fileNameSpaceText = parseSearchResults.fileSpace(httpText, resultBool);
				System.out.println("fileNameSpaceText = " + fileNameSpaceText);
				String variableSpaceText = parseSearchResults.variableSpace(httpText, resultBool);
				int  searchItemsFound = parseSearchResults.countNumStrings(httpText);
	        
				String workspaceArray[] = parseSearchResults.splitString(workSpaceText, searchItemsFound, resultBool);
				String fileNameSpaceArray[] = parseSearchResults.splitString(fileNameSpaceText, searchItemsFound, resultBool);
				String variableSpaceArray[] = parseSearchResults.splitString(variableSpaceText, searchItemsFound, resultBool);
				String httpTextSpaceArray[] = parseSearchResults.splitString(httpText, searchItemsFound, resultBool);
				String[][] packed_Str = parseSearchResults.repack(workspaceArray, fileNameSpaceArray, variableSpaceArray, searchItemsFound);
				
				
				//System.out.println(" httpTextSpaceArray = " + httpTextSpaceArray);
				//System.exit(0);
            
				if(searchItemsFound<=0)
					janetSearchedStr = null;
				else
				{
					janetNoDupArray = parseSearchResults.removeDupForSemGen(workspaceArray, fileNameSpaceArray, variableSpaceArray,httpTextSpaceArray, searchItemsFound);
					dirPath =fileSystemUpdate.makeDir(janetNoDupArray);
					fileSystemUpdate.downloadFiles(janetNoDupArray,dirPath);
					janetSearchedStr = parseSearchResults.packReturnStringforSemGen(janetNoDupArray);	
					//janetSearchedStr = parseSearchResults.removeDupForSemGen(workspaceArray, fileNameSpaceArray, variableSpaceArray,httpTextSpaceArray, searchItemsFound);
					
					
				}
			
			
		} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	
		// String[] janetstrArray = new String[] {janetSearchedStr};
		 String[] janetstrArray = janetSearchedStr.split(","); 
		 //System.out.println("janetstrArray[0] = "  + janetstrArray[0]);
		 //System.out.println("janetstrArray[1] = "  + janetstrArray[1]);
		// Set<String> mySet = new HashSet<String>(Arrays.asList(janetstrArray));
		 //return new SearchResultSet("PMR", mySet.toArray(new String[mySet.size()]));
		 
		 return new SearchResultSet("PMR", janetstrArray);
		//return janetstrArray;
		
		
	}
	
	
}
	