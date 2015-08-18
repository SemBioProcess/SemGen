package semgen.stage.serialization;

/**
 * Created by Ryan on 8/6/2015.
 * Added method srsIsEmpty by Sarad on 8/18/2015.
 */
public class SearchResultSet {
    public String source;
    public String[] results;

    public SearchResultSet(String source, String[] results) {
        this.source = source;
        this.results = results;
    }
   
   public static Boolean srsIsEmpty (SearchResultSet[] srs){ 
      int lengthOfsrs = srs.length;
	  int count =0;
	  for(int i=0;i<lengthOfsrs;i++)
	 		count += srs[i].results.length;
				
	  if(count<=0)
		  return true;
	  else
		  return false;
	}
   
}
