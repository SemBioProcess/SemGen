package semgen.stage.serialization;

import com.google.gson.annotations.Expose;

/**
 * Created by Ryan on 8/6/2015.
 */
public class SearchResultSet {
	@Expose public String source;
	@Expose public String[] results;

    public SearchResultSet(String source, String[] results) {
        this.source = source;
        this.results = results;
    }
}
