package semgen;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Observable;

import semgen.utilities.file.SemGenOpenFileChooser;
import semsim.utilities.ResourcesManager;
/**
 * Structure for storing and retrieving application settings during run time. A copy can
 * be made for an object's private use.
 */
public class SemGenSettings extends Observable{
	public enum SettingChange {TOGGLETREE, SHOWIMPORTS, cwsort, toggleproptype, autoannotatemapped}
	public static SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyyHHmmssSSSZ");
	public static SimpleDateFormat sdflog = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z");
	private HashMap<String, String[]> startsettingstable;
	private Boolean maximize = false;
	private Boolean autoannmapped;
	
	private final int defwidth = 1280, defheight = 1024;
	private Dimension screensize;
	private Dimension framesize;
	private Point framepos;	
	
	public static Color lightblue = new Color(207, 215, 252, 255);
	
	public SemGenSettings() {
		try {
			String settingspath = SemGen.cfgwritepath + "startSettings.txt";
			File settingsfile = new File(settingspath);
			
			if(settingsfile.exists())
				startsettingstable = ResourcesManager.createHashMapFromFile(settingspath, true);
			else
				startsettingstable = ResourcesManager.createHashMapFromFile(SemGen.cfgreadpath + "startSettings.txt", true);
		} 
		catch (FileNotFoundException e3) {
				e3.printStackTrace();
		}
		screensize = Toolkit.getDefaultToolkit().getScreenSize();
		getPreviousScreenSize();
		getPreviousScreenPosition();
		autoannmapped = startsettingstable.get("autoAnnotateMapped")[0].trim().equals("true");
	}
	
	public SemGenSettings(SemGenSettings old) {
		screensize = old.screensize;
		startsettingstable = new HashMap<String, String[]>(old.startsettingstable);
		framepos = old.framepos;
		framesize = old.framesize;
		autoannmapped = old.autoannmapped;
	}
	
	public int scaleWidthforScreen(int w) {
		return Math.round(w*screensize.width/defwidth);
	}
	
	public int scaleHeightforScreen(int h) {
		return Math.round(h*screensize.height/defheight);
	}
	
	public void getPreviousScreenSize() {
		String dim = startsettingstable.get("screensize")[0];
		String[] dims = dim.trim().split("x");
		framesize = new Dimension(Integer.parseInt(dims[0]), Integer.parseInt(dims[1]));
		
	}
	
	public void getPreviousScreenPosition() {
		String dim = startsettingstable.get("screenpos")[0];
		String[] dims = dim.trim().split("x");
		framepos = new Point(Integer.parseInt(dims[0]), Integer.parseInt(dims[1]));
	}
	
	public Dimension getAppSize() {
		return new Dimension(framesize);
	}
	
	public Point getAppCenter() {
		return new Point(framepos.x+framesize.width/2, framepos.y + framesize.height/2);
	}
	
	public void setAppSize(Dimension dim) {
		framesize.width = dim.width;
		framesize.height = dim.height;
	}
	
	public Point centerDialogOverApplication(Dimension dialogsize) {
		Point appcenter = getAppCenter();
		Dimension dialogradius = new Dimension(dialogsize.width/2, dialogsize.height/2);

		int yloc = appcenter.y-dialogradius.height;
		if (yloc < 0) {
			yloc = 0;
		}
		else if (yloc - 100 >= 0) {
			yloc -= 100;
		}
		if ( yloc+dialogsize.height > getScreenHeight()) {
			yloc = getScreenHeight() - dialogsize.height;
		}
		int xloc = appcenter.x-dialogradius.width;
		if (xloc < 0 ) {
			xloc = 0;
		}
		else if (appcenter.x+dialogsize.width > getScreenWidth() ) {
			xloc = getScreenWidth() - dialogsize.width;
		}
		return new Point(xloc, yloc);
	}
	
	public int getAppWidth() {
		return framesize.width;
	}
	
	public int getAppHeight() {
		return framesize.height;
	}
	
	public int getScreenWidth() {
		return screensize.width;
	}
	
	public int getScreenHeight() {
		return screensize.height;
	}
	
	public Point getAppLocation() {
		return new Point(framepos);
	}
	
	public void setAppLocation(Point pt) {
		framepos.x = pt.x; framepos.y = pt.y;
	}
		
	public int getAppXPos() {
		return framepos.x;
	}
	
	public int getAppYPos() {
		return framepos.y;
	}
	public String getStartDirectory() {
		return startsettingstable.get("startDirectory")[0];
	}
	
	public Boolean maximizeScreen() {
		String max = startsettingstable.get("maximize")[0];
		if (max==null) return false;
		return startsettingstable.get("maximize")[0].trim().equals("true");
	}
	
	public Boolean doAutoAnnotate() {
		return startsettingstable.get("autoAnnotate")[0].trim().equals("true");
	}

	public Boolean showImports() {
		return startsettingstable.get("showImports")[0].trim().equals("true");
	}
	
	public Boolean useDisplayMarkers() {
		return startsettingstable.get("displayMarkers")[0].trim().equals("true");
	}
	
