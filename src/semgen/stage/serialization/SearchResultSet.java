package semgen.stage.serialization;

/**
 * Created by Ryan on 8/6/2015.
 */
public class SearchResultSet {
    public String source;
    public String[] results;

    public SearchResultSet(String source, String[] results) {
        this.source = source;
        this.results = results;
    }
}
