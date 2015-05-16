package semgen.annotation.annotatorpane.subpanels;

import java.awt.event.ActionEvent;
import java.util.ArrayList;

import semgen.annotation.common.AnnotationChooserPanel;
import semgen.annotation.workbench.SemSimTermLibrary;
import semgen.annotation.workbench.drawers.AnnotatorDrawer;

public class SingularAnnotationPanel extends AnnotationChooserPanel{
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
	public void eraseButtonClicked() {
		setSelection(-1);
		
	}

	@Override
	public void searchButtonClicked() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void createButtonClicked() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void modifyButtonClicked() {
		// TODO Auto-generated method stub
		
	}
}
