package semgen.annotation.dialog.selector;


import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import semgen.annotation.annotatorpane.AnnotationPanel;
import semgen.annotation.workbench.AnnotatorWorkbench;
import semgen.utilities.uicomponent.SemGenDialog;
import semsim.model.SemSimComponent;
import semsim.model.SemSimModel;
import semsim.writing.CaseInsensitiveComparator;

public class SemSimComponentSelectorDialog extends SemGenDialog implements ActionListener {
	private static final long serialVersionUID = -1776906245210358895L;
	public JPanel panel = new JPanel();
	public JCheckBox markallbox = new JCheckBox("Mark all/none");
	public JOptionPane optionPane;
	protected Object[] options = new Object[]{"OK","Cancel"};

	public Map<String, Object> nameobjectmap = new HashMap<String, Object>();
	public String[] results = null;
	public Set<? extends SemSimComponent> selectableset;
	public Set<? extends SemSimComponent> preselectedset;
	public SemSimComponent ssctoignore;
	public Set<? extends SemSimComponent> sscstodisable;
	public AnnotationPanel anndia;
	protected AnnotatorWorkbench workbench;
	
	public SemSimComponentSelectorDialog(
			AnnotatorWorkbench wb,
			Set<? extends SemSimComponent> settolist,
			SemSimComponent ssctoignore,
			Set<? extends SemSimComponent> preselectedset,
			Set<? extends SemSimComponent> sscstodisable,
			Boolean withdescriptions,
			String title) {
		super(title);
		workbench = wb;
		this.selectableset = settolist;
		this.preselectedset = preselectedset;
		this.ssctoignore = ssctoignore;
		this.sscstodisable = sscstodisable;
	}
	
	
	public void setUpUI(PropertyChangeListener proplistenerparent) {
		Set<Object> sscset2 = new HashSet<Object>();
		sscset2.addAll(selectableset);
		if(ssctoignore!=null)
			sscset2.remove(ssctoignore);
		
		for(Object tssc : sscset2){
			if(tssc instanceof SemSimComponent){
				System.out.println(tssc + "; " + ((SemSimComponent) tssc).getName() + "; " + ((SemSimComponent) tssc).getDescription());
				String name = ((SemSimComponent)tssc).getName();
				if(!name.equals(SemSimModel.unspecifiedName))
					nameobjectmap.put(name, (SemSimComponent)tssc);
			}
			else if(tssc instanceof URI)
				nameobjectmap.put(((URI)tssc).toString(), (URI)tssc);
		}
		
		results = nameobjectmap.keySet().toArray(new String[]{});
		
		Arrays.sort(results, new CaseInsensitiveComparator());
		setPreferredSize(new Dimension(500, 600));

		markallbox.setFont(new Font("SansSerif", Font.ITALIC, 11));
		markallbox.setForeground(Color.blue);
		markallbox.setSelected(false);
		markallbox.addActionListener(this);

		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

		for (int y = 0; y < results.length; y++) {
			JCheckBox checkbox = new JCheckBox(results[y]);
			checkbox.setName(results[y]);
			panel.add(checkbox);
			if(preselectedset!=null)
				checkbox.setSelected(preselectedset.contains(nameobjectmap.get(results[y])));
			if(sscstodisable!=null)
				checkbox.setEnabled(!sscstodisable.contains(nameobjectmap.get(results[y])));
		}

		JScrollPane scroller = new JScrollPane(panel);
		scroller.getVerticalScrollBar().setUnitIncrement(12);
		
		Object[] array = {scroller};

		optionPane = new JOptionPane(array, JOptionPane.PLAIN_MESSAGE,
				JOptionPane.OK_CANCEL_OPTION, null);
		
		// Need an argument to this function for the parent of the addPropertyChangeListener so can use ObjectPropertyEditor function
		optionPane.addPropertyChangeListener(proplistenerparent);
		optionPane.setOptions(options);
		optionPane.setInitialValue(options[0]);

		setContentPane(optionPane);
		
		showDialog();
	}

	public void actionPerformed(ActionEvent e) {
		Object o = e.getSource();
		if (o == markallbox) {
			for (int i = 0; i < panel.getComponentCount(); i++) {
				if (panel.getComponent(i) instanceof JCheckBox && panel.getComponent(i).isEnabled()) {
					JCheckBox checkbox = (JCheckBox) panel.getComponent(i);
					checkbox.setSelected(markallbox.isSelected());
				}
			}
		}
	}

}
