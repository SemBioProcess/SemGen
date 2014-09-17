package semgen.annotation.dialog.selectordialog;

import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JCheckBox;

import semgen.annotation.annotationpane.AnnotationPanel;
import semsim.model.physical.Submodel;

public class SelectorDialogForSubmodelsOfSubmodel extends SemSimComponentSelectorDialog implements PropertyChangeListener {
	private static final long serialVersionUID = 1L;
	private Submodel submodel;

	public SelectorDialogForSubmodelsOfSubmodel(
			Set<Submodel> setofsubs, 
			Submodel sub2ignore,
			Submodel sub,
			Set<Submodel> preselected,
			Set<Submodel> substodisable,
			String title){
		super(setofsubs, sub2ignore, preselected, substodisable, title);
		this.submodel = sub;
		setUpUI(new Object[]{"OK", "Close"});
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent e) {
		String value = optionPane.getValue().toString();
		if (value == "OK") {
			Component[] complist = panel.getComponents();
			// Remove previous submodel nesting data
			submodel.setSubmodels(new HashSet<Submodel>());
			// Set the new data
			for (int r = 0; r < complist.length; r++) {
				if (complist[r] instanceof JCheckBox) {
					JCheckBox box = (JCheckBox) complist[r];
					if (box.isSelected()) {
						submodel.addSubmodel(anndia.semsimmodel.getSubmodel(box.getText()));
					}
				}
			}
		}
		dispose();
		
	}
}
