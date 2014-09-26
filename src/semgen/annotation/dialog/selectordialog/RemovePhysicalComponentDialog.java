package semgen.annotation.dialog.selectordialog;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;

import javax.swing.JOptionPane;

public class RemovePhysicalComponentDialog extends SemSimComponentSelectorDialog {

	private static final long serialVersionUID = -3622371132272217159L;
	
	public RemovePhysicalComponentDialog(ArrayList<String> settolist){
		super(settolist, null, null, "Select components to remove");
		setUpUI(new Object[]{"Remove", "Close"});
	}

	public Boolean confirmDialog(PropertyChangeEvent arg0) {
			optionPane.setValue(JOptionPane.UNINITIALIZED_VALUE);
			int choice = JOptionPane.showConfirmDialog(this, 
					"This will remove all selected terms from existing annotations.\nAre you sure you want to continue?",
					"Confirm",
					JOptionPane.YES_NO_OPTION);
			if(JOptionPane.YES_OPTION == choice){
				return true;
			}
			return false;
	}

}
