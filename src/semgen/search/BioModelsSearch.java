package semgen.search;

import semgen.stage.serialization.SearchResultSet;
import uk.ac.ebi.biomodels.ws.BioModelsWSClient;

import java.util.*;

public class BioModelsSearch {
    public static final String SourceName = "BioModels";

    public static SearchResultSet bioModelsSearch(String searchString) throws Exception {
        BioModelsWSClient client = new BioModelsWSClient();
        System.out.println("Searching BioModels...");

        List<Set<String>> compareResults = new ArrayList<Set<String>>();

        String queryArray[];
        queryArray = searchString.toUpperCase().split(" ");
        // Example queries - "CHEBI:15355", "CHEBI:27897"

        for(String keyword : queryArray) {
            Set<String> searchResults = new HashSet<String>();

            addAllIfNotNull(searchResults, client.getModelsIdByChEBI(keyword));
            addAllIfNotNull(searchResults, client.getModelsIdByChEBIId(keyword));
            addAllIfNotNull(searchResults, client.getModelsIdByGO(keyword));
            addAllIfNotNull(searchResults, client.getModelsIdByGOId(keyword));
            addAllIfNotNull(searchResults, client.getModelsIdByName(keyword));
            addAllIfNotNull(searchResults, client.getModelsIdByPerson(keyword));
            addAllIfNotNull(searchResults, client.getModelsIdByPublication(keyword));
            addAllIfNotNull(searchResults, client.getModelsIdByTaxonomy(keyword));
            addAllIfNotNull(searchResults, client.getModelsIdByTaxonomyId(keyword));
            addAllIfNotNull(searchResults, client.getModelsIdByUniprot(keyword));
            addAllIfNotNull(searchResults, client.getModelsIdByUniprotId(keyword));

            compareResults.add(searchResults);
        }



        // Find the intersection of the results for each keyword
        Set<String> finalResults = new HashSet<String>();
        for(Set<String> resultSet : compareResults) {
            finalResults = compareResults.get(0);
            finalResults.retainAll(resultSet);
        }
        return new SearchResultSet(SourceName, finalResults.toArray(new String[finalResults.size()]));
    }

    public static <String> void addAllIfNotNull(Set<String> list, String[] c) {
        if (c != null) {
            list.addAll(Arrays.asList(c));
        }
    }

}
