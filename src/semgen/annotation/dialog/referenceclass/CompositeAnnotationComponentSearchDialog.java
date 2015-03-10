package semgen.annotation.dialog.referenceclass;

import semgen.annotation.AnnotatorTab;
import semgen.annotation.annotatorpane.composites.SemSimComponentAnnotationPanel;
import semgen.annotation.workbench.AnnotatorWorkbench;

public class CompositeAnnotationComponentSearchDialog extends AddReferenceClassDialog{
	private static final long serialVersionUID = -6053255066931420852L;
	
	public CompositeAnnotationComponentSearchDialog(AnnotatorWorkbench wb, SemSimComponentAnnotationPanel pmcpanel, String[] ontList, Object[] options){
		super(wb, pmcpanel.annpanel.annotator, ontList, options, pmcpanel.smc);
		utilarea.setText("Current annotation: " + pmcpanel.combobox.getSelectedItem());
	}

	public CompositeAnnotationComponentSearchDialog(AnnotatorWorkbench wb, AnnotatorTab ann, String[] ontList, Object[] options, String prev){
		super(wb, ann, ontList, options, null);
		utilarea.setText("Current annotation: " + prev);
	}
}
