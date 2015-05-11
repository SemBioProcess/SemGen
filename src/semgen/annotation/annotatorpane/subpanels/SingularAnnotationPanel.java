package semgen.annotation.annotatorpane.subpanels;

import java.awt.event.ActionEvent;

import semgen.annotation.common.AnnotationChooserPanel;
import semgen.annotation.workbench.drawers.AnnotatorDrawer;

public class SingularAnnotationPanel extends AnnotationChooserPanel{
	private static final long serialVersionUID = 1L;

	public AnnotatorDrawer<?> drawer;
	
	public SingularAnnotationPanel(AnnotatorDrawer<?> wb) {
		super();
		drawer = wb;
		if (drawer.isEditable()) {
			makeSingularAnnotationSelector();
		}
		else {
			makeStaticPanel(drawer.getSingularAnnotationasString(), drawer.hasHumanReadableDef());
		}
		constructSelector();
	}
	
	public void makeSingularAnnotationSelector() {
		addURLButton();
		addSearchButton();
		addEraseButton();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		
		
	}

	public void refreshSingularAnnotation(){
		// Get the singular annotation for the codeword
		
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
