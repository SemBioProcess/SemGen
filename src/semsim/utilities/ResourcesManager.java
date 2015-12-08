package semsim.utilities;


import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

import semsim.annotation.Ontology;
import semsim.definitions.ReferenceOntologies.ReferenceOntology;

public class ResourcesManager {
	
	public static Set<String> createSetFromFile(String path) throws FileNotFoundException {
		Scanner unitsfilescanner = new Scanner(new File(path));
		
		if (unitsfilescanner.hasNext()) {
			Set<String> stringlist = new HashSet<String>();
			
			String nextline = "";
			while (unitsfilescanner.hasNext()) {
				nextline = unitsfilescanner.nextLine();
				nextline.trim();
				if (nextline.startsWith("#")) { //Allow commenting with hashtag
					continue;
				}
				stringlist.add(nextline);
			}
			unitsfilescanner.close();
			return stringlist;
		}
		unitsfilescanner.close();
		return null;
	}
	
	public static HashMap<String, String[]> createHashMapFromFile(String path, boolean usecommaseparator) throws FileNotFoundException {
			Set<String> buffer = createSetFromFile(path);
			if (buffer == null) return null;
			
			HashMap<String, String[]> table = new HashMap<String, String[]>();
			Set<String> values = new HashSet<String>();
			int semiseparatorindex = 0;
			int commaseparatorindex = 0;
			String key = "";
			for (String nextline : buffer) {
				values.clear();

				semiseparatorindex = nextline.indexOf(";");
				key = nextline.substring(0, semiseparatorindex);
				Boolean repeat = true;
				if(nextline.indexOf(";") == nextline.length()-1){ // If there is nothing after the ;
					repeat = false;
				}
				else{
					nextline = nextline.substring(semiseparatorindex + 2, nextline.length());
				}
				if(usecommaseparator)
					while (repeat) {
						if (!nextline.contains(",")) {
							values.add(nextline);
							repeat = false;
							break;
						}
						commaseparatorindex = nextline.indexOf(",");
						values.add(nextline.substring(0, nextline.indexOf(",")));
						commaseparatorindex = nextline.indexOf(",");
						nextline = nextline.substring(commaseparatorindex + 2,
								nextline.length());
					}
				else values.add(nextline);
				
				table.put(key, (String[]) values.toArray(new String[] {}));
			}
			return table;
	}
	
	// Reader for base unit to OPB class mapping file
		public static HashMap<HashMap<String, Double>, String[]> createHashMapFromBaseUnitFile(String path) throws FileNotFoundException {
			Scanner unitsfilescanner = new Scanner(new File(path));
			if (unitsfilescanner.hasNext()) {
				HashMap<HashMap<String, Double>, String[]> table = new HashMap<HashMap<String, Double>, String[]>();
				Set<String> opbclasses = new HashSet<String>();
				int semiseparatorindex = 0;
				int commaseparatorindex = 0;
				int commentseparatorindex = 0;
				String nextline = "";
				String baseunitstring = "";
				while (unitsfilescanner.hasNext()) {
					// Hashtable of baseunits:exponents for a given OPB class
					HashMap<String, Double> baseunitcombination = new HashMap<String, Double>();
					opbclasses.clear();
					nextline = unitsfilescanner.nextLine();
					nextline.trim();
					if (nextline.startsWith("#")) { //Allow commenting with hashtag
						break;
					}
					// Allow in-line commenting with hashtag 
					else if (nextline.contains("#")) {
						commentseparatorindex = nextline.indexOf("#");
						nextline = nextline.substring(0, commentseparatorindex).trim();
					}
					semiseparatorindex = nextline.indexOf(";");
					baseunitstring = nextline.substring(0, semiseparatorindex);
					// Parse baseunitstring into hashtable as "baseunit"+"^"+"exponent"
					String[] baseunitarray = baseunitstring.split(",");
					for (int i = 0; i < baseunitarray.length; i++) {
						String[] baseunitexponent = baseunitarray[i].split("\\^");
						String baseunit = baseunitexponent[0].trim();
						Double exponent = Double.parseDouble(baseunitexponent[1]);
						baseunitcombination.put(baseunit, exponent);
					}
					Boolean repeat = true;
					if(nextline.indexOf(";") == nextline.length()-1){ // If there is nothing after the ;
						repeat = false;
					}
					else{
						nextline = nextline.substring(semiseparatorindex + 2, nextline.length());
					}
					while (repeat) {
						if (!nextline.contains(",")) {
							opbclasses.add(nextline);
							repeat = false;
							break;
						}
						commaseparatorindex = nextline.indexOf(",");
						opbclasses.add(nextline.substring(0, nextline.indexOf(",")));
						commaseparatorindex = nextline.indexOf(",");
						nextline = nextline.substring(commaseparatorindex + 2,
								nextline.length());
					}
					table.put(baseunitcombination, (String[]) opbclasses.toArray(new String[] {}));
				}
				unitsfilescanner.close();
				return table;
			}		 
			unitsfilescanner.close();
			return null;
		}
		
		public static HashSet<Ontology> loadOntologyDescriptions() throws FileNotFoundException {
			HashSet<Ontology> ontologies = new HashSet<Ontology>();
			for (ReferenceOntology ro : ReferenceOntology.values()) {
				ontologies.add(new Ontology(ro));
			}
			Set<String> buffer = createSetFromFile("cfg/local_ontologies.txt");
			
			for (String rawont : buffer) {
				rawont.trim();
				rawont.replace(".", "");
				String[] split = rawont.split(",");
				split[2].replace("{", "");
				split[2].replace("}", "");
				String[] nspaces = split[2].split(" ");
				if (split.length==5) {
					ontologies.add(new Ontology(split[0].trim(), split[1].trim(), nspaces, split[3], split[4]));
				}
				else {
					ontologies.add(new Ontology(split[0].trim(), split[1].trim(), nspaces, split[3]));
				}
			}
			
			return ontologies;
		}
}

