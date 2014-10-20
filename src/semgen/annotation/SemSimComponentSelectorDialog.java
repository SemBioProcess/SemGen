package semgen.annotation;


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
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import semgen.SemGenGUI;
import semsim.model.SemSimComponent;
import semsim.writing.CaseInsensitiveComparator;

public class SemSimComponentSelectorDialog extends JDialog implements ActionListener {
	private static final long serialVersionUID = -1776906245210358895L;
	public JPanel panel;
	public JCheckBox markallbox = new JCheckBox("Mark all/none");
	public JOptionPane optionPane;
	protected Object[] options;

	public Map<String, Object> nameobjectmap = new HashMap<String, Object>();
	public String[] results = null;
	public Set<? extends SemSimComponent> selectableset;
	public Set<? extends SemSimComponent> preselectedset;
	public SemSimComponent ssctoignore;
	public Set<? extends SemSimComponent> sscstodisable;
	public AnnotationDialog anndia;
	public String title;

	public SemSimComponentSelectorDialog(
			Set<? extends SemSimComponent> settolist,
			SemSimComponent ssctoignore,
			Set<? extends SemSimComponent> preselectedset,
			Set<? extends SemSimComponent> sscstodisable,
			Boolean withdescriptions,
			String title) {

		this.selectableset = settolist;
		this.preselectedset = preselectedset;
		this.ssctoignore = ssctoignore;
		this.sscstodisable = sscstodisable;
		this.title = title;
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
				if(!name.equals(SemGenGUI.unspecifiedName))
					nameobjectmap.put(name, (SemSimComponent)tssc);
			}
			else if(tssc instanceof URI)
				nameobjectmap.put(((URI)tssc).toString(), (URI)tssc);
		}
		
		results = nameobjectmap.keySet().toArray(new String[]{});
		
		Arrays.sort(results, new CaseInsensitiveComparator());

		setPreferredSize(new Dimension(500, 600));
		setTitle(this.title);

		markallbox.setFont(new Font("SansSerif", Font.ITALIC, 11));
		markallbox.setForeground(Color.blue);
		markallbox.setSelected(false);
		markallbox.addActionListener(this);

		panel = new JPanel();
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

		options = new Object[]{"OK","Cancel"};
		optionPane = new JOptionPane(array, JOptionPane.PLAIN_MESSAGE,
				JOptionPane.OK_CANCEL_OPTION, null);
		
		// Need an argument to this function for the parent of the addPropertyChangeListener so can use ObjectPropertyEditor function
		optionPane.addPropertyChangeListener(proplistenerparent);
		optionPane.setOptions(options);
		optionPane.setInitialValue(options[0]);

		setContentPane(optionPane);
		setTitle(title);
		setModalityType(ModalityType.APPLICATION_MODAL);  // If put before pack() get null pointer error when hit OK, if here doesn't act modal
		pack();
		setLocationRelativeTo(SemGenGUI.desktop);
		setVisible(true);
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
