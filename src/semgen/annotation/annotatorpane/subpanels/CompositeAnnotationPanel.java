package semgen.annotation.annotatorpane.subpanels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import semgen.SemGen;
import semgen.SemGenSettings;
import semgen.annotation.common.AnnotationChooserPanel;
import semgen.annotation.common.EntitySelectorGroup;
import semgen.annotation.dialog.termlibrary.AddReferenceClassDialog;
import semgen.annotation.dialog.termlibrary.CustomTermDialog;
import semsim.annotation.SemSimTermLibrary;
import semsim.annotation.SemSimTermLibrary.LibraryEvent;
import semgen.annotation.workbench.drawers.CodewordToolDrawer;
import semgen.utilities.SemGenFont;
import semsim.definitions.PropertyType;
import semsim.definitions.ReferenceOntologies.OntologyDomain;

public class CompositeAnnotationPanel extends Box implements ActionListener {
	private static final long serialVersionUID = 1L;
	private CodewordToolDrawer drawer;
	private SemSimTermLibrary termlib;
	private SemGenSettings settings;
	private int indent = 15;
	private JButton addentbutton = new JButton("Add entity");
	private JButton addprocbutton = new JButton("Add process");
	private JButton addenergydiffbutton = new JButton("Add energy differential");
	private JEditorPane ptextpane;

	private PropertySelectorPanel propsel;
	private EntitySelectorGroup esg;
	private Box pmcpanel;
	private ProcessSelectorPanel psp;
	private EnergyDiffSelectorPanel fsp;
	
	public CompositeAnnotationPanel(SemSimTermLibrary lib, CodewordToolDrawer bench, SemGenSettings sets, int orientation){
		super(orientation);
		drawer = bench;
		termlib = lib;
		settings = sets;
		setBackground(SemGenSettings.lightblue);
		setAlignmentX(Box.LEFT_ALIGNMENT);
		setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

		createPropertyPanel();
		
		addentbutton.addActionListener(this);
		addprocbutton.addActionListener(this);
		addenergydiffbutton.addActionListener(this);
		
		validate();
	}

	private void createPropertyPanel() {
		propsel = new PropertySelectorPanel(!drawer.isEditable());
		if (drawer.isEditable()) {
			propsel.setComboList(termlib.getSortedAssociatePhysicalPropertyIndicies(), drawer.getIndexofPhysicalProperty());
		}
		propsel.constructSelector();
		propsel.setBorder(BorderFactory.createEmptyBorder(0, indent, 0, 0));
		
		JLabel propertyoflabel = new JLabel("property_of");
        propertyoflabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        propertyoflabel.setFont(SemGenFont.defaultItalic());
        propertyoflabel.setBorder(BorderFactory.createEmptyBorder(4, indent*2, 4, 0));
        
        JPanel propofpanel = new JPanel(new BorderLayout());
		propofpanel.setBackground(SemGenSettings.lightblue);
		propofpanel.add(propsel, BorderLayout.NORTH);
        propofpanel.add(propertyoflabel, BorderLayout.SOUTH);
        propofpanel.setAlignmentX(Box.LEFT_ALIGNMENT);
        add(propofpanel);
        
        onPropertyChange();
	}
	
	private void showAddEntityProcessEnergyDiffButtons() {
		if (pmcpanel!=null) remove(pmcpanel);
		if (esg!=null) esg = null;
		Box btnbox = new Box(BoxLayout.X_AXIS);
		btnbox.setAlignmentX(Box.LEFT_ALIGNMENT);
		
		addentbutton.setEnabled(drawer.isEditable());
		addprocbutton.setEnabled(drawer.isEditable());
		addenergydiffbutton.setEnabled(drawer.isEditable());
		
		btnbox.add(addentbutton);
		btnbox.add(addprocbutton);
		btnbox.add(addenergydiffbutton);
		btnbox.add(Box.createHorizontalGlue());
		pmcpanel = btnbox;
		btnbox.setBorder(BorderFactory.createEmptyBorder(0, indent*2, 0, 0));
		add(pmcpanel);
	}
	
