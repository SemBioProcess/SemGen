package semsim.definitions;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SBMLconstants {

	public static final Map<String,String> SBML_LEVEL_2_RESERVED_UNITS_MAP;
	public static final Set<String> SBML_LEVEL_3_BASE_UNITS;
	public static final Set<String> SBML_LEVEL_2_VERSION_4_BASE_UNITS;
	public static final Set<String> SBML_LEVEL_2_VERSION_1_BASE_UNITS;
//	public static final Map<String, String> SUBSTANCE_AMOUNT_UNITS_AND_OPB_PROPERTIES; 

	static {
		
		// Mapping between reserved units in SBML level 2 models and their default base units
		Map<String,String> map0 = new HashMap<String,String>();		
		map0.put("substance", "mole");
		map0.put("volume", "litre");
		map0.put("area", "square metre");
		map0.put("length", "metre");
		map0.put("time", "second");
		
		SBML_LEVEL_2_RESERVED_UNITS_MAP = Collections.unmodifiableMap(map0);
		
		// SBML level 3 base unit set
		Set<String> set0  = new HashSet<String>();
		set0.add("ampere");
		set0.add("farad");
		set0.add("joule");
		set0.add("lux");
		set0.add("radian");
		set0.add("volt");
		set0.add("avogadro");
		set0.add("gram");
		set0.add("katal");
		set0.add("metre");
		set0.add("second");
		set0.add("watt");
		set0.add("becquerel");
		set0.add("gray");
		set0.add("kelvin");
		set0.add("mole");
		set0.add("siemends");
		set0.add("weber");
		set0.add("candela");
		set0.add("henry");
		set0.add("kilogram");
		set0.add("newton");
		set0.add("sievert");
		set0.add("coulomb");
		set0.add("hertz");
		set0.add("litre");
		set0.add("ohm");
		set0.add("steradian");
		set0.add("dimensionless");
		set0.add("item");
		set0.add("lumen");
		set0.add("pascal");
		set0.add("tesla");

		SBML_LEVEL_3_BASE_UNITS = Collections.unmodifiableSet(set0);
		
		SBML_LEVEL_2_VERSION_4_BASE_UNITS = new HashSet<String>();
		SBML_LEVEL_2_VERSION_4_BASE_UNITS.addAll(SBML_LEVEL_3_BASE_UNITS);
		SBML_LEVEL_2_VERSION_4_BASE_UNITS.remove("avogadro");
		
		SBML_LEVEL_2_VERSION_1_BASE_UNITS = new HashSet<String>();
		SBML_LEVEL_2_VERSION_1_BASE_UNITS.addAll(SBML_LEVEL_2_VERSION_4_BASE_UNITS);
		SBML_LEVEL_2_VERSION_4_BASE_UNITS.add("Celsius");
		
		
//		Map<String,String> map1 = new HashMap<String,String>();
//		map1.put("mole", SemSimConstants.OPB_NAMESPACE + "OPB_00425"); // Chemical molar amount
		
		//'dimensionless', 
//		 'mole', 'item', 'kilogram', 'gram'
	}
}
