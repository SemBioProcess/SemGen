package semsim;

import org.jdom.Namespace;

import semsim.annotation.CurationalMetadata;

public class CellMLconstants {
	public static Namespace docNS = Namespace.getNamespace("http://cellml.org/tmp-documentation");
	public static Namespace cmetaNS = Namespace.getNamespace("cmeta", "http://www.cellml.org/metadata/1.0#");
	public static Namespace bqsNS = Namespace.getNamespace("bqs", "http://www.cellml.org/bqs/1.0#");
	public static Namespace cellml1_0NS = Namespace.getNamespace("http://www.cellml.org/cellml/1.0#");
	public static Namespace cellml1_1NS = Namespace.getNamespace("http://www.cellml.org/cellml/1.1#");
	public static Namespace mathmlNS = Namespace.getNamespace("http://www.w3.org/1998/Math/MathML");
	public static Namespace xlinkNS = Namespace.getNamespace("xlink", "http://www.w3.org/1999/xlink");
	public static Namespace bqbNS = Namespace.getNamespace("bqbiol", SemSimConstants.BQB_NAMESPACE);
	public static Namespace rdfNS = Namespace.getNamespace("rdf", SemSimConstants.RDF_NAMESPACE);
	public static Namespace dctermsNS = Namespace.getNamespace("dcterms", CurationalMetadata.DCTERMS_NAMESPACE);
	public static Namespace semsimNS = Namespace.getNamespace("semsim", SemSimConstants.SEMSIM_NAMESPACE);
}