	private void setProcessSelector() {
		if (pmcpanel!=null) remove(pmcpanel);
		if (esg!=null) esg = null;
		Box procbox = new Box(BoxLayout.Y_AXIS);
		procbox.setAlignmentX(Box.LEFT_ALIGNMENT);
		
		psp = new ProcessSelectorPanel( ! drawer.isEditable());

		psp.setComboList(termlib.getSortedPhysicalProcessIndicies(), drawer.getIndexOfAssociatedPhysicalModelComponent());
		
		ptextpane = new JEditorPane("text/html",listProcessParticipants());
		ptextpane.setEditable(false);
		ptextpane.setOpaque(false);
		ptextpane.setBackground(new Color(0,0,0,0));
		ptextpane.setFont(SemGenFont.defaultPlain(-2));
		ptextpane.setBorder(BorderFactory.createEmptyBorder(3, 30, 3, 0));
		
		procbox.add(psp);
		procbox.add(ptextpane, BorderLayout.SOUTH);
		procbox.setBorder(BorderFactory.createEmptyBorder(0, indent*2, 0, 0));
		pmcpanel = procbox;
		add(pmcpanel);
	}
	
	
	private void setEnergyDiffSelector() {
		if (pmcpanel!=null) remove(pmcpanel);
		if (esg!=null) esg = null;
		Box procbox = new Box(BoxLayout.Y_AXIS);
		procbox.setAlignmentX(Box.LEFT_ALIGNMENT);
		
		fsp = new EnergyDiffSelectorPanel( ! drawer.isEditable());
		fsp.getComboBox().setModel(new DefaultComboBoxModel<String>(new String[]{"anonymous energy differential"}));
		fsp.setLibraryIndicies(new ArrayList<Integer>());
		fsp.toggleNoneSelected(false);
		
		ptextpane = new JEditorPane("text/html",listEnergyDifferentialParticipants());
		ptextpane.setEditable(false);
		ptextpane.setOpaque(false);
		ptextpane.setBackground(new Color(0,0,0,0));
		ptextpane.setFont(SemGenFont.defaultPlain(-2));
		ptextpane.setBorder(BorderFactory.createEmptyBorder(3, 30, 3, 0));
		
		procbox.add(fsp);
		procbox.add(ptextpane, BorderLayout.SOUTH);
		procbox.setBorder(BorderFactory.createEmptyBorder(0, indent*2, 0, 0));
		pmcpanel = procbox;
		add(pmcpanel);
	}
	
	
	private void showProcessParticipants() {
		ptextpane.setText(listProcessParticipants());
	}
	
	private void showEnergyDifferentialParticipants(){
		ptextpane.setText(listEnergyDifferentialParticipants());
	}
	
	private String listProcessParticipants() {
		return termlib.listProcessParticipants(drawer.getIndexOfAssociatedPhysicalModelComponent());
	}
	
	private String listEnergyDifferentialParticipants(){
		return termlib.listEnergyDifferentialParticipants(drawer.getIndexOfAssociatedPhysicalModelComponent());
	}
	
	private void setCompositeSelector() {
		if (pmcpanel!=null) remove(pmcpanel);
		esg = new CodewordCompositeSelectors(termlib);
		pmcpanel = esg;
		esg.setAlignmentX(Box.LEFT_ALIGNMENT);
		esg.setBorder(BorderFactory.createEmptyBorder(0, indent*2, 0, 0));
		add(pmcpanel);
	}
	
	public void onPropertyChange() {
		propsel.toggleNoneSelected(drawer.getIndexofPhysicalProperty()==-1);

		if (!drawer.hasPhysicalModelComponent() && !(drawer.hasAssociatedPhysicalProperty())) {
			showAddEntityProcessEnergyDiffButtons();
		}
		else {
			if (drawer.isProcess() ) {
				setProcessSelector();
			}
			else if(drawer.isEnergyDifferential()) {
				setEnergyDiffSelector();
			}
			else {
				setCompositeSelector();
			}
		}
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		Object obj = e.getSource();
		if (obj==addentbutton) {
			setCompositeSelector();
		}
		if (obj==addprocbutton) {
			setProcessSelector();
		}
		if (obj==addenergydiffbutton) {
			setEnergyDiffSelector();
		}
	}
	
