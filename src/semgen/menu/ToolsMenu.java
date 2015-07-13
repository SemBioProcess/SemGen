package semgen.menu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JMenuItem;

import semgen.GlobalActions;
import semgen.SemGenSettings;
import semgen.encoding.Encoder;
import semgen.utilities.uicomponent.SemGenMenu;

public class ToolsMenu extends SemGenMenu implements ActionListener{
	private static final long serialVersionUID = 1L;

	private JMenuItem toolsitemannotate;
	private JMenuItem toolsitemmerge;
	private JMenuItem toolsitemcode;
	private JMenuItem toolsitemextract;
	private JMenuItem toolsitemstage;
	
	public ToolsMenu(SemGenSettings sets, GlobalActions acts) {
		super("Tools", sets, acts);
		// Build the AnnotatorTab menu
		getAccessibleContext().setAccessibleDescription("Select a new SemGen Tool");
		
		toolsitemstage = formatMenuItem(toolsitemstage, "New stage", KeyEvent.VK_T,true,true);
		toolsitemstage.setToolTipText("Open a new stage");
		add(toolsitemstage);
		
		toolsitemannotate = formatMenuItem(toolsitemannotate, "New Annotator",KeyEvent.VK_A,true,true);
		toolsitemannotate.setToolTipText("Open a new Annotator tool");
		add(toolsitemannotate);
		
		toolsitemextract = formatMenuItem(toolsitemextract,"New Extractor",KeyEvent.VK_E,true,true);
		toolsitemextract.setToolTipText("Open a new Extractor tool");
		add(toolsitemextract);
		
		toolsitemmerge = formatMenuItem(toolsitemmerge,"New Merger",KeyEvent.VK_M,true,true);
		toolsitemmerge.setToolTipText("Open a new Merger tool");
		add(toolsitemmerge);
		
		toolsitemcode = formatMenuItem(toolsitemcode, "New code generator", KeyEvent.VK_G,true,true);
		toolsitemcode.setToolTipText("Open a new code generator tool");
		add(toolsitemcode);
	}
	
	public void actionPerformed(ActionEvent e) {
		Object o = e.getSource();
		
		if (o == toolsitemstage){
			globalactions.NewStageTab();
		}
		
		if (o == toolsitemmerge){
			globalactions.NewMergerTab();
		}
	
		if (o == toolsitemcode) {
			new Encoder();
		}
		
		if (o == toolsitemannotate) {
			globalactions.NewAnnotatorTab();
		}
		
		if (o == toolsitemextract) {
			globalactions.NewExtractorTab();
		}
		
	}
}
