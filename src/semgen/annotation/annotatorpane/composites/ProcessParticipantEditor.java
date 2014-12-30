package semgen.annotation.annotatorpane.composites;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import semgen.annotation.dialog.selector.SemSimComponentSelectorDialog;
import semgen.utilities.SemGenError;
import semgen.utilities.SemGenIcon;
import semgen.utilities.uicomponent.SemGenScrollPane;
import semsim.SemSimConstants;
import semsim.annotation.SemSimRelation;
import semsim.model.SemSimComponent;
import semsim.model.SemSimModel;
import semsim.model.physical.PhysicalEntity;
import semsim.model.physical.PhysicalProcess;

public class ProcessParticipantEditor extends JPanel implements ActionListener, PropertyChangeListener {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 5639851498722801279L;
	public SemSimModel model;
	public SemSimRelation relation;
	public PhysicalProcess process;
	public JButton plusbutton = new JButton(SemGenIcon.plusicon);
	public JButton minusbutton = new JButton(SemGenIcon.minusicon);
	public JTable table;
	public ProcessParticipantTableModel tablemod;
	public Map<String,PhysicalEntity> namesandparticipantmap = new HashMap<String,PhysicalEntity>();
	public SemSimComponentSelectorDialog sscsd; 

	public ProcessParticipantEditor(SemSimModel model, SemSimRelation relation, PhysicalProcess process) {	
		this.model = model;
		this.relation = relation;
		this.process = process;

		plusbutton.addActionListener(this);
		plusbutton.setToolTipText("Add process participant");

		minusbutton.addActionListener(this);
		minusbutton.setToolTipText("Remove selected process participant");
		
		JPanel headerpanel = new JPanel();
		headerpanel.setOpaque(false);
		headerpanel.add(new JLabel(relation.getName()));
		headerpanel.add(plusbutton);
		headerpanel.add(minusbutton);
		
		setTableData();
		
		SemGenScrollPane scroller = new SemGenScrollPane(table);
		scroller.setPreferredSize(new Dimension(550, 100));
		scroller.getViewport().setBackground(Color.white);
		
		setLayout(new BorderLayout());
		add(headerpanel, BorderLayout.NORTH);
		add(scroller, BorderLayout.SOUTH);
	}
	
	public void setTableData(){
		Set<PhysicalEntity> participants = new HashSet<PhysicalEntity>();
		if(relation == SemSimConstants.HAS_SOURCE_RELATION) participants.addAll(process.getSourcePhysicalEntities());
		else if(relation == SemSimConstants.HAS_SINK_RELATION) participants.addAll(process.getSinkPhysicalEntities());
		else if(relation == SemSimConstants.HAS_MEDIATOR_RELATION) participants.addAll(process.getMediatorPhysicalEntities());
		
		ArrayList<PhysicalEntity> temp = new ArrayList<PhysicalEntity>();
		for(PhysicalEntity pp : participants){
			temp.add(pp);
		}
		tablemod = new ProcessParticipantTableModel(temp);
		table = new JTable(tablemod);
		table.setFillsViewportHeight(false);
	}
	
	public class ProcessParticipantTableModel extends AbstractTableModel{
		private static final long serialVersionUID = 1L;
		private String[] columnNames = new String[]{"Physical entity", "Multiplier"};
		public ArrayList<PhysicalEntity> data = new ArrayList<PhysicalEntity>();
		
		public ProcessParticipantTableModel(ArrayList<PhysicalEntity> data){
			this.data = data;
		}
		
		public int getColumnCount() {
			return columnNames.length;
		}

		public int getRowCount() {
			return data.size();
		}
		
		public String getColumnName(int col) {
		      return columnNames[col];
	    }

		public Object getValueAt(int rowIndex, int columnIndex) {
			PhysicalEntity pp = data.get(rowIndex);
			if(columnIndex==0) return pp.getName();
			else return process.getStoichiometry(pp);
		}
		
		@Override
		public boolean isCellEditable(int x, int y){
			return (y==1);
		}
		
		public void addRow(Object[] rowData){
			PhysicalEntity pp = ((PhysicalEntity)rowData[0]);
			data.add(pp);
			fireTableRowsInserted(getRowCount(), getRowCount());
		}
		
		public void removeRows(int[] rows){
			for(int x : rows){
				data.remove(x);
				fireTableRowsDeleted(x, x);
			}
		}
		
		@Override
		public void setValueAt(Object value, int row, int col){
			if(col==0){
				data.set(row, (PhysicalEntity)value);
			}
			else if(col==1){
				int val = 1;
				try{
					val = Integer.parseInt((String)value);
				}
				catch(NumberFormatException ex){
					SemGenError.showError("Multiplier not a valid number.", "Invalid Number");
					return;
				}

				process.setStoichiometry(data.get(row), val);
			}
		    fireTableCellUpdated(row, col);
		}
	}

	public void actionPerformed(ActionEvent ae) {
		Object o = ae.getSource();
		if (o == plusbutton) {		
			Set<SemSimComponent> sscs = new HashSet<SemSimComponent>();
			for(String ssctempname : namesandparticipantmap.keySet()){
				sscs.add((SemSimComponent) namesandparticipantmap.get(ssctempname));
			}
			PhysicalEntity unspecent = model.getCustomPhysicalEntityByName(SemSimModel.unspecifiedName);
			sscsd = new SemSimComponentSelectorDialog(model.getPhysicalEntitiesAndExclude(unspecent), null, sscs, null, false, "Physical entities in model");
			sscsd.setUpUI(this);
		}
		
		if (o == minusbutton) {
			tablemod.removeRows(table.getSelectedRows());
		}
		

	};

	public void propertyChange(PropertyChangeEvent e) {
		String propertyfired = e.getPropertyName();
		if (propertyfired.equals("value")) {
			String value = sscsd.optionPane.getValue().toString();
			if (value == "OK") {
				namesandparticipantmap.clear();
				for(Component c : sscsd.panel.getComponents()){
					if(c instanceof JCheckBox){
						JCheckBox box = (JCheckBox)c;
						if(box.isSelected()){
							tablemod.addRow(new Object[]{(PhysicalEntity)sscsd.nameobjectmap.get(box.getText())});
						}
					}
				}
			}
			sscsd.dispose();
		}
	}
}
