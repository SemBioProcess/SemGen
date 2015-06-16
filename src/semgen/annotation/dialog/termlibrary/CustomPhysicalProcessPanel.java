package semgen.annotation.dialog.termlibrary;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import semgen.annotation.common.CustomTermOptionPane;
import semgen.annotation.common.ObjectPropertyEditor;
import semgen.annotation.common.ProcessParticipantEditor;
import semgen.annotation.dialog.SemSimComponentSelectionDialog;
import semgen.annotation.workbench.SemSimTermLibrary;
import semsim.SemSimConstants;
import semsim.annotation.SemSimRelation;

public abstract class CustomPhysicalProcessPanel extends CustomTermOptionPane {
	private static final long serialVersionUID = 1L;
	
	private ArrayList<ParticipantEditor> editors;
	
	public CustomPhysicalProcessPanel(SemSimTermLibrary lib) {
		super(lib);
	}
	
	public CustomPhysicalProcessPanel(SemSimTermLibrary lib, int processindex) {
		super(lib, processindex);
		
	}
	
	@Override
	protected void makeUnique() {
		editors = new ArrayList<ParticipantEditor>();
		ArrayList<Integer> versionrels = library.getIndiciesofReferenceRelations(termindex, SemSimConstants.BQB_IS_VERSION_OF_RELATION);
		objecteditors.add(new ProcessEditor(library, SemSimConstants.BQB_IS_VERSION_OF_RELATION, versionrels));
		editors.add(new ParticipantEditor("Source Participants", library));
		editors.add(new ParticipantEditor("Sink Participants", library));
		editors.add(new ParticipantEditor("Mediator Participants", library));
		
		for (ParticipantEditor editor : editors) {
			add(editor);
		}
		setParticipants();
	}
	
	protected void setParticipants() {
		if (termindex!=-1) {
			editors.get(0).setTableData(library.getProcessSourcesIndexMultiplierMap(termindex));
			editors.get(1).setTableData(library.getProcessSinksIndexMultiplierMap(termindex));
			editors.get(2).setTableData(library.getProcessMediatorIndicies(termindex));
		}
		else {
			editors.get(0).setTableData(new LinkedHashMap<Integer, Double>());
			editors.get(1).setTableData(new LinkedHashMap<Integer, Double>());
			editors.get(2).setTableData(new ArrayList<Integer>());
		}
	}
	
	public String getTitle() {
		if (termindex==-1) return "Create Custom Physical Process";
		return "Edit " + library.getComponentName(termindex);
	}
	
	@Override
	protected void createTerm() {
		termindex = library.createProcess(mantextfield.getText(), descriptionarea.getText());
		for (ObjectPropertyEditor ope : objecteditors) {
			ope.setRelationships(termindex);
		}
		setProcessParticipants();
	}
	
	@Override
	protected void modifyTerm() {
		library.editProcess(termindex, mantextfield.getText(), descriptionarea.getText());
		for (ObjectPropertyEditor ope : objecteditors) {
			ope.setRelationships(termindex);
		}
		setProcessParticipants();
	}
	
	private void setProcessParticipants() {
		library.setProcessSources(termindex, editors.get(0).getParticipants(), editors.get(0).getMultipliers());
		library.setProcessSinks(termindex, editors.get(1).getParticipants(), editors.get(1).getMultipliers());
		library.setProcessMediators(termindex, editors.get(2).getParticipants());
	}
	
	private class ProcessEditor extends ObjectPropertyEditor {
		private static final long serialVersionUID = 1L;

		public ProcessEditor(SemSimTermLibrary lib, SemSimRelation rel, ArrayList<Integer> complist) {
			super(lib, rel, complist);
		}

		@Override
		protected void showSelectionDialog() {
			ArrayList<Integer> entities = library.getSortedPhysicalProcessIndicies();
			ArrayList<Integer> preselected = new ArrayList<Integer>();
			for (Integer i : components) {
				preselected.add(entities.indexOf(i));
			}
			
			String dialogname = "Annotate " + mantextfield.getText() + " with " + relation.getURIFragment() + " relations.";
			SemSimComponentSelectionDialog seldialog = new SemSimComponentSelectionDialog(dialogname, library.getComponentNames(entities), preselected);
			if (seldialog.isConfirmed()) {
				preselected = seldialog.getSelections();	
				setElements(preselected, entities);			
			}
		}
	}
	
	private class ParticipantEditor extends ProcessParticipantEditor {
		private static final long serialVersionUID = 1L;

		public ParticipantEditor(String name, SemSimTermLibrary lib) {
			super(name, lib);
		}

		@Override
		public void addParticipant() {
			ArrayList<Integer> cpes = library.getSortedCompositePhysicalEntityIndicies();
			ArrayList<ParticipantEditor> notthis = new ArrayList<ParticipantEditor>(editors);
			notthis.remove(this);
			
			ArrayList<Integer> templist = new ArrayList<Integer>();
			for (ParticipantEditor editor : notthis) {
				templist.addAll(editor.getParticipants());
			}
			
			ArrayList<Integer> todisable = new ArrayList<Integer>();
			for (Integer i : templist) {
				todisable.add(cpes.indexOf(i));
			}
			ArrayList<Integer> preselect = new ArrayList<Integer>();
			for (Integer i : getParticipants()) {
				preselect.add(cpes.indexOf(i));
			}
			
			String dialogname = "Add participants to " + mantextfield.getText();
			SemSimComponentSelectionDialog seldialog = new SemSimComponentSelectionDialog(dialogname, library.getComponentNames(cpes), preselect, todisable);
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
}
