package semgen.annotation.common;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import org.apache.commons.lang3.tuple.Pair;

import semgen.annotation.workbench.SemSimTermLibrary;
import semgen.utilities.SemGenIcon;
import semgen.utilities.uicomponent.SemGenScrollPane;

public class ProcessParticipantEditor extends JPanel implements ActionListener {
	private static final long serialVersionUID = 1L;
	private JButton plusbutton = new JButton(SemGenIcon.plusicon);
	private JButton minusbutton = new JButton(SemGenIcon.minusicon);
	private JButton createbutton = new JButton("Create");
	private ProcessParticipantTableModel tablemodel;
	private JTable table = new JTable(tablemodel);
	private SemSimTermLibrary library;
	private ArrayList<Integer> participants;
	
	public ProcessParticipantEditor(String name, SemSimTermLibrary lib) {
		library = lib;
		setLayout(new BorderLayout());
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
		
		add(headerpanel, BorderLayout.NORTH);
		add(scroller, BorderLayout.SOUTH);
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
	
	public void addParticipant(Integer part, Double mult) {
		participants.add(part);
	}

	private class ProcessParticipantTableModel extends AbstractTableModel{
		private static final long serialVersionUID = 1L;
		private String[] columnNames;
		private ArrayList<String[]> data = new ArrayList<String[]>();
		
		public ProcessParticipantTableModel(LinkedHashMap<Integer, Double> map) {
			for (Integer key : map.keySet()) {
				participants.add(key);
				addRow(new String[]{library.getComponentName(key), map.get(key).toString()});
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
		
	}
	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		
	}
}
