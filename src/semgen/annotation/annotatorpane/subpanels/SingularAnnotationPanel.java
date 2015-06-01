package semgen.annotation.annotatorpane.subpanels;

import java.awt.event.ActionEvent;

import semgen.annotation.common.AnnotationChooserPanel;
import semgen.annotation.dialog.termlibrary.AddReferenceClassDialog;
import semgen.annotation.workbench.SemSimTermLibrary;
import semgen.annotation.workbench.drawers.AnnotatorDrawer;
import semsim.utilities.ReferenceOntologies.OntologyDomain;

public class SingularAnnotationPanel extends AnnotationChooserPanel {
	private static final long serialVersionUID = 1L;
	private AnnotatorDrawer<?> drawer;
	
	public SingularAnnotationPanel(AnnotatorDrawer<?> wb, SemSimTermLibrary lib) {
		super(lib);
		drawer = wb;
		library = lib;
		if (drawer.isEditable()) {
			makeSingularAnnotationSelector();
		}
		else {
			makeStaticPanel(drawer.getSingularAnnotationLibraryIndex());
		}
		constructSelector();
		refreshSingularAnnotation();
	}
	
	public void makeSingularAnnotationSelector() {
		addURLButton();
		addSearchButton();
		addEraseButton();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource()==combobox) {
			int sing = getSelection();
			drawer.setSingularAnnotation(sing);
			toggleNoneSelected(sing == -1);
		}
	}
	
	// Get the singular annotation for the codeword
	public void refreshSingularAnnotation(){
		Integer i = drawer.getSingularAnnotationLibraryIndex();
		setComboList(library.getSortedPhysicalPropertyIndicies(), i);
	}
	
	@Override
	public void searchButtonClicked() {
		AddReferenceClassDialog rcfd = new AddReferenceClassDialog(library, OntologyDomain.PhysicalProperty);
		if (rcfd.getIndexofSelection()!=-1) {
			setComboList(library.getSortedPhysicalPropertyIndicies(), rcfd.getIndexofSelection());
		}
		
	}

	@Override
	public void createButtonClicked() {	}

	@Override
	public void modifyButtonClicked() {	}

}
