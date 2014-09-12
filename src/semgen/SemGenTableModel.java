package semgen;

import javax.swing.table.DefaultTableModel;

public class SemGenTableModel extends DefaultTableModel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5754076585721642363L;
	public String[] colNames;
	public String[][] data;

	public SemGenTableModel(String[][] data, String[] colNames) {
		super(data, colNames);
		this.data = data;
		this.colNames = colNames;
	}

	public int getColumnCount() {
		return colNames.length;
	}

	public int getRowCount() {
		return data.length;
	}

	public String getColumnName(int col) {
		return colNames[col];
	}

	public Object getValueAt(int row, int col) {
		return data[row][col];
	}

}
