package semgen.annotation.dialog.termlibrary;

import java.awt.event.ActionEvent;
import semgen.annotation.common.CustomTermOptionPane;
import semgen.annotation.workbench.SemSimTermLibrary;
import semgen.utilities.uicomponent.SemGenDialog;

public class CustomTermDialog extends SemGenDialog {
	private static final long serialVersionUID = 1L;
	CustomTermOptionPane custompane;
	
	public CustomTermDialog() {
		super("");
		
	}

	public void makeEntityTerm(SemSimTermLibrary lib) {
		custompane = new CustomEntityPane(lib);
		makeDialog();
	}
	
	public void makeEntityTerm(SemSimTermLibrary lib, int termindex) {
		custompane = new CustomEntityPane(lib, termindex);
		makeDialog();
	}

	public void makeProcessTerm(SemSimTermLibrary lib) {
		custompane = new CustomProcessPane(lib);
		makeDialog();
	}
	
	public void makeProcessTerm(SemSimTermLibrary lib, int termindex) {
		custompane = new CustomProcessPane(lib, termindex);
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
	
	private class CustomProcessPane extends CustomPhysicalProcessPanel {
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
				createTerm();
				dispose();
			}
		}
	}
}
