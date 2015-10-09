package semgen.annotation.annotatorpane.subpanels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
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
import semgen.annotation.workbench.SemSimTermLibrary;
import semgen.annotation.workbench.SemSimTermLibrary.LibraryEvent;
import semgen.annotation.workbench.drawers.CodewordToolDrawer;
import semgen.utilities.SemGenFont;
import semsim.annotation.ReferenceOntologies.OntologyDomain;

public class CompositeAnnotationPanel extends Box implements ActionListener {
	private static final long serialVersionUID = 1L;
	private CodewordToolDrawer drawer;
	private SemSimTermLibrary termlib;
	private SemGenSettings settings;
	private int indent = 15;
	private JButton addentbutton = new JButton("Add entity");
	private JButton addprocbutton = new JButton("Add process");
	private JEditorPane ptextpane;

	private PropertySelectorPanel propsel;
	private EntitySelectorGroup esg;
	private Box pmcpanel;
	private ProcessSelectorPanel pcp;
	
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
	
	private void showAddEntityProcessButtons() {
		if (pmcpanel!=null) remove(pmcpanel);
		if (esg!=null) esg = null;
		Box btnbox = new Box(BoxLayout.X_AXIS);
		btnbox.setAlignmentX(Box.LEFT_ALIGNMENT);
		
		addentbutton.setEnabled(drawer.isEditable());
		addprocbutton.setEnabled(drawer.isEditable());
		
		btnbox.add(addentbutton);
		btnbox.add(addprocbutton);
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
		pcp = new ProcessSelectorPanel(!drawer.isEditable());

		pcp.setComboList(termlib.getSortedPhysicalProcessIndicies(), drawer.getIndexofModelComponent());
		
		ptextpane = new JEditorPane("text/html",listParticipants());
		ptextpane.setEditable(false);
		ptextpane.setOpaque(false);
		ptextpane.setBackground(new Color(0,0,0,0));
		ptextpane.setFont(SemGenFont.defaultPlain(-2));
		ptextpane.setBorder(BorderFactory.createEmptyBorder(3, 30, 3, 0));
		
		procbox.add(pcp);
		procbox.add(ptextpane, BorderLayout.SOUTH);
		procbox.setBorder(BorderFactory.createEmptyBorder(0, indent*2, 0, 0));
		pmcpanel = procbox;
		add(pmcpanel);
	}
	
	private void showProcessParticipants() {
		ptextpane.setText(listParticipants());
	}
	
	private String listParticipants() {
		return termlib.listParticipants(drawer.getIndexofModelComponent());
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
			showAddEntityProcessButtons();
		}
		else {
			if (drawer.isProcess() ) {
				setProcessSelector();
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
		else if (pcp!=null && evt.equals(LibraryEvent.PROCESS_CHANGE)|| evt.equals(LibraryEvent.TERM_CHANGE)) {
			pcp.setComboList(termlib.getSortedPhysicalProcessIndicies(), drawer.getIndexofModelComponent());
			listParticipants();
		}
	}
	
	private boolean incompatibleProperty(boolean procprop) {
		String msg;
		if (procprop) {
			msg = new String("A property of a process cannot be applied to a composite entity. Remove composite physical entity and apply property?");
		}
		else {
			msg = new String("A property of a physical entity cannot be applied to a process. Remove physical process and apply property?");
		}
		int confirm = JOptionPane.showConfirmDialog(this, msg);
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
				if (!drawer.checkPropertyPMCCompatibility(getSelection())) {
					if (!incompatibleProperty(SemGen.semsimlib.isOPBprocessProperty(library.getReferenceComponentURI(getSelection())))) {
						setSelection(drawer.getIndexofPhysicalProperty());
						return;
					}
					drawer.setDataStructureComposite(-1);
				}
				drawer.setDatastructurePhysicalProperty(getSelection());
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
		public void createButtonClicked() {}

		@Override
		public void modifyButtonClicked() {}
	}
	
	@SuppressWarnings("serial")
	private class ProcessSelectorPanel extends AnnotationChooserPanel {
		protected ProcessSelectorPanel(boolean isstatic) {
			super(termlib);
			if (isstatic) {
				makeStaticPanel(drawer.getIndexofModelComponent());
			}
			else makeProcessSelector();
			constructSelector();
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getSource()==combobox) {
				drawer.setDataStructureComposite(getSelection());
				toggleNoneSelected(getSelection() == -1);
				showProcessParticipants();
				if (settings.doAutoAnnotateMapped()) drawer.copyToLocallyMappedVariables();
			}
		}

		@Override
		public void searchButtonClicked() {}

		@Override
		public void createButtonClicked() {
			CustomTermDialog ctd = new CustomTermDialog();
			ctd.makeProcessTerm(termlib);
			createTerm(ctd.getSelection());
		}

		@Override
		public void modifyButtonClicked() {
			CustomTermDialog ctd = new CustomTermDialog();
			ctd.makeProcessTerm(termlib, getSelection());
			showProcessParticipants();
		}
			
		private void createTerm(int procindex) {
			if (procindex != -1) {
				setSelection(procindex);
			}
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
			drawer.setDataStructureComposite(compin);
			if (settings.doAutoAnnotateMapped()) drawer.copyToLocallyMappedVariables();
		}		
	}
}
