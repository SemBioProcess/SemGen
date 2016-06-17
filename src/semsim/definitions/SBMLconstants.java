package semsim.definitions;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import semsim.SemSimLibrary;

public class SBMLconstants {

	public static final Map<String,String> SBML_LEVEL_2_RESERVED_UNITS_MAP;
	public static final Set<String> SBML_LEVEL_3_BASE_UNITS;
	public static final Set<String> SBML_LEVEL_2_VERSION_4_BASE_UNITS;
	public static final Set<String> SBML_LEVEL_2_VERSION_1_BASE_UNITS;
	
	public static final Set<URI> OPB_PROPERTIES_FOR_COMPARTMENTS;
	public static final Set<URI> OPB_PROPERTIES_FOR_SPECIES;
	public static final Set<URI> OPB_PROPERTIES_FOR_REACTIONS;
	
	static {
		
		// Mapping between reserved units in SBML level 2 models and their default base units
		LinkedHashMap<String,String> map0 = new LinkedHashMap<String,String>();		
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
		
		
		Set<URI> set1 = new HashSet<URI>();
		set1.add(SemSimLibrary.OPB_FLUID_VOLUME_URI);
		set1.add(SemSimLibrary.OPB_AREA_OF_SPATIAL_ENTITY_URI);
		set1.add(SemSimLibrary.OPB_SPAN_OF_SPATIAL_ENTITY_URI);
		
		OPB_PROPERTIES_FOR_COMPARTMENTS = Collections.unmodifiableSet(set1);
		
		Set<URI> set2 = new HashSet<URI>();
		set2.add(SemSimLibrary.OPB_CHEMICAL_CONCENTRATION_URI);
		set2.add(SemSimLibrary.OPB_CHEMICAL_MOLAR_AMOUNT_URI);
		set2.add(SemSimLibrary.OPB_PARTICLE_COUNT_URI);
		set2.add(SemSimLibrary.OPB_PARTICLE_CONCENTRATION_URI);
		set2.add(SemSimLibrary.OPB_MASS_OF_SOLID_ENTITY_URI);
		set2.add(SemSimLibrary.OPB_MASS_LINEAL_DENSITY_URI);
		set2.add(SemSimLibrary.OPB_MASS_AREAL_DENSITY_URI);
		set2.add(SemSimLibrary.OPB_MASS_VOLUMETRIC_DENSITY_URI);
		
		OPB_PROPERTIES_FOR_SPECIES = Collections.unmodifiableSet(set2);
		
		Set<URI> set3 = new HashSet<URI>();
		set3.add(SemSimLibrary.OPB_CHEMICAL_MOLAR_FLOW_RATE_URI);
		set3.add(SemSimLibrary.OPB_MATERIAL_FLOW_RATE_URI);
		set3.add(SemSimLibrary.OPB_PARTICLE_FLOW_RATE_URI);
		
		OPB_PROPERTIES_FOR_REACTIONS = Collections.unmodifiableSet(set3);
	}
}
