package semgen.stage.janet;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import semgen.stage.serialization.SearchResultSet;



public class janet_calls
{
	
	
	public static SearchResultSet TrimParsedJanetData(String remoteSearchData, boolean remotequeryHasResults) {
		
		
		
		String janetResults = null;
		String janetSearchedStr = null;
		String janetNoDupArray[][] = null;
		String dirPath= null;
		
			//janetResults = Network.fetchedData("OPB_01023");
		try {
				boolean resultBool = remotequeryHasResults; 
				janetResults = remoteSearchData;
		
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
		String[] janetstrArray = null;
		if(janetSearchedStr!=null)
			janetstrArray = janetSearchedStr.split(",");
		 
		 return new SearchResultSet("PMR", janetstrArray);
	}

	public static boolean boolSearchResult(String fetchedRemoteString) throws Exception {
		boolean resultBool = false;
		resultBool = parseSearchResults.parseSearchStatus(fetchedRemoteString); //Search result exists or no
		return resultBool;
	}
	
	public static String fetchRemoteData(String searchString) throws Exception {
		String janetResults = Network.fetchedData(searchString);
		return janetResults;
	}

	
}
	