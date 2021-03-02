package semsim.definitions;

import org.jdom.Namespace;

/**
 * Enumeration of various RDF namespaces used in biosimulation modeling formats.
 *
 */
public enum RDFNamespace  {
	PKB("http://www.virtualrat.edu/physkb/", "physkb"),
	SEMSIM("http://bime.uw.edu/semsim/", "semsim"), // Old was http://www.bhi.washington.edu/SemSim#
	OPB("http://bhi.washington.edu/OPB#", "opb"),
	RO("http://www.obofoundry.org/ro/ro.owl#", "ro"),
	BQB("http://biomodels.net/biology-qualifiers/", "bqbiol"),
	BQM("http://biomodels.net/model-qualifiers/", "bqm"),
	RDF("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "rdf"),
	DCTERMS("https://dublincore.org/specifications/dublin-core/dcmi-terms/", "dc"),
	DCTERMS11("http://purl.org/dc/elements/1.1/", "dcterms11"),
	FOAF("http://xmlns.com/foaf/0.1/", "foaf"),
	MATHML("http://www.w3.org/1998/Math/MathML", "mathml"),
	XLINK("http://www.w3.org/1999/xlink", "xlink"),
	CELLML1_0("http://www.cellml.org/cellml/1.0#", "cellml"),
	CELLML1_1("http://www.cellml.org/cellml/1.1#", "cellml"),
	BQS("http://www.cellml.org/bqs/1.0#", "bqs"),
	CMETA("http://www.cellml.org/metadata/1.0#", "cmeta"),
	DOC("http://cellml.org/tmp-documentation", "doc"),
	VCARD("http://www.w3.org/2001/vcard-rdf/3.0#","vCard"),
	OMEX_LIBRARY("http://omex-library.org/","OMEXlibrary");
	
	private String namespace;
	private String owlid;
	
	RDFNamespace(String namespace, String id) {
		this.namespace = namespace;
		owlid = id;
	}
	
	/**
	 * @return The RDF namespace as a string (e.g. http://www.w3.org/1998/Math/MathML)
	 */
	public String getNamespaceAsString() {
		return namespace;
	}
	
	/**
	 * @return A prefix for the namespace
	 */
	public String getOWLid() {
		return owlid;
	}
	
	/**
	 * @return The namespace as a JDom Namespace object
	 */
	public Namespace createJdomNamespace() {
		return Namespace.getNamespace(getOWLid(), getNamespaceAsString());
	}
}
