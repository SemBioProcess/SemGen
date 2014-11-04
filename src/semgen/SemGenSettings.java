package semgen;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Hashtable;
import java.util.Observable;

import semgen.resource.file.SemGenOpenFileChooser;
import semsim.ResourcesManager;

public class SemGenSettings extends Observable{
	public static SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyyHHmmssSSSZ");
	public static SimpleDateFormat sdflog = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z");
	public Hashtable<String, String[]> startsettingstable;
	private int width = 900, height = 720;
	private int xpos = 0, ypos = 0;
	
	public static Color lightblue = new Color(207, 215, 252, 255);
	
	public SemGenSettings() {
		try {
				startsettingstable = ResourcesManager.createHashtableFromFile("cfg/startSettings.txt");
		} 
		catch (FileNotFoundException e3) {
				e3.printStackTrace();
		}
			
			// Set the window position and size
			xpos = Integer.parseInt(startsettingstable.get("XstartPosition")[0].trim());
			ypos = Integer.parseInt(startsettingstable.get("YstartPosition")[0].trim());
			width = Integer.parseInt(startsettingstable.get("startWidth")[0].trim());
			height = Integer.parseInt(startsettingstable.get("startHeight")[0].trim());
	}
	
	public SemGenSettings(SemGenSettings old) {
		startsettingstable = old.startsettingstable;
		xpos = old.xpos;
		ypos = old.ypos;
		width = old.width;
		height = old.height;
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
	
	public Dimension getAppLocation() {
		return new Dimension(xpos, ypos);
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
	}
	
	public void toggleByPropertyType() {
		Boolean tog = !organizeByPropertyType();
		startsettingstable.put("organizeByPropertyType", new String[]{tog.toString()});
	}
	
	public void toggleDisplayMarkers() {
		Boolean tog = !useDisplayMarkers();
		startsettingstable.put("displayMarkers", new String[]{tog.toString()});
	}
	
	public void toggleTreeView() {
		Boolean tog = !useTreeView();
		startsettingstable.put("treeView", new String[]{tog.toString()});
	}
	
	public void toggleShowImports() {
		Boolean tog = !showImports();
		startsettingstable.put("showImports", new String[]{tog.toString()});
	}
	
	public void toggleAutoAnnotate(Boolean tog) {
		startsettingstable.put("autoAnnotate", new String[]{tog.toString()});
	}
	
	public void toggleCompositeCompleteness(Boolean tog) {
		startsettingstable.put("sortbyCompositeCompleteness", new String[]{tog.toString()});
	}
	
	public void toggleByPropertyType(Boolean tog) {
		startsettingstable.put("organizeByPropertyType", new String[]{tog.toString()});
	}
	
	public void toggleDisplayMarkers(Boolean tog) {
		startsettingstable.put("displayMarkers", new String[]{tog.toString()});
	}
	
	public void toggleTreeView(Boolean tog) {
		startsettingstable.put("treeView", new String[]{tog.toString()});
	}
	
	public void toggleShowImports(Boolean tog) {
		startsettingstable.put("showImports", new String[]{tog.toString()});
	}
	public String getHelpURL() {
		return startsettingstable.get("helpURL")[0];
	}
	
	public void storeSettings() throws URISyntaxException {
		PrintWriter writer;
		try {
			writer = new PrintWriter(new FileWriter(new File("cfg/startSettings.txt")));
			writer.println("XstartPosition; " + xpos);
			writer.println("YstartPosition; " + ypos);
			writer.println("startHeight; " + height);
			writer.println("startWidth; " + width);
			writer.println("startDirectory; " + SemGenOpenFileChooser.currentdirectory.getAbsolutePath());
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
