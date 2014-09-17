package semgen.annotation.dialog;

import java.awt.Color;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import semgen.annotation.annotationpane.AnnotationPanel;
import semgen.annotation.annotationpane.ProcessParticipantEditor;
import semsim.SemSimConstants;
import semsim.model.SemSimModel;
import semsim.model.annotation.ReferenceOntologyAnnotation;
import semsim.model.physical.PhysicalModelComponent;
import semsim.model.physical.PhysicalProcess;
import semsim.model.physical.ProcessParticipant;


public class CustomPhysicalComponentEditor extends JDialog implements PropertyChangeListener {

	private static final long serialVersionUID = 1L;

	public PhysicalModelComponent pmc;
	
	public JTextField mantextfield;
	public JTextArea descriptionarea = new JTextArea();
	public JOptionPane optionPane;

	public ObjectPropertyEditor versionofeditor;
	public ProcessParticipantEditor hassourceeditor;
	public ProcessParticipantEditor hassinkeditor;
	public ProcessParticipantEditor hasmediatoreditor;
	public JComponent[] objectpropertyeditors;

	public CustomPhysicalComponentEditor(PhysicalModelComponent pmc) {
		this.pmc = pmc;
		
		mantextfield = new JTextField(pmc.getName());
		mantextfield.setEditable(true);
		mantextfield.setForeground(Color.blue);
		mantextfield.setPreferredSize(new Dimension(450, 28));
		
		descriptionarea.setForeground(Color.blue);
		descriptionarea.setText(pmc.getDescription());
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

		String title = "Edit custom physical ";
		if(pmc instanceof PhysicalProcess){
			title = title + "process";
			versionofeditor = new ObjectPropertyEditor(model, SemSimConstants.BQB_IS_VERSION_OF_RELATION, this.pmc);
			hassourceeditor = new ProcessParticipantEditor(model, SemSimConstants.HAS_SOURCE_RELATION, (PhysicalProcess)pmc);
			hassinkeditor = new ProcessParticipantEditor(model, SemSimConstants.HAS_SINK_RELATION, (PhysicalProcess)pmc);
			hasmediatoreditor = new ProcessParticipantEditor(model, SemSimConstants.HAS_MEDIATOR_RELATION, (PhysicalProcess)pmc);
			
			objectpropertyeditors = new JComponent[]{hassourceeditor, hassinkeditor, hasmediatoreditor, versionofeditor}; //, partofeditor, hasparteditor};
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
		setModalityType(ModalityType.APPLICATION_MODAL);
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
	}

	public void propertyChange(PropertyChangeEvent arg0) {
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
					for(ProcessParticipant pp : hassourceeditor.tablemod.data){
						((PhysicalProcess)pmc).addSource(pp.getPhysicalEntity(), pp.getMultiplier());
					}
					((PhysicalProcess)pmc).getSinks().clear();
					for(ProcessParticipant pp : hassinkeditor.tablemod.data){
						((PhysicalProcess)pmc).addSink(pp.getPhysicalEntity(), pp.getMultiplier());
					}
					((PhysicalProcess)pmc).getMediators().clear();
					for(ProcessParticipant pp : hasmediatoreditor.tablemod.data){
						((PhysicalProcess)pmc).addMediator(pp.getPhysicalEntity(), pp.getMultiplier());
					}
				}
				dispose();
			}
			else{
				JOptionPane.showMessageDialog(this, "Please enter a name.");
			}
		}
		else if(value == "Cancel"){
			dispose();
		}
		optionPane.setValue(JOptionPane.UNINITIALIZED_VALUE);
	}
}
