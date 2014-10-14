package semgen;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Scanner;
import java.util.Set;


public class ResourcesManager {

	public void createResourcesFolder(){
		new File("resources").mkdir();
	}
	
	public static Hashtable<String, String[]> createHashtableFromResource(String path) throws FileNotFoundException {
		Hashtable<String, String[]> table = new Hashtable<String, String[]>();
		Scanner filescanner = new Scanner(SemGenGUI.class.getResourceAsStream(path));
		String nextline = "";
		String key = "";
		Set<String> values = new HashSet<String>();

		if (!filescanner.hasNext()) {
			SemGenGUI.logfilewriter.println("Could not create hashtable from file: " + path);
		} else {
			int semiseparatorindex = 0;
			int commaseparatorindex = 0;
			while (filescanner.hasNext()) {
				values.clear();
				nextline = filescanner.nextLine();
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
					if (nextline.contains(",")) {
						commaseparatorindex = nextline.indexOf(",");
						values.add(nextline.substring(0, nextline.indexOf(",")));
						commaseparatorindex = nextline.indexOf(",");
						nextline = nextline.substring(commaseparatorindex + 2,
								nextline.length());
					} else {
						values.add(nextline);
						repeat = false;
					}
				}
				if(values.isEmpty()){values.add("");}
				table.put(key, (String[]) values.toArray(new String[] {}));
			}
		}
		return table;
	}
	
	
	public static Hashtable<String, String[]> createHashtableFromFile(String path) throws FileNotFoundException {
		Hashtable<String, String[]> table = new Hashtable<String, String[]>();
		Scanner unitsfilescanner = new Scanner(new File(path));
		String nextline = "";
		String key = "";
		Set<String> values = new HashSet<String>();

		if (!unitsfilescanner.hasNext()) {
			SemGenGUI.logfilewriter.println("Could not create hashtable from file: " + path);
		} else {
			int semiseparatorindex = 0;
			int commaseparatorindex = 0;
			while (unitsfilescanner.hasNext()) {
				values.clear();
				nextline = unitsfilescanner.nextLine();
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
					if (nextline.contains(",")) {
						commaseparatorindex = nextline.indexOf(",");
						values.add(nextline.substring(0, nextline.indexOf(",")));
						commaseparatorindex = nextline.indexOf(",");
						nextline = nextline.substring(commaseparatorindex + 2,
								nextline.length());
					} else {
						values.add(nextline);
						repeat = false;
					}
				}
				table.put(key, (String[]) values.toArray(new String[] {}));
			}
		}
		return table;
	}
	
	
	public static File writeResourceToTempDir(String nameoffile) throws IOException{
		InputStream in = new FileInputStream(new File("cfg/" + nameoffile));
		File file = new File(SemGenGUI.tempdir + "/" + nameoffile);
		if (file.exists()){}
		else
		  {
			FileOutputStream fos = new FileOutputStream(file.getAbsolutePath());
		    byte[] buf = new byte[256];
		    int read = 0;
		    while ((read = in.read(buf)) > 0) {
		      fos.write(buf, 0, read);
		    }
		}
		return file;
	}
}

