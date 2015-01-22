package semgen.annotation.dialog;

import java.awt.Color;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.apache.commons.lang3.tuple.Pair;

import semgen.annotation.annotatorpane.AnnotationPanel;
import semgen.annotation.annotatorpane.composites.ProcessParticipantEditor;
import semgen.annotation.dialog.referenceclass.ObjectPropertyEditor;
import semgen.utilities.uicomponent.SemGenDialog;
import semsim.SemSimConstants;
import semsim.annotation.ReferenceOntologyAnnotation;
import semsim.model.SemSimModel;
import semsim.model.physical.PhysicalEntity;
import semsim.model.physical.PhysicalModelComponent;
import semsim.model.physical.PhysicalProcess;


public class CustomPhysicalComponentEditor extends SemGenDialog implements PropertyChangeListener {
	private static final long serialVersionUID = 1L;
	
	public SemSimModel model;
	public PhysicalModelComponent pmc;
	public AnnotationPanel anndia;

	public JTextField mantextfield;
	public JTextArea descriptionarea;

	public JOptionPane optionPane;

	public ObjectPropertyEditor versionofeditor;
	public ProcessParticipantEditor hassourceeditor;
	public ProcessParticipantEditor hassinkeditor;
	public ProcessParticipantEditor hasmediatoreditor;


	public CustomPhysicalComponentEditor(AnnotationPanel anndia, PhysicalModelComponent pmc) {
		super("");
		this.anndia = anndia;
		this.model = anndia.semsimmodel;
		this.pmc = pmc;
		
		mantextfield = new JTextField(pmc.getName());
		mantextfield.setEditable(true);
		mantextfield.setForeground(Color.blue);
		mantextfield.setPreferredSize(new Dimension(450, 28));
		
		descriptionarea = new JTextArea(pmc.getDescription());
		descriptionarea.setForeground(Color.blue);
		descriptionarea.setLineWrap(true);
		descriptionarea.setWrapStyleWord(true);
		
		JScrollPane descscroller = new JScrollPane(descriptionarea);
		descscroller.setPreferredSize(new Dimension(450,100));
		
		JPanel namepanel = new JPanel();
		namepanel.add(new JLabel("Name: "));
		namepanel.add(mantextfield);
		
		JPanel descriptionpanel = new JPanel();
		descriptionpanel.add(new JLabel("Description: "));
		descriptionpanel.add(descscroller);
		
		JComponent[] objectpropertyeditors;
		String title = "Edit custom physical ";
		if(pmc instanceof PhysicalProcess){
			title = title + "process";
			versionofeditor = new ObjectPropertyEditor(model, SemSimConstants.BQB_IS_VERSION_OF_RELATION, this.pmc);
			hassourceeditor = new ProcessParticipantEditor(model, SemSimConstants.HAS_SOURCE_RELATION, (PhysicalProcess)pmc);
			hassinkeditor = new ProcessParticipantEditor(model, SemSimConstants.HAS_SINK_RELATION, (PhysicalProcess)pmc);
			hasmediatoreditor = new ProcessParticipantEditor(model, SemSimConstants.HAS_MEDIATOR_RELATION, (PhysicalProcess)pmc);
			
			objectpropertyeditors = new JComponent[]{hassourceeditor, hassinkeditor, hasmediatoreditor, versionofeditor};
		}
		else{
			title = title + "entity";
			versionofeditor = new ObjectPropertyEditor(model, SemSimConstants.BQB_IS_VERSION_OF_RELATION, this.pmc);

			objectpropertyeditors = new ObjectPropertyEditor[]{versionofeditor}; 
		}
		
		JPanel mainpanel = new JPanel();
		mainpanel.setLayout(new BoxLayout(mainpanel, BoxLayout.Y_AXIS));
		mainpanel.add(namepanel);
		mainpanel.add(descriptionpanel);
		for(int y=0; y<objectpropertyeditors.length; y++) mainpanel.add(objectpropertyeditors[y]);
		mainpanel.add(Box.createVerticalGlue());

		Object[] options = new Object[]{"OK","Cancel"};
		optionPane = new JOptionPane(mainpanel, JOptionPane.PLAIN_MESSAGE,
				JOptionPane.OK_CANCEL_OPTION, null);
		optionPane.addPropertyChangeListener(this);
		optionPane.setOptions(options);
		optionPane.setInitialValue(options[0]);
		
		setContentPane(optionPane);
		setTitle(title);
		showDialog();
	}

	public void propertyChange(PropertyChangeEvent arg0) {
		String propertyfired = arg0.getPropertyName();
		if (propertyfired.equals("value")) {
			String value = optionPane.getValue().toString();
	
			if(value == "OK"){
				if(mantextfield.getText()!=null && !mantextfield.getText().equals("")){
					pmc.setName(mantextfield.getText());
					if(descriptionarea.getText()!=null) pmc.setDescription(descriptionarea.getText());
					else pmc.setDescription(pmc.getName());
					
					pmc.removeAllReferenceAnnotations();
					ObjectPropertyEditor[] refanneds = new ObjectPropertyEditor[]{versionofeditor}; 
					
					// Iterate through the object property editors shared by physical entities and processes and add the annotations
					for(ObjectPropertyEditor ed : refanneds){
						for(String pmcname : ed.namesandobjects.keySet()){
							PhysicalModelComponent refpmc = (PhysicalModelComponent)ed.namesandobjects.get(pmcname);
							System.out.println(refpmc);
							System.out.println(refpmc.getName());
							System.out.println(refpmc.getFirstRefersToReferenceOntologyAnnotation());
							ReferenceOntologyAnnotation roa = refpmc.getFirstRefersToReferenceOntologyAnnotation();
							pmc.addReferenceOntologyAnnotation(ed.relation, roa.getReferenceURI(), roa.getValueDescription());
						}
					}
	
					// Deal with process-specific data (sources, sinks and mediators not added as annotations to SemSimModel:
					// they are proeprties of the PhysicalProcess class
					if(pmc instanceof PhysicalProcess){
						if(hassourceeditor.table.isEditing())
							  hassourceeditor.table.getCellEditor().stopCellEditing();
						if(hassinkeditor.table.isEditing())
							  hassinkeditor.table.getCellEditor().stopCellEditing();
						if(hasmediatoreditor.table.isEditing())
							  hasmediatoreditor.table.getCellEditor().stopCellEditing();
						
						((PhysicalProcess)pmc).getSources().clear();
						for(Pair<PhysicalEntity, Double> pe : hassourceeditor.getTableData()){
							((PhysicalProcess)pmc).addSource(pe.getLeft(), pe.getRight());
						}
						((PhysicalProcess)pmc).getSinks().clear();
						for(Pair<PhysicalEntity, Double> pe : hassinkeditor.getTableData()){
							((PhysicalProcess)pmc).addSink(pe.getLeft(), pe.getRight());
						}
						((PhysicalProcess)pmc).getMediators().clear();
						for(Pair<PhysicalEntity, Double> pe : hasmediatoreditor.getTableData()){
							((PhysicalProcess)pmc).addMediator(pe.getLeft(), pe.getRight());
						}
					}
					anndia.refreshCompositeAnnotation();
					anndia.annotator.setModelSaved(false);
				}
				else{
					JOptionPane.showMessageDialog(this, "Please enter a name.");
				}
			}
			dispose();
		}
	}
}
