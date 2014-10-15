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
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

import org.semanticweb.owlapi.model.OWLException;

import semgen.SemGenGUI;
import semgen.resource.SemGenFont;
import semsim.SemSimLibrary;

public class SemGen extends JFrame{
	private static final long serialVersionUID = 1L;
	public static SemSimLibrary semsimlib = new SemSimLibrary();
	
	public static PrintWriter logfilewriter;
	public static File tempdir = new File(System.getProperty("java.io.tmpdir"));
	public static final String logfileloc = tempdir.getAbsolutePath() + "/SemGen_log.txt";
	public static final File logfile = new File(logfileloc);
	
	private final static int WINDOWS=1;
	private static final int MACOSX=2;
	
	private SemGenSettings settings = new SemGenSettings();
	public static Date datenow = new Date();
	public SimpleDateFormat sdflog = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z");
	
	JMenuBar menubar = new JMenuBar();
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
	public SemGen() {
		super("OSXAdapter");
		
		OSValidation();

		// Need this for programmatic use of jsbatch
		System.setProperty("jsim.home", "./jsimhome");
		SemGenFont.defaultUIFont();
		
		setTitle(":: S e m  G e n ::");
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

		setSize(settings.getAppSize());
		setLocation(settings.getAppXPos(), settings.getAppYPos());
		
		contentpane = new SemGenGUI(settings, menubar);
		setContentPane(contentpane);
		setJMenuBar(menubar);
		
		setVisible(true);
		addListeners();
		this.pack();
		
		System.out.println("Loaded.");
		logfilewriter.println("Session started on: " + sdflog.format(datenow) + "\n");
	}
	//Check which OS SemGen is being run under
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
	//Define and Add Frame Listeners
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
	//Quit - behavior defined in content frame
	public boolean quit() throws HeadlessException, OWLException {
		return contentpane.quit();
	}
}
