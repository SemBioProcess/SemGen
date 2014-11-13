package semgen.menu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JMenuItem;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import semgen.GlobalActions;
import semgen.SemGenSettings;
import semgen.encoding.Encoder;
import semgen.resource.uicomponent.SemGenMenu;

public class ToolsMenu extends SemGenMenu implements ActionListener, MenuListener{
	private static final long serialVersionUID = 1L;

	public JMenuItem toolsitemannotate;
	public JMenuItem toolsitemmerge;
	private JMenuItem toolsitemcode;
	public JMenuItem toolsitemextract;
	public JMenuItem toolsitemdefaults;
	
	public ToolsMenu(SemGenSettings sets, GlobalActions acts) {
		super("Tools", sets, acts);
		// Build the AnnotatorTab menu
		getAccessibleContext().setAccessibleDescription("Select a new SemGen Tool");
		
		toolsitemannotate = formatMenuItem(toolsitemannotate, "New AnnotatorTab",KeyEvent.VK_A,true,true);
		toolsitemannotate.setToolTipText("Open a new AnnotatorTab tool");
		add(toolsitemannotate);
		
		toolsitemextract = formatMenuItem(toolsitemextract,"New Extractor",KeyEvent.VK_E,true,true);
		toolsitemextract.setToolTipText("Open a new Extractor tool");
		add(toolsitemextract);
		
		toolsitemmerge = formatMenuItem(toolsitemmerge,"New MergerTab",KeyEvent.VK_M,true,true);
		toolsitemmerge.setToolTipText("Open a new MergerTab tool");
		add(toolsitemmerge);
		
		toolsitemcode = formatMenuItem(toolsitemcode, "New code generator", KeyEvent.VK_G,true,true);
		toolsitemcode.setToolTipText("Open a new code generator tool");
		add(toolsitemcode);
		
		toolsitemdefaults = formatMenuItem(toolsitemdefaults, "Set Defaults", KeyEvent.VK_G,true,true);
		toolsitemdefaults.setToolTipText("Change SemGen's default settings");
		add(toolsitemdefaults);
	}
	
	public void actionPerformed(ActionEvent e) {
		Object o = e.getSource();
		
//		if (o == toolsitemmerge){
//			globalactions.NewMergerTab();
//		}
//		
		if (o == toolsitemcode) {
			new Encoder();
		}
//		
		if (o == toolsitemannotate) {
			globalactions.NewAnnotatorTab();
		}
		
//		if (o == toolsitemextract) {
//			uacts.NewExtractorTab();
//		}
		
	}

	@Override
	public void menuCanceled(MenuEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void menuDeselected(MenuEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void menuSelected(MenuEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateMenu() {
		// TODO Auto-generated method stub
		
	}
}
