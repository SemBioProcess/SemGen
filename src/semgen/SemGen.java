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
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.semanticweb.owlapi.model.OWLException;

import semgen.SemGenGUI;
import semgen.menu.SemGenMenuBar;
import semgen.utilities.OntologyCache;
import semgen.utilities.SemGenError;
import semgen.utilities.SemGenFont;
import semgen.utilities.file.SemGenOpenFileChooser;
import semgen.utilities.uicomponent.SemGenDialog;
import semsim.ErrorLog;
import semsim.SemSimLibrary;
import semsim.reading.ModelReader;
import semsim.writing.ModelWriter;

public class SemGen extends JFrame implements Observer{
	private static final long serialVersionUID = 1L;

	public static double version = 3.0;
	public static PrintWriter logfilewriter;
	public static File tempdir = new File(System.getProperty("java.io.tmpdir"));
	public static final String logfileloc = tempdir.getAbsolutePath() + "/SemGen_log.txt";
	public static OntologyCache termcache = new OntologyCache();
	private final static File logfile = new File(logfileloc);
	
	private final static int WINDOWS=1;
	private static final int MACOSX=2;
	
	public static Date datenow = new Date();
	public SimpleDateFormat sdflog = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z");
	
	//The application store for the default settings
	private SemGenSettings settings = new SemGenSettings();
	//A library of SemSim constants and definitions. This is created once and referenced
	//without modification by the rest of the program.
	public static SemSimLibrary semsimlib = new SemSimLibrary();
	//A class for application level events such as exiting and creating new tabs
	private GlobalActions gacts = new GlobalActions();
	private SemGenGUI contentpane = null; 
	
	/** Main method for running an instance of SemGen */
	public static void main(String[] args) {
		try {
			logfilewriter = new PrintWriter(new FileWriter(logfile));
		} catch (IOException e4) {
			e4.printStackTrace();
		}
		
		System.out.print("Loading SemGen...");
		logfilewriter.println("Loading SemGen");
		configureSemSim();
		
		 SwingUtilities.invokeLater(new Runnable() {
		     public void run() {
		        createAndShowGUI();
		     }
		  });
	}
	
	private static void configureSemSim() {
		ModelReader.pointtoSemSimLibrary(semsimlib);
		ModelWriter.pointtoSemSimLibrary(semsimlib);
		ErrorLog.setLogFile(logfilewriter);
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
		    
		    JFrame frame = new SemGen();
		    frame.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	//Launch application
	public SemGen() throws NoSuchMethodException, SecurityException {
		super("OSXAdapter");
		OSValidation();
		
		setTitle(":: S e m  G e n ::");
		//Set the default location for the creation of child windows (ie: dialogs) as the center  
		//of the main frame
		SemGenError.setFrame(this);
		SemGenDialog.setFrame(this);
		
		SemGenOpenFileChooser.currentdirectory = new File(settings.getStartDirectory());
		// Need this for programmatic use of jsbatch
		System.setProperty("jsim.home", "./jsimhome");
		//Create an instance of SemGen's default font and load it into memory
		SemGenFont.defaultUIFont();
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		gacts.addObserver(this);
		 
		SemGenMenuBar menubar = new SemGenMenuBar(settings, gacts);
		contentpane = new SemGenGUI(settings, menubar, gacts);
		setContentPane(contentpane);
		setJMenuBar(menubar);
		
		setVisible(true);
		addListeners();
		this.pack();
		
		//Maximize screen
		setExtendedState(getExtendedState() | JFrame.MAXIMIZED_BOTH);
		settings.setAppSize(getSize());
		System.out.println("Loaded.");
		logfilewriter.println("Session started on: " + sdflog.format(datenow) + "\n");
		
		new NewTaskDialog(gacts);
	}
	
	//Check which OS SemGen is being run under
	private void OSValidation() throws NoSuchMethodException, SecurityException {
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
		      OSXAdapter.setQuitHandler(this, getClass().getMethod("quit", (Class<?>[])null));
		      break;
		default : 
			libsbmlfile = new File("cfg/libsbmlj.so");
		}
		
		if(libsbmlfile.exists()){
			try {
				System.load(libsbmlfile.getAbsolutePath());
			}
			catch (UnsatisfiedLinkError e){
				JOptionPane.showMessageDialog(this, "Unable to load " + libsbmlfile.getAbsolutePath() + ". JSim may not work properly. Error: {0}" + e.getMessage());
			}
		}
		else 
			JOptionPane.showMessageDialog(this, "Couldn't open " + libsbmlfile.getAbsolutePath() + " for loading.");
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
	public void quit() throws HeadlessException, OWLException {
		
		if(contentpane.quit()){
			try {
				settings.storeSettings();
				termcache.storeCachedOntologyTerms();
				System.exit(0);
			} 
			catch (URISyntaxException e) {e.printStackTrace();}
		}
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
