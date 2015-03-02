package semgen.annotation.dialog.referenceclass.compositedialog;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import semgen.SemGenSettings;
import semgen.annotation.AnnotatorTab;
import semgen.annotation.dialog.CustomPhysicalComponentEditor;
import semgen.annotation.dialog.referenceclass.CompositeAnnotationComponentSearchDialog;
import semgen.annotation.workbench.AnnotatorWorkbench;
import semgen.utilities.SemGenIcon;
import semgen.utilities.uicomponent.ExternalURLButton;
import semsim.Annotatable;
import semsim.SemSimConstants;
import semsim.annotation.ReferenceOntologyAnnotation;
import semsim.model.SemSimComponent;
import semsim.model.physical.PhysicalEntity;
import semsim.model.physical.PhysicalModelComponent;
import semsim.model.physical.object.CompositePhysicalEntity;
import semsim.model.physical.object.ReferencePhysicalEntity;

abstract public class SemSimEntityPanel extends JPanel implements ActionListener{

	private static final long serialVersionUID = -6606265105415658572L;
	private AnnotatorWorkbench workbench;
	private AnnotatorTab annotator;
	public Map<String,SemSimComponent> listdataandsmcmap = new HashMap<String, SemSimComponent>();
	public JComboBox<String> combobox = new JComboBox<String>(new String[]{});

	public ComponentPanelLabel searchlabel = 
			new ComponentPanelLabel(SemGenIcon.searchicon,"Look up reference ontology term");
	public ComponentPanelLabel createlabel = 
			new ComponentPanelLabel(SemGenIcon.createicon,"Create new custom term");
	public ComponentPanelLabel eraselabel = 
			new ComponentPanelLabel(SemGenIcon.eraseicon, "Remove annotation component");
	public ComponentPanelLabel modifylabel = new ComponentPanelLabel(SemGenIcon.modifyicon, "Edit custom term");
	
	public ExternalURLButton urlbutton = new ExternalURLButton();

	protected SemGenSettings settings;
	
	public SemSimEntityPanel(AnnotatorWorkbench wb, AnnotatorTab tab, DefaultComboBoxModel<String> cbmodel){
		workbench = wb;
		annotator = tab;
		//this.setBackground(new Color(207, 215, 252));
				
		combobox.setPreferredSize(new Dimension(350,30));
		combobox.setMaximumSize(new Dimension(350,30));

		setLayout(new BorderLayout());
		JPanel itempanel = new JPanel();
		
		combobox.setModel(cbmodel);
		combobox.setSelectedIndex(0);
		//itempanel.setBackground(SemGenSettings.lightblue);
		itempanel.add(combobox);
		itempanel.add(urlbutton);
		itempanel.add(searchlabel);
		itempanel.add(createlabel);
		itempanel.add(modifylabel);
		itempanel.add(eraselabel);
		
		modifylabel.setEnabled(false);
		urlbutton.setEnabled(false);
		
		add(itempanel, BorderLayout.WEST);
		add(Box.createGlue(), BorderLayout.EAST);
		
		itempanel.validate();
		validate();
	}
		
	public void setComboBoxList(DefaultComboBoxModel<String> stringlist) {
		String sel = combobox.getSelectedItem().toString();
		combobox.removeAllItems();
		combobox.setModel(stringlist);
		combobox.repaint();
		combobox.setSelectedItem(sel);
	}
	
	private void searchLabelClicked() {
		String[] ontList = new String[]{
				SemSimConstants.CELL_TYPE_ONTOLOGY_FULLNAME,
				SemSimConstants.CHEMICAL_ENTITIES_OF_BIOLOGICAL_INTEREST_FULLNAME,
				SemSimConstants.FOUNDATIONAL_MODEL_OF_ANATOMY_FULLNAME,
				SemSimConstants.GENE_ONTOLOGY_FULLNAME,
				SemSimConstants.MOUSE_ADULT_GROSS_ANATOMY_ONTOLOGY_FULLNAME,
				SemSimConstants.UNIPROT_FULLNAME};

			new CompositeAnnotationComponentSearchDialog(workbench, annotator, ontList, new String[]{"Apply","Cancel"}, (String) combobox.getSelectedItem()) {
				private static final long serialVersionUID = 1L;

				public void propertyChange(PropertyChangeEvent arg0) {
					String propertyfired = arg0.getPropertyName();
					if (propertyfired.equals("value")) {
						String value = optionPane.getValue().toString();
						if(value == "Apply" && this.getFocusOwner() != refclasspanel.findbox){
							
							// If something from list actually selected
							if(refclasspanel.resultslistright.getSelectedValue()!=null){
								PhysicalEntity smc = null;
								String desc = refclasspanel.getSelection();
								URI uri = URI.create(refclasspanel.getSelectionURI());
								
								//Otherwise, if the reference term hasn't been added to the model yet...
								if(workbench.getSemSimModel().getPhysicalModelComponentByReferenceURI(uri)==null){
									smc = workbench.getSemSimModel().addReferencePhysicalEntity(uri, desc);
										// If we are using an FMA term, store the numerical version of the ID
										String altID = null;
										((ReferencePhysicalEntity)smc).getFirstRefersToReferenceOntologyAnnotation().setAltNumericalID(altID);
								}
								// Otherwise reuse existing annotation
								else smc = workbench.getSemSimModel().getPhysicalEntityByReferenceURI(uri);

								if(smc.hasRefersToAnnotation()) 
									urlbutton.setTermURI(smc.getFirstRefersToReferenceOntologyAnnotation().getReferenceURI());
								ReferenceOntologyAnnotation ra = ((ReferencePhysicalEntity)smc).getFirstRefersToReferenceOntologyAnnotation();
								
								refreshAndSet(ra.getValueDescription() + " (" + ra.getOntologyAbbreviation() + ")");
							}
						}
						else if (value == "Cancel") {
							refclasspanel.querythread.stop();
						}
						if (value!=JOptionPane.UNINITIALIZED_VALUE) dispose();
					}
				}	
			};
	}

