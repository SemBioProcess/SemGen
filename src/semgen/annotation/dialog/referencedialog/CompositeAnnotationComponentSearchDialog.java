package semgen.annotation.dialog.referencedialog;

import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.net.URI;

import javax.swing.JOptionPane;

import org.semanticweb.owlapi.model.OWLException;

import semgen.annotation.annotationpane.SemSimComponentAnnotationPanel;
import semsim.SemSimConstants;
import semsim.model.physical.PhysicalEntity;
import semsim.model.physical.PhysicalProcess;
import semsim.model.physical.PhysicalProperty;
import semsim.model.physical.ReferencePhysicalEntity;

public class CompositeAnnotationComponentSearchDialog extends AddReferenceClassDialog{

	private static final long serialVersionUID = -6053255066931420852L;
	public SemSimComponentAnnotationPanel pmcPanel;
	
	public CompositeAnnotationComponentSearchDialog(SemSimComponentAnnotationPanel pmcpanel, String[] ontList, Object[] options){
		super(ontList, options);
		pmcPanel = pmcpanel;
		utilarea.setText("Current annotation: " + pmcPanel.combobox.getSelectedItem());
		refclasspanel.ontologychooser.addActionListener(refclasspanel);
		packAndSetModality();
	}
	
	@Override
	public void packAndSetModality(){
		setModalityType(ModalityType.APPLICATION_MODAL);
		pack();
		setLocationRelativeTo(getParent());
		setVisible(true);
	}
	
	public void propertyChange(PropertyChangeEvent arg0) {
		String value = optionPane.getValue().toString();
		if(value == "Apply" && this.getFocusOwner() != refclasspanel.findbox){
			
			// If something from list actually selected
			if(refclasspanel.resultslistright.getSelectedValue()!=null){
				String desc = (String) refclasspanel.resultslistright.getSelectedValue();
				URI uri = URI.create(refclasspanel.resultsanduris.get(refclasspanel.resultslistright.getSelectedValue()));
				
				// If we're annotating a physical property...
				if(pmcPanel.smc instanceof PhysicalProperty){
					if(pmcPanel.checkOPBpropertyValidity(uri)){
						pmcPanel.smc.removeAllReferenceAnnotations();
						pmcPanel.smc.addReferenceOntologyAnnotation(SemSimConstants.REFERS_TO_RELATION, uri, desc);
					}
					else{
						pmcPanel.showInvalidOPBpropertyError();
						return;
					}
				}
				//Otherwise, if the reference term hasn't been added to the model yet...
				else if(pmcPanel.semsimmodel.getPhysicalModelComponentByReferenceURI(uri)==null){
					if(pmcPanel.smc instanceof PhysicalProcess){
						pmcPanel.smc = pmcPanel.semsimmodel.addReferencePhysicalProcess(uri, desc);
					}
					else if(pmcPanel.smc instanceof PhysicalEntity){
						pmcPanel.smc = pmcPanel.semsimmodel.addReferencePhysicalEntity(uri, desc);
						
						// If we are using an FMA term, store the numerical version of the ID
						String altID = null;
						((ReferencePhysicalEntity)pmcPanel.smc).getFirstRefersToReferenceOntologyAnnotation().setAltNumericalID(altID);
					}
				}
				// Otherwise reuse existing annotation
				else pmcPanel.smc = pmcPanel.semsimmodel.getPhysicalModelComponentByReferenceURI(uri);
				
				// Refresh the annotation based on the PhysicalModelComponents specified in the PhysicalModelComponentPanels
				try {
					pmcPanel.anndialog.updateCompositeAnnotationFromUIComponents();
				} catch (OWLException e) {
					e.printStackTrace();
				}
				
				pmcPanel.anndialog.compositepanel.setAddButtonsEnabled();
				
				// Refresh all the comboboxes in the composite annotation interface
				for(Component c : pmcPanel.anndialog.compositepanel.getComponents()){
					if(c instanceof SemSimComponentAnnotationPanel){
						((SemSimComponentAnnotationPanel)c).refreshComboBoxItemsAndButtonVisibility();
					}
				}
				// Refresh combobox items in singular annotation interface
				pmcPanel.anndialog.singularannpanel.refreshComboBoxItemsAndButtonVisibility();
				
				if(pmcPanel.smc.hasRefersToAnnotation()) 
					pmcPanel.urlbutton.setTermURI(pmcPanel.smc.getFirstRefersToReferenceOntologyAnnotation().getReferenceURI());
				optionPane.setValue(JOptionPane.UNINITIALIZED_VALUE);
				if(refclasspanel.ontologychooser.getComponentCount()>2){
				}
				
				dispose();
			}
		}
		else if (value == "Cancel") {
			refclasspanel.querythread.stop();
			optionPane.setValue(JOptionPane.UNINITIALIZED_VALUE);
			dispose();
		}
	}
}
