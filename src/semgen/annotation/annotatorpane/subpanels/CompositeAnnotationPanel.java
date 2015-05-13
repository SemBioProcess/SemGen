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
import semgen.annotation.workbench.SemSimTermLibrary;
import semgen.annotation.workbench.drawers.CodewordToolDrawer;
import semgen.utilities.SemGenFont;
import semsim.PropertyType;

public class CompositeAnnotationPanel extends Box implements ActionListener{
	private static final long serialVersionUID = 1L;
	private CodewordToolDrawer drawer;
	SemSimTermLibrary termlib;
	private int indent = 15;
	private JButton addentbutton = new JButton("Add entity");
	private JButton addprocbutton = new JButton("Add process");

	private PropertySelectorPanel propsel;
	private EntitySelectorGroup esg;
	private JPanel pmcpanel = new JPanel();
	
	public CompositeAnnotationPanel(SemSimTermLibrary lib, CodewordToolDrawer bench, int orientation){
		super(orientation);
		drawer = bench;
		termlib = lib;
		setBackground(SemGenSettings.lightblue);
		setAlignmentX(Box.LEFT_ALIGNMENT);
		
		createPropertyPanel();
		
		pmcpanel.setLayout(new BoxLayout(pmcpanel, BoxLayout.X_AXIS));
		pmcpanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		
		addentbutton.addActionListener(this);
		addprocbutton.addActionListener(this);
		setPhysicalComponentPanel();
		onPropertyChange();
		validate();
	}

	private void createPropertyPanel() {
		propsel = new PropertySelectorPanel(!drawer.isEditable());
		if (drawer.isEditable()) {
			refreshPropertyTerms();
			onPropertyChange();
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
        add(propofpanel);
        
	}
	
	private void setPhysicalComponentPanel() {
		pmcpanel.removeAll();
		
		if (drawer.hasPhysicalModelComponent()) {
			esg = new EntitySelectorGroup(drawer);
			pmcpanel.add(esg);
		}
		else {
			esg = null;
			showAddEntityProcessButtons();
		}
		add(pmcpanel);
	}
	
	private void showAddEntityProcessButtons() {
		pmcpanel.add(addentbutton);
		pmcpanel.add(addprocbutton);
		
	}
	
	private void refreshPropertyTerms() {
		propsel.setComboList(termlib.getPhysicalPropertyNames(), drawer.getIndexofPhysicalProperty());
	}
	
	public void onPropertyChange() {
		propsel.toggleNoneSelected(drawer.getIndexofPhysicalProperty()==-1);
		if (esg==null) {
		PropertyType type = drawer.getPropertyType();
		switch (type) {
			case PropertyOfPhysicalEntity:
				addentbutton.setEnabled(true); 
				addprocbutton.setEnabled(false);
				break;
			case PropertyOfPhysicalProcess:
				addentbutton.setEnabled(false); 
				//addprocbutton.setEnabled();
				break;
			default:
				addentbutton.setEnabled(true); 
				addprocbutton.setEnabled(true);
				break;
			
			}
		}
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		
	}
	
	@SuppressWarnings("serial")
	private class PropertySelectorPanel extends AnnotationChooserPanel {
		protected PropertySelectorPanel(boolean isstatic) {
			if (isstatic) {
				String ppname;
				Integer index = drawer.getIndexofPhysicalProperty();
				if (index==-1) {
					ppname = AnnotationChooserPanel.unspecifiedName;
				}
				else {
					ppname = termlib.getPhysicalPropertyName(index);
				}
				makeStaticPanel(ppname, true);
			}
			else makePhysicalPropertySelector();
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getSource()==combobox) {
				drawer.setDatastructurePhysicalProperty(combobox.getSelectedIndex());
			}
		}

		@Override
		public void webButtonClicked() {
			urlbutton.openTerminBrowser(drawer.getPhysicalPropertyURI());
		}

		@Override
		public void eraseButtonClicked() {
			setSelection(-1);
		}

		@Override
		public void searchButtonClicked() {
			
		}

		@Override
		public void createButtonClicked() {}

		@Override
		public void modifyButtonClicked() {}
	}

}
