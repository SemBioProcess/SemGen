package semgen.stage.janet;

import semgen.stage.serialization.SearchResultSet;



public class janet_main{

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
		
		boolean keyboardTimeLapsed = waitOnKeyStrokes.autoComplete(searchString);
		boolean remotequeryHasResults =false;
		String fetchRemoteData = null;
		/*try {
			 fetchRemoteData = janet_calls.fetchRemoteData(searchString);
			 remotequeryHasResults =janet_calls.boolSearchResult(fetchRemoteData);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		

		
		
	    //if(SearchResultSet.srsIsEmpty(resultSets) && searchString.equals("OPB_01023"))
	    if(SearchResultSet.srsIsEmpty(resultSets)  && keyboardTimeLapsed)
		{
			try {
				  System.out.println("Remote query executed");	
				  fetchRemoteData = janet_calls.fetchRemoteData(searchString);
				  remotequeryHasResults =janet_calls.boolSearchResult(fetchRemoteData);
				  if(remotequeryHasResults)
				  {
					  resultSetsJanet =janet_calls.TrimParsedJanetData(fetchRemoteData,remotequeryHasResults);
					  resultSets[0] = resultSetsJanet;
				  }
				} catch (Exception e) {
				e.printStackTrace();
			}
			

		}
		return resultSets;
	}
	
}


/*


if(searchString.equals("OPB_01023"))
{
	
	System.out.println("iNSSSSSSSIDEEE ");
	for(int i=0;i<resultSets.length;i++)
	{
	
		System.out.println("resultSets[i].results.length= " + resultSets[i].results.length);
		System.out.println("Janet Source= " + resultSets[i].source);
	
		for(int j=0;j<resultSets[i].results.length;j++){
		
			System.out.println("Janet Results= " + resultSets[i].results[j]);
		}
	}	
}

*/