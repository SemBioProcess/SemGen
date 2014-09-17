package semgen.extraction;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.IOException;

import javax.swing.JMenuItem;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;

import semgen.SemGenSettings;
import semgen.UniversalActions;
import semgen.extraction.workbench.ExtractorWorkbench;
import semgen.resource.uicomponents.SemGenMenu;

public class ExtractorToolbar extends JToolBar implements ActionListener {
	private static final long serialVersionUID = 1L;

	private ExtractorWorkbench workbench;
	//private SemGenSettings settings;
	private UniversalActions uacts;
	private JMenuItem extractoritembatchcluster;
	private JMenuItem extractoritemopenann;
	
	public ExtractorToolbar(UniversalActions ua, SemGenSettings sets, ExtractorWorkbench wb) {
		workbench = wb;
		uacts=ua;
		//settings = sets;
		extractoritembatchcluster = formatMenuItem(extractoritembatchcluster, "Automated clustering analysis", KeyEvent.VK_B,true,true);
		extractoritembatchcluster.setToolTipText("Performs clustering analysis on model");
		add(extractoritembatchcluster);
	
		extractoritemopenann = formatMenuItem(extractoritemopenann, "Open model in AnnotatorTab", null, true, true);
		add(extractoritemopenann);	
	}
	
	// Format menu items, assign shortcuts, action listeners
	public JMenuItem formatMenuItem(JMenuItem item, String text, Integer accelerator, Boolean enabled, Boolean addactionlistener){
		item = new JMenuItem(text);
		item.setEnabled(enabled);
		if(accelerator!=null){item.setAccelerator(KeyStroke.getKeyStroke(accelerator, SemGenMenu.maskkey));}
		if(addactionlistener){item.addActionListener(this);}
		return item;
	}
	
	public void actionPerformed(ActionEvent e) {
		Object o = e.getSource();

		if (o == extractoritembatchcluster) {
				try {
					workbench.batchCluster();
				} catch (IOException e1) {e1.printStackTrace();}
		}

		if (o == extractoritemopenann) {
			uacts.NewAnnotatorTab(workbench.getSourceFile());
		}
	}
}
