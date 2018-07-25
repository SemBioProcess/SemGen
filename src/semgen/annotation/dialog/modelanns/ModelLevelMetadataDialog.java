package semgen.annotation.dialog.modelanns;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URI;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextArea;

import org.sbml.jsbml.CVTerm.Qualifier;

import semgen.SemGen;
import semgen.annotation.common.ComponentPanelLabel;
import semgen.annotation.workbench.AnnotatorWorkbench;
import semgen.annotation.workbench.drawers.ModelAnnotationsBench.ModelChangeEnum;
import semgen.utilities.SemGenIcon;
import semgen.utilities.uicomponent.SemGenDialog;
import semgen.utilities.uicomponent.SemGenScrollPane;
import semsim.annotation.ReferenceOntologyAnnotation;
import semsim.annotation.Relation;
import semsim.definitions.RDFNamespace;
import semsim.definitions.SemSimRelations;
import semsim.definitions.SemSimRelations.SemSimRelation;
import semsim.model.collection.SemSimModel;

public class ModelLevelMetadataDialog extends SemGenDialog implements PropertyChangeListener, ActionListener{

	private static final long serialVersionUID = -578937181064501858L;
	private AnnotatorWorkbench workbench;
	private JOptionPane optionPane;
	private SemSimModel semsimmodel;
	private JTextArea descriptionarea;
	private SemGenScrollPane scroller;
	private JPanel outerannpanel;
	private JPanel annlistpanel;
	private JButton addbutton;
	
	public ModelLevelMetadataDialog(AnnotatorWorkbench wb) {
		super("Model-level annotations");
		this.workbench = wb;
		this.semsimmodel = wb.getSemSimModel();
		this.setModalExclusionType(Dialog.ModalExclusionType.NO_EXCLUDE);
		
		setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
		setPreferredSize(new Dimension(1000, 850));
		setMaximumSize(getPreferredSize());
		setMinimumSize(getPreferredSize());
		setResizable(true);
		
		outerannpanel = new JPanel();
		outerannpanel.setBorder(BorderFactory.createTitledBorder("Knowledge resource annotations"));
		outerannpanel.setLayout(new BoxLayout(outerannpanel, BoxLayout.Y_AXIS));
		
		JPanel addbuttonpanel = new JPanel();
		addbuttonpanel.setLayout(new BoxLayout(addbuttonpanel,BoxLayout.X_AXIS));
		addbuttonpanel.setAlignmentY(LEFT_ALIGNMENT);
		
		addbutton = new JButton("Add annotation");
		addbutton.addActionListener(this);
		addbuttonpanel.add(addbutton);
		
		addbuttonpanel.add(Box.createHorizontalGlue());
		addbuttonpanel.setPreferredSize(new Dimension(900,40));
		addbuttonpanel.setMaximumSize(addbuttonpanel.getPreferredSize());
		addbuttonpanel.setMinimumSize(addbuttonpanel.getPreferredSize());
		
		JPanel descriptionpanel = new JPanel();
		descriptionpanel.setBorder(BorderFactory.createTitledBorder("Description"));
		
		descriptionpanel.setLayout(new BoxLayout(descriptionpanel,BoxLayout.X_AXIS));
		descriptionarea = new JTextArea();
		descriptionarea.setWrapStyleWord(true);
		descriptionarea.setLineWrap(true);

		SemGenScrollPane descriptionscroller = new SemGenScrollPane(descriptionarea);
		descriptionscroller.setPreferredSize(new Dimension(900,200));
		descriptionscroller.setMinimumSize(descriptionscroller.getPreferredSize());
		descriptionscroller.setMaximumSize(descriptionscroller.getPreferredSize());
		
		descriptionpanel.add(descriptionscroller);
		descriptionpanel.add(Box.createGlue());
				
		descriptionarea.setText(semsimmodel.getDescription());
		
		annlistpanel = new JPanel();
		annlistpanel.setLayout(new BoxLayout(annlistpanel, BoxLayout.Y_AXIS));
		
		ArrayList<JPanel> metadatapanels = new ArrayList<JPanel>();
		
		// Add the individual annotations
		for(ReferenceOntologyAnnotation ann : semsimmodel.getReferenceOntologyAnnotations()){
			
			if(ann.getRelation()==null) continue;

			AnnPanel newpanel = new AnnPanel(ann);
			metadatapanels.add(newpanel);
			annlistpanel.add(newpanel);
		}
		
		annlistpanel.add(Box.createVerticalGlue());
		
		scroller = new SemGenScrollPane(annlistpanel);
		scroller.setPreferredSize(new Dimension(900,400));
		scroller.setMaximumSize(scroller.getPreferredSize());
		scroller.setMinimumSize(scroller.getPreferredSize());
		
		outerannpanel.add(addbuttonpanel);
		outerannpanel.add(scroller);
		
		descriptionscroller.scrollToTop();
		scroller.scrollToTop();

		Object[] array = { descriptionpanel, new JSeparator(), outerannpanel, Box.createVerticalGlue()};
		
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		optionPane = new JOptionPane(array, JOptionPane.PLAIN_MESSAGE,
				JOptionPane.OK_CANCEL_OPTION, null);
		optionPane.addPropertyChangeListener(this);
		Object[] options = new Object[] {"Apply","Cancel"};
		optionPane.setOptions(options);
		optionPane.setInitialValue(options[0]);

		setContentPane(optionPane);
		showDialog();
	}
	

