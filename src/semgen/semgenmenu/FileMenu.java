package semgen.semgenmenu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JMenuItem;
import javax.swing.JSeparator;
import javax.swing.KeyStroke;

import semgen.SemGenSettings;
import semgen.UniversalActions;
import semgen.resource.uicomponents.SemGenMenu;

public class FileMenu extends SemGenMenu implements ActionListener {
	private static final long serialVersionUID = 1L;
	private JMenuItem fileitemnew;
	private JMenuItem fileitemopen;
	private JMenuItem fileitemsave;
	private JMenuItem fileitemsaveas;
	private JMenuItem fileitemclose;
	private JMenuItem fileitemexit;
	private UniversalActions uacts;
	
	public FileMenu(SemGenSettings sets, UniversalActions acts) {
		super("File", sets, acts);
		// Build the File menu
		getAccessibleContext().setAccessibleDescription("Create new files, opening existing files, import raw model code, etc.");

		// File menu items
		fileitemnew = formatMenuItem(fileitemnew,"New",KeyEvent.VK_N,false,true);
		fileitemopen = formatMenuItem(fileitemopen,"Open",KeyEvent.VK_O,true,true);
		add(fileitemopen);
		fileitemsave = formatMenuItem(fileitemsave,"Save",KeyEvent.VK_S,true,true);
		add(fileitemsave);
		fileitemsaveas = formatMenuItem(fileitemsaveas,"Save As",null,true,true);
		fileitemsaveas.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.SHIFT_MASK + maskkey));
		add(fileitemsaveas);
		
		fileitemclose = formatMenuItem(fileitemclose,"Close",KeyEvent.VK_W,true,true);
		add(fileitemclose);
		fileitemexit = formatMenuItem(fileitemexit,"Quit SemGen",KeyEvent.VK_Q,true,true);
		add(new JSeparator());
		add(fileitemexit);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object o = e.getSource();
		
		if (o == fileitemopen) {
			uacts.newTask();
		}

		if (o == fileitemsave) {
			uacts.getCurrentTab().requestSave();
		}

		if (o == fileitemsaveas) {
			uacts.requestSaveAs();
		}
		if( o == fileitemclose){
			uacts.closeTab();
		}

		if (o == fileitemexit) {
			uacts.quit();
		}
	}
	
		@Override
		public void updateMenu() {
			fileitemsave.setEnabled(!uacts.getCurrentTab().isSaved());			
		}	

}
