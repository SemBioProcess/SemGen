package semgen.search;

import semgen.stage.serialization.SearchResultSet;
import uk.ac.ebi.biomodels.ws.BioModelsWSClient;
import uk.ac.ebi.biomodels.ws.BioModelsWSException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

public class BioModelsSearch {
    public static BioModelsWSClient client = new BioModelsWSClient();
    public static final String SourceName = "BioModels";

    public static SearchResultSet bioModelsSearch(String searchString) throws Exception {
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
//        String [] finalResultsByName = new String[finalResultsById.length];
//
//        for (int i = 0; i < finalResultsById.length; i++) {
//            finalResultsByName[i] = client.getModelNameById(finalResultsById[i]); //This add a minute of search time...
//        }

        return new SearchResultSet(SourceName, finalResultsById);
    }

    public static <String> void addAllIfNotNull(Set<String> list, String[] c) {
        if (c != null) {
            list.addAll(Arrays.asList(c));
        }
    }

    public static String getModelSBMLById(String id) throws BioModelsWSException {
        return client.getModelSBMLById(id);
    }

    public static String findPubmedAbstract(String modelId) throws BioModelsWSException {
        String pubmedId = client.getPublicationByModelId(modelId);
        String abstr = "";
        if (!pubmedId.equals("") && pubmedId.matches("[0-9]+")) {
            try {
                URL url = new URL("https://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?db=pubmed&id=" + pubmedId + "&retmode=text&rettype=abstract");
                System.out.println(url);
                URLConnection yc = url.openConnection();
                yc.setReadTimeout(60000); // Tiemout after a minute
                StringBuilder stringBuilder = new StringBuilder();
                BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream()));
                String inputLine;
                while ((inputLine = in.readLine()) != null)
                    stringBuilder.append(inputLine + '\n');
                abstr = stringBuilder.toString();
                in.close();
            } catch (IOException e) {
            }
        }
        return abstr;
    }
}