	@Override
	public void propertyChange(PropertyChangeEvent arg0) {
		String propertyfired = arg0.getPropertyName();
		if (propertyfired.equals("value")) {
			String value = optionPane.getValue().toString();
			
			if (value.equals("Apply")) {
				// save changes here
				semsimmodel.setDescription(descriptionarea.getText());
				semsimmodel.removeAllReferenceAnnotations();
				// store annotations
				for(Component c : annlistpanel.getComponents()){
					if(c instanceof AnnPanel){
						AnnPanel annpanel = (AnnPanel)c;
						AnnComboBox acb = annpanel.jcb;
						Integer index = acb.items.indexOf(acb.getSelectedItem());
						Relation rel = acb.relations.get(index);
						String objval = annpanel.jta.getText();
						
						if(rel != SemSimRelation.UNKNOWN && ! objval.trim().isEmpty())
							semsimmodel.addReferenceOntologyAnnotation(rel, URI.create(objval), "", SemGen.semsimlib);
					}
				}
				workbench.update(null, ModelChangeEnum.METADATACHANGED);
			}
			setVisible(false);
			optionPane.setValue(JOptionPane.UNINITIALIZED_VALUE);
		}
	}
	
	/**
	 * Call when the dialog is ready for display
	 */
	protected void showDialog() {
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
	}
	
	
	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		Object o = arg0.getSource();
		if (o == addbutton) {
			this.annlistpanel.add(new AnnPanel(new ReferenceOntologyAnnotation(SemSimRelation.UNKNOWN,URI.create(""),"",SemGen.semsimlib)));
			scroller.scrollToBottom();
			scroller.validate();
			scroller.repaint();
		}
	}
	
	
	// Nested class for dialog-specific panels that correspond to the annotations
	// on the model
	private class AnnPanel extends JPanel{
		private static final long serialVersionUID = 8599058171666528014L;
		private AnnComboBox jcb;
		private JTextArea jta;
		private ComponentPanelLabel removelabel;
		
		@SuppressWarnings("serial")
		private AnnPanel(ReferenceOntologyAnnotation ann){
			jcb = new AnnComboBox(ann.getRelation());
			jta = new JTextArea();
			jta.setPreferredSize(new Dimension(500,30));
			jta.setText(ann.getReferenceURI().toString());
			
			removelabel = new ComponentPanelLabel(SemGenIcon.eraseicon,"Remove annotation") {
				public void onClick() {
					eraseButtonClicked();
				}
			};
			
			add(jcb);
			add(jta);
			add(removelabel);
		}
		
		private void eraseButtonClicked(){
			ModelLevelMetadataDialog.this.annlistpanel.remove(this);
			ModelLevelMetadataDialog.this.annlistpanel.validate();
			ModelLevelMetadataDialog.this.annlistpanel.repaint();
		}
	}
	
	
	// Nested class for dialog-specific JComboBox
	private class AnnComboBox extends JComboBox<String>{
		private static final long serialVersionUID = 7055838562079528159L;
		private ArrayList<Relation> relations = new ArrayList<Relation>();
		private ArrayList<String> items = new ArrayList<String>();
		private Relation selectedrelation = null;
		
		private AnnComboBox(Relation rel){
			this.selectedrelation = rel;
			initialize();
		}
		
		private void initialize(){
			
			relations.add(SemSimRelation.UNKNOWN);
			items.add("UNKNOWN");
			addItem("UNKNOWN");
			
			for(Qualifier qual : Qualifier.values()){
				
				Relation relitem = null;
				String uri = null;
				String label = null;
				
				if(qual.isBiologicalQualifier())
					relitem = SemSimRelations.getRelationFromBiologicalQualifier(qual);
				else if(qual.isModelQualifier())
					relitem = SemSimRelations.getRelationFromModelQualifier(qual);
					
				if(relitem == null) continue;
				
				uri = relitem.getURIasString();
				label = qual.isBiologicalQualifier() ? uri.replace(RDFNamespace.BQB.getNamespaceAsString(),"bqbiol:") : 
					uri.replace(RDFNamespace.BQM.getNamespaceAsString(),"bqmodel:");

				relations.add(relitem);
				items.add(label);
				addItem(label);
			}
			
			setSelectedItem(items.get(relations.indexOf(selectedrelation)));
		}
	}
}
