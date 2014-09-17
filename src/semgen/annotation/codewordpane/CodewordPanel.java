package semgen.annotation.codewordpane;

import java.util.Observable;

import javax.swing.Box;
import javax.swing.JLabel;

import semgen.SemGenSettings;
import semgen.annotation.uicomponents.AnnotationObjectButton;
import semgen.annotation.uicomponents.ComponentPane;
import semgen.annotation.workbench.CodewordAnnotations;

public class CodewordPanel extends ComponentPane {
	private static final long serialVersionUID = 1L;
	private CodewordAnnotations cwa;
	
	public CodewordPanel(CodewordAnnotations canvas, SemGenSettings sets) {
		super(sets);
		cwa = canvas;
	}

	public void makeButtons() {
		int nbuttons = settings.showImports() ? cwa.getNumberVisible() : cwa.getNumberofElements(); 
		for (int i=0; i<nbuttons; i++) {
			aoblist.add(new CodewordButton());
		}
		add(Box.createGlue());
		setTitle();
	}
		
	private void setTitle() {
		setName("Codewords " + "(" + cwa.countDataStructures() + " total, " + aoblist.size() + " editable)" );
		if (cwa.countDataStructures()==0) add(new JLabel("No codewords or dependencies found"));
	}
	
	@Override
	public void update(Observable o, Object arg) {
		refreshAnnotatableElements();
		repaint();
		validate();
	}
	
	public void showMarkers() {
		for(AnnotationObjectButton aob : aoblist){
			((CodewordButton)aob).togglePropertyofLabel(settings.useDisplayMarkers());
			aob.validate();
		}
	}
	
	@Override
	public void refreshAnnotatableElements() {
		int nb = aoblist.size();
		//If number of submodels has changed redraw buttons
		if (cwa.getNumberVisible() != nb) {
			removeAll();
			makeButtons();
		}
		if (settings.organizeByPropertyType()) {
			orderByType();
		}
		else orderByName();
		setTitle();
	}

	private void orderByType() {
		for (AnnotationObjectButton smb : aoblist) {
			for (Integer i : cwa.getTypeSortedList()) {
				if (!aobmap.containsKey(i) && cwa.isVisible(i)) {
					aobmap.put(i, smb);
					smb.assignButton(cwa.getName(i), new Boolean[]{cwa.hasFreeText(i), cwa.hasSingular(i), cwa.hasComposite(i)});	
				}
			}
		}
		add(Box.createGlue());
	}
	
	private void orderByName() {
		for (AnnotationObjectButton smb : aoblist) {
			for (int i=0; i<cwa.getNumberofElements(); i++) {
				if (!aobmap.containsKey(i) && cwa.isVisible(i)) {
					aobmap.put(i, smb);
					smb.assignButton(cwa.getName(i), new Boolean[]{cwa.hasFreeText(i), cwa.hasSingular(i), cwa.hasComposite(i)});	
				}
			}
		}
	}

	@Override
	public void freeTextRequest(int index) {
		cwa.setFreeText(index);
		
	}

	@Override
	public void singAnnRequest(int index) {
		cwa.setSingular(index);
	}
}