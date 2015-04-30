package semgen.annotation.componentlistpanes;

import java.awt.event.MouseEvent;
import java.util.Observable;

import javax.swing.Box;

import semgen.SemGenSettings;
import semgen.SemGenSettings.SettingChange;
import semgen.annotation.componentlistpanes.buttons.CodewordButton;
import semgen.annotation.workbench.AnnotatorWorkbench;
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

	@Override
	public void update(Observable arg0, Object arg1) {
		if (arg0==settings) {
			if (arg1==SettingChange.toggletree && settings.useTreeView()) {
				destroy();
			}
			
			if (arg1==SettingChange.toggleproptype) {
				toggleMarkers();
			}
			else if ((arg1==SettingChange.cwsort) || (arg1==SettingChange.showimports)) {
				update();
			}
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
