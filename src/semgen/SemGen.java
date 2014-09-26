package semgen;

//SemGen Initialization
import java.awt.Color;
import java.awt.HeadlessException;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

import org.semanticweb.owlapi.model.OWLException;

import semgen.semgenmenu.SemGenMenuBar;
import semsim.SemSimLibrary;

public class SemGen extends JFrame{
	private static final long serialVersionUID = 1L;
	
	public static double version = 2.0;
	private final static int WINDOWS=1;
	private static final int MACOSX=2;
	
	public static Date datenow = new Date();
	public static PrintWriter logfilewriter;
	private SemGenSettings settings = new SemGenSettings();
	
	public static File tempdir = new File(System.getProperty("java.io.tmpdir"));
	public static final String logfileloc = tempdir.getAbsolutePath() + "/SemGen_log.txt";
	public static final File logfile = new File(logfileloc);
	
	SemGenGUI contentpane = null; 
	
	/** Main method for running an instance of SemGen */
	public static void main(String[] args) {
		try {
			logfilewriter = new PrintWriter(new FileWriter(logfile));
		} catch (IOException e4) {
			e4.printStackTrace();
		}
		
		System.out.print("Loading SemGen...");
		logfilewriter.println("Loading SemGen");
		
		JFrame frame = new SemGen();
		frame.setVisible(true);
	}

	public SemGen() {
		super("OSXAdapter");
		SemSimLibrary semsimlib = new SemSimLibrary();
		
		try {
			UIManager.put("nimbusOrange", new Color(51,98,140));
			UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
		    for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
		        if ("Nimbus".equals(info.getName())) {
		            UIManager.setLookAndFeel(info.getClassName());
		            break;
		        }
			}
		    OSValidation();
		} catch (Exception e)
			{e.printStackTrace();}

		// Need this for programmatic use of jsbatch
		System.setProperty("jsim.home", "./jsimhome");
		
		setTitle(":: S e m  G e n ::");
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		UniversalActions act = new UniversalActions();
		SemGenMenuBar menubar = new SemGenMenuBar(settings, act);
		act.addObserver(menubar);
		contentpane = new SemGenGUI(settings, semsimlib, act);
		act.addObserver(contentpane);
		
		setContentPane(contentpane);
		setJMenuBar(menubar);
		
		addListeners();
		
		setSize(settings.getAppSize());
		setLocation(settings.getAppXPos(), settings.getAppYPos());
		setVisible(true);
		System.out.println("Loaded.");
		
		this.pack();
		act.newTask();
		logfilewriter.println("Session started on: " + SemGenSettings.sdflog.format(datenow) + "\n");
	}
	
	private void OSValidation() {
		int OS = 0;
		if (OSValidator.isMac()) OS = MACOSX;
		else if (OSValidator.isWindows()) OS = WINDOWS;
		
		File libsbmlfile = null;
		
		switch (OS) { 
		case WINDOWS :
			libsbmlfile = new File("cfg/sbmlj.dll"); 
			break;
		case MACOSX :
		      libsbmlfile = new File("cfg/libsbmlj.jnilib");
		      new OSXAdapter(this);
		      break;
		default : 
			libsbmlfile = new File("cfg/libsbmlj.so");
		}
		
		// Needed because JSim API uses libSBML native library
		if(libsbmlfile.exists()){
			System.load(libsbmlfile.getAbsolutePath());
		}
		else 
			JOptionPane.showMessageDialog(this, "Couldn't open " + libsbmlfile.getAbsolutePath() + " for loading.");
	}
	
	private void addListeners() {
		//On frame resize or move, store new dimensions
				addComponentListener(new ComponentAdapter() {
					public void componentResized(ComponentEvent e) {
						settings.setAppSize(e.getComponent().getSize());
					}
					
					public void componentMoved(ComponentEvent e) {
						settings.setAppLocation(e.getComponent().getLocationOnScreen());
					}
				});
				
				// Set closing behavior
				addWindowListener(new WindowAdapter() {
					public void windowClosing(WindowEvent e) {
						try {
							quit();
						} catch (HeadlessException | OWLException e1) {
							e1.printStackTrace();
						}
					}
				});
	}
	
	public boolean quit() throws HeadlessException, OWLException {
		return contentpane.quit();
	}
			
}