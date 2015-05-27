package semgen.annotation.annotatorpane.subpanels;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import semgen.annotation.common.AnnotationChooserPanel;
import semgen.annotation.workbench.SemSimTermLibrary;
import semgen.annotation.workbench.drawers.AnnotatorDrawer;

public class SingularAnnotationPanel extends AnnotationChooserPanel {
	private static final long serialVersionUID = 1L;
	private SemSimTermLibrary library;
	private AnnotatorDrawer<?> drawer;
	private ArrayList<Integer> combolist;
	
	public SingularAnnotationPanel(AnnotatorDrawer<?> wb, SemSimTermLibrary lib) {
		super();
		drawer = wb;
		library = lib;
		if (drawer.isEditable()) {
			makeSingularAnnotationSelector();
		}
		else {
			makeStaticPanel(drawer.getSingularAnnotationasString(), drawer.hasHumanReadableDef());
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
		combolist = library.getAllReferenceTerms();
		ArrayList<String> names = library.getComponentNames(combolist);
		
		Integer i = combolist.indexOf(drawer.getSingularAnnotationLibraryIndex());
		setComboList(names, i);
	}
	
	@Override
	public void webButtonClicked() {
		urlbutton.openTerminBrowser(drawer.getSingularAnnotationURI());
	}

	@Override
	public void searchButtonClicked() {
	}

	@Override
	public void createButtonClicked() {	}

	@Override
	public void modifyButtonClicked() {	}

}