	public Boolean useTreeView() {
		return startsettingstable.get("treeView")[0].trim().equals("true");
	}
	
	public Boolean organizeByPropertyType() {
		return startsettingstable.get("organizeByPropertyType")[0].trim().equals("true");
	}
	
	public Boolean organizeByCompositeCompleteness() {
		return startsettingstable.get("sortbyCompositeCompleteness")[0].trim().equals("true");
	}
	
	public Boolean doAutoAnnotateMapped() {
		return startsettingstable.get("autoAnnotateMapped")[0].trim().equals("true");
	}
	
	public void toggleAutoAnnotate() {
		Boolean tog = !doAutoAnnotate();
		startsettingstable.put("autoAnnotate", new String[]{tog.toString()});
	}
	
	public void toggleCompositeCompleteness() {
		Boolean tog = !organizeByCompositeCompleteness();
		startsettingstable.put("sortbyCompositeCompleteness", new String[]{tog.toString()});
		setChanged();
		notifyObservers(SettingChange.cwsort);
	}
	
	public void toggleByPropertyType() {
		Boolean tog = !organizeByPropertyType();
		startsettingstable.put("organizeByPropertyType", new String[]{tog.toString()});
		setChanged();
		notifyObservers(SettingChange.cwsort);
	}
	
	public void toggleDisplayMarkers() {
		Boolean tog = !useDisplayMarkers();
		startsettingstable.put("displayMarkers", new String[]{tog.toString()});
		setChanged();
		notifyObservers(SettingChange.toggleproptype);
	}
	
	public void toggleTreeView() {
		Boolean tog = !useTreeView();
		startsettingstable.put("treeView", new String[]{tog.toString()});
		setChanged();
		notifyObservers(SettingChange.TOGGLETREE);
	}
	
	public void toggleShowImports() {
		Boolean tog = !showImports();
		startsettingstable.put("showImports", new String[]{tog.toString()});
		setChanged();
		notifyObservers(SettingChange.SHOWIMPORTS);
	}
	
	public void toggleAutoAnnotateMapped(){
		Boolean tog = !doAutoAnnotateMapped();
		startsettingstable.put("autoAnnotateMapped", new String[]{tog.toString()});
		setChanged();
		notifyObservers(SettingChange.autoannotatemapped);
	}
	
	public void toggleAutoAnnotate(Boolean tog) {
		startsettingstable.put("autoAnnotate", new String[]{tog.toString()});
	}
	
	public void toggleAutoAnnotateMapped(Boolean tog){
		startsettingstable.put("autoAnnotateMapped", new String[]{tog.toString()});
	}
	
	public void toggleCompositeCompleteness(Boolean tog) {
		startsettingstable.put("sortbyCompositeCompleteness", new String[]{tog.toString()});
		setChanged();
		notifyObservers(SettingChange.cwsort);
	}
	
	public void toggleByPropertyType(Boolean tog) {
		startsettingstable.put("organizeByPropertyType", new String[]{tog.toString()});
		setChanged();
		notifyObservers(SettingChange.cwsort);
	}
	
	public void toggleDisplayMarkers(Boolean tog) {
		startsettingstable.put("displayMarkers", new String[]{tog.toString()});
		setChanged();
		notifyObservers(SettingChange.toggleproptype);
	}
	
	public void toggleTreeView(Boolean tog) {
		startsettingstable.put("treeView", new String[]{tog.toString()});
		setChanged();
		notifyObservers(SettingChange.TOGGLETREE);
	}
	
	public void toggleShowImports(Boolean tog) {
		startsettingstable.put("showImports", new String[]{tog.toString()});
		setChanged();
		notifyObservers(SettingChange.SHOWIMPORTS);
	}
	
	public String getHelpURL() {
		return startsettingstable.get("helpURL")[0];
	}
	
	public void setIsMaximized(boolean maxed) {
		maximize = maxed;
	}
	
	
	public void storeSettings() throws URISyntaxException {
		PrintWriter writer;
		try {
			writer = new PrintWriter(new FileWriter(new File(SemGen.cfgwritepath + "startSettings.txt")));
			writer.println("startDirectory; " + SemGenOpenFileChooser.currentdirectory.getAbsolutePath());
			writer.println("maximize; " + maximize.toString());
			writer.println("screensize; " + this.getAppWidth() + "x" + this.getAppHeight());
			writer.println("screenpos; " + this.getAppXPos() + "x" + this.getAppYPos());
			writer.println("autoAnnotate; " + doAutoAnnotate().toString());
			writer.println("showImports; " + showImports().toString());
			writer.println("displayMarkers; " + useDisplayMarkers().toString());
			writer.println("organizeByPropertyType; " + organizeByPropertyType().toString());
			writer.println("sortbyCompositeCompleteness; " + organizeByCompositeCompleteness());
			writer.println("treeView; " + useTreeView().toString());
			writer.println("helpURL; " + getHelpURL());
			writer.println("autoAnnotateMapped; " + doAutoAnnotateMapped().toString());
			
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public SemGenSettings makeCopy() {
		return this;
	}
}
