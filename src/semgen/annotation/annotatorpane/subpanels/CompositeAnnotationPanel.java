package semgen.annotation.annotatorpane.subpanels;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import semgen.SemGenSettings;
import semgen.annotation.common.AnnotationChooserPanel;
import semgen.annotation.workbench.SemSimTermLibrary;
import semgen.annotation.workbench.drawers.CodewordToolDrawer;
import semgen.utilities.SemGenFont;

public class CompositeAnnotationPanel extends Box {
	private static final long serialVersionUID = 1L;
	private CodewordToolDrawer drawer;
	SemSimTermLibrary termlib;
	private int indent = 15;
	private JButton addbutton;

	private PropertySelectorPanel propsel;
	
	public CompositeAnnotationPanel(SemSimTermLibrary lib, CodewordToolDrawer bench, int orientation){
		super(orientation);
		drawer = bench;
		termlib = lib;
		setBackground(SemGenSettings.lightblue);
		setAlignmentX(Box.LEFT_ALIGNMENT);
		
		createPropertyPanel();
		validate();
	}

	private void createPropertyPanel() {
		propsel = new PropertySelectorPanel(!drawer.isEditable());
		if (drawer.isEditable()) {
			refreshPropertyTerms();
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
	
	private void refreshPropertyTerms() {
		propsel.setComboList(termlib.getPhysicalPropertyNames(), drawer.getIndexofPhysicalProperty());
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
			
		}

		@Override
		public void eraseButtonClicked() {
			
		}

		@Override
		public void searchButtonClicked() {
			
		}

		@Override
		public void createButtonClicked() {
			
		}

		@Override
		public void modifyButtonClicked() {
			
		}
	}
	

		
}