	private void createLabelClicked() {
		String input = JOptionPane.showInputDialog("Enter name for new custom physical entity.");
		if(input!=null && !input.equals("")){
			PhysicalEntity smc = workbench.getSemSimModel().addCustomPhysicalEntity(input, "");

			CustomPhysicalComponentEditor cpce = new CustomPhysicalComponentEditor(workbench, (PhysicalModelComponent)smc);
			
			if (cpce.getCustomTerm()!=null ) {
				refreshAndSet(smc.getName());
				modifylabel.setEnabled(true);
			}
		}
	}
	
	public String getSelection() {
		return (String) combobox.getSelectedItem();
	}
	
	abstract protected void refreshAndSet(String selid);
	
	abstract protected void modifyLabelClicked();
	
	protected abstract void eraseLabelClicked();
	
	public void actionPerformed(ActionEvent arg0) {
		Object o = arg0.getSource();
		if(o == combobox){
			
			// If for property, look up the pmc for the item, but don't change the pmc, just apply its annotation to the current pmc
			Annotatable selectedsmc = (Annotatable) listdataandsmcmap.get(combobox.getSelectedItem());

			// If not for a physical property, get the reference annotation for the selected item and apply it to whatever is being annotated
				if(selectedsmc!=null){
										
					// This happens for singular annotations and non-composite physical entity annotations
					if(selectedsmc.hasRefersToAnnotation()){
						urlbutton.setTermURI(selectedsmc.getFirstRefersToReferenceOntologyAnnotation().getReferenceURI());
						modifylabel.setEnabled(false);
						urlbutton.setEnabled(true);
					}
					else {
						modifylabel.setEnabled(true);
						urlbutton.setEnabled(false);
					}
				}
		}
	}

	public void applyReferenceOntologyAnnotation(ReferenceOntologyAnnotation ann, boolean refreshCodes){
		urlbutton.setTermURI(ann.getReferenceURI());
	}
	
	class ComponentPanelLabel extends JLabel {
		private static final long serialVersionUID = 1L;

		ComponentPanelLabel(Icon icon, String tooltip) {
			super(icon);
			setBorder(BorderFactory.createEmptyBorder(1,1,1,1));
			setBackground(Color.white);
			addMouseListener(new LabelMouseBehavior());
			setToolTipText(tooltip);
		}
		
		class LabelMouseBehavior extends MouseAdapter {
			public void mouseClicked(MouseEvent arg0) {
				Component clickedcomponent = arg0.getComponent();
				
				// If we're opening the search dialog for ontology terms
				if (clickedcomponent == searchlabel) {
					searchLabelClicked();
				}
				else if(clickedcomponent == createlabel){
					createLabelClicked();
				}
				else if(clickedcomponent == modifylabel && modifylabel.isEnabled()){
					modifyLabelClicked();
				}
				// If we're removing a component of the annotation
				else if(clickedcomponent == eraselabel){
					eraseLabelClicked();
				}
			}
			
			public void mouseEntered(MouseEvent e) {
				setCursor(new Cursor(Cursor.HAND_CURSOR));
			}
	
			public void mouseExited(MouseEvent e) {
				setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			}
			public void mousePressed(MouseEvent arg0) {
				setBorder(BorderFactory.createLineBorder(Color.blue,1));
			}
	
			public void mouseReleased(MouseEvent arg0) {
				setBorder(BorderFactory.createEmptyBorder(1,1,1,1));
			}
		}
	}
	

}
