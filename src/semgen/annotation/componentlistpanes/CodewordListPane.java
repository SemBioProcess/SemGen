package semgen.annotation.componentlistpanes;

import java.awt.event.ActionEvent;
import java.util.Observable;

import javax.swing.Box;

import semgen.SemGenSettings;
import semgen.annotation.componentlistpanes.buttons.CodewordButton;
import semgen.annotation.workbench.AnnotatorWorkbench;
import semgen.annotation.workbench.CodewordToolDrawer;

public class CodewordListPane extends AnnotatorListPane<CodewordButton, CodewordToolDrawer> {
	private static final long serialVersionUID = 1L;

	public CodewordListPane(AnnotatorWorkbench wb, SemGenSettings sets) {
		super(wb, sets, wb.openCodewordDrawer());
	}

	public void updateButtonTable(){
		Boolean[] dispoptions = new Boolean[]{
				settings.showImports(),
				settings.organizeByPropertyType()
		};
		
		// Associate codeword names with their buttons
		for(Integer index : drawer.getCodewordstoDisplay(dispoptions)){
			ListButton cbutton = new ListButton(drawer.getCodewordName(index), drawer.isEditable(index), settings.useDisplayMarkers());
			addButton(cbutton, index);
			cbutton.toggleHumanDefinition(drawer.hasHumanReadableDef(index));
			cbutton.toggleSingleAnnotation(drawer.hasSingularAnnotation(index));
			cbutton.refreshCompositeAnnotationCode(drawer.getAnnotationStatus(index));
			
		}
		addPanelTitle("Codewords ", btnlist.size(), "No codewords or dependencies found");
		buttonpane.add(Box.createGlue());
	}
	
	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		
	}

	@Override
	public void update(Observable arg0, Object arg1) {
		
	}
	
	private class ListButton extends CodewordButton {
		private static final long serialVersionUID = 1L;

		public ListButton(String name, boolean canedit, boolean showmarkers) {
			super(name, canedit, showmarkers);
			
			humdeflabel.addMouseListener(new FreeTextMouseListener());
		}
	}
}
