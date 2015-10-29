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
		 
		System.out.println("janetstrArrayr## == " +janetSearchedStr);
		 return new SearchResultSet("PMR", janetstrArray);
	}
	
	
	
	
	
	
public static SearchResultSet FinalTrimParsedJanetData(String remoteSearchData, boolean remotequeryHasResults) {
		
		
		
		String janetResults = null;
		String janetSearchedStr = null;
		String janetNoDupArray[][] = null;
		String dirPath= null;
		
	
		try {

				janetResults = remoteSearchData;
				System.out.println(" janetResults = " + janetResults);
			
	        
				if(remotequeryHasResults==false)
					janetSearchedStr = null;
				else
				{
					//janetNoDupArray = parseSearchResults.removeDupForSemGen(workspaceArray, fileNameSpaceArray, variableSpaceArray,httpTextSpaceArray, searchItemsFound);
					
					
					String splitStrURL[] = finalSplitURL(janetResults);
					System.out.println("splitStrURL.length = " + splitStrURL.length); 
					for(int i =3; i <= splitStrURL.length-2 ; i++)
					    System.out.println("splitStrURL[" +i+" ]= "+splitStrURL[i]);
					//System.exit(0);
					
					for(int i =2; i <= splitStrURL.length-2 ; i++)
					{
					
						dirPath =fileSystemUpdate.FinalmakeDir(splitStrURL[i]);
						System.out.println("dirPath = " + dirPath);
						fileSystemUpdate.FinaldownloadFiles(splitStrURL[i],dirPath);
					}
					
					
					janetSearchedStr = parseSearchResults.FinalpackReturnStringforSemGen(splitStrURL);
					//System.exit(0); //work completed till here
					
					//janetSearchedStr = parseSearchResults.packReturnStringforSemGen(janetNoDupArray);//original
					
					
					
					
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
		 
		System.out.println("janetstrArrayr## == " +janetSearchedStr);
		 return new SearchResultSet("PMR", janetstrArray);
	}
	
	
	
	
	

	private static String[] finalSplitURL(String janetSearchedStr) {
		
		 String[] temp = janetSearchedStr.split(" ");
		 for(int i =0; i < temp.length ; i++)
			    System.out.println("**="+temp[i]);
		 return temp;
}






	public static boolean boolSearchResult(String fetchedRemoteString) throws Exception {
		boolean resultBool = false;
		resultBool = parseSearchResults.parseSearchStatus(fetchedRemoteString); //Search result exists or no
		return resultBool;
	}

	public static boolean FinalboolSearchResult(String fetchedRemoteString) throws Exception {
		boolean resultBool = false;
		if( fetchedRemoteString.contains("returned []"))
			resultBool = false;
		else
			resultBool = true;
		return resultBool;
	}
	
	
	
	
	public static String fetchRemoteData(String searchString) throws Exception {
		String janetResults = Network.fetchedData(searchString);
		return janetResults;
	}
	
	public static String FinalfetchRemoteData(String searchString) throws Exception {
		String janetResults = Network. FinalfetchRemoteData(searchString);
		return janetResults;
	}

	
}
	