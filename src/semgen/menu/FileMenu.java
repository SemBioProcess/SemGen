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
import semgen.NewTaskDialog;
import semgen.OSValidator;
import semgen.PreferenceDialog;
import semgen.SemGenSettings;
import semgen.utilities.uicomponent.SemGenMenu;

public class FileMenu extends SemGenMenu implements ActionListener, Observer {
	private static final long serialVersionUID = 1L;
	public JMenuItem fileitemnew;
	private JMenuItem fileitemclose;
	private JMenuItem fileitemsave;
	private JMenuItem fileitemsaveas;
	private JMenuItem fileitemproperties;
	private JMenuItem fileitemexit;
	
	public FileMenu(SemGenSettings sets, GlobalActions acts) {
		super("File", sets, acts);
		// Build the File menu
		getAccessibleContext().setAccessibleDescription("Create new files, opening existing files, import raw model code, etc.");

		// File menu items
		fileitemnew = formatMenuItem(fileitemnew,"New",KeyEvent.VK_N,true,true);
		add(fileitemnew);
		fileitemclose = formatMenuItem(fileitemclose,"Close",KeyEvent.VK_W,true,true);
		add(fileitemclose);
		fileitemsave = formatMenuItem(fileitemsave,"Save",KeyEvent.VK_S,false,true);
		add(new JSeparator());
		add(fileitemsave);
		fileitemsaveas = formatMenuItem(fileitemsaveas,"Save As",null,true,true);
		fileitemsaveas.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.SHIFT_MASK + maskkey));
		add(fileitemsaveas);
		
		add(new JSeparator());
		fileitemproperties = formatMenuItem(fileitemproperties,"Properties", KeyEvent.VK_P,true,true);
		add(fileitemproperties);
		
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
		
		if (o == fileitemnew) {
			new NewTaskDialog(globalactions);
		}

		if (o == fileitemsave) {
			globalactions.getCurrentTab().requestSave();
		}

		if (o == fileitemsaveas) {
			globalactions.requestSaveAs();
		}
		
		if( o == fileitemclose){
			globalactions.closeTab();
		}

		if( o == fileitemproperties){
			new PreferenceDialog(settings);
		}
		
		if (o == fileitemexit) {
			globalactions.quit();
		}
	}

		@Override
		public void update(Observable o, Object arg) {
			if (arg==GlobalActions.appactions.SAVED || arg==GlobalActions.appactions.TABCHANGED) {
				fileitemsave.setEnabled(!globalactions.getCurrentTab().isSaved());	
			}
			if (arg==GlobalActions.appactions.TABCLOSED || arg==GlobalActions.appactions.TABOPENED) {
				if (globalactions.getNumOpenTabs()==0) {
					fileitemsaveas.setEnabled(false);
					fileitemclose.setEnabled(false);
				}
				else {
					fileitemsaveas.setEnabled(true);
					fileitemclose.setEnabled(true);
				}
			}
		}	

}
