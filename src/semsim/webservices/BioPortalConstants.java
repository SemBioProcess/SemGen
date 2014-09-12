package semsim.webservices;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import semsim.SemSimConstants;

public class BioPortalConstants {
	
	public static final Map<String, String> ONTOLOGY_FULL_NAMES_AND_BIOPORTAL_IDS;
	    static {
	        Map<String, String> aMap = new HashMap<String,String>();
			aMap.put(SemSimConstants.ECG_ONTOLOGY_FULLNAME, "1146");
			aMap.put(SemSimConstants.PHENOTYPE_AND_TRAIT_ONTOLOGY_FULLNAME, "1107");
			aMap.put(SemSimConstants.CHEMICAL_ENTITIES_OF_BIOLOGICAL_INTEREST_FULLNAME, "1007");
			aMap.put(SemSimConstants.ONTOLOGY_OF_PHYSICS_FOR_BIOLOGY_FULLNAME, "1141");
			aMap.put(SemSimConstants.FOUNDATIONAL_MODEL_OF_ANATOMY_FULLNAME, "1053");
			aMap.put(SemSimConstants.GENE_ONTOLOGY_FULLNAME, "1070");
			aMap.put(SemSimConstants.SYSTEMS_BIOLOGY_ONTOLOGY_FULLNAME, "1046");
			aMap.put(SemSimConstants.SNOMEDCT_FULLNAME, "1353");
			aMap.put(SemSimConstants.PROTEIN_ONTOLOGY_FULLNAME, "1062");
			aMap.put(SemSimConstants.CELL_TYPE_ONTOLOGY_FULLNAME, "1006");
			aMap.put(SemSimConstants.BRENDA_TISSUE_ONTOLOGY_FULLNAME, "1005");
			aMap.put(SemSimConstants.UBERON_FULLNAME, "1404");
			aMap.put(SemSimConstants.CLINICAL_MEASUREMENT_ONTOLOGY_FULLNAME, "1583");
			aMap.put(SemSimConstants.HUMAN_DISEASE_ONTOLOGY_FULLNAME, "1009");
			aMap.put(SemSimConstants.MOUSE_ADULT_GROSS_ANATOMY_ONTOLOGY_FULLNAME, "1000");

	        ONTOLOGY_FULL_NAMES_AND_BIOPORTAL_IDS = Collections.unmodifiableMap(aMap);
	    }
}
