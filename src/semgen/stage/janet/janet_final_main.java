package semgen.stage.janet;

import semgen.stage.serialization.SearchResultSet;



public class janet_final_main{

	public static SearchResultSet[] processSearch(SearchResultSet[] resultSets, String searchString) {
		
		
		
		//System.out.println("resultSets length = " + resultSets.length);
		searchString =searchString.toUpperCase();
		SearchResultSet resultSetsJanet = null;
	
		
		/*for(int i=0;i<resultSets.length;i++)
		{
			
			System.out.println("resultSets[i].results.length= " + resultSets[i].results.length);
			System.out.println("SemGen Source= " + resultSets[i].source);
			
			for(int j=0;j<resultSets[i].results.length;j++){
				
			System.out.println("SemGen Results= " + resultSets[i].results[j]);
			}
		}*/	
		
		boolean remotequeryHasResults =false;
		String fetchRemoteData = null;

		System.out.println("in janet_final_main");

		 if(SearchResultSet.srsIsEmpty(resultSets))
		{
			try {
				  System.out.println("Remote query executed");	
				  System.out.println("searchString =" + searchString);
				  
				  fetchRemoteData = janet_calls.FinalfetchRemoteData(searchString);
				  System.out.println(" fetchRemoteData = " + fetchRemoteData);
				  remotequeryHasResults =janet_calls.FinalboolSearchResult(fetchRemoteData);
				  System.out.println(" remotequeryHasResults = " + remotequeryHasResults);
				 
				  
				  
				  if(remotequeryHasResults)
				  {
					  resultSetsJanet =janet_calls.FinalTrimParsedJanetData(fetchRemoteData,remotequeryHasResults);
					  
					  
					  resultSets[0] = resultSetsJanet;
				  }
				} catch (Exception e) {
				e.printStackTrace();
			}
			

		}
		return resultSets;
	}
	
}


