package semgen.annotation.dialog.referenceclass;

import semgen.annotation.annotatorpane.AnnotationPanel;
import semgen.annotation.workbench.AnnotatorWorkbench;
import semsim.Annotatable;
import semsim.SemSimConstants;
import semsim.annotation.ReferenceOntologyAnnotation;

public class SingularAnnotationEditor extends AddReferenceClassDialog {
	/**
	 * 
	 */
	private static final long serialVersionUID = -293956235473792163L;

	public SingularAnnotationEditor(AnnotatorWorkbench wb, AnnotationPanel anndialog, Object[] options) {
		super(wb, anndialog.annotator, SemSimConstants.ALL_SEARCHABLE_ONTOLOGIES, options, (Annotatable)anndialog.smc);
		utilarea.setText("Old annotation: <none>");
		if(anndialog.smc instanceof Annotatable){
			Annotatable x = ((Annotatable)anndialog.smc);
			ReferenceOntologyAnnotation roa = x.getFirstRefersToReferenceOntologyAnnotation();
			if(x.hasRefersToAnnotation()) utilarea.setText("Old annotation: " + roa.getValueDescription() 
					+ " (" + roa.getOntologyAbbreviation() + ")");
		}
	}


	

}
