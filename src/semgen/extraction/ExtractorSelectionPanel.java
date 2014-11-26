package semgen.extraction;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.semanticweb.owlapi.model.OWLException;

import semgen.utilities.ComparatorByName;
import semgen.utilities.SemGenFont;
import semgen.utilities.SemGenIcon;
import semgen.utilities.uicomponent.SemGenScrollPane;
import semsim.model.SemSimComponent;
import semsim.model.computational.datastructures.DataStructure;
import semsim.model.physical.PhysicalModelComponent;
import semsim.model.physical.Submodel;

public class ExtractorSelectionPanel extends JPanel implements ActionListener, MouseListener {
	private static final long serialVersionUID = -487389420876921191L;
	public JLabel titlelabel = new JLabel();
	public JCheckBox markallbox = new JCheckBox("");
	public JPanel checkboxpanel = new JPanel();
	public SemGenScrollPane scroller;
	public Hashtable<? extends SemSimComponent,Set<DataStructure>> termandcdwdstable;
	public Map<String, JCheckBox> termandcheckboxmap = new HashMap<String,JCheckBox>();
	JPanel titlepanel = new JPanel();
	public ExtractorTab extractor;
	public JButton expandcontractbutton = new JButton(SemGenIcon.expendcontracticon);

	public ExtractorSelectionPanel(ExtractorTab extractor, String title, Hashtable<? extends SemSimComponent,Set<DataStructure>> table, JComponent addon){
		this.extractor = extractor;
		markallbox.setFont(SemGenFont.defaultItalic(-2));
		markallbox.setToolTipText("Select all/none");
		termandcdwdstable = (Hashtable<? extends SemSimComponent, Set<DataStructure>>) table;
		setBackground(Color.white);
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		checkboxpanel.setLayout(new BoxLayout(checkboxpanel, BoxLayout.Y_AXIS));
		checkboxpanel.setBackground(Color.white);

		titlepanel.setLayout(new BoxLayout(titlepanel, BoxLayout.X_AXIS));
		titlepanel.setPreferredSize(new Dimension(ExtractorTab.leftpanewidth, 30));
		titlepanel.setMaximumSize(new Dimension(9999999,35));
		titlepanel.setMinimumSize(titlepanel.getPreferredSize());
		titlepanel.setBorder(BorderFactory.createEtchedBorder());

		expandcontractbutton.addMouseListener(this);
		expandcontractbutton.setBorderPainted(false);
		expandcontractbutton.setContentAreaFilled(false);
		expandcontractbutton.setAlignmentX(RIGHT_ALIGNMENT);
		expandcontractbutton.setToolTipText("Expand/collapse panel");

		titlelabel.addMouseListener(this);
		titlelabel.setFont(SemGenFont.defaultBold());
		titlelabel.setForeground(Color.blue);
		titlelabel.setBorder(BorderFactory.createEmptyBorder(0,5,0,5));
		
		markallbox.addActionListener(this);
		
		scroller = new SemGenScrollPane(checkboxpanel);
		scroller.setPreferredSize(new Dimension(ExtractorTab.leftpanewidth,250));
		scroller.getHorizontalScrollBar().setMaximumSize(new Dimension(999999, 3));
		
		int numcb = addCheckBoxes(termandcdwdstable);
		if(numcb==0) scroller.setVisible(false);

		titlelabel.setText(title + " (" + numcb + ")");

		titlepanel.add(markallbox);
		titlepanel.add(titlelabel);
		markallbox.setBorder(BorderFactory.createEmptyBorder(0,5,0,0));
		if(addon!=null){titlepanel.add(addon);}
		titlepanel.add(Box.createGlue());
		titlepanel.add(expandcontractbutton);
		
		for(Component c : this.getComponents()){
			((JComponent) c).setAlignmentX(LEFT_ALIGNMENT);
		}
	}
	
	public int addCheckBoxes(Hashtable<? extends SemSimComponent,Set<DataStructure>> table) {
		
		ArrayList<JCheckBox> cbarray = new ArrayList<JCheckBox>();
		String checkboxtext = null;
		for (SemSimComponent ssc : table.keySet()) {
			if(ssc instanceof PhysicalModelComponent){
				PhysicalModelComponent pmc  = (PhysicalModelComponent)ssc;
				if(pmc.hasRefersToAnnotation() && !(pmc instanceof Submodel)){
					checkboxtext = pmc.getName() + " (" + pmc.getFirstRefersToReferenceOntologyAnnotation().getOntologyAbbreviation() + ")";
				}
				else checkboxtext = pmc.getName();
			}
			else checkboxtext = ssc.getName();
			
			JCheckBox checkbox;
			if(ssc instanceof PhysicalModelComponent)
				checkbox = new ExtractorJCheckBox(checkboxtext, (PhysicalModelComponent)ssc, (Set<DataStructure>) table.get(ssc));
			else
				checkbox = new ExtractorJCheckBox(checkboxtext, (Set<DataStructure>) table.get(ssc));
			
			
			checkbox.setBackground(Color.white);
			checkbox.setName(checkboxtext);
			checkbox.setSelected(false);
			checkbox.addActionListener(this);
			checkbox.addItemListener(extractor);
			checkbox.setFont(SemGenFont.defaultBold(-1));
			cbarray.add(checkbox);
			termandcheckboxmap.put(checkboxtext, checkbox);
		}
		Comparator<Component> byVarName = new ComparatorByName();
		JCheckBox[] sortedarray = cbarray.toArray(new JCheckBox[]{});
		Arrays.sort(sortedarray, byVarName);
		for(JCheckBox cb : sortedarray){
			checkboxpanel.add(cb);
		}
		return sortedarray.length;
	}

	public void actionPerformed(ActionEvent e) {
		Object o = e.getSource();
		if (o == markallbox) {
			// Get checkboxes, temporarily remove item listeners so we don't get a huge number of viz updates
			for (int i = 0; i < checkboxpanel.getComponentCount(); i++) {
				if (checkboxpanel.getComponent(i) instanceof JCheckBox) {
					JCheckBox checkbox = (JCheckBox) checkboxpanel.getComponent(i);
					checkbox.removeItemListener(extractor);
					checkbox.setSelected(markallbox.isSelected());
				}
			}
			try {
				extractor.visualize(extractor.primeextraction(), false);
			} catch (OWLException | IOException e1) {
				e1.printStackTrace();
			}
			// Add the item listener back
			for(Component c : checkboxpanel.getComponents()){
				if (c instanceof JCheckBox) {
					JCheckBox checkbox = (JCheckBox) c;
					checkbox.addItemListener(extractor);
				}
			}
		}
	}

	public void mouseClicked(MouseEvent arg0) {
		if(arg0.getSource() == expandcontractbutton || (arg0.getClickCount()==2 && arg0.getSource()==titlelabel)){
			scroller.setVisible(!scroller.isVisible());
			extractor.repaint();
			extractor.validate();
		}
	}

	public void mouseEntered(MouseEvent arg0) {
		if(arg0.getSource() == expandcontractbutton){
			expandcontractbutton.setBorderPainted(true);
			expandcontractbutton.setContentAreaFilled(false);
		}
	}

	public void mouseExited(MouseEvent arg0) {
		if(arg0.getSource() == expandcontractbutton){
			expandcontractbutton.setBorderPainted(false);
			expandcontractbutton.setContentAreaFilled(false);
		}
	}

	public void mousePressed(MouseEvent arg0) {}

	public void mouseReleased(MouseEvent arg0) {}
}
