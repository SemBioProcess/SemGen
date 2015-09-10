package semgen.annotation.common;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import semgen.annotation.workbench.SemSimTermLibrary;
import semgen.utilities.SemGenIcon;
import semgen.utilities.uicomponent.SemGenScrollPane;
import semsim.annotation.SemSimRelation;

public abstract class ObjectPropertyEditor extends JPanel implements ActionListener, ListDataListener, ListSelectionListener {
	private static final long serialVersionUID = 1L;
	private JButton plusbutton = new JButton(SemGenIcon.plusicon);
	private JButton minusbutton = new JButton(SemGenIcon.minusicon);
	private JList<String> listcomponent = new JList<String>();
	protected SemSimRelation relation;
	
	protected ArrayList<Integer> components; 
	protected SemSimTermLibrary library;
	
	public ObjectPropertyEditor(SemSimTermLibrary lib, SemSimRelation rel, ArrayList<Integer> complist) {
		setOpaque(false);
		setAlignmentY(Box.TOP_ALIGNMENT);
		relation = rel;
		library = lib;
		components = complist;		
		JLabel headerlabel = new JLabel(relation.getURIFragment());

		setMaximumSize(new Dimension(9999, 250));
		
		plusbutton.addActionListener(this);
		plusbutton.setToolTipText("Add reference term");

		minusbutton.addActionListener(this);
		minusbutton.setToolTipText("Remove selected reference term");
		minusbutton.setEnabled(false);
		
		JPanel headerpanel = new JPanel();
		headerpanel.setOpaque(false);
		headerpanel.add(headerlabel);
		headerpanel.add(plusbutton);
		headerpanel.add(minusbutton);
		
		
		listcomponent.addListSelectionListener(this);
		SemGenScrollPane scroller = new SemGenScrollPane(listcomponent);
		scroller.setPreferredSize(new Dimension(150, 70));

		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		add(headerpanel);
		add(scroller);	
		add(Box.createVerticalGlue());
		loadList();
	}

	protected void loadList() {
		String[] names = new String[components.size()];
		for (int i=0; i< components.size(); i++) {
			names[i] = library.getComponentName(components.get(i));
		}
		listcomponent.setListData(names);
		listcomponent.getModel().addListDataListener(this);
	}
	
	protected void removeElement() {
		int index = listcomponent.getSelectedIndex();
		listcomponent.clearSelection();
		components.remove(index);
		loadList();
	}
	public void clear() {
		components.clear();
		listcomponent.removeAll();
	}
	
	protected abstract void showSelectionDialog();
	
	protected void setElements(ArrayList<Integer> selections, ArrayList<Integer> entities) {
		components.clear();
		for (Integer i : selections) {
			components.add(entities.get(i));
		}
		loadList();
	}
	
	public void setRelationships(int index) {
		library.clearRelations(index, relation);
		if (!components.isEmpty()) {
			for (Integer c : components) {
				library.addRelationship(index, relation, c);
			}
		}
	}
	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		Object obj = arg0.getSource();
		if (minusbutton.equals(obj)) {
			removeElement();
		}
		if (plusbutton.equals(obj)) {
			showSelectionDialog();
		}
	}
	
	@Override
	public void contentsChanged(ListDataEvent e) {
		minusbutton.setEnabled(components.size()!=0);
	}

	@Override
	public void intervalAdded(ListDataEvent e) {
		
	}

	@Override
	public void intervalRemoved(ListDataEvent e) {
		
	}
	
	@Override
	public void valueChanged(ListSelectionEvent arg0) {
		minusbutton.setEnabled(!listcomponent.isSelectionEmpty());
	}
	
	public void addActionListener(ActionListener l) {
		plusbutton.addActionListener(l);
		minusbutton.addActionListener(l);
	}
}
