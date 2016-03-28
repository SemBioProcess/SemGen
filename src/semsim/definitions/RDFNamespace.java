package semsim.definitions;

import org.jdom.Namespace;

public enum RDFNamespace  {
	PKB("http://www.virtualrat.edu/physkb/", "physkb"),
	SEMSIM("http://www.bhi.washington.edu/SemSim#", "semsim"),
	OPB("http://bhi.washington.edu/OPB#", "opb"),
	RO("http://www.obofoundry.org/ro/ro.owl#", "ro"),
	BQB("http://biomodels.net/biology-qualifiers/", "bqbiol"),
	BQM("http://biomodels.net/model-qualifiers/", "bqm"),
	RDF("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "rdf"),
	DCTERMS("http://purl.org/dc/terms/", "dcterms"),
	DCTERMS11("http://purl.org/dc/elements/1.1/", "dcterms11"),
	MATHML("http://www.w3.org/1998/Math/MathML", "mathml"),
	XLINK("http://www.w3.org/1999/xlink", "xlink"),
	CELLML1_0("http://www.cellml.org/cellml/1.0#", "cellml"),
	CELLML1_1("http://www.cellml.org/cellml/1.1#", "cellml"),
	BQS("http://www.cellml.org/bqs/1.0#", "bqs"),
	CMETA("http://www.cellml.org/metadata/1.0#", "cmeta"),
	DOC("http://cellml.org/tmp-documentation", "doc"),
	VCARD("http://www.w3.org/2001/vcard-rdf/3.0#","vCard");
	
	private String namespace;
	private String owlid;
	
	RDFNamespace(String namespace, String id) {
		this.namespace = namespace;
		owlid = id;
	}
	
	public String getNamespaceasString() {
		return namespace;
	}
	
	public String getOWLid() {
		return owlid;
	}
	
	public Namespace createJdomNamespace() {
		return Namespace.getNamespace(getOWLid(), getNamespaceasString());
	}
}