	public void onTermUpdate(Object evt) {
		if (evt==LibraryEvent.SINGULAR_TERM_CHANGE || evt.equals(LibraryEvent.SINGULAR_TERM_CREATED) || evt.equals(LibraryEvent.TERM_CHANGE)) {
			propsel.setComboList(termlib.getSortedAssociatePhysicalPropertyIndicies(), drawer.getIndexofPhysicalProperty());
			if (esg!=null) {
				esg.refreshLists();
			}
		}
		else if (evt.equals(LibraryEvent.COMPOSITE_ENTITY_CHANGE) && esg!=null) {
			esg.drawBox(true);
		}
		else if (psp!=null && evt.equals(LibraryEvent.PROCESS_CHANGE)|| evt.equals(LibraryEvent.TERM_CHANGE)) {
			psp.setComboList(termlib.getSortedPhysicalProcessIndicies(), drawer.getIndexOfAssociatedPhysicalModelComponent());
			listProcessParticipants();
		}
		else if(fsp != null && evt.equals(LibraryEvent.ENERGY_DIFFERENTIAL_CHANGE)){
			listEnergyDifferentialParticipants();
		}
	}
	
	private boolean showIncompatiblePropertyMessage(PropertyType proptype) {
		String msg;
		if (proptype==PropertyType.PROPERTY_OF_PHYSICAL_PROCESS) {
			msg = new String("A property of a process cannot be applied to a physical entity or energy differential.");
		}
		else if(proptype==PropertyType.PROPERTY_OF_PHYSICAL_ENTITY){
			msg = new String("A property of a physical entity cannot be applied to a physical process or energy differential.");
		}
		else if(proptype==PropertyType.PROPERTY_OF_PHYSICAL_ENERGY_DIFFERENTIAL){
			msg = new String("A property of a physical energy differential cannot be applied to a physical entity or process.");
		}
		else msg = "Incompatible property selected.";
		
		msg = msg + "\nRemove property bearer and apply property?";
		
		int confirm = JOptionPane.showConfirmDialog(SemGen.getSemGenGUI(), msg);
		return confirm==JOptionPane.YES_OPTION;
	}
	
	@SuppressWarnings("serial")
	private class PropertySelectorPanel extends AnnotationChooserPanel {
		protected PropertySelectorPanel(boolean isstatic) {
			super(termlib);
			if (isstatic) {
				makeStaticPanel(drawer.getIndexofPhysicalProperty());
			}
			else makePhysicalPropertySelector();
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getSource()==combobox) {
				
				if ( ! drawer.checkPropertyPMCCompatibility(getSelection())) {
					
					URI propuri = library.getReferenceComponentURI(getSelection());
					PropertyType proptype = PropertyType.PROPERTY_OF_PHYSICAL_ENTITY;
					if(SemGen.semsimlib.isOPBprocessProperty(propuri)) proptype = PropertyType.PROPERTY_OF_PHYSICAL_PROCESS;
					else if(SemGen.semsimlib.isOPBenergyDiffProperty(propuri)) proptype = PropertyType.PROPERTY_OF_PHYSICAL_ENERGY_DIFFERENTIAL;
					
					if ( ! showIncompatiblePropertyMessage(proptype)) {
						setSelection(drawer.getIndexofPhysicalProperty());
						return;
					}
					drawer.setDataStructureAssociatedPhysicalComponent(-1);
				}
				drawer.setDataStructurePhysicalProperty(getSelection());
				toggleNoneSelected(getSelection() == -1);
				onPropertyChange();
				
				if(settings.doAutoAnnotateMapped()) drawer.copyToLocallyMappedVariables();
				
			}
		}

		@Override
		public void searchButtonClicked() {
			AddReferenceClassDialog rcd = new AddReferenceClassDialog(termlib, OntologyDomain.AssociatePhysicalProperty);
			if (rcd.getIndexofSelection()!=-1) {
				setSelection(rcd.getIndexofSelection());
				onPropertyChange();
				if(settings.doAutoAnnotateMapped()) drawer.copyToLocallyMappedVariables();
			}
		}

