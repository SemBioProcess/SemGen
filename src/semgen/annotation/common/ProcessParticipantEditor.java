package semgen.annotation.common;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;

import semgen.SemGenSettings;
import semgen.annotation.dialog.CreateCompositeDialog;
import semgen.utilities.SemGenError;
import semgen.utilities.SemGenIcon;
import semgen.utilities.uicomponent.SemGenScrollPane;
import semsim.annotation.SemSimTermLibrary;

public abstract class ProcessParticipantEditor extends JPanel implements ActionListener {
	private static final long serialVersionUID = 1L;
	private JButton plusbutton = new JButton(SemGenIcon.plusicon);
	private JButton minusbutton = new JButton(SemGenIcon.minusicon);
	private JButton createbutton = new JButton("Create");
	private ProcessParticipantTableModel tablemodel;
	private JTable table = new JTable(tablemodel);
	private SemSimTermLibrary library;
	protected ArrayList<Integer> participants;
	
	public ProcessParticipantEditor(String name, SemSimTermLibrary lib) {
		library = lib;
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		setBackground(SemGenSettings.lightblue);
		plusbutton.addActionListener(this);
		plusbutton.setToolTipText("Add process participant");

		minusbutton.addActionListener(this);
		minusbutton.setToolTipText("Remove selected process participant");
		
		createbutton.addActionListener(this);
		createbutton.setToolTipText("Create process participant");
		
		JPanel headerpanel = new JPanel();
		headerpanel.setOpaque(false);
		headerpanel.add(new JLabel(name));
		headerpanel.add(plusbutton);
		headerpanel.add(minusbutton);
		headerpanel.add(createbutton);
		
		SemGenScrollPane scroller = new SemGenScrollPane(table);
		scroller.setPreferredSize(new Dimension(550, 100));
		scroller.getViewport().setBackground(Color.white);
		
		add(headerpanel);
		add(scroller);
	}
	
	public void setTableData(LinkedHashMap<Integer, Double> map) {
		participants = new ArrayList<Integer>();
		tablemodel = new ProcessParticipantTableModel(map);
		table.setModel(tablemodel);
	}
	
	public void setTableData(ArrayList<Integer> map) {
		participants = map;
		tablemodel = new ProcessParticipantTableModel(map);
		table.setModel(tablemodel);
	}
	
	public void addParticipant(Integer part) {
		participants.add(part);
		if (tablemodel.getColumnCount()>1) {
			tablemodel.addRow(new String[]{library.getComponentName(part), "1.0"});
		}
		else {
			tablemodel.addRow(new String[]{library.getComponentName(part)});
		}
	}
	
	public void clear() {
		participants.clear();
		tablemodel.clear();
	}
	
	public ArrayList<Integer> getParticipants() {
		return participants;
	}
	
	public ArrayList<Double> getMultipliers() {
		ArrayList<Double> multipliers = new ArrayList<Double>();
		for (int i=0; i<participants.size(); i++) {
			multipliers.add(Double.valueOf((String) tablemodel.getValueAt(i, 1)));
		}
		return multipliers;
	}
	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		Object obj = arg0.getSource();
		if (obj==plusbutton) {
			addParticipant();
		}
		if (obj==minusbutton) {
			removeParticipants();
		}
		if (obj==createbutton) {
			createParticipant();
		}
	}
	
	public void removeParticipant(Integer index) {
		int row = participants.indexOf(index);
		participants.remove(index);
		tablemodel.removeRow(row);
	}
	
	private void removeParticipants() {
		int[] rows = table.getSelectedRows();
		for (int row : rows) {
			participants.remove(row);
		}
		tablemodel.removeRows(rows);
	}
	
	private void createParticipant() {
		CreateCompositeDialog ccd = new CreateCompositeDialog(library);
		if (ccd.getComposite() != -1) {
			addParticipant(ccd.getComposite());
		}
	}
	
	public abstract void addParticipant();
	
	private class ProcessParticipantTableModel extends AbstractTableModel{
		private static final long serialVersionUID = 1L;
		private String[] columnNames;
		private ArrayList<String[]> data = new ArrayList<String[]>();
		
		public ProcessParticipantTableModel(LinkedHashMap<Integer, Double> map) {
			for (Integer key : map.keySet()) {
				participants.add(key);
				data.add(new String[]{library.getComponentName(key), map.get(key).toString()});
			}
			columnNames = new String[]{"Physical entity", "Multiplier"};
		}
		
		public ProcessParticipantTableModel(ArrayList<Integer> tabledata) {
			for (Integer part : tabledata) {
				addRow(new String[]{library.getComponentName(part)});
			}
		
			columnNames = new String[]{"Physical Entity"};
		}
		
		public void addRow(String[] row) {
			data.add(row);
			fireTableDataChanged();
		}
		
		@Override
		public boolean isCellEditable(int x, int y){
			return (y==1);
		}
		
		@Override
		public int getColumnCount() {
			return columnNames.length;
		}
		
		@Override
		public int getRowCount() {
			return data.size();
		}
		
		@Override
		public Object getValueAt(int row, int col) {
			return data.get(row)[col];
		}
		
		public String getColumnName(int col) {
		      return columnNames[col];
	    }
		
		public void clear() {
			int n = data.size();
			data.clear();
			fireTableRowsDeleted(0, n);
		}
		
		public void removeRow(int row) {
			data.remove(row);
			fireTableRowsDeleted(row, row);
		}
		
		public void removeRows(int[] rows){
			for(int x : rows){
				data.remove(x);
				fireTableRowsDeleted(x, x);
			}
		}
		
		@Override
		public void setValueAt(Object value, int row, int col) {
			if(col==1){
				try{
					Double.parseDouble((String)value);
				}
				catch(NumberFormatException ex){
					SemGenError.showError("Multiplier not a valid number.", "Invalid Number");
					return;
				}
			}
			data.get(row)[col] = value.toString();
			fireTableCellUpdated(row, col);
		}
		
	}
	
	public void addTableModelListener(TableModelListener l) {
		table.getModel().addTableModelListener(l);
	}
}
