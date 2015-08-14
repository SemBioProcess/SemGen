package semgen.stage.janet;



public class janet_calls
{
	
	
	public static String TrimParsedJanetData(String searchString) {
		
		
		
		String janetResults = null;
		String janetSearchedStr = null;
		
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
				String[][] packed_Str = parseSearchResults.repack(workspaceArray, fileNameSpaceArray, variableSpaceArray, searchItemsFound);
				
            
				if(searchItemsFound<=0)
					janetSearchedStr = null;
				else
					janetSearchedStr = parseSearchResults.repackForSemGen(workspaceArray, fileNameSpaceArray, variableSpaceArray,searchItemsFound);
			//}
			
		} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	
		
		return janetSearchedStr;
		
		
	}
	
	
}
	