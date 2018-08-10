package semgen.annotation.annotatorpane.subpanels;

import java.awt.event.ActionEvent;

import javax.swing.BorderFactory;

import semgen.SemGenSettings;
import semgen.annotation.common.AnnotationChooserPanel;
import semgen.annotation.dialog.termlibrary.AddReferenceClassDialog;
import semgen.annotation.workbench.SemSimTermLibrary;
import semgen.annotation.workbench.drawers.CodewordToolDrawer;
import semsim.definitions.ReferenceOntologies.OntologyDomain;

public class SingularAnnotationPanel extends AnnotationChooserPanel {
	private static final long serialVersionUID = 1L;
	private CodewordToolDrawer drawer;
	private SemGenSettings settings;
	private int indent = 15;
	
	public SingularAnnotationPanel(CodewordToolDrawer wb, SemSimTermLibrary lib, SemGenSettings sets) {
		super(lib);
		drawer = wb;
		library = lib;
		settings = sets;
		this.setBorder(BorderFactory.createEmptyBorder(0, indent, 0, 0));
		
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
			if (settings.doAutoAnnotateMapped()) drawer.copyToLocallyMappedVariables();
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
			setSelection(rcfd.getIndexofSelection());
		}
		
	}
	
	@Override
	public void eraseButtonClicked(){
		setSelection(-1);
	}

	@Override
	public void createButtonClicked() {	}

	@Override
	public void modifyButtonClicked() {	}

}
