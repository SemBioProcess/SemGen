package semgen.menu;

import java.awt.Dimension;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JMenuBar;

import semgen.SemGenSettings;
import semgen.GlobalActions;

public class SemGenMenuBar extends JMenuBar implements Observer {
	private static final long serialVersionUID = 1L;
	private SemGenSettings settings;
	public FileMenu filemenu;
	public ToolsMenu toolsmenu;
	
	public SemGenMenuBar(SemGenSettings sets, GlobalActions gacts) {
		settings = sets;
		setOpaque(true);
		setPreferredSize(new Dimension(settings.getAppWidth(), 20));
		
		filemenu = new FileMenu(settings, gacts);
		add(filemenu);
		toolsmenu = new ToolsMenu(settings, gacts);
		add(toolsmenu);
		add(new HelpMenu(settings));
	}
	
	public void updateSemGenMenuOptions(){		
		//filemenu.updateMenu();
	}
	
	@Override
	public void update(Observable o, Object arg) {
		// TODO Auto-generated method stub
		
	}

}
