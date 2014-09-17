package semgen.resource;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;

import semgen.SemGenSettings;

public class SettingsDialog extends JDialog implements ActionListener{
	private static final long serialVersionUID = 1L;

	public JRadioButton annotateitemsortbytype;
	public JRadioButton annotateitemsortalphabetically;

	public JCheckBox autoannotate;
	public JCheckBox annotateitemshowimports;
	public JCheckBox annotateitemshowmarkers;
	public JCheckBox annotateitemtreeview;
	
	public SettingsDialog(SemGenSettings settings) {
		autoannotate = new JCheckBox("Auto-annotate during translation");
		autoannotate.setSelected(settings.doAutoAnnotate().equals("true"));
		autoannotate.setToolTipText("Automatically add annotations based on physical units, etc. when possible");
		add(autoannotate);
		add(new JSeparator());
			
		annotateitemshowimports = new JCheckBox("Show imported codewords/sub-models");
		annotateitemshowimports.setSelected(settings.showImports());
		annotateitemshowimports.setToolTipText("Make imported codewords and submodels visible");
		add(annotateitemshowimports);
			
		annotateitemshowmarkers = new JCheckBox("Display physical type markers");
		annotateitemshowmarkers.setSelected(settings.useDisplayMarkers());
		annotateitemshowmarkers.setToolTipText("Display markers that indicate a codeword's property type");
		add(annotateitemshowmarkers);

		annotateitemsortbytype = new JRadioButton("By physical type");
		annotateitemsortbytype.setSelected(settings.organizeByPropertyType());
		annotateitemsortalphabetically = new JRadioButton("Alphabetically");
		annotateitemsortalphabetically.setSelected(!annotateitemsortbytype.isSelected());
		
		ButtonGroup sortbuttons = new ButtonGroup();
		sortbuttons.add(annotateitemsortbytype);
		sortbuttons.add(annotateitemsortalphabetically);
		
		JPanel sortCodewordsMenu = new JPanel();
		sortCodewordsMenu.add(new JLabel("Sort codewords..."));
		sortCodewordsMenu.add(annotateitemsortbytype);
		sortCodewordsMenu.add(annotateitemsortalphabetically);
		
		annotateitemsortbytype.setToolTipText("Sort codewords according to whether they represent a property of a physical entity, process, or dependency");
		add(sortCodewordsMenu);
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		// TODO Auto-generated method stub
		
	}
}
