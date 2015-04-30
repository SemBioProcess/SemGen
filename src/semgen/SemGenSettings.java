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
import java.util.Hashtable;
import java.util.Observable;

import semgen.utilities.file.SemGenOpenFileChooser;
import semsim.utilities.ResourcesManager;
/**
 * Structure for storing and retrieving application settings during run time. A copy can
 * be made for an object's private use.
 */
public class SemGenSettings extends Observable{
	public enum SettingChange {toggletree, showimports, cwsort, toggleproptype}
	public static SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyyHHmmssSSSZ");
	public static SimpleDateFormat sdflog = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z");
	public Hashtable<String, String[]> startsettingstable;
	private final int defwidth = 1280, defheight = 1024;
	private Dimension screensize;
	private Boolean maximize = false;
	private int width = 1280, height = 1024;
	private int xpos = 0, ypos = 0;
	
	public static Color lightblue = new Color(207, 215, 252, 255);
	
	public SemGenSettings() {
		try {
			startsettingstable = ResourcesManager.createHashtableFromFile("cfg/startSettings.txt");
		} 
		catch (FileNotFoundException e3) {
				e3.printStackTrace();
		}
		screensize = Toolkit.getDefaultToolkit().getScreenSize();
		width = screensize.width; height = screensize.height;
		getPreviousScreenSize();
		getPreviousScreenPosition();
	}
	
	public SemGenSettings(SemGenSettings old) {
		screensize = old.screensize;
		startsettingstable = new Hashtable<String, String[]>(old.startsettingstable);
		xpos = old.xpos;
		ypos = old.ypos;
		width = old.width;
		height = old.height;
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
		width = Integer.parseInt(dims[0]);
		height = Integer.parseInt(dims[1]);
		
	}
	
	public void getPreviousScreenPosition() {
		String dim = startsettingstable.get("screenpos")[0];
		String[] dims = dim.trim().split("x");
		xpos = Integer.parseInt(dims[0]);
		ypos = Integer.parseInt(dims[1]);
	}
	
	public Dimension getAppSize() {
		return new Dimension(width, height);
	}
	
	public void setAppSize(Dimension dim) {
		width = dim.width; height = dim.height;
	}
	
	public int getAppWidth() {
		return width;
	}
	
	public int getAppHeight() {
		return height;
	}
	
	public Point getAppLocation() {
		return new Point(xpos, ypos);
	}
	
	public void setAppLocation(Point pt) {
		xpos = pt.x; ypos = pt.y;
	}
		
	public int getAppXPos() {
		return xpos;
	}
	
	public int getAppYPos() {
		return ypos;
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
	
	public void toggleAutoAnnotate() {
		Boolean tog = !organizeByCompositeCompleteness();
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
		notifyObservers(SettingChange.toggletree);
	}
	
	public void toggleShowImports() {
		Boolean tog = !showImports();
		startsettingstable.put("showImports", new String[]{tog.toString()});
		setChanged();
		notifyObservers(SettingChange.showimports);
	}
	
	public void toggleAutoAnnotate(Boolean tog) {
		startsettingstable.put("autoAnnotate", new String[]{tog.toString()});
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
		notifyObservers(SettingChange.toggletree);
	}
	
	public void toggleShowImports(Boolean tog) {
		startsettingstable.put("showImports", new String[]{tog.toString()});
		setChanged();
		notifyObservers(SettingChange.showimports);
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
			writer = new PrintWriter(new FileWriter(new File("cfg/startSettings.txt")));
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
