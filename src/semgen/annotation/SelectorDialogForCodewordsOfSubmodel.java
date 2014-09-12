package semgen.annotation;

import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JCheckBox;

import semsim.model.computational.DataStructure;
import semsim.model.physical.Submodel;

public class SelectorDialogForCodewordsOfSubmodel extends SemSimComponentSelectorDialog implements PropertyChangeListener{

	/**
	 * 
	 */
	private static final long serialVersionUID = -1981003062200298737L;
	public Set<DataStructure> cdwdsfromcomps;
	public Submodel submodel;

	
	public SelectorDialogForCodewordsOfSubmodel(
			AnnotationDialog anndia,
			Set<DataStructure> dss,
			DataStructure dstoignore, 
			Submodel sub,
			Set<DataStructure> preselected,
			Set<DataStructure> dstodisable,
			Boolean withdescriptions,
			String title){
		super(dss, dstoignore, preselected, dstodisable, withdescriptions, title);
		this.anndia = anndia;
		submodel = sub;
		setUpUI(this);
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent e) {
		String value = optionPane.getValue().toString();
		if (value == "OK") {
			Component[] complist = panel.getComponents();
			submodel.setAssociatedDataStructures(new HashSet<DataStructure>());
			for (int r = 0; r < complist.length; r++) {
				if (complist[r] instanceof JCheckBox) {
					JCheckBox box = (JCheckBox) complist[r];
					if (box.isSelected() && box.isEnabled()) {
						submodel.addDataStructure((DataStructure)nameobjectmap.get(box.getName()));
					}
				}
			}
			anndia.annotator.setModelSaved(false);
			anndia.refreshSubmodelData();
			setVisible(false);
		}
		else if(value == "Cancel") setVisible(false);
	}
}
