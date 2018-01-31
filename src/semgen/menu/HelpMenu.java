package semgen.menu;

import semgen.SemGen;
import semgen.SemGenSettings;
import semgen.utilities.BrowserLauncher;
import semgen.utilities.LogViewer;
import semgen.utilities.uicomponent.SemGenMenu;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.FileNotFoundException;

public class HelpMenu extends SemGenMenu implements ActionListener {
	private static final long serialVersionUID = 1L;
	private JMenuItem viewlog;
	private JMenuItem helpitemabout;
	private JMenuItem helpitemweb;
	private JMenuItem helpitemannotator;

	public HelpMenu(SemGenSettings sets) {
		super("Help", sets);
		getAccessibleContext().setAccessibleDescription("User help, Versioning, etc.");
		helpitemabout = formatMenuItem(helpitemabout,"About",null,true,true);
		
		helpitemweb = formatMenuItem(helpitemweb,"Help manual (opens browser)", KeyEvent.VK_H,true,true);

		helpitemannotator = formatMenuItem(helpitemannotator, "Annotator tutorial (opens browser)", null, true, true);
		
		viewlog = formatMenuItem(viewlog,"Session log",KeyEvent.VK_L,true,true);
		viewlog.setToolTipText("View the current session's log file");
		
		add(helpitemabout);
		add(helpitemweb);
		add(helpitemannotator);
	}
	public void actionPerformed(ActionEvent e) {
		Object o = e.getSource();
			if (o == viewlog) {
				try {
					new LogViewer();
				} catch (FileNotFoundException k) {k.printStackTrace();}
			}
		
			if (o == helpitemabout) 
				AboutDialog();
		
			if (o == helpitemweb) 
				BrowserLauncher.openURL(settings.getHelpURL());

			if (o == helpitemannotator)
				BrowserLauncher.openURL(settings.getAnnotatorHelpURL());
	}	
	
	public void AboutDialog() {
		String COPYRIGHT = "\u00a9";
		JOptionPane.showMessageDialog(null, "SemGen\nVersion " + SemGen.version + "\n"
						+ COPYRIGHT
						+ "2010-2017\n\n"
						+ "Contributors:\n" 
						+ "  Maxwell L. Neal\n"
						+ "  Christopher T. Thompson\n"
						+ "  Karam G. Kim\n"
						+ "  Ryan C. James", "About SemGen",
						JOptionPane.PLAIN_MESSAGE);
	}
}
