package semgen.annotation.dialog.termlibrary;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import semgen.annotation.common.CustomTermOptionPane;
import semgen.annotation.common.ObjectPropertyEditor;
import semgen.annotation.dialog.SemSimComponentSelectionDialog;
import semsim.annotation.SemSimTermLibrary;
import semsim.definitions.SemSimRelations.SemSimRelation;

public abstract class CustomPhysicalProcessOptionPane extends CustomTermOptionPane implements TableModelListener {
	private static final long serialVersionUID = 1L;
	
	private ArrayList<ParticipantEditor> editors;
	
	public CustomPhysicalProcessOptionPane(SemSimTermLibrary lib) {
		super(lib);
	}
	
	public CustomPhysicalProcessOptionPane(SemSimTermLibrary lib, int processindex) {
		super(lib, processindex);
		
	}
	
	@Override
	protected void makeUnique() {
		editors = new ArrayList<ParticipantEditor>();
		ArrayList<Integer> versionrels = library.getIndiciesofReferenceRelations(termindex, SemSimRelation.BQB_IS_VERSION_OF);
		objecteditors.add(new ProcessEditor(library, SemSimRelation.BQB_IS_VERSION_OF, versionrels));
		
		editors.add(new ParticipantEditor("Source Participants", library, this));
		editors.add(new ParticipantEditor("Sink Participants", library, this));
		editors.add(new ParticipantEditor("Mediator Participants", library, this));
		
		setParticipantTableData();
		
		// If process is from an SBML model in an OMEX archive, don't allow
		// users to change the participant info, since it's embedded in the SBML
		if(termindex != -1) {
			
			if(library.getPhysicalProcess(termindex).isFromSBMLinOMEXarchive()) { 
				
				for(ParticipantEditor ed : editors) {
					ed.setEditable(false);
				}
			}
		}
	}
	
	protected void setParticipantTableData() {
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
		for (ParticipantEditor editor : editors) {
			editor.addTableModelListener(this);
			add(editor);
		}
	}
	
	@Override
	public String getTitle() {
		if (termindex==-1) return "Create Custom Physical Process";
		return "Edit " + library.getComponentName(termindex);
	}
	
	@Override
	protected Integer createTerm() {
		termindex = library.createCustomProcess(mantextfield.getText(), descriptionarea.getText());
		for (ObjectPropertyEditor ope : objecteditors) {
			ope.setRelationships(termindex);
		}
		setProcessParticipants();
		return termindex;
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
	
	public void clear() {
		super.clear();
		for (ParticipantEditor e : editors) {
			e.clear();
		}
	}
	
	public class ProcessEditor extends ObjectPropertyEditor {
		private static final long serialVersionUID = 1L;

		public ProcessEditor(SemSimTermLibrary lib, SemSimRelation rel, ArrayList<Integer> complist) {
			super(lib, rel, complist);
		}

		@Override
		protected void showSelectionDialog() {
			ArrayList<Integer> entities = library.getSortedReferencePhysicalProcessIndicies();
			ArrayList<Integer> preselected = new ArrayList<Integer>();
			for (Integer i : components) {
				preselected.add(entities.indexOf(i));
			}
			
			String dialogname = "Annotate " + mantextfield.getText() + " with " + relation.getName() + " relations.";
			SemSimComponentSelectionDialog seldialog = new SemSimComponentSelectionDialog(dialogname, library.getComponentNames(entities), preselected);
			if (seldialog.isConfirmed()) {
				preselected = seldialog.getSelections();	
				setElements(preselected, entities);			
			}
		}
	}
	
	
	@Override
	public void tableChanged(TableModelEvent arg0) {
		if (termindex!=-1) {
			createbtn.setEnabled(true);
		}
	}
		
}
