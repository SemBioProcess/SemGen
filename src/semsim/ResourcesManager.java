package semsim;


import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Scanner;
import java.util.Set;

public class ResourcesManager {
	
	public static Hashtable<String, String[]> createHashtableFromFile(String path) throws FileNotFoundException {
		Scanner unitsfilescanner = new Scanner(new File(path));
		
		if (unitsfilescanner.hasNext()) {
			Hashtable<String, String[]> table = new Hashtable<String, String[]>();
			Set<String> values = new HashSet<String>();
			int semiseparatorindex = 0;
			int commaseparatorindex = 0;
			String nextline = "";
			String key = "";
			while (unitsfilescanner.hasNext()) {
				values.clear();
				nextline = unitsfilescanner.nextLine();
				nextline.trim();
				if (nextline.startsWith("#")) { //Allow commenting with hashtag
					break;
				}
				semiseparatorindex = nextline.indexOf(";");
				key = nextline.substring(0, semiseparatorindex);
				Boolean repeat = true;
				if(nextline.indexOf(";") == nextline.length()-1){ // If there is nothing after the ;
					repeat = false;
				}
				else{
					nextline = nextline.substring(semiseparatorindex + 2, nextline.length());
				}
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
				table.put(key, (String[]) values.toArray(new String[] {}));
			}
			unitsfilescanner.close();
			return table;
		}		 
		unitsfilescanner.close();
		return null;
	}
	
	// Reader for base unit to OPB class mapping file
	public static Hashtable<Hashtable<String, Double>, String[]> createHashtableFromBaseUnitFile(String path) throws FileNotFoundException {
		Scanner unitsfilescanner = new Scanner(new File(path));
		
		if (unitsfilescanner.hasNext()) {
			Hashtable<Hashtable<String, Double>, String[]> table = new Hashtable<Hashtable<String, Double>, String[]>();
			Set<String> opbclasses = new HashSet<String>();
			int semiseparatorindex = 0;
			int commaseparatorindex = 0;
			int commentseparatorindex = 0;
			String nextline = "";
			String baseunitstring = "";
			while (unitsfilescanner.hasNext()) {
				// Hashtable of baseunits:exponents for a given OPB class
				Hashtable<String, Double> baseunitcombination = new Hashtable<String, Double>();
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
}

