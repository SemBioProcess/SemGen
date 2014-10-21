package semgen.annotation.dialog;

import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Set;

import javax.swing.JCheckBox;
import javax.swing.JOptionPane;

import semgen.SemGenGUI;
import semgen.annotation.AnnotatorTab;
import semgen.annotation.CodewordButton;
import semgen.annotation.dialog.selector.SemSimComponentSelectorDialog;
import semsim.model.SemSimComponent;
import semsim.model.physical.CompositePhysicalEntity;
import semsim.model.physical.PhysicalEntity;
import semsim.model.physical.PhysicalProcess;
import semsim.model.physical.PhysicalProperty;

public class RemovePhysicalComponentDialog extends SemSimComponentSelectorDialog implements
		PropertyChangeListener {

	private static final long serialVersionUID = -3622371132272217159L;
	public AnnotatorTab annotator;

	public RemovePhysicalComponentDialog(AnnotatorTab ann,
			Set<? extends SemSimComponent> settolist, SemSimComponent ssctoignore, Boolean withdescriptions, String title){
		super(settolist, ssctoignore, null, null, withdescriptions, title);
		this.annotator = ann;
		setUpUI(this);
	}

	@Override
	public void propertyChange(PropertyChangeEvent arg0) {
		String value = optionPane.getValue().toString();
		if (value == "OK") {
			optionPane.setValue(JOptionPane.UNINITIALIZED_VALUE);
			int choice = JOptionPane.showConfirmDialog(this, 
					"This will remove all selected terms from existing annotations.\nAre you sure you want to continue?",
					"Confirm",
					JOptionPane.YES_NO_OPTION);
			if(JOptionPane.YES_OPTION == choice){
				removeComponentFromModel();
				setVisible(false);
				if(annotator.focusbutton instanceof CodewordButton) annotator.anndialog.compositepanel.refreshUI();
			}
		}
		else if(value == "Cancel"){
			setVisible(false);
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
					
					for(PhysicalProperty prop: annotator.semsimmodel.getPhysicalProperties()){
						// Remove the component's use from non-composite physical entities
						if(prop.getPhysicalPropertyOf()==removedComp){
							
							if(prop.getPhysicalPropertyOf() instanceof PhysicalEntity)
								prop.setPhysicalPropertyOf(annotator.semsimmodel.getCustomPhysicalEntityByName(SemGenGUI.unspecifiedName));
							if(prop.getPhysicalPropertyOf() instanceof PhysicalProcess){
								// Need to do sources, sinks, meds here?
								prop.setPhysicalPropertyOf(annotator.semsimmodel.getCustomPhysicalProcessByName(SemGenGUI.unspecifiedName));
							}
							
						}
						// Remove the component's use from composite physical entities
						else if(prop.getPhysicalPropertyOf() instanceof CompositePhysicalEntity){
							CompositePhysicalEntity cpe = (CompositePhysicalEntity)prop.getPhysicalPropertyOf();
							
							for(int k=0; k<cpe.getArrayListOfEntities().size(); k++){
								if(cpe.getArrayListOfEntities().get(k)==removedComp)
									cpe.getArrayListOfEntities().set(k, annotator.semsimmodel.getCustomPhysicalEntityByName(SemGenGUI.unspecifiedName));
							}
						}
					}
					annotator.setModelSaved(false);
				}
			}
		}
	}
}
