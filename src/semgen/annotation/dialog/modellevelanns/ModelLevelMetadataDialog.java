package semgen.annotation.dialog.modellevelanns;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.sbml.jsbml.CVTerm.Qualifier;

import semgen.SemGen;
import semgen.annotation.common.ComponentPanelLabel;
import semgen.annotation.workbench.AnnotatorWorkbench;
import semgen.annotation.workbench.drawers.ModelAnnotationsBench.ModelChangeEnum;
import semgen.utilities.SemGenError;
import semgen.utilities.SemGenIcon;
import semgen.utilities.uicomponent.SemGenDialog;
import semgen.utilities.uicomponent.SemGenScrollPane;
import semsim.annotation.Annotation;
import semsim.annotation.Person;
import semsim.annotation.ReferenceOntologyAnnotation;
import semsim.annotation.Relation;
import semsim.definitions.RDFNamespace;
import semsim.definitions.SemSimRelations;
import semsim.definitions.SemSimRelations.SemSimRelation;
import semsim.definitions.SemSimRelations.StructuralRelation;
import semsim.model.collection.SemSimModel;

public class ModelLevelMetadataDialog extends SemGenDialog implements PropertyChangeListener, ActionListener{

	private static final long serialVersionUID = -578937181064501858L;
	private AnnotatorWorkbench workbench;
	private JOptionPane optionPane;
	private SemSimModel semsimmodel;
	private SemGenScrollPane personscroller;
	private JPanel personpanel;
	private JPanel personlistpanelinscroller;
	private JTextArea descriptionarea;
	private JTextArea cellmldocumentationarea;
	private SemGenScrollPane refannscroller;
	private JPanel outerrefannpanel;
	private JPanel annlistpanel;
	private JButton addrefannbutton;
	private JButton addpersonbutton;
	private int panelwidth = 1200;
	private int panelheight = 700;
	
	private enum Role {
		CREATOR,
		CONTRIBUTOR;
	}
	
