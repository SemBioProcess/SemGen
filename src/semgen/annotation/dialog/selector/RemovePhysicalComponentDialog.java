package semgen.annotation.dialog.selector;

import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Set;

import javax.swing.JCheckBox;
import javax.swing.JOptionPane;

import semgen.annotation.AnnotatorTab;
import semgen.annotation.componentlistpanes.codewords.CodewordButton;
import semgen.annotation.workbench.AnnotatorWorkbench;
import semsim.model.SemSimComponent;
import semsim.model.SemSimModel;
import semsim.model.physical.PhysicalEntity;
import semsim.model.physical.PhysicalProcess;
import semsim.model.physical.object.CompositePhysicalEntity;
import semsim.model.physical.object.PhysicalProperty;

public class RemovePhysicalComponentDialog extends SemSimComponentSelectorDialog implements
		PropertyChangeListener {

	private static final long serialVersionUID = -3622371132272217159L;
	public AnnotatorTab annotator;

	public RemovePhysicalComponentDialog(AnnotatorWorkbench wb, AnnotatorTab ann,
			Set<? extends SemSimComponent> settolist, SemSimComponent ssctoignore, Boolean withdescriptions, String title){
		super(wb, settolist, ssctoignore, null, null, withdescriptions, title);
		this.annotator = ann;
		setUpUI(this);
	}

	@Override
	public void propertyChange(PropertyChangeEvent e) {
		String propertyfired = e.getPropertyName();
		if (propertyfired.equals("value")) {
			String value = optionPane.getValue().toString();
			if (value == "OK") {
				optionPane.setValue(JOptionPane.UNINITIALIZED_VALUE);
				int choice = JOptionPane.showConfirmDialog(this, 
						"This will remove all selected terms from existing annotations.\nAre you sure you want to continue?",
						"Confirm",
						JOptionPane.YES_NO_OPTION);
				if(JOptionPane.YES_OPTION == choice){
					removeComponentFromModel();
					if(annotator.focusbutton instanceof CodewordButton) annotator.annotatorpane.compositepanel.refreshUI();
				}
			}
			dispose();
		}
	}

	// This replaces the physical component with the *unspecified* component place-holder
	public void removeComponentFromModel() {
		Component[] panelcomps = panel.getComponents();
		for (int i = 0; i < panelcomps.length; i++) {
			if (panel.getComponent(i).isVisible()
					&& panel.getComponent(i) instanceof JCheckBox) {
				JCheckBox tempbox = (JCheckBox) panel.getComponent(i);
				if (tempbox.isSelected()) {
					Object removedComp = nameobjectmap.get(tempbox.getText());
					
					for(PhysicalProperty prop: workbench.getSemSimModel().getPhysicalProperties()){
						// Remove the component's use from non-composite physical entities
						if(prop.getPhysicalPropertyOf()==removedComp){
							
							if(prop.getPhysicalPropertyOf() instanceof PhysicalEntity)
								prop.setPhysicalPropertyOf(workbench.getSemSimModel().getCustomPhysicalEntityByName(SemSimModel.unspecifiedName));
							if(prop.getPhysicalPropertyOf() instanceof PhysicalProcess){
								// Need to do sources, sinks, meds here?
								prop.setPhysicalPropertyOf(workbench.getSemSimModel().getCustomPhysicalProcessByName(SemSimModel.unspecifiedName));
							}
						}
						// Remove the component's use from composite physical entities
						else if(prop.getPhysicalPropertyOf() instanceof CompositePhysicalEntity){
							CompositePhysicalEntity cpe = (CompositePhysicalEntity)prop.getPhysicalPropertyOf();
							
							for(int k=0; k<cpe.getArrayListOfEntities().size(); k++){
								if(cpe.getArrayListOfEntities().get(k)==removedComp)
									cpe.getArrayListOfEntities().set(k, workbench.getSemSimModel().getCustomPhysicalEntityByName(SemSimModel.unspecifiedName));
							}
						}
					}
					annotator.setModelSaved(false);
				}
			}
		}
	}
}
