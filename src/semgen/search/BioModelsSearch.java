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
        Set<String> intersectingResults = new HashSet<String>();
        for(Set<String> resultSet : compareResults) {
            intersectingResults = compareResults.get(0);
            intersectingResults.retainAll(resultSet);
        }
        String [] finalResultsById = intersectingResults.toArray(new String[intersectingResults.size()]);
        String [] finalResultsByName = new String[finalResultsById.length];

        for (int i = 0; i < finalResultsById.length; i++) {
            finalResultsByName[i] = client.getModelNameById(finalResultsById[i]); //This add a minute of search time...
        }

        return new SearchResultSet(SourceName, finalResultsByName);
    }

    public static <String> void addAllIfNotNull(Set<String> list, String[] c) {
        if (c != null) {
            list.addAll(Arrays.asList(c));
        }
    }

}
