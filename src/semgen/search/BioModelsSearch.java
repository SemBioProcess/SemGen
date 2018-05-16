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
            String[] biomodelResultSet = client.getModelsIdByChEBI(keyword);
            if (biomodelResultSet != null) {
                Set<String> searchResults = new HashSet<String>(Arrays.asList(biomodelResultSet));
                compareResults.add(searchResults);
            }
        }

//        Map<String, List<SimpleModel>> biomodelResultSet = client.getModelsIdByChEBIIds(queryArray);
//        Iterator iter = biomodelResultSet.keySet().iterator();
//        while (iter.hasNext()) {
//            Set<String> searchResults = new HashSet<String>();
//            String CHEBIId = (String) iter.next();
//            ArrayList modelList = (ArrayList) biomodelResultSet.get(CHEBIId);
//            Iterator modelIter = modelList.iterator();
//            while (modelIter.hasNext()) {
//                SimpleModel model = (SimpleModel) modelIter.next();
//                searchResults.add(model.getName());
//            }
//            // Store the list of models found for each keyword
//            compareResults.add(searchResults);
//        }

        // Find the intersection of the results for each keyword
        Set<String> finalResults = new HashSet<String>();
        for(Set<String> resultSet : compareResults) {
            finalResults = compareResults.get(0);
            finalResults.retainAll(resultSet);
        }
        return new SearchResultSet(SourceName, finalResults.toArray(new String[finalResults.size()]));
    }
}
