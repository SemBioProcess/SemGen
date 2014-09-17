package semgen.semgenmenu;

import java.awt.Dimension;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JMenuBar;

import semgen.SemGenSettings;
import semgen.UniversalActions;

public class SemGenMenuBar extends JMenuBar implements Observer {
	private static final long serialVersionUID = 1L;
	private SemGenSettings settings;
	private FileMenu filemenu;
	
	public SemGenMenuBar(SemGenSettings sets, UniversalActions uacts) {
		settings = sets;
		setOpaque(true);
		setPreferredSize(new Dimension(settings.getAppWidth(), 20));
		
		filemenu = new FileMenu(settings, uacts);
		add(filemenu);
		add(new ToolsMenu(settings, uacts));
		add(new HelpMenu(settings, uacts));
	}
	
	public void updateSemGenMenuOptions(){		
		filemenu.updateMenu();
	}
	
	@Override
	public void update(Observable o, Object arg) {
		// TODO Auto-generated method stub
		
	}

}
