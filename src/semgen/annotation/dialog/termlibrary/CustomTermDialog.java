package semgen.annotation.dialog.termlibrary;

import java.awt.event.ActionEvent;
import semgen.annotation.common.CustomTermOptionPane;
import semgen.utilities.uicomponent.SemGenDialog;
import semsim.annotation.SemSimTermLibrary;

public class CustomTermDialog extends SemGenDialog {
	private static final long serialVersionUID = 1L;
	public CustomTermOptionPane custompane;
	
	public CustomTermDialog() {
		super("");
		
	}

	public void setAsEntityTermDialog(SemSimTermLibrary lib) {
		custompane = new CustomEntityPane(lib);
		makeDialog();
	}
	
	public void setAsEntityTermDialog(SemSimTermLibrary lib, int termindex) {
		custompane = new CustomEntityPane(lib, termindex);
		makeDialog();
	}

	public void setAsProcessTermDialog(SemSimTermLibrary lib) {
		custompane = new CustomProcessPane(lib);
		makeDialog();
	}
	
	public void setAsProcessTermDialog(SemSimTermLibrary lib, int termindex) {
		custompane = new CustomProcessPane(lib, termindex);
		makeDialog();
	}
	
	public void setAsEnergyDiffTermDialog(SemSimTermLibrary lib, int termindex) {
		custompane = new CustomForcePane(lib, termindex);
		makeDialog();
	}
	
	
	
	private void makeDialog() {
		setTitle(custompane.getTitle());
		setContentPane(custompane);
		showDialog();
	}
	
	public int getSelection() {
		return custompane.getSelection();
	}
	
	// If dealing with an entity
	private class CustomEntityPane extends CustomTermOptionPane {
		private static final long serialVersionUID = 1L;

		public CustomEntityPane(SemSimTermLibrary lib) {
			super(lib);
		}
		
		public CustomEntityPane(SemSimTermLibrary lib, Integer libindex) {
			super(lib, libindex);
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			Object obj = arg0.getSource();
			if (obj.equals(cancelbtn)) {
				termindex = -1;
				dispose();
			}
			if (obj.equals(createbtn)) {
				if (termindex!=-1) modifyTerm();
				else createTerm();
				dispose();
			}
		}
	}
	
	
	// If dealing with a process
	private class CustomProcessPane extends CustomPhysicalProcessOptionPane {
		private static final long serialVersionUID = 1L;

		public CustomProcessPane(SemSimTermLibrary lib) {
			super(lib);
		}
		
		public CustomProcessPane(SemSimTermLibrary lib, Integer libindex) {
			super(lib, libindex);
			
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			Object obj = arg0.getSource();
			if (obj.equals(cancelbtn)) {
				termindex = -1;
				dispose();
			}
			if (obj.equals(createbtn)) {
				if (termindex!=-1) modifyTerm();
				else createTerm();
				dispose();
			}
		}
	}
	
	
	// If dealing with a force
	private class CustomForcePane extends CustomPhysicalForceOptionPane {
		private static final long serialVersionUID = 1L;

//		public CustomForcePane(SemSimTermLibrary lib) {
//			super(lib);
//		}
		
		public CustomForcePane(SemSimTermLibrary lib, Integer libindex) {
			super(lib, libindex);
		}
		

		@Override
		public void actionPerformed(ActionEvent arg0) {
			Object obj = arg0.getSource();
			if (obj.equals(cancelbtn)) {
				termindex = -1;
				dispose();
			}
			if (obj.equals(createbtn)) {
				modifyTerm();
				dispose();
			}
		}
	}
}
