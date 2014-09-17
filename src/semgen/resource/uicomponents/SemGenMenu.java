package semgen.resource.uicomponents;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import semgen.SemGenSettings;
import semgen.UniversalActions;

public abstract class SemGenMenu extends JMenu implements ActionListener {
	private static final long serialVersionUID = 1L;
	protected UniversalActions uacts;
	
	protected SemGenSettings settings;
	public static int maskkey = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
	
	public SemGenMenu(SemGenSettings sets, UniversalActions acts) {
		settings = sets;
		uacts = acts;
	}
	
	public SemGenMenu(String title, SemGenSettings sets, UniversalActions acts) {
		super(title);
		uacts = acts;
		settings = sets;
	}
	
	public abstract void updateMenu();
	
	// Format menu items, assign shortcuts, action listeners
	protected JMenuItem formatMenuItem(JMenuItem item, String text, Integer accelerator, Boolean enabled, Boolean addactionlistener){
		item = new JMenuItem(text);
		item.setEnabled(enabled);
		if(accelerator!=null){item.setAccelerator(KeyStroke.getKeyStroke(accelerator,maskkey));}
		item.addActionListener(this);
		return item;
	}
	@Override
	public abstract void actionPerformed(ActionEvent arg0);
	
}
