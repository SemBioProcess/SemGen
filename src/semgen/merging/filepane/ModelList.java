package semgen.merging.filepane;

import java.awt.Color;
import java.awt.Component;
import java.util.Observable;
import java.util.Observer;

import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import semgen.merging.workbench.MergerWorkbench;
import semgen.merging.workbench.MergerWorkbench.MergeEvent;
import semgen.utilities.SemGenFont;
import semgen.utilities.uicomponent.SemGenScrollPane;

public class ModelList extends SemGenScrollPane implements Observer {
	private static final long serialVersionUID = 1L;
	private Color selectioncol = new Color(207, 215, 252);
	private DefaultListModel<String> listmodel = new DefaultListModel<String>();
	private JList<String> modelnamelist = new JList<String>();
	private MergerWorkbench workbench;
	
	public ModelList(MergerWorkbench bench) {
		workbench = bench;
		workbench.addObserver(this);

		modelnamelist.addListSelectionListener(SelectionListener);
		modelnamelist.setLayoutOrientation(JList.VERTICAL);
		modelnamelist.setBackground(Color.white);
		modelnamelist.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		modelnamelist.setCellRenderer(new ModelListRenderer());
		modelnamelist.setVisible(true);
		this.setViewportView(modelnamelist);
		setVisible(true);
	}
	
	@Override
	public void update(Observable arg0, Object arg1) {
		if (arg1 == MergeEvent.modellistupdated) {
			listmodel.removeAllElements();
			for (String name : workbench.getModelNames()) {
				listmodel.addElement(name);
			}
			modelnamelist.setModel(listmodel);
			modelnamelist.validate();
			validate();
		}
	}
	
	
	public class ModelListRenderer extends JLabel implements ListCellRenderer<String>{
		private static final long serialVersionUID = 1L;

		public ModelListRenderer() {
			setOpaque(true);
		}
		
		@Override
		public Component getListCellRendererComponent(
				JList<? extends String> list, String value, int index,
				boolean isSelected, boolean cellHasFocus) {
			Color col = Color.white;
			setFont(SemGenFont.defaultPlain());
			if (isSelected) col = selectioncol;
			else if (index == 0) setForeground(Color.blue);
			else if (index == 1) setForeground(Color.red);
			setBackground(col);
			setText(value.toString());
			
			return this;
		}
	}
	
	private ListSelectionListener SelectionListener = new ListSelectionListener() {
		public void valueChanged(ListSelectionEvent e) {
			workbench.setSelection(modelnamelist.getSelectedIndex());
		}
	};
}
