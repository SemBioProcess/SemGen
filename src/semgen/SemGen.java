/**
 * Application entry point. Resource verification and initialization, application configuration
 * and Frame creation are all specified in this class.
 */

package semgen;

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
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.semanticweb.owlapi.model.OWLException;

import semgen.SemGenGUI;
import semgen.menu.SemGenMenuBar;
import semgen.utilities.OntologyCache;
import semgen.utilities.SemGenError;
import semgen.utilities.SemGenFont;
import semgen.utilities.file.SemGenFileChooser;
import semgen.utilities.uicomponent.SemGenDialog;
import semsim.SemSimLibrary;
import semsim.reading.ModelReader;
import semsim.utilities.ErrorLog;
import semsim.writing.ModelWriter;

public class SemGen extends JFrame implements Observer{
	private static final long serialVersionUID = 1L;

	public static String version = "3.0.4";
	public static PrintWriter logfilewriter;
	public static File tempdir = new File(System.getProperty("java.io.tmpdir"));
	public static final String logfileloc = tempdir.getAbsolutePath() + "/SemGen_log.txt";
	public static OntologyCache termcache;
	public static boolean debug = true;
	private static boolean openstage = false;
	
	//A class for application level events such as exiting and creating new tabs
	public static GlobalActions gacts = new GlobalActions();

	private final static File logfile = new File(logfileloc);
	
	private final static int WINDOWS=1;
	private static final int MACOSX=2;
	
	public static Date datenow = new Date();
	public SimpleDateFormat sdflog = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z");
	
	//The application store for the default settings
	private SemGenSettings settings; 
	//A library of SemSim constants and definitions. This is created once and referenced
	//without modification by the rest of the program.
	public static SemSimLibrary semsimlib;
	
	public static String cfgreadpath = "cfg/";
	public static String cfgwritepath = "cfg/";
	public static String examplespath = "examples/";

	private SemGenGUI contentpane = null; 
	
	/** Main method for running an instance of SemGen 
	 * @throws SecurityException 
	 * @throws NoSuchMethodException */
	public static void main(String[] args) throws NoSuchMethodException, SecurityException {
		setup(args);
		 SwingUtilities.invokeLater(new Runnable() {
		     public void run() {
		        createAndShowGUI();
		     }
		  });
	}
	
	public static void setup(String[] args) throws NoSuchMethodException, SecurityException {
		for (String arg : args) {
			if (arg.equals("-debug") || arg.equals("-Debug") || arg.equals("-d")) {
				debug = true;
			}
			if (arg.equals("-stage")) {
				openstage = true;
			}
		}
		try {
			logfilewriter = new PrintWriter(new FileWriter(logfile));
		} catch (IOException e4) {
			e4.printStackTrace();
		}
		
		System.out.print("Loading SemGen...");
		logfilewriter.println("Loading SemGen");
		configureSemSim();
	}
	
	private static void configureSemSim() throws NoSuchMethodException, SecurityException {

		OSValidation();
		
		semsimlib = new SemSimLibrary(cfgreadpath);
		ModelReader.pointtoSemSimLibrary(semsimlib);
		ModelWriter.pointtoSemSimLibrary(semsimlib);
		ErrorLog.setLogFile(logfilewriter);
		// Need this for programmatic use of jsbatch
		System.setProperty("jsim.home", cfgreadpath + "jsimhome");
	}

