package semsim.utilities;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

import org.apache.commons.io.IOUtils;

import semsim.SemSimLibrary;
import semsim.annotation.Ontology;
import semsim.definitions.ReferenceOntologies.ReferenceOntology;

/**
 * Class for reading files either outside or inside the SemSim package and 
 * organizing the info in lookup tables, reference sets, etc.
 * @author mneal
 *
 */

public class ResourcesManager {
	
	
	/**
	 * Create a String set from a file's contents
	 * @param path Path to file
	 * @return Set of Strings representing file's contents
	 * @throws FileNotFoundException
	 */
	public static Set<String> createSetFromFile(String path) throws FileNotFoundException {
		Scanner unitsfilescanner = new Scanner(new File(path));
		return createSetFromScanner(unitsfilescanner);
	}
	
	
	/**
	 * Create a String set from a file within the SemSim package
	 * @param internalpath Internal path to file in SemSim package
	 * @return Set of Strings representing file's contents
	 */
	public static Set<String> createSetFromResource(String internalpath){
		
		String resourcestring = getStringFromResourceStream(internalpath);
		return createSetFromScanner(new Scanner(resourcestring));
	}
	
	
	/**
	 * Create a String set from the contents of a Scanner object
	 * @param scanner A Scanner object
	 * @return String set representing contents of the Scanner object
	 */
	private static Set<String> createSetFromScanner(Scanner scanner){
		if (scanner.hasNext()) {
			Set<String> stringlist = new HashSet<String>();
			
			String nextline = "";
			while (scanner.hasNext()) {
				nextline = scanner.nextLine();
				nextline.trim();
				if (nextline.startsWith("#")) { //Allow commenting with hashtag
					continue;
				}
				stringlist.add(nextline);
			}
			scanner.close();
			return stringlist;
		}
		scanner.close();
		return null;
	}
	
	
	/**
	 * @param internalpath Internal path to a file contained in SemSim package
	 * @return File contents as a String
	 */
	private static String getStringFromResourceStream(String internalpath){
		InputStream stream = ResourcesManager.class.getClassLoader().getResourceAsStream(internalpath);
		StringWriter writer = new StringWriter();

		if(stream != null){
			try {
				IOUtils.copy(stream, writer, StandardCharsets.UTF_8);
				stream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return writer.toString();
	}
	
	
	/**
	 * Read contents of a file into a HashMap. Semi-colon indicates separator
	 * between key String and the key's values.
	 * @param path Path to file
	 * @param usecommaseparator Whether to use the comma character as the separator between 
	 * entries in the String[] part of the mapping
	 * @return HashMap that relates a String key with an array of String values
	 * @throws FileNotFoundException
	 */
	public static HashMap<String, String[]> createHashMapFromFile(String path, boolean usecommaseparator) throws FileNotFoundException {
		Set<String> buffer = createSetFromFile(path);
		return buildHashMapFromStringSet(buffer, usecommaseparator);
	}
	
	
	/**
	 * Creates a HashMap that relates String indexes to String arrays.
	 * @param stringset A set of Strings to process (that use a semi-colon to separate key-value pairs)
	 * @param usecommaseparator Whether to use the comma character to create separate the elements of the 
	 * String[] object in a mapping. 
	 * @return A HashMap that relates a String index to a String array.
	 */
	private static HashMap<String,String[]> buildHashMapFromStringSet(Set<String> stringset, boolean usecommaseparator){

		if (stringset == null) return null;
		
		HashMap<String, String[]> table = new HashMap<String, String[]>();
		Set<String> values = new HashSet<String>();
		int semiseparatorindex = 0;
		int commaseparatorindex = 0;
		String key = "";
		for (String nextline : stringset) {
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
	
	/**
	 * For loading HashMaps from resources internally stored in SemSim packages
	 * @param internalpath Internal path to file
	 * @param usecommaseparator Whether to use the comma character to create separate the elements of the 
	 * String[] object in a mapping.
	 * @return A HashMap that relates a String index to a String array.
	 * @throws IOException
	 */
	public static HashMap<String,String[]> createHashMapFromResource(String internalpath, boolean usecommaseparator) throws IOException{
		String resourcestring = getStringFromResourceStream(internalpath);
		Set<String> stringset = createSetFromScanner(new Scanner(resourcestring));
		
		return buildHashMapFromStringSet(stringset, usecommaseparator);
	}
	
	
	/**
	 * Reader for base unit to OPB class mapping file
	 * @param path Path to file that indicates valid OPB physical property classes 
	 * for fundamental physical unit combinations
	 * @return Table that indicates valid OPB physical property classes for certain 
	 * combinations of fundamental physical units
	 * @throws FileNotFoundException
	 */
	public static HashMap<HashMap<String, Double>, String[]> createHashMapFromBaseUnitFile(String path) throws FileNotFoundException {
		
		Scanner unitsfilescanner;
		
		if(path.startsWith(SemSimLibrary.DEFAULT_CFG_PATH)){
			String resourcestring = getStringFromResourceStream(path);
			unitsfilescanner = new Scanner(resourcestring);
		}
		else unitsfilescanner = new Scanner(new File(path));
				
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
	
	
	/**
	 * Get externally-stored info about reference ontologies used in annotation
	 * @param cfgpath Path to file containing formatted ontology descriptions
	 * @return A Set of {@link Ontology} objects corresponding to the contents of
	 * the input file
	 * @throws IOException
	 */
	public static HashSet<Ontology> loadOntologyDescriptions(String cfgpath) throws IOException {
		HashSet<Ontology> ontologies = new HashSet<Ontology>();
		for (ReferenceOntology ro : ReferenceOntology.values()) {
			ontologies.add(new Ontology(ro));
		}
		
		Set<String> buffer = cfgpath.startsWith(SemSimLibrary.DEFAULT_CFG_PATH) ?
				createSetFromResource(SemSimLibrary.DEFAULT_CFG_PATH + "local_ontologies.txt") :
					createSetFromFile(cfgpath + "local_ontologies.txt");

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

