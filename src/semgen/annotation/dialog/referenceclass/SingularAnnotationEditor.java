package semgen.annotation.dialog.referenceclass;

import java.beans.PropertyChangeEvent;
import java.net.URI;

import javax.swing.JOptionPane;

import semgen.annotation.annotatorpane.AnnotationPanel;
import semsim.Annotatable;
import semsim.SemSimConstants;
import semsim.model.annotation.ReferenceOntologyAnnotation;

public class SingularAnnotationEditor extends AddReferenceClassDialog {
	/**
	 * 
	 */
	private static final long serialVersionUID = -293956235473792163L;
	public AnnotationPanel dia;
	public String codeword;

	public SingularAnnotationEditor(AnnotationPanel anndialog, Object[] options) {
		super(anndialog.annotator, SemSimConstants.ALL_SEARCHABLE_ONTOLOGIES, options, (Annotatable)anndialog.smc);
		dia = anndialog;
		codeword = dia.codeword;
		refclasspanel.codeword = codeword;
		utilarea.setText("Old annotation: <none>");
		if(dia.smc instanceof Annotatable){
			Annotatable x = ((Annotatable)dia.smc);
			ReferenceOntologyAnnotation roa = x.getFirstRefersToReferenceOntologyAnnotation();
			if(x.hasRefersToAnnotation()) utilarea.setText("Old annotation: " + roa.getValueDescription() 
					+ " (" + roa.getOntologyAbbreviation() + ")");
		}
		packAndSetModality();
	}
	
	@Override
	public void packAndSetModality(){
		setModalityType(ModalityType.APPLICATION_MODAL);
		pack();
		setVisible(true);
	}

	public void addClassToOntology() {
		if (refclasspanel.resultslistright.getSelectedValue() != null) {
			String selectedname = (String) refclasspanel.resultslistright.getSelectedValue();
			String referenceuri = (String) refclasspanel.resultsanduris.get(selectedname);
			selectedname = selectedname.replaceAll("\"", "");
			ReferenceOntologyAnnotation ann = new ReferenceOntologyAnnotation(SemSimConstants.REFERS_TO_RELATION, URI.create(referenceuri), selectedname);
			dia.singularannpanel.applyReferenceOntologyAnnotation(ann, true);
		}
		dia.singularannpanel.refreshComboBoxItemsAndButtonVisibility();
	}
	
	public void propertyChange(PropertyChangeEvent arg0) {
		String propertyfired = arg0.getPropertyName();
		if (propertyfired.equals("value")) {
			String value = optionPane.getValue().toString();

			//	 If we're using this dialog to apply a non-composite annotation
			if(value == "Annotate" && this.getFocusOwner() != refclasspanel.findbox){
				addClassToOntology();
				dia.annotator.setModelSaved(false);
				optionPane.setValue(JOptionPane.UNINITIALIZED_VALUE);
			}
			dispose();
		}
	}
}
