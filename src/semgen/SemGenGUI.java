package semgen;

import org.semanticweb.owlapi.model.OWLException;

import semgen.annotation.AnnotatorTab;
import semgen.extraction.ExtractorTab;
import semgen.merging.MergerTab;
import semgen.resource.SemGenFont;
import semgen.resource.file.SemGenOpenFileChooser;

import semgen.resource.uicomponents.SemGenTab;
import semsim.SemSimLibrary;

import java.net.URI;
import java.net.URISyntaxException;
import java.awt.Color;
import java.awt.HeadlessException;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import java.io.File;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

public class SemGenGUI extends JTabbedPane implements ChangeListener, Observer {

	private static final long serialVersionUID = 3618439495848469800L;
	private int numtabs = 0;

	private ArrayList<AnnotatorTab> anntabs = new ArrayList<AnnotatorTab>();
	
	private SemGenSettings settings;
	public static SemSimLibrary semsimlib;
	private UniversalActions uacts;

	public SemGenGUI(SemGenSettings sets, SemSimLibrary lib, UniversalActions ua){
		settings = sets;
		semsimlib = lib;
		uacts = ua;
		
		SemGenOpenFileChooser.currentdirectory = new File(settings.getStartDirectory());
		
		setPreferredSize(settings.getAppSize());
		setOpaque(true);
		setBackground(Color.white);
		addChangeListener(this);	

		numtabs = 0;
		StyleConstants.setFontSize(new SimpleAttributeSet(), 13);
		SemGenFont.defaultUIFont();
	}
	

			
	public Boolean isOntologyOpenForEditing(URI uritocheck) {
		for (AnnotatorTab at : anntabs) {
			if (at.checkFile(uritocheck)) {
				JOptionPane.showMessageDialog(null,"Cannot create or load \n"+ uritocheck.toString()+
					"\n because the file is already open for editing.",null, JOptionPane.PLAIN_MESSAGE);
				return true;
				}
		}
		return false;
	}
	
	public void newAnnotatorTabAction(File file) {
		// Create a new tempfile using the date	
		URI selectedURI = file.toURI(); // The selected file for annotation
		
		// Check to make sure SemSim model isn't already being annotated before proceeding
		if (!isOntologyOpenForEditing(selectedURI)) {
			AnnotatorTab tab = new AnnotatorTab(file, settings, uacts);
			anntabs.add(tab);
			addTab(tab);
		}
	}
	
	public void newExtractorTabAction(File file) {
		ExtractorTab tab = null;
		try {
			tab = new ExtractorTab(file, settings, uacts);
		}
		catch (OWLException e) {
			e.printStackTrace();
		}
		addTab(tab);
	}
	
	public void newMergerTabAction(){
		MergerTab MergerTab = new MergerTab(settings, uacts);
		MergerTab.Initialize();
		setMnemonicAt(0, KeyEvent.VK_1);
		addTab(MergerTab);
	}
	
	public void addTab(SemGenTab tab) {
		numtabs++;
		addTab(tab.getName(), tab);
		setTabComponentAt(numtabs-1, tab.getTabLabel());
		tab.setClosePolicy(new tabCloseListener(tab));
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			   public void run() { 
				   setSelectedComponent(getComponentAt(numtabs - 1));
		}});
		uacts.setCurrentTab(tab);
	}
	
	private class tabCloseListener extends MouseAdapter {
		SemGenTab tab;
		tabCloseListener(SemGenTab t) {
			tab = t;
		}
		public void mouseClicked(MouseEvent arg0) {
			closeTabAction(tab);
		}
	}

	// WHEN THE CLOSE TAB ACTION IS PERFORMED ON AN AnnotatorTab, MergerTab OR EXTRACTOR FRAME
	public int closeTabAction(SemGenTab component) {
		// If the file has been altered, prompt for a save
		int returnval =  component.closeTab();
		if (returnval != JOptionPane.CANCEL_OPTION) {
			anntabs.remove(component);
			remove(indexOfComponent(component));
			numtabs = numtabs - 1;
		}
		
		return returnval;
	}
		
	public boolean quit() throws HeadlessException, OWLException {
		while (getTabCount()!=0) {
			if (closeTabAction((SemGenTab)getComponentAt(0)) == JOptionPane.CANCEL_OPTION) {
				return false;
			}
		}
		try {
			settings.storeSettings();
			semsimlib.storeCachedOntologyTerms();
			System.exit(0);
		} 
		catch (URISyntaxException e) {e.printStackTrace();}
		return true;
	}
	
	public void stateChanged(ChangeEvent arg0) {
		uacts.setCurrentTab((SemGenTab)arg0.getSource());
	}

	@Override
	public void update(Observable o, Object arg) {
		if (arg ==uacts.QUIT) {
			try {
				quit();
			} catch (HeadlessException | OWLException e) {
				e.printStackTrace();
			}
		}
		if (arg==uacts.TABCLOSED) {
			closeTabAction(uacts.getCurrentTab());
		}
		if (arg ==uacts.annotate) {
			newAnnotatorTabAction(uacts.getSeed());
		}
		if (arg ==uacts.merge) {
			newMergerTabAction();
		}
		if (arg ==uacts.extract) {
			newExtractorTabAction(uacts.getSeed());
		}
		
	}
}
