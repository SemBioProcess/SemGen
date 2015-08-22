package semgen.stage.janet;

import java.io.FileNotFoundException;

import semgen.stage.serialization.SearchResultSet;

public class firstJanetThread {

    public static void ParamsPassedForThread(SearchResultSet[] resultSets, String searchString) {
        Runnable r = new  callJanetThread(resultSets, searchString);
        new Thread(r).start();
    }
}