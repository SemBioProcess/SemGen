package semgen.annotation.dialog.referencedialog;

import java.beans.PropertyChangeEvent;
import java.net.URI;

import javax.swing.JOptionPane;

import semsim.SemSimConstants;
import semsim.model.annotation.ReferenceOntologyAnnotation;

public class SingularAnnotationEditor extends AddReferenceClassDialog {
	private static final long serialVersionUID = -293956235473792163L;
	
	ReferenceOntologyAnnotation roa;

	public SingularAnnotationEditor(ReferenceOntologyAnnotation ra) {
		super(SemSimConstants.ALL_SEARCHABLE_ONTOLOGIES, new Object[]{"Annotate", "Close"});
		roa = ra;
		
		if(roa!=null) utilarea.setText("Old annotation: " + roa.getValueDescription() 
					+ " (" + roa.getOntologyAbbreviation() + ")");
		else utilarea.setText("Old annotation: <none>");
		
		packAndSetModality();
	}
	
	@Override
	public void packAndSetModality(){
		setModalityType(ModalityType.APPLICATION_MODAL);
		pack();
		setLocationRelativeTo(getParent());
		setVisible(true);
	}
	
	public void propertyChange(PropertyChangeEvent arg0) {
		String value = optionPane.getValue().toString();

//	 If we're using this dialog to apply a non-composite annotation
		if(value == "Annotate" && this.getFocusOwner() != refclasspanel.findbox){
			if (refclasspanel.resultslistright.getSelectedValue() != null) {
				addClassToOntology();
			}
			optionPane.setValue(JOptionPane.UNINITIALIZED_VALUE);
		}
		dispose();
	}
	
	protected void addClassToOntology() {
			String selectedname = (String) refclasspanel.resultslistright.getSelectedValue();
			String referenceuri = (String) refclasspanel.resultsanduris.get(selectedname);
			if (roa.getReferenceURI().toString()!=referenceuri) {
				selectedname = selectedname.replaceAll("\"", "");
				roa = new ReferenceOntologyAnnotation(SemSimConstants.REFERS_TO_RELATION, URI.create(referenceuri), selectedname);
			}
			
	}
	
	public ReferenceOntologyAnnotation getAnnotation() {
		return roa;
	}
}