	public ModelLevelMetadataDialog(AnnotatorWorkbench wb) {
		super("Model-level annotations");
		this.workbench = wb;
		this.semsimmodel = wb.getSemSimModel();
		this.setModalExclusionType(Dialog.ModalExclusionType.NO_EXCLUDE);
		
		outerrefannpanel = new JPanel();
		outerrefannpanel.setBorder(BorderFactory.createTitledBorder("Knowledge resource annotations"));
		outerrefannpanel.setLayout(new BoxLayout(outerrefannpanel, BoxLayout.Y_AXIS));
		outerrefannpanel.setAlignmentX(LEFT_ALIGNMENT);
		outerrefannpanel.setAlignmentY(TOP_ALIGNMENT);
		
		JPanel addbuttonpanel = new JPanel();
		addbuttonpanel.setLayout(new BoxLayout(addbuttonpanel,BoxLayout.X_AXIS));
		addbuttonpanel.setAlignmentY(LEFT_ALIGNMENT);
		
		addrefannbutton = new JButton("Add annotation");
		addrefannbutton.addActionListener(this);
		addbuttonpanel.add(addrefannbutton);
		addbuttonpanel.add(Box.createHorizontalGlue());
		
		JPanel addpersonpanel = new JPanel();
		addpersonpanel.setLayout(new BoxLayout(addpersonpanel,BoxLayout.X_AXIS));
		addpersonpanel.setAlignmentY(LEFT_ALIGNMENT);
		
		addpersonbutton = new JButton("Add creator/contributor");
		addpersonbutton.addActionListener(this);
		addpersonpanel.add(addpersonbutton);
		addpersonpanel.add(Box.createHorizontalGlue());

		personpanel = new JPanel();
		personpanel.setBorder(BorderFactory.createTitledBorder("Creators/Contributors"));
		personpanel.setLayout(new BoxLayout(personpanel, BoxLayout.Y_AXIS));

		// Add individual person entries
		personlistpanelinscroller = new JPanel();
		personlistpanelinscroller.setLayout(new BoxLayout(personlistpanelinscroller, BoxLayout.Y_AXIS));
		personlistpanelinscroller.setAlignmentY(TOP_ALIGNMENT);
		
		personscroller = new SemGenScrollPane(personlistpanelinscroller);
		personscroller.setPreferredSize(new Dimension(530,222));
		personpanel.setMinimumSize(personscroller.getPreferredSize());

		personpanel.add(addpersonpanel);
		personpanel.add(personscroller);
		
		if(semsimmodel.getCreators().size()==0 && semsimmodel.getContributors().size()==0) 
			personlistpanelinscroller.add(new PersonPanel(new Person(), Role.CREATOR));
		else {
			// Get the model creator and contributor info and put it in the interface
			for(Person creator : semsimmodel.getCreators())
				personlistpanelinscroller.add(new PersonPanel(creator, Role.CREATOR));
			
			for(Person contributor : semsimmodel.getContributors())
				personlistpanelinscroller.add(new PersonPanel(contributor, Role.CONTRIBUTOR));
		}
		
		personlistpanelinscroller.add(Box.createVerticalGlue());
		
		JPanel descriptionpanel = new JPanel();
		descriptionpanel.setBorder(BorderFactory.createTitledBorder("Description"));
		
		descriptionpanel.setLayout(new BoxLayout(descriptionpanel,BoxLayout.X_AXIS));
		descriptionarea = new JTextArea();
		descriptionarea.setWrapStyleWord(true);
		descriptionarea.setLineWrap(true);
		descriptionarea.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));

		SemGenScrollPane descriptionscroller = new SemGenScrollPane(descriptionarea);
		descriptionscroller.setPreferredSize(new Dimension(530,222));
		descriptionpanel.add(descriptionscroller);
				
		descriptionarea.setText(semsimmodel.getDescription());
		
		JPanel cellmldocumentationpanel = new JPanel();
		cellmldocumentationpanel.setBorder(BorderFactory.createTitledBorder("CellML documentation"));
		
		cellmldocumentationpanel.setLayout(new BoxLayout(cellmldocumentationpanel,BoxLayout.X_AXIS));
		cellmldocumentationpanel.setAlignmentX(LEFT_ALIGNMENT);
		cellmldocumentationpanel.setAlignmentY(TOP_ALIGNMENT);
		cellmldocumentationarea = new JTextArea();
		cellmldocumentationarea.setWrapStyleWord(true);
		cellmldocumentationarea.setLineWrap(true);
		cellmldocumentationarea.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));

		SemGenScrollPane cellmldocumentationscroller = new SemGenScrollPane(cellmldocumentationarea);
		cellmldocumentationscroller.setPreferredSize(new Dimension(530,222));
		cellmldocumentationscroller.setMinimumSize(new Dimension(530,222));
		cellmldocumentationpanel.add(cellmldocumentationscroller);
				
		String cellmldoctext = semsimmodel.getFirstAnnotationObjectForRelationAsString(SemSimRelation.CELLML_DOCUMENTATION);
		cellmldocumentationarea.setText(cellmldoctext);
		
		annlistpanel = new JPanel();
		annlistpanel.setLayout(new BoxLayout(annlistpanel, BoxLayout.Y_AXIS));
		annlistpanel.setAlignmentY(TOP_ALIGNMENT);
		
		// Add the individual reference ontology annotations
		for(ReferenceOntologyAnnotation ann : semsimmodel.getReferenceOntologyAnnotations()){
			
			if(ann.getRelation()==null) continue;

			ReferenceAnnotationTriplePanel newpanel = new ReferenceAnnotationTriplePanel(ann);
			annlistpanel.add(newpanel);
		}
		
		
		annlistpanel.add(Box.createVerticalGlue());
		
		refannscroller = new SemGenScrollPane(annlistpanel);
		refannscroller.setPreferredSize(new Dimension(530,222));
		refannscroller.setMinimumSize(refannscroller.getPreferredSize());
		
		outerrefannpanel.add(addbuttonpanel);
		outerrefannpanel.add(refannscroller);
				
		descriptionscroller.scrollToTop();
		cellmldocumentationscroller.scrollToTop();
		refannscroller.scrollToTop();

		// Adjust things if there's no cellml documentation to show
		if(cellmldoctext.isEmpty()) {
			cellmldocumentationpanel.setVisible(false);
			refannscroller.setPreferredSize(new Dimension(530,500));
			refannscroller.setMinimumSize(refannscroller.getPreferredSize());
		}
		
		// Left side of dialog contains creators/contributors, description
		// Right side contains reference ontology annotation triples, cellml documentation if present
		JPanel leftpanel = new JPanel();
		leftpanel.setLayout(new BoxLayout(leftpanel, BoxLayout.Y_AXIS));
		leftpanel.setAlignmentX(LEFT_ALIGNMENT);
		leftpanel.setAlignmentY(TOP_ALIGNMENT);
		leftpanel.add(personpanel);
		leftpanel.add(descriptionpanel);
		
		JPanel rightpanel = new JPanel();
		rightpanel.setLayout(new BoxLayout(rightpanel, BoxLayout.Y_AXIS));
		rightpanel.setAlignmentX(LEFT_ALIGNMENT);
		rightpanel.setAlignmentY(TOP_ALIGNMENT);
		rightpanel.add(outerrefannpanel);
		rightpanel.add(cellmldocumentationpanel);
		
		setPreferredSize(new Dimension(panelwidth, panelheight));
		setMaximumSize(getPreferredSize());
		setMinimumSize(getPreferredSize());
		setResizable(true);
		
		JPanel rootpanel = new JPanel();
		rootpanel.setLayout(new BoxLayout(rootpanel, BoxLayout.X_AXIS));
		rootpanel.add(leftpanel);
		rootpanel.add(rightpanel);
		rootpanel.setPreferredSize(new Dimension(1075,530));
		rootpanel.setMinimumSize(new Dimension(1075,530));
		
		optionPane = new JOptionPane(rootpanel, JOptionPane.PLAIN_MESSAGE,
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
				
				// Changes are saved here
				// Save creator/contributor info
				ArrayList<Person> newcreators = new ArrayList<Person>();
				ArrayList<Person> newcontributors = new ArrayList<Person>();

				for(Component c : personlistpanelinscroller.getComponents()) {
					
					if(c instanceof PersonPanel) {
						PersonPanel ce = (PersonPanel)c;

						if(ce.hasSomeData()) {
							
							String accountnametext = ce.accountnamejta.getText().trim();
							if ( ! validateAccountURI(accountnametext) && ! accountnametext.isEmpty()) {
								SemGenError.showError(this, 
										"The account name " + accountnametext + " is not a valid URI.\nPlease enter a valid URI.", "Invalid account name");
								optionPane.setValue(JOptionPane.UNINITIALIZED_VALUE);
								setVisible(true);
								return;
							}
							
							String accounthptext = ce.accounthomepagejta.getText();
							if ( ! validateAccountURI(accounthptext) && ! accounthptext.isEmpty()) {
								SemGenError.showError(this, 
										"The account homepage " + accounthptext + " is not a valid URI.\nPlease enter a valid URI.", "Invalid account homepage");
								optionPane.setValue(JOptionPane.UNINITIALIZED_VALUE);
								setVisible(true);
								return;
							}
							
							// If we're here, the account name and homepage fields are OK
							Person persontoadd = new Person(ce.namejta.getText(), 
									ce.emailjta.getText(), 
									URI.create(ce.accountnamejta.getText().trim()),
									URI.create(ce.accounthomepagejta.getText().trim()));
							
							if(ce.roleselector.getSelectedIndex()==0)
								newcreators.add(persontoadd);
							else newcontributors.add(persontoadd);
						}
					}
				}
				semsimmodel.setCreators(newcreators);
				semsimmodel.setContributors(newcontributors);
				
				// Store description
				semsimmodel.setDescription(descriptionarea.getText());
				
				// store cellml documentation edits
				for(Annotation ann : semsimmodel.getAnnotations()){
					if(ann.getRelation()==SemSimRelation.CELLML_DOCUMENTATION){
						ann.setValue(cellmldocumentationarea.getText());
					}
				}
				
				// store annotations
				semsimmodel.removeAllReferenceAnnotations();
				for(Component c : annlistpanel.getComponents()){
					if(c instanceof ReferenceAnnotationTriplePanel){
						ReferenceAnnotationTriplePanel annpanel = (ReferenceAnnotationTriplePanel)c;
						AnnotationComboBox acb = annpanel.jcb;
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
	 * Test whether an account name or account homepage is a valid URI
	 * @param text
	 * @return
	 */
	private boolean validateAccountURI(String text) {
		try {
            new URL(text).toURI();
            return true;
        }
        catch (Exception e) { return false; }
	}
	
	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		Object o = arg0.getSource();
		
		// If "Add annotation" button clicked
		if (o == addrefannbutton) {
			int numcomponents = annlistpanel.getComponentCount();
			this.annlistpanel.add(new ReferenceAnnotationTriplePanel(
					new ReferenceOntologyAnnotation(SemSimRelation.UNKNOWN,URI.create(""),"",SemGen.semsimlib)), 
					numcomponents-1); // So that the panel is placed above the vertical glue at the bottom of annlistpanel
			refannscroller.scrollToBottom();
			refannscroller.validate();
			refannscroller.repaint();
		}
		
		// If "Add creator/contributor" button clicked
		else if(o == addpersonbutton) {
			int numcomponents = personlistpanelinscroller.getComponentCount();
			this.personlistpanelinscroller.add(new PersonPanel(new Person(), Role.CREATOR), 
					numcomponents-1); // So that panel is placed above vertical glue in creatorlistpanel
			personscroller.scrollToBottom();
			personscroller.validate();
			personscroller.repaint();
		}
	}
	
	
	// Nested class for dialog-specific panels that correspond to the annotations on the model
	private class ReferenceAnnotationTriplePanel extends JPanel{
		private static final long serialVersionUID = 8599058171666528014L;
		private AnnotationComboBox jcb;
		private JTextArea jta;
		private ComponentPanelLabel removelabel;
		
		@SuppressWarnings("serial")
		private ReferenceAnnotationTriplePanel(ReferenceOntologyAnnotation ann){
			jcb = new AnnotationComboBox(ann.getRelation());
			jta = new JTextArea();
			jta.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
			jta.setText(ann.getReferenceURI().toString());
			
			removelabel = new ComponentPanelLabel(SemGenIcon.eraseicon,"Remove annotation") {
				public void onClick() {
					eraseButtonClicked();
				}
			};
			
			setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
			setAlignmentX(LEFT_ALIGNMENT);
			setMaximumSize(new Dimension(530, 40));
			setBorder(BorderFactory.createEmptyBorder(3, 3, 0, 3));
			add(jcb);
			JScrollPane scroller = new JScrollPane(jta);
			scroller.setPreferredSize(new Dimension(340,40));
			scroller.setMinimumSize(new Dimension(300, 40));
			add(scroller, BorderLayout.PAGE_START);
			add(removelabel);
		}
		
		private void eraseButtonClicked(){
			annlistpanel.remove(this);
			annlistpanel.validate();
			annlistpanel.repaint();
		}
	}
	
	
	// Nested class for dialog-specific panels that correspond to the creators/contributors of the model
		private class PersonPanel extends JPanel{
			private static final long serialVersionUID = 8599058171666528014L;
			
			private JComboBox<String> roleselector = new JComboBox<String>(new String[] {"Creator","Contributor"});
			private JTextArea namejta;
			private JTextArea emailjta;
			private JTextArea accountnamejta;
			private JTextArea accounthomepagejta;
			
			private ComponentPanelLabel nameremovelabel;
			private ComponentPanelLabel emailremovelabel;
			private ComponentPanelLabel accountnameremovelabel;
			private ComponentPanelLabel accounthomepageremovelabel;

			@SuppressWarnings("serial")
			private PersonPanel(Person person, Role therole){
				
				setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
				setAlignmentY(TOP_ALIGNMENT);
				setMaximumSize(new Dimension(530, 200));
				
				nameremovelabel = new ComponentPanelLabel(SemGenIcon.eraseicon,"Remove annotation") {
					public void onClick() {
						eraseButtonClicked(namejta);
					}
				};
				
				emailremovelabel = new ComponentPanelLabel(SemGenIcon.eraseicon,"Remove annotation") {
					public void onClick() {
						eraseButtonClicked(emailjta);
					}
				};
				
				accountnameremovelabel = new ComponentPanelLabel(SemGenIcon.eraseicon,"Remove annotation") {
					public void onClick() {
						eraseButtonClicked(accountnamejta);
					}
				};
				
				accounthomepageremovelabel = new ComponentPanelLabel(SemGenIcon.eraseicon,"Remove annotation") {
					public void onClick() {
						eraseButtonClicked(accounthomepagejta);
					}
				};
				
				namejta = makeTextArea(person.getName());
				emailjta = makeTextArea(person.getEmail());
				accountnamejta = makeTextArea("");
				accounthomepagejta = makeTextArea("");

				if(person.hasAccountName()) 
					accountnamejta.setText(person.getAccountName().toString());
				else
					accountnamejta.setText("");
				
				if(person.hasAccountServicesHomepage()) 
					accounthomepagejta.setText(person.getAccountServiceHomepage().toString());
				else
					accounthomepagejta.setText("");
				
				// Make role panel that sits on top of the person's info and allows user to select role
				int roleindex = therole==Role.CREATOR ? 0 : 1;
				roleselector.setPreferredSize(new Dimension(100,25));
				roleselector.setMaximumSize(new Dimension(100,25));
				roleselector.setSelectedIndex(roleindex);
				roleselector.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
				
				JPanel rolepanel = new JPanel();
				rolepanel.setLayout(new BoxLayout(rolepanel, BoxLayout.X_AXIS));
				rolepanel.setBorder(BorderFactory.createEmptyBorder(1, 4, 0, 2));
				JLabel rolelabel = new JLabel("Role");
				rolelabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));

				rolepanel.add(rolelabel);
				rolepanel.add(Box.createHorizontalStrut(9));
				rolepanel.add(roleselector);
				rolepanel.add(Box.createHorizontalGlue());
				rolepanel.setAlignmentX(LEFT_ALIGNMENT);

				// Add the panels that store info about the person
				JPanel namepanel = makeFieldPanel("Name", namejta, nameremovelabel);
				JPanel emailpanel = makeFieldPanel("Email", emailjta, emailremovelabel);
				JPanel accountnamepanel = makeFieldPanel("Account name (e.g. ORCID)", accountnamejta, accountnameremovelabel);
				JPanel accounthomepagepanel = makeFieldPanel("Account homepage", accounthomepagejta, accounthomepageremovelabel);

				add(Box.createVerticalStrut(4));
				add(rolepanel);
				add(namepanel);
				add(emailpanel);
				add(accountnamepanel);
				add(accounthomepagepanel);
				add(Box.createVerticalStrut(5));
				
				setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, Color.DARK_GRAY));
			}
			
			private void eraseButtonClicked(JTextArea area){
				area.setText("");
				area.validate();
				area.repaint();
			}
			
			private JTextArea makeTextArea(String text) {
				JTextArea ta = new JTextArea();
				ta.setMargin(new Insets(0,0,0,0));
				ta.setText(text);
				ta.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
				
				return ta;
			}
			
			private JPanel makeFieldPanel(String labeltext, JTextArea ta, ComponentPanelLabel removelabel) {
				JPanel apanel = new JPanel();
				apanel.setLayout(new BoxLayout(apanel, BoxLayout.X_AXIS));
				apanel.setAlignmentX(LEFT_ALIGNMENT);
				apanel.setPreferredSize(new Dimension(520,40));
				apanel.setBorder(BorderFactory.createEmptyBorder(1, 4, 0, 2));
				JLabel label = new JLabel(labeltext);
				label.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
				apanel.add(label);
				apanel.add(Box.createHorizontalStrut(5));
				JScrollPane scroller = new JScrollPane(ta);
				scroller.setPreferredSize(new Dimension(350,40));
				scroller.setMinimumSize(new Dimension(300, 40));
				apanel.add(scroller, BorderLayout.PAGE_START);
				apanel.add(removelabel);
				return apanel;
			}
			
			// Returns whether there's any info entered for the creator/contributor
			private boolean hasSomeData() {
				return ! (namejta.getText() + emailjta.getText() + accountnamejta.getText() + accounthomepagejta.getText()).equals("");
			}
		}
		
		
	
	
	// Nested class for dialog-specific JComboBox
	private class AnnotationComboBox extends JComboBox<String>{
		private static final long serialVersionUID = 7055838562079528159L;
		private ArrayList<Relation> relations = new ArrayList<Relation>();
		private ArrayList<String> items = new ArrayList<String>();
		private Relation selectedrelation = null;
		
		private AnnotationComboBox(Relation rel){
			this.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
			this.setMaximumSize(new Dimension(100,30));
			this.selectedrelation = rel;
			
			// Use RO structural relations internally
			if(selectedrelation == StructuralRelation.BQB_HAS_PART)
				selectedrelation = StructuralRelation.HAS_PART;
			
			else if(selectedrelation == StructuralRelation.BQB_IS_PART_OF)
				selectedrelation = StructuralRelation.PART_OF;
			
			initialize();
		}
		
		private void initialize(){
			
			relations.add(SemSimRelation.UNKNOWN);
			items.add("UNKNOWN");
			addItem("UNKNOWN");

			relations.add(SemSimRelation.MODEL_CREATED);
			items.add("dc:created");
			addItem("dc:created");

			// Add available predicates
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
				
				if(relitem==StructuralRelation.HAS_PART || relitem==StructuralRelation.PART_OF)
					label = uri.replace(RDFNamespace.RO.getNamespaceAsString(), "ro:");
				
				relations.add(relitem);
				items.add(label);
				addItem(label);
			}
			
			setSelectedItem(items.get(relations.indexOf(selectedrelation)));
		}
	}
}
