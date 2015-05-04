package semgen.annotation.annotatorpane.subpanels;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import semgen.SemGenSettings;
import semgen.annotation.common.AnnotationSelectorPanel;
import semgen.annotation.workbench.drawers.CodewordToolDrawer;
import semgen.utilities.SemGenFont;

public class CompositeAnnotationPanel extends Box {
	private static final long serialVersionUID = 1L;
	private CodewordToolDrawer drawer;
	private int indent =15;
	private JButton addbutton;

	private PropertySelectorPanel propsel = new PropertySelectorPanel();
	
	public CompositeAnnotationPanel(CodewordToolDrawer bench, int orientation){
		super(orientation);
		drawer = bench;
		this.setBackground(SemGenSettings.lightblue);
		setAlignmentX(Box.LEFT_ALIGNMENT);
		
		
		JPanel propofpanel = new JPanel( new BorderLayout() );
		
		JLabel propertyoflabel = new JLabel("property_of");
        propertyoflabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        propertyoflabel.setFont(SemGenFont.defaultItalic());
        propertyoflabel.setBorder(BorderFactory.createEmptyBorder(0, indent, 0, 0));
        propofpanel.add(propertyoflabel);
        add(propofpanel);
	}
	


	@SuppressWarnings("serial")
	private class PropertySelectorPanel extends AnnotationSelectorPanel {
		protected PropertySelectorPanel() {
			
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			
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
