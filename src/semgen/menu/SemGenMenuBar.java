package semgen.menu;

import java.awt.Dimension;

import javax.swing.JMenuBar;

import semgen.SemGenSettings;
import semgen.GlobalActions;

public class SemGenMenuBar extends JMenuBar {
	private static final long serialVersionUID = 1L;
	private SemGenSettings settings;
	public FileMenu filemenu;
	private ToolsMenu toolsmenu;
	
	public SemGenMenuBar(SemGenSettings sets, GlobalActions gacts) {
		settings = sets;
		setOpaque(true);
		setPreferredSize(new Dimension(settings.getAppWidth(), 20));
		
		filemenu = new FileMenu(settings, gacts);
		gacts.addObserver(filemenu);
		add(filemenu);
		toolsmenu = new ToolsMenu(settings, gacts);
		add(toolsmenu);
		add(new HelpMenu(settings));
	}

}
