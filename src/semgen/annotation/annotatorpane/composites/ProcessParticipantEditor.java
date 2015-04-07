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
import javax.swing.table.TableColumn;

import org.apache.commons.lang3.tuple.Pair;

import semgen.annotation.dialog.referenceclass.compositedialog.CreateCompositeDialog;
import semgen.annotation.dialog.selector.SemSimComponentSelectorDialog;
import semgen.annotation.workbench.AnnotatorWorkbench;
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

	private static final long serialVersionUID = 5639851498722801279L;
	public AnnotatorWorkbench workbench;
	public SemSimRelation relation;
	public PhysicalProcess process;
	private JButton plusbutton = new JButton(SemGenIcon.plusicon);
	private JButton minusbutton = new JButton(SemGenIcon.minusicon);
	private JButton createbutton = new JButton("Create");
	public JTable table;
	public ProcessParticipantTableModel tablemod;
	public Map<String,PhysicalEntity> namesandparticipantmap = new HashMap<String,PhysicalEntity>();
	private SemSimComponentSelectorDialog sscsd; 

	public ProcessParticipantEditor(AnnotatorWorkbench wb, SemSimRelation relation, PhysicalProcess process) {	
		workbench = wb;
		this.relation = relation;
		this.process = process;

		plusbutton.addActionListener(this);
		plusbutton.setToolTipText("Add process participant");

		minusbutton.addActionListener(this);
		minusbutton.setToolTipText("Remove selected process participant");
		
		createbutton.addActionListener(this);
		createbutton.setToolTipText("Create process participant");
		
		JPanel headerpanel = new JPanel();
		headerpanel.setOpaque(false);
		headerpanel.add(new JLabel(relation.getName()));
		headerpanel.add(plusbutton);
		headerpanel.add(minusbutton);
		headerpanel.add(createbutton);
		
		setTableData();
		
		SemGenScrollPane scroller = new SemGenScrollPane(table);
		scroller.setPreferredSize(new Dimension(550, 100));
		scroller.getViewport().setBackground(Color.white);
		
		setLayout(new BorderLayout());
		add(headerpanel, BorderLayout.NORTH);
		add(scroller, BorderLayout.SOUTH);
	}
	
	public ArrayList<Pair<PhysicalEntity, Double>> getTableData() {
		return tablemod.getAllRows();
	}
	
	private void setTableData(){
		Set<PhysicalEntity> participants = new HashSet<PhysicalEntity>();
		if(relation == SemSimConstants.HAS_SOURCE_RELATION) participants.addAll(process.getSourcePhysicalEntities());
		else if(relation == SemSimConstants.HAS_SINK_RELATION) participants.addAll(process.getSinkPhysicalEntities());
		else if(relation == SemSimConstants.HAS_MEDIATOR_RELATION) participants.addAll(process.getMediatorPhysicalEntities());
		
		ArrayList<PhysicalEntity> temp = new ArrayList<PhysicalEntity>();
		for(PhysicalEntity pe : participants){
			temp.add(pe);
		}
		tablemod = new ProcessParticipantTableModel(temp);
		table = new JTable(tablemod);
		
		// If we are showing the mediators, remove the multiplier field in the table
		if(relation == SemSimConstants.HAS_MEDIATOR_RELATION){
			TableColumn multcolumn = table.getColumn("Multiplier");
			table.getColumnModel().removeColumn(multcolumn);
		}
		table.setFillsViewportHeight(false);
	}
	
	public class ProcessParticipantTableModel extends AbstractTableModel{
		private static final long serialVersionUID = 1L;
		private String[] columnNames = new String[]{"Physical entity", "Multiplier"};
		public ArrayList<ParticipantRow> data = new ArrayList<ParticipantRow>();
		
		public ProcessParticipantTableModel(ArrayList<PhysicalEntity> pes){
			for (PhysicalEntity pe : pes) {
				data.add(new ParticipantRow(pe,process.getStoichiometry(pe)));
			}
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
			PhysicalEntity pe = data.get(rowIndex).getParticipant();
			if(columnIndex==0) return pe.getName();
			else return data.get(rowIndex).getMultiplier();
		}
		private ArrayList<Pair<PhysicalEntity, Double>> getAllRows() {
			ArrayList<Pair<PhysicalEntity, Double>> tabledata = new ArrayList<Pair<PhysicalEntity, Double>>();
			for (ParticipantRow row : data) {
				tabledata.add(Pair.of(row.getParticipant(), row.getMultiplier()));
			}
			return tabledata;
		}
		
		@Override
		public boolean isCellEditable(int x, int y){
			return (y==1);
		}
		
		public void addRow(Object[] rowData){
			PhysicalEntity pe = ((PhysicalEntity)rowData[0]);
			data.add(new ParticipantRow(pe));
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
			if(col==1){
				Double val = 1.0;
				try{
					val = Double.parseDouble((String)value);
				}
				catch(NumberFormatException ex){
					SemGenError.showError("Multiplier not a valid number.", "Invalid Number");
					return;
				}
				data.get(row).setMultiplier(val);
			}
		    fireTableCellUpdated(row, col);
		}
		
		protected class ParticipantRow {
			PhysicalEntity participant;
			Double multiplier;
			
			ParticipantRow(PhysicalEntity pe) {
				participant = pe;
				multiplier = 1.0;
			}
			
			ParticipantRow(PhysicalEntity pe, Double stoich) {
				participant = pe;
				multiplier = stoich;
			}
			
			String getParticipantName() {
				return participant.getName();
			}
			
			PhysicalEntity getParticipant() {
				return participant;
			}
			
			Double getMultiplier() {
				return multiplier;
			}
			
			void setMultiplier(Double stoich) {
				multiplier = stoich;
			}
		}
	}

	public void actionPerformed(ActionEvent ae) {
		Object o = ae.getSource();
		if (o == plusbutton) {		
			Set<SemSimComponent> sscs = new HashSet<SemSimComponent>();
			for(String ssctempname : namesandparticipantmap.keySet()){
				sscs.add((SemSimComponent) namesandparticipantmap.get(ssctempname));
			}
			PhysicalEntity unspecent = workbench.getSemSimModel().getCustomPhysicalEntityByName(SemSimModel.unspecifiedName);
			sscsd = new SemSimComponentSelectorDialog(workbench, workbench.getSemSimModel().getPhysicalEntitiesAndExclude(unspecent), null, sscs, null, false, "Physical entities in model");
			sscsd.setUpUI(this);
		}
		
		if (o == minusbutton) {
			tablemod.removeRows(table.getSelectedRows());
		}
		if (o == createbutton) {
			CreateCompositeDialog ccd = new CreateCompositeDialog(workbench, null, false);
			if (ccd.getComposite()!=null) tablemod.addRow(new Object[]{ccd.getComposite()});
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
