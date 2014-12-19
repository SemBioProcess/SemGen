package semgen.annotation.annotatorpane;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.ArrayList;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import semgen.SemGenSettings;
import semgen.annotation.workbench.AnnotatorWorkbench;
import semgen.annotation.workbench.ModelAnnotationsBench;

public class ModelAnnotationPanel extends JPanel {
	private static final long serialVersionUID = 1L;

	ArrayList<ModelAnnPanel> annpanels = new ArrayList<ModelAnnPanel>();
	ModelAnnotationsBench metadatabench;
	
	public ModelAnnotationPanel(AnnotatorWorkbench wb) {
		setLayout(new BorderLayout(0,0));
		metadatabench = wb.getModelAnnotationsWorkbench();
		
		setBackground(SemGenSettings.lightblue);
		drawAnnotationList();
	}
	
	private void drawAnnotationList() {
		ArrayList<String[]> list = metadatabench.getAllMetadataInformation();
		
		for (String[] sarray : list) {
			AnnTextPanel newpanel = new AnnTextPanel(sarray[0]);
			newpanel.setText(sarray[1]);
			annpanels.add(newpanel);
		}
	}
	
	private void setMetadataValue(AnnTextPanel panel, String value) {
		metadatabench.setMetadataValuebyIndex(annpanels.indexOf(panel), value);
	}
	
	public void focusOnSelection() {
		giveFocus(metadatabench.getFocusIndex());
	}
	
	public void giveFocus(int index) {
		annpanels.get(index).giveFocus();
	}
	
	private abstract class ModelAnnPanel extends JPanel {
		private static final long serialVersionUID = 1L;
		protected boolean wasedited = false;
		
		protected ModelAnnPanel(String title) {
			setLayout(new BorderLayout(0,0));
			JLabel discription = new JLabel(title);
			add(discription, BorderLayout.WEST);
		}
		public abstract void giveFocus();
		public abstract void removeFocus();
	}
	
	private class AnnTextPanel extends ModelAnnPanel {
		private static final long serialVersionUID = 1L;
		JTextField textbox = new JTextField();
		
		
		AnnTextPanel(String title) {
			super(title);
			add(textbox);
		}
		
		public void setText(String text) {
			textbox.setText(text);
		}
		
		private String getText() {
			return textbox.getText();
		}
		
		public void giveFocus() {
			setBackground(Color.yellow);
			textbox.requestFocus();
			textbox.select(0, textbox.getText().length());
		}
		
		public void removeFocus() {
			setBackground(SemGenSettings.lightblue);
			if (wasedited) {
				setMetadataValue(this, getText());
			}
		}
	}
}