		@Override
		public void eraseButtonClicked() {
			setSelection(-1);
		}
		
		@Override
		public void createButtonClicked() {}

		@Override
		public void modifyButtonClicked() {}
	}
	
	// Process selector panel
	@SuppressWarnings("serial")
	private class ProcessSelectorPanel extends AnnotationChooserPanel {
		protected ProcessSelectorPanel(boolean isstatic) {
			super(termlib);
			if (isstatic) {
				makeStaticPanel(drawer.getIndexOfAssociatedPhysicalModelComponent());
			}
			else makeProcessSelector();
			constructSelector();
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getSource()==combobox) {
				drawer.setDataStructureAssociatedPhysicalComponent(getSelection());
				toggleNoneSelected(getSelection() == -1);
				toggleCustom( ! termlib.isReferenceTerm(getSelection()));
				showProcessParticipants();
				if (settings.doAutoAnnotateMapped()) drawer.copyToLocallyMappedVariables();
			}
		}

		@Override
		public void searchButtonClicked() {}

		@Override
		public void createButtonClicked() {
			CustomTermDialog ctd = new CustomTermDialog();
			ctd.setAsProcessTermDialog(termlib);
			createTerm(ctd.getSelection());
		}

		@Override
		public void modifyButtonClicked() {
			CustomTermDialog ctd = new CustomTermDialog();
			ctd.setAsProcessTermDialog(termlib, getSelection());
			showProcessParticipants();
		}
		
		@Override
		public void eraseButtonClicked(){
			setSelection(-1);
		}
			
		private void createTerm(int procindex) {
			if (procindex != -1) {
				setSelection(procindex);
			}
		}
	}
	
	
	// Energy differential selector panel
	@SuppressWarnings("serial")
	private class EnergyDiffSelectorPanel extends AnnotationChooserPanel {
		protected EnergyDiffSelectorPanel(boolean isstatic) {
			super(termlib);
			if (isstatic) {
				makeStaticPanel(drawer.getIndexOfAssociatedPhysicalModelComponent());
			}
			else makeEnergyDifferentialSelector();
			constructSelector();
		}

		@Override
		public void actionPerformed(ActionEvent e) {
		}

		@Override
		public void searchButtonClicked() {}

		@Override
		public void createButtonClicked() {}

		@Override
		public void modifyButtonClicked() {
			CustomTermDialog ctd = new CustomTermDialog();
			Integer termindex = drawer.getIndexOfAssociatedPhysicalModelComponent();
			
			if(termindex==-1){
				termindex = library.createEnergyDifferential();
				drawer.setDataStructureAssociatedPhysicalComponent(termindex);
			}
			
			ctd.setAsEnergyDiffTermDialog(termlib, termindex);
			showEnergyDifferentialParticipants();
			// We copy here because mapped variables may not all be pointing to same energy differential object
			// which is a bit different than what happens when modifying participants for processes.
			if (settings.doAutoAnnotateMapped()) drawer.copyToLocallyMappedVariables();
		}
		
		@Override
		public void eraseButtonClicked(){
			drawer.setDataStructureAssociatedPhysicalComponent(-1);
			showEnergyDifferentialParticipants();
			if (settings.doAutoAnnotateMapped()) drawer.copyToLocallyMappedVariables();
		}
	}
	
	
	
	
	
	private class CodewordCompositeSelectors extends EntitySelectorGroup {
		private static final long serialVersionUID = 1L;

		public CodewordCompositeSelectors(SemSimTermLibrary lib) {
			super(lib, drawer.getCompositeEntityIndicies(), drawer.isEditable());
			
		}

		@Override
		public void onChange() {
			Integer compin = termlib.createCompositePhysicalEntity(pollSelectors());
			drawer.setDataStructureAssociatedPhysicalComponent(compin);
			if (settings.doAutoAnnotateMapped()) drawer.copyToLocallyMappedVariables();
		}		
	}
}
