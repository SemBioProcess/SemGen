package semsim.webservices;

public class SemGenOlsOntologyService {
	
}
//package webservices;
//package uk.ac.ebi.ontocat.ols;
/*
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import javax.xml.rpc.ServiceException;

import org.apache.log4j.Logger;

import uk.ac.ebi.ontocat.AbstractOntologyService;
import uk.ac.ebi.ontocat.Ontology;
import uk.ac.ebi.ontocat.OntologyService;
import uk.ac.ebi.ontocat.OntologyServiceException;
import uk.ac.ebi.ontocat.OntologyTerm;
import uk.ac.ebi.ook.web.model.DataHolder;
import uk.ac.ebi.ook.web.services.Query;
import uk.ac.ebi.ook.web.services.QueryService;
import uk.ac.ebi.ook.web.services.QueryServiceLocator;

/**
 * Using ols-client.jar from
 * http://www.ebi.ac.uk/ontology-lookup/implementationOverview.do
 * 
 * @author Morris Swertz, Tomasz Adamusiak
 * 
 *         Dependencies:
 *         <ul>
 *         <li>ols-client.jar 1.14
 *         <li>axis.jar 1.4
 *         <li>commons-discovery.jar 0.2.jar
 *         <li>commons-logging.jar 1.0.3
 *         <li>log4j.jar 1.2.8
 *         <li>jaxrpc.jar 1.1
 *         <li>saaj.jar 1.2
 *         <li>wsdl4j.jar
 *         </ul>
 * 
 

@SuppressWarnings("unchecked")
public class SemGenOlsOntologyService extends AbstractOntologyService implements
		OntologyService {
	Logger logger = Logger.getLogger(SemGenOlsOntologyService.class);
	// Webservice API
	QueryService locator;
	// query handle to OLS
	Query qs;
	// Base URL for EBI lookups
	String lookupURI = "http://www.ebi.ac.uk/ontology-lookup/?termId=";
	// cache of annotations
	Map<String, Map<String, List<String>>> annotationCache = new TreeMap<String, Map<String, List<String>>>();
	// logger
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(SemGenOlsOntologyService.class
			.getName());

	public SemGenOlsOntologyService() throws OntologyServiceException {
		// Try to make connections to the database and the webservice
		try {
			this.locator = new QueryServiceLocator();
			this.qs = locator.getOntologyQuery();
		} catch (ServiceException e) {
			throw new OntologyServiceException(e);
		}
	}

	public List<Ontology> getOntologies() throws OntologyServiceException {
		try {
			List<Ontology> result = new ArrayList<Ontology>();

			Map<String, String> terms = qs.getOntologyNames();
			for (String acc : terms.keySet()) {
				Ontology o = getOntology(acc.split(":")[0]);
				result.add(o);
			}
			return result;
		} catch (RemoteException e) {
			throw new OntologyServiceException(e);
		}
	}

	public List<OntologyTerm> searchOntology(String ontologyAccession,
			String query, SearchOptions... options)
			throws OntologyServiceException {
		// Make sure the ontology exists
		getOntology(ontologyAccession);
		List<SearchOptions> ops = new ArrayList<SearchOptions>(
				Arrays.asList(options));
		try {
			Map<String, String> terms = new HashMap<String, String>();
			if (ops.contains(SearchOptions.EXACT)) {
				terms = qs.getTermsByExactName(query, ontologyAccession);
			} else {
				terms = qs.getTermsByName(query, ontologyAccession, false);
			}
			if (ops.contains(SearchOptions.INCLUDE_PROPERTIES)) {
				// params for getTermsByAnnotationData()
				// ontologyName - - the name of the ontology to limit the term
				// search on (mandatory)
				// annotationType - - the type of annotation to limit the search
				// strValue - - the string value of annotations to limit search
				// fromDblValue - - lower numeric value of annotations to limit
				// toDblValue - - higher numeric value of annotations to limit
				// FIXME: This always returns NULL!
				DataHolder[] dhs = qs.getTermsByAnnotationData(
						ontologyAccession, null, query, 0, 0);
				if (dhs != null) {
					for (DataHolder dh : dhs) {
						// filter out non-exact terms if necessary
						if (ops.contains(SearchOptions.EXACT)
								&& dh.getAnnotationStringValue()
										.equalsIgnoreCase(query))
							continue;
						terms.put(dh.getTermId(), dh.getTermName());
					}
				}
			}
			List<OntologyTerm> result = new ArrayList<OntologyTerm>();
			for (String key : terms.keySet()) {
				// splitting e.g. 228975=NEWT:Thymus magnus
				String ontAccession = key.split(":|_")[0];
				String label = terms.get(key);
				result.add(new OntologyTerm(ontAccession, key, label));
			}
			return injectTermContext(result, query, options);
		} catch (RemoteException e) {
			throw new OntologyServiceException(e);
		}
	}

	public List<OntologyTerm> searchAll(String query, SearchOptions... options)
			throws OntologyServiceException {
		List<SearchOptions> ops = new ArrayList<SearchOptions>(
				Arrays.asList(options));
		if (ops.contains(SearchOptions.INCLUDE_PROPERTIES)) {
			// remove include properties from ops
			// so that it does not show up in context
			ops.remove(SearchOptions.INCLUDE_PROPERTIES);
			logger.debug("OLS does not support searching properties in searchAll(), use searchOntology() instead");
		}
		try {
			Set<Map.Entry<String, String>> sTerms = qs.getPrefixedTermsByName(
					query, false).entrySet();
			List<OntologyTerm> result = new ArrayList<OntologyTerm>();
			for (Map.Entry<String, String> entry : sTerms) {
				// splitting e.g. 228975=NEWT:Thymus magnus
				String termAccession = entry.getKey();
				String ontologyAccession = entry.getValue().split(":")[0];
				String label = entry.getValue().split(":")[1];
				// filter out non-exact terms if necessary
				if (ops.contains(SearchOptions.EXACT)
						&& !label.equalsIgnoreCase(query))
					continue;
				// Inject searchoptions into context
				OntologyTerm ot = new OntologyTerm(ontologyAccession,
						termAccession, label);
				result.add(ot);
			}
			return injectTermContext(result, query,
					ops.toArray(new SearchOptions[0]));
		} catch (RemoteException e) {
			throw new OntologyServiceException(e);
		}
	}

	public Ontology getOntology(String ontologyAccession)
			throws OntologyServiceException {
		Ontology o = new Ontology(ontologyAccession);
		try {
			String label = (String) qs.getOntologyNames()
					.get(ontologyAccession);
			if (label == null)
				return null;
			o.setLabel(label);
			o.setDateReleased(qs.getOntologyLoadDate(ontologyAccession));
			o.setVersionNumber(qs.getVersion());
		} catch (RemoteException e) {
			throw new OntologyServiceException(e);
		}
		o.setAbbreviation(ontologyAccession);
		return o;
	}

	public OntologyTerm getTerm(String ontologyAccession, String termAccession)
			throws OntologyServiceException {
		String label = null;
		try {
			label = qs.getTermById(termAccession, ontologyAccession);
			if (label.equals(termAccession))
				return null;
		} catch (RemoteException e) {
			throw new OntologyServiceException(e);
		}
		return new OntologyTerm(ontologyAccession, termAccession, label);
	}

	public OntologyTerm getTerm(String termAccession)
			throws OntologyServiceException {

		return getTerm(getOntologyAccFromTerm(termAccession), termAccession);
	}

	private String getOntologyAccFromTerm(String termAccession) {
		// MP:0001823 or EFO_0000866
		if (termAccession.contains(":") || termAccession.contains("_")) {
			return termAccession.split("[:_]")[0];
		} else {
			logger.warn("No ontologyAccession in OLS term <" + termAccession
					+ "> assumming NEWT");
			return "NEWT";
		}
	}

	public List<OntologyTerm> getRootTerms(String ontologyAccession)
			throws OntologyServiceException {
		// Make sure the ontology exists
		getOntology(ontologyAccession);
		try {
			return fetchFullTerms(qs.getRootTerms(ontologyAccession));
		} catch (RemoteException e) {
			e.printStackTrace();
			throw new OntologyServiceException(e.getMessage());
		}
	}

	public Map<String, List<String>> getAnnotations(String ontologyAccession, String termAccession) throws OntologyServiceException {
		// Make sure the term exists
		getTerm(ontologyAccession, termAccession);
		String key = ontologyAccession + ":" + termAccession;
		if (!annotationCache.containsKey(key)) {
			try {
				Map result = qs.getTermMetadata(termAccession,ontologyAccession);
				// clean out the String from String[]
				for (Object metadataKey : result.keySet()) {
					// logger.debug("getting annotation "+metadataKey);
					if (result.get(metadataKey) instanceof String) {
						result.put(metadataKey, Arrays.asList(new String[] { (String) result.get(metadataKey) }));
					} else if (result.get(metadataKey) instanceof String[]) {
						result.put(metadataKey, Arrays.asList((String[]) result.get(metadataKey)));
					} else {
						throw new OntologyServiceException("annotation error: " + metadataKey);
					}
				}
				annotationCache.put(key, result);
			} catch (RemoteException e) {
				throw new OntologyServiceException(e);
			}
		}
		return annotationCache.get(key);
	}

	public List<String> getSynonyms(String ontologyAccession,
			String termAccession) throws OntologyServiceException {
		List<String> result = getAnnotations(ontologyAccession, termAccession)
				.get("exact_synonym");
		if (result == null)
			return Collections.EMPTY_LIST;
		return result;
	}

	public String makeLookupHyperlink(String termAccession) {
		return lookupURI + termAccession;
	}

	public String makeLookupHyperlink(String ontologyAccession,
			String termAccession) {
		return makeLookupHyperlink(termAccession);
	}

	// helper methods
	protected List<OntologyTerm> fetchFullTerms(Map<String, String> terms)
			throws OntologyServiceException {
		List<OntologyTerm> result = new ArrayList<OntologyTerm>();
		for (String termAccession : terms.keySet()) {
			OntologyTerm o = getTerm(termAccession);
			result.add(o);
		}
		return result;
	}

	public List<String> getDefinitions(String ontologyAccession,
			String termAccession) throws OntologyServiceException {
		List<String> result = getAnnotations(ontologyAccession, termAccession)
				.get("definition");
		if (result == null)
			return Collections.EMPTY_LIST;
		return result;
	}

	public List<OntologyTerm> getChildren(String ontologyAccession,
			String termAccession) throws OntologyServiceException {
		// Make sure the term exists
		getTerm(ontologyAccession, termAccession);
		try {
			return fetchFullTerms(qs.getTermChildren(termAccession,
					ontologyAccession, 1, null));
		} catch (RemoteException e) {
			throw new OntologyServiceException(e);
		}
	}

	public List<OntologyTerm> getParents(String ontologyAccession,
			String termAccession) throws OntologyServiceException {
		// Make sure the term exists
		getTerm(ontologyAccession, termAccession);
		try {
			return fetchFullTerms(qs.getTermParents(termAccession,
					ontologyAccession));
		} catch (RemoteException e) {
			throw new OntologyServiceException(e);
		}

	}

	public List<OntologyTerm> getTermPath(String ontologyAccession,
			String termAccession) throws OntologyServiceException {
		List<OntologyTerm> path = new ArrayList<OntologyTerm>();
		// seed the list with this term
		OntologyTerm current = this.getTerm(ontologyAccession, termAccession);
		path.add(current);

		int iteration = 0;
		// get its parents and iterate over first parent
		List<OntologyTerm> parents = getParents(ontologyAccession,
				termAccession);
		while (parents.size() != 0) {
			current = parents.get(0);
			path.add(current);
			parents = getParents(current.getOntologyAccession(),
					current.getAccession());

			// safety break for circular relations
			if (iteration++ > 100) {
				throw new OntologyServiceException(
						"findSearchTermPath(): TOO MANY ITERATIONS ("
								+ iteration + "x)");
			}
		}

		// reverse order
		Collections.reverse(path);
		return path;
	}

	public Map<String, Set<OntologyTerm>> getRelations(
			String ontologyAccession, String termAccession)
			throws OntologyServiceException {
		// Make sure the term exists
		getTerm(ontologyAccession, termAccession);
		Map<String, Set<OntologyTerm>> result = new HashMap<String, Set<OntologyTerm>>();
		try {
			Map<String, String> relations = qs.getTermRelations(termAccession,
					ontologyAccession);
			// add terms
			for (Entry<String, String> entry : relations.entrySet()) {
				String relType = entry.getValue();
				Set<OntologyTerm> set = result.get(relType);
				if (set == null)
					set = new HashSet<OntologyTerm>();
				set.add(getTerm(entry.getKey()));
				result.put(relType, set);
			}

		} catch (RemoteException e) {
			throw new OntologyServiceException(e);
		}
		return result;
	}

}
*/
