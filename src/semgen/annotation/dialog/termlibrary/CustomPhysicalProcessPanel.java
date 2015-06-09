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
	
	private ProcessParticipantEditor sourceeditor;
	private ProcessParticipantEditor sinkeditor;
	private ProcessParticipantEditor mediatoreditor;
	
	public CustomPhysicalProcessPanel(SemSimTermLibrary lib) {
		super(lib);
	}
	
	public CustomPhysicalProcessPanel(SemSimTermLibrary lib, int processindex) {
		super(lib, processindex);
		
	}
	
	@Override
	protected void makeUnique() {
		ArrayList<Integer> versionrels = library.getIndiciesofReferenceRelations(termindex, SemSimConstants.BQB_IS_VERSION_OF_RELATION);
		objecteditors.add(new ProcessEditor(library, SemSimConstants.BQB_IS_VERSION_OF_RELATION, versionrels));
		sourceeditor = new ProcessParticipantEditor("Source Participants", library);
		add(sourceeditor);
		sinkeditor = new ProcessParticipantEditor("Sink Participants", library);
		add(sinkeditor);
		mediatoreditor = new ProcessParticipantEditor("Mediator Participants", library);
		add(mediatoreditor);
		
		setParticipants();
	}
	
	protected void setParticipants() {
		if (termindex!=-1) {
			sourceeditor.setTableData(library.getProcessSourcesIndexMultiplierMap(termindex));
			sinkeditor.setTableData(library.getProcessSinksIndexMultiplierMap(termindex));
			mediatoreditor.setTableData(library.getProcessMediatorIndicies(termindex));
		}
		else {
			sourceeditor.setTableData(new LinkedHashMap<Integer, Double>());
			sinkeditor.setTableData(new LinkedHashMap<Integer, Double>());
			mediatoreditor.setTableData(new ArrayList<Integer>());
		}
	}
	
	public String getTitle() {
		if (termindex==-1) return "Create Custom Physical Process";
		return "Edit " + library.getComponentName(termindex);
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
			
			preselected = seldialog.getSelections();
			
			setElements(preselected, entities);			
		}
		
	}
}
