package semsim;


import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Scanner;
import java.util.Set;

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
	
	public static Hashtable<String, String[]> createHashtableFromFile(String path) throws FileNotFoundException {
			Set<String> buffer = createSetFromFile(path);
			if (buffer == null) return null;
			
			Hashtable<String, String[]> table = new Hashtable<String, String[]>();
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
			return table;
	}
}

