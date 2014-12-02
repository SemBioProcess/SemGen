package semsim;


import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.jdom.Namespace;

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
	public static Namespace dctermsNS = Namespace.getNamespace("dcterms", SemSimConstants.DCTERMS_NAMESPACE);
	public static Namespace semsimNS = Namespace.getNamespace("semsim", SemSimConstants.SEMSIM_NAMESPACE);
	public static Set<String> CellML_UNIT_DICTIONARY;
		
	static{
		Set<String> aSet = new HashSet<String>();
		aSet.add("ampere");
		aSet.add("farad");
		aSet.add("katal");
		aSet.add("lux");
		aSet.add("pascal");
		aSet.add("tesla");
		aSet.add("becquerel");
		aSet.add("gram");
		aSet.add("kelvin");
		aSet.add("meter");
		aSet.add("radian");
		aSet.add("volt");
		aSet.add("candela");
		aSet.add("gray");
		aSet.add("kilogram");
		aSet.add("metre");
		aSet.add("second");
		aSet.add("watt");
		aSet.add("celsius");
		aSet.add("henry");
		aSet.add("liter");
		aSet.add("mole");
		aSet.add("coulomb");	
		aSet.add("hertz");
		aSet.add("litre");
		aSet.add("newton");
		aSet.add("sievert");
		aSet.add("siemens");	
		aSet.add("weber");
		aSet.add("dimensionless");	
		aSet.add("joule");
		aSet.add("lumen");
		aSet.add("ohm");
		aSet.add("steradian");
	    CellML_UNIT_DICTIONARY = Collections.unmodifiableSet(aSet);
	}
}