	/**Set the user interface look and feel to the Nimbus Swing layout and create the frame*/
	public static void createAndShowGUI() {
		try {
			UIManager.put("nimbusOrange", new Color(51,98,140));
			UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
		    for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
		        if ("Nimbus".equals(info.getName())) {
		            UIManager.setLookAndFeel(info.getClassName());
		            break;
		        }
			}
		    
		    new SemGen();
		    
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	//Launch application
	public SemGen() throws NoSuchMethodException, SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
		super("OSXAdapter");
						
		setTitle(":: S e m  G e n ::");
		//this.setIconImage(SemGenIcon.semgenbigicon.getImage());
		//this.setIconImages(SemGenIcon.getSemGenLogoList());
		
		termcache = new OntologyCache();
		
		settings = new SemGenSettings();
		SemGenFileChooser.currentdirectory = new File(settings.getStartDirectory());

		//Create an instance of SemGen's default font and load it into memory
		SemGenFont.defaultUIFont();
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		gacts.addObserver(this);
		SemGenError.setFrame(this);
		SemGenDialog.setFrame(this);
		
		SemGenMenuBar menubar = new SemGenMenuBar(settings, gacts);
		contentpane = new SemGenGUI(settings, menubar, gacts);
		setContentPane(contentpane);
		setJMenuBar(menubar);

		setVisible(true);
		
		this.pack();
		
		//Maximize screen
		if (settings.maximizeScreen()) {
			setExtendedState(getExtendedState() | JFrame.MAXIMIZED_BOTH);
		}
		else {
			setSize(settings.getAppSize());
			setLocation(settings.getAppLocation());
		}
		
		addListeners();
		settings.setAppSize(getSize());
		
		System.out.println("Loaded.");
		logfilewriter.println("Session started on: " + sdflog.format(datenow) + "\n");
		
		if(OSValidator.isMac())
			OSXAdapter.setQuitHandler(this, getClass().getMethod("quit", (Class<?>[])null));
		
		if (openstage) {
				contentpane.startNewStageTask();
		}
		else {
			new NewTaskDialog(gacts);
		}
	}
	
	//Check which OS SemGen is being run under
	private static void OSValidation() throws NoSuchMethodException, SecurityException{
		int OS = 0;
		if (OSValidator.isMac()) OS = MACOSX;
		else if (OSValidator.isWindows()) OS = WINDOWS;
		
		switch (OS) { 
		case WINDOWS :
			if (!new File(cfgreadpath).canRead()) {
				cfgreadpath = System.getProperty("user.home") + "/AppData/local/SemGen/cfg/";
				cfgwritepath = cfgreadpath;
			}
			break;
		
		case MACOSX :
			
			// Set read and write paths to Mac-specific locations
			String appbundlepath = OSXAdapter.getPathToApplicationBundle();
			
			if(appbundlepath.contains("SemGen.app")){
				cfgreadpath = appbundlepath + "/Contents/Resources/cfg/";
				String homeDir = System.getProperty("user.home");
				cfgwritepath = new File(homeDir + "/Library/Preferences/SemGen/cfg/").getAbsolutePath() + "/";
				examplespath = appbundlepath + "/Contents/Resources/examples/";
			}
			break;
		
		default : 
		}
	}
	
	/**Define and Add Frame Listeners */
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

	/** Quit - verify that it is OK to quit and store the user's current preferences
	 * and any local ontology terms if it yes
	 * */
	public boolean quit() throws HeadlessException, OWLException {
		
		if(contentpane.quit()){
			try {
				settings.setIsMaximized(getExtendedState()==JFrame.MAXIMIZED_BOTH);
				
				// If we haven't set up the user preferences location on Mac, do so
				if(OSValidator.isMac()){
										
					File cfgwritefolder = new File(cfgwritepath);
					
					if( ! cfgwritefolder.exists()){
						boolean madedir = cfgwritefolder.mkdirs();
						
						if( ! madedir) System.err.println("Could not create write directory at " + cfgwritepath);
					}
				}
				
				settings.storeSettings();
				termcache.storeCachedOntologyTerms();
				dispose();
				return true;
			} 
			catch (URISyntaxException e) {e.printStackTrace();}
		}
		return false;
	}
	
	@Override
	public void update(Observable o, Object arg) {
		if (arg ==GlobalActions.appactions.QUIT) {
			try {
				quit();
			} catch (HeadlessException | OWLException e) {
				e.printStackTrace();
			}
		}
		
	}
}
