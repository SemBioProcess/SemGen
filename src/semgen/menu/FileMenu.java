package semgen.menu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JMenuItem;
import javax.swing.JSeparator;
import javax.swing.KeyStroke;

import semgen.GlobalActions;
import semgen.OSValidator;
import semgen.PreferenceDialog;
import semgen.SemGenSettings;
import semgen.stage.StageTab;
import semgen.utilities.uicomponent.SemGenMenu;
import semgen.utilities.uicomponent.SemGenTab;

public class FileMenu extends SemGenMenu implements ActionListener, Observer {
	private static final long serialVersionUID = 1L;

	private JMenuItem fileitemnewstage;
	private JMenuItem fileitemnewannotator;
	private JMenuItem fileitemclose;
	private JMenuItem fileitemsave;
	private JMenuItem fileitemsaveas;
	private JMenuItem fileitemexport;
	private JMenuItem fileitemeditmetadata;
	private JMenuItem fileitempreferences;
	private JMenuItem fileitemexit;
	private JSeparator writeseparator = new JSeparator();
	
	public FileMenu(SemGenSettings sets, GlobalActions acts) {
		super("File", sets, acts);
		// Build the File menu
		getAccessibleContext().setAccessibleDescription("File manipulation menu");

		// File menu items
		fileitemnewstage = formatMenuItem(fileitemnewstage, "New Project Tab", KeyEvent.VK_N,true,true);
		fileitemnewstage.setToolTipText("Open a new Project Tab");
		add(fileitemnewstage);
		
		fileitemnewannotator = formatMenuItem(fileitemnewannotator, "New Annotator Tab",KeyEvent.VK_A,true,true);
		fileitemnewannotator.setToolTipText("Open a new Annotator Tab");
		add(fileitemnewannotator);
		
		fileitemclose = formatMenuItem(fileitemclose,"Close Tab",KeyEvent.VK_W,true,true);
		add(fileitemclose);
		
		add(new JSeparator());

		fileitemsave = formatMenuItem(fileitemsave,"Save",KeyEvent.VK_S,false,true);
		add(fileitemsave);
		
		fileitemsaveas = formatMenuItem(fileitemsaveas,"Save As...",null,true,true);
		fileitemsaveas.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.SHIFT_MASK + maskkey));
		add(fileitemsaveas);
		
		fileitemexport = formatMenuItem(fileitemexport,"Export...",KeyEvent.VK_E,true,true);
		fileitemexport.setToolTipText("Write out model in a specified format");
		add(fileitemexport);
		
		fileitemeditmetadata = formatMenuItem(fileitemeditmetadata,"Edit Model-level Annotations",KeyEvent.VK_M,true,true);
		fileitemeditmetadata.setToolTipText("Edit the model-level annotations");
		add(fileitemeditmetadata);
		
		add(writeseparator);
		
		fileitempreferences = formatMenuItem(fileitempreferences,"Preferences", KeyEvent.VK_P,true,true);
		add(fileitempreferences);
		
		Integer quitaccelerator = OSValidator.isMac() ? null : KeyEvent.VK_Q;
		fileitemexit = formatMenuItem(fileitemexit, "Quit SemGen", quitaccelerator, true, true);
		add(new JSeparator());
		add(fileitemexit);
		
		fileitemsaveas.setEnabled(false);
		fileitemclose.setEnabled(false);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object o = e.getSource();
		
		if (o == fileitemnewstage){
			globalactions.NewStageTab();
		}

		if (o == fileitemnewannotator) {
			globalactions.NewAnnotatorTab();
		}
		
		if (o == fileitemsave) {
			globalactions.getCurrentTab().requestSave();
		}

		if (o == fileitemsaveas) {
			globalactions.requestSaveAs();
		}
		
		if (o == fileitemexport) {
			globalactions.requestExport();
		}
		
		if (o == fileitemeditmetadata){
			globalactions.requestEditModelLevelMetadata();
		}
		
		if( o == fileitemclose){
			globalactions.closeTab();
		}

		if( o == fileitempreferences){
			new PreferenceDialog(settings);
		}
		
		if (o == fileitemexit) {
			globalactions.quit();
		}
	}
	
	public void canSaveOut(boolean enable) {
		fileitemsaveas.setEnabled(enable);
		fileitemexport.setEnabled(enable);
		fileitemeditmetadata.setEnabled(enable);
		
	}

	@Override
	public void update(Observable o, Object arg) {
		
		SemGenTab currenttab = globalactions.getCurrentTab();
		
		if (arg==GlobalActions.appactions.SAVED || arg==GlobalActions.appactions.TABCHANGED) {
			fileitemsave.setEnabled(! currenttab.isSaved());	
		}
		if (arg==GlobalActions.appactions.TABCLOSED || arg==GlobalActions.appactions.TABOPENED) {
				fileitemclose.setEnabled(globalactions.getNumOpenTabs()!=0);
		}
		if (globalactions.getNumOpenTabs()!=0) {
			canSaveOut(this.globalactions.isModelLoaded());
			
			// Make the "Save", "Save As" and "Export" file items visible only 
			// we're not on a Project Tab
			boolean currtabisproject = currenttab instanceof StageTab;
			fileitemsave.setVisible( ! currtabisproject);
			fileitemsaveas.setVisible( ! currtabisproject);
			fileitemexport.setVisible( ! currtabisproject);
			writeseparator.setVisible( ! currtabisproject);
			fileitemeditmetadata.setVisible( ! currtabisproject);
		}
		else {
			fileitemsave.setEnabled(false);
		}
	}	

}
