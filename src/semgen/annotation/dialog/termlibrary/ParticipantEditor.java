package semgen.annotation.dialog.termlibrary;

import java.util.ArrayList;

import semgen.annotation.common.CustomTermOptionPane;
import semgen.annotation.common.ProcessParticipantEditor;
import semgen.annotation.dialog.SemSimComponentSelectionDialog;
import semsim.annotation.SemSimTermLibrary;

public class ParticipantEditor extends ProcessParticipantEditor {
	private static final long serialVersionUID = 1L;
	private CustomTermOptionPane ctop;

	public ParticipantEditor(String name, SemSimTermLibrary lib, CustomTermOptionPane ctop) {
		super(name, lib);
		this.ctop = ctop;
	}

	@Override
	public void addParticipant() {
		ArrayList<Integer> cpes = library.getSortedCompositePhysicalEntityIndicies();
		ArrayList<Integer> preselect = new ArrayList<Integer>();
		
		for (Integer i : getParticipants()) {
			preselect.add(cpes.indexOf(i));
		}
		
		String dialogname = "Add participants to " + ctop.mantextfield.getText();
		SemSimComponentSelectionDialog seldialog = new SemSimComponentSelectionDialog(dialogname, library.getComponentNames(cpes), preselect);
		if (seldialog.isConfirmed()) {
			preselect = seldialog.getSelections();
			ArrayList<Integer> newsels = new ArrayList<Integer>();
			for (Integer i : preselect) {
				newsels.add(cpes.get(i));
			}
			//Remove components that are no longer selected
			for (Integer p : new ArrayList<Integer>(participants)) {
				if (!newsels.contains(p)) {
					removeParticipant(p);
				}
			}
			//Add new participants
			for (Integer p : newsels) {
				if (!participants.contains(p)) {
					addParticipant(p);
				}
			}
		}
	}	
}