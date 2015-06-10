package semgen.annotation.annotatorpane.subpanels;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import semgen.SemGenSettings;
import semgen.annotation.common.AnnotationChooserPanel;
import semgen.annotation.common.EntitySelectorGroup;
import semgen.annotation.dialog.termlibrary.AddReferenceClassDialog;
import semgen.annotation.dialog.termlibrary.CustomTermDialog;
import semgen.annotation.workbench.SemSimTermLibrary;
import semgen.annotation.workbench.drawers.CodewordToolDrawer;
import semgen.utilities.SemGenFont;
import semsim.utilities.ReferenceOntologies.OntologyDomain;

public class CompositeAnnotationPanel extends Box implements ActionListener{
	private static final long serialVersionUID = 1L;
	private CodewordToolDrawer drawer;
	SemSimTermLibrary termlib;
	private int indent = 15;
	private JButton addentbutton = new JButton("Add entity");
	private JButton addprocbutton = new JButton("Add process");

	private PropertySelectorPanel propsel;
	private EntitySelectorGroup esg;
	private Box pmcpanel;
	
	public CompositeAnnotationPanel(SemSimTermLibrary lib, CodewordToolDrawer bench, int orientation){
		super(orientation);
		drawer = bench;
		termlib = lib;
		setBackground(SemGenSettings.lightblue);
		setAlignmentX(Box.LEFT_ALIGNMENT);

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
        propertyoflabel.setBorder(BorderFactory.createEmptyBorder(0, indent*2, 0, 0));
        
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
		Box btnbox = new Box(BoxLayout.X_AXIS);
		btnbox.add(addentbutton);
		btnbox.add(addprocbutton);
		btnbox.add(Box.createHorizontalGlue());
		pmcpanel = btnbox;
		btnbox.setBorder(BorderFactory.createEmptyBorder(0, indent*2, 0, 0));
		add(pmcpanel);
	}
	
	private void setProcessSelector() {
		if (pmcpanel!=null) remove(pmcpanel);
		Box procbox = new Box(BoxLayout.X_AXIS);
		ProcessSelectorPanel pcp = new ProcessSelectorPanel(!drawer.isEditable());

		pcp.setComboList(termlib.getSortedPhysicalProcessIndicies(), drawer.getIndexofModelComponent());
		procbox.add(pcp);
		procbox.setBorder(BorderFactory.createEmptyBorder(0, indent*2, 0, 0));
		pmcpanel = procbox;
		add(pmcpanel);
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
			if (drawer.isProcess()) {
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
				drawer.setDatastructurePhysicalProperty(getSelection());
				toggleNoneSelected(getSelection() == -1);
				onPropertyChange();
			}
		}

		@Override
		public void searchButtonClicked() {
			AddReferenceClassDialog rcd = new AddReferenceClassDialog(termlib, OntologyDomain.AssociatePhysicalProperty);
			if (rcd.getIndexofSelection()!=-1) {
				setComboList(termlib.getSortedAssociatePhysicalPropertyIndicies(), rcd.getIndexofSelection());
				onPropertyChange();
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
				drawer.setDataStructureComposite(getSelection(), false);
				toggleNoneSelected(getSelection() == -1);
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
			createTerm(ctd.getSelection());
		}
			
		private void createTerm(int procindex) {
			if (procindex != -1) {
				setComboList(termlib.getSortedPhysicalProcessIndicies(), procindex);
			}
		}
	}
	
	private class CodewordCompositeSelectors extends EntitySelectorGroup {
		private static final long serialVersionUID = 1L;

		public CodewordCompositeSelectors(SemSimTermLibrary lib) {
			super(lib, drawer.getCompositeEntityIndicies(), drawer.isEditable());
			
		}

		@Override
		public void onChange(boolean customchange) {
			Integer compin = termlib.createCompositePhysicalEntity(pollSelectors());
			drawer.setDataStructureComposite(compin, customchange);
		}		
	}
}
