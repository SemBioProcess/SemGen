package semgen.annotation.componentlistpanes;

import java.awt.event.MouseEvent;

import javax.swing.Box;

import semgen.SemGenSettings;
import semgen.SemGenSettings.SettingChange;
import semgen.annotation.componentlistpanes.buttons.CodewordButton;
import semgen.annotation.workbench.AnnotatorWorkbench;
import semgen.annotation.workbench.AnnotatorWorkbench.modeledit;
import semgen.annotation.workbench.drawers.CodewordToolDrawer;

public class CodewordListPane extends AnnotatorListPane<CodewordButton, CodewordToolDrawer> {
	private static final long serialVersionUID = 1L;

	public CodewordListPane(AnnotatorWorkbench wb, SemGenSettings sets) {
		super(wb, sets, wb.openCodewordDrawer());
	}

	public void updateButtonTable(){
		Boolean[] dispoptions = new Boolean[]{
				settings.showImports(),
				settings.organizeByCompositeCompleteness(),
				settings.organizeByPropertyType()
		};
		
		// Associate codeword names with their buttons
		for(Integer index : drawer.getCodewordstoDisplay(dispoptions)){
			ListButton cbutton = new ListButton(drawer.getCodewordName(index), drawer.isEditable(index), settings.useDisplayMarkers());
			addButton(cbutton, index);
			
			cbutton.toggleHumanDefinition(drawer.hasHumanReadableDef(index));
			cbutton.toggleSingleAnnotation(drawer.hasSingularAnnotation(index));
			cbutton.refreshCompositeAnnotationCode(drawer.getAnnotationStatus(index));
			cbutton.refreshPropertyOfMarker(drawer.getPropertyType(index));
			
		}
		addPanelTitle("Codewords ", btnlist.size(), "No codewords or dependencies found");
		buttonpane.add(Box.createGlue());
	}
	
	private void toggleMarkers() {
		for (CodewordButton cwb : btnarray) {
			cwb.togglePropertyMarkers(settings.useDisplayMarkers());
		}
	}

	
	protected void refreshChangedButtons() {
		for (Integer i : drawer.getChangedComponents()) {
			CodewordButton cb = btnlist.inverseBidiMap().get(i);
			cb.toggleHumanDefinition(drawer.hasHumanReadableDef(i));
			cb.toggleSingleAnnotation(drawer.hasSingularAnnotation(i));
			cb.refreshCompositeAnnotationCode(drawer.getAnnotationStatus(i));
		}
	}

	
	@Override
	public void updateUnique(Object arg) {
			if (arg==SettingChange.toggleproptype) {
				toggleMarkers();
			}
			if	(arg==modeledit.codewordchanged) {
				refreshChangedButtons();
			}
	}
	
	private class ListButton extends CodewordButton {
		private static final long serialVersionUID = 1L;

		public ListButton(String name, boolean canedit, boolean showmarkers) {
			super(name, canedit, showmarkers);
		}

		@Override
		public void mouseClicked(MouseEvent arg0) {
			changeButtonFocus(this);
			if (arg0.getSource()==humdeflabel) {
				workbench.requestFreetextChange();
			}
		}
	}
}
