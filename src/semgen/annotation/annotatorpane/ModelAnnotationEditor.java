package semgen.annotation.annotatorpane;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import semgen.SemGenSettings;
import semgen.annotation.workbench.AnnotatorWorkbench;
import semgen.annotation.workbench.drawers.ModelAnnotationsBench;
import semgen.annotation.workbench.drawers.ModelAnnotationsBench.ModelChangeEnum;

public class ModelAnnotationEditor extends JPanel implements Observer {
	private static final long serialVersionUID = 1L;

	ArrayList<ModelAnnPanel> annpanels = new ArrayList<ModelAnnPanel>();
	ModelAnnotationsBench metadatabench;
	
	public ModelAnnotationEditor(AnnotatorWorkbench wb) {
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		metadatabench = wb.getModelAnnotationsWorkbench();
		metadatabench.addObserver(this);
		
		setBackground(SemGenSettings.lightblue);
		drawAnnotationList();
		//makeDescriptionsUniform();
	}
	
	private void drawAnnotationList() {
		ArrayList<String[]> list = metadatabench.getAllMetadataInformation();

		for (String[] sarray : list) {
			AnnTextPanel newpanel = new AnnTextPanel(sarray[0]);
			newpanel.setText(sarray[1]);

			annpanels.add(newpanel);
			add(newpanel);
		}
		validate();
		focusOnSelection();
	}
	
	private void makeDescriptionsUniform() {
		int descsize;
		int largest = 0;
		for (ModelAnnPanel pan : annpanels) {
			descsize = pan.getDescriptionWidth();
			if (descsize>largest) largest = descsize;
		}
		for (ModelAnnPanel pan : annpanels) {
			pan.setDescriptionWidth(largest);
		}
		validate();
		repaint();
	}
	
	private void setMetadataValue(AnnTextPanel panel, String value) {
		metadatabench.setMetadataValuebyIndex(annpanels.indexOf(panel), value);
	}
	
	private void focusOnSelection() {
		giveFocus(metadatabench.getFocusIndex());
	}
	
	private void giveFocus(int index) {
		annpanels.get(index).giveFocus();
	}
	
	@Override
	public void update(Observable arg0, Object arg1) {
		if (arg1 == ModelChangeEnum.METADATASELECTED) {
			focusOnSelection();
		}
	}
	
	private abstract class ModelAnnPanel extends JPanel {
		private static final long serialVersionUID = 1L;
		protected boolean wasedited = false;
		protected JLabel description;
		
		protected ModelAnnPanel(String title) {
			setBackground(SemGenSettings.lightblue);
			addMouseListener(new MAPMouseAdapter());
			addKeyListener(new MAPKeyboardListener());
			setLayout(new BorderLayout(0,0));
			description = new JLabel(title);
			add(description, BorderLayout.WEST);
			add(Box.createGlue());
		}
		public abstract void giveFocus();
		public abstract void removeFocus();
		
		protected class MAPMouseAdapter extends MouseAdapter{
			@Override
			public void mouseClicked(MouseEvent e) {
				annpanels.get(metadatabench.getFocusIndex()).removeFocus();
				setMetadataIndex();
				giveFocus();
			}
		}
		
		private void setMetadataIndex() {
			metadatabench.setMetadataSelectionIndex(annpanels.indexOf(this));
		}
		
		protected int getDescriptionWidth() {
			return description.getWidth();
		}
		
		protected void setDescriptionWidth(int width) {
			description.setSize(width, description.getHeight());
			validate();
			repaint();
		}
		
		protected class MAPFocusListener implements FocusListener {
			@Override
			public void focusGained(FocusEvent e) {
				giveFocus();
			}

			@Override
			public void focusLost(FocusEvent e) {
				removeFocus();
			}
		}
		
		protected class MAPKeyboardListener implements KeyListener {
			@Override
			public void keyPressed(KeyEvent arg0) {

			}

			@Override
			public void keyReleased(KeyEvent arg0) {

			}

			@Override
			public void keyTyped(KeyEvent arg0) {
				wasedited = true;
			}
		}
	}
	
	private class AnnTextPanel extends ModelAnnPanel {
		private static final long serialVersionUID = 1L;
		JTextField textbox = new JTextField();
		
		AnnTextPanel(String title) {
			super(title);
			//textbox.addFocusListener(new MAPFocusListener());
			textbox.addMouseListener(new MAPMouseAdapter());
			textbox.addKeyListener(new MAPKeyboardListener());
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
