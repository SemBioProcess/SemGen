package semgen.annotation.dialog.referenceclass;

import semgen.annotation.annotatorpane.composites.SemSimComponentAnnotationPanel;

public class CompositeAnnotationComponentSearchDialog extends AddReferenceClassDialog{
	private static final long serialVersionUID = -6053255066931420852L;
	
	public CompositeAnnotationComponentSearchDialog(SemSimComponentAnnotationPanel pmcpanel, String[] ontList, Object[] options){
		super(pmcpanel.anndialog.annotator, ontList, options, pmcpanel.smc);
		utilarea.setText("Current annotation: " + pmcpanel.combobox.getSelectedItem());
	}


}
