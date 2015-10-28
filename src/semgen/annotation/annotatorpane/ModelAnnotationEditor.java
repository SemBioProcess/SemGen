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

import javax.swing.BorderFactory;
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
		metadatabench = wb.openModelAnnotationsWorkbench();
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
	
	private void setMetadataValue(AnnTextPanel panel, String value) {
		metadatabench.setMetadataValuebyIndex(annpanels.indexOf(panel), value);
	}
	
	private void focusOnSelection() {
		annpanels.get(metadatabench.getFocusIndex()).giveFocus();
	}
	
	@Override
	public void update(Observable arg0, Object arg1) {
		if (arg1 == ModelChangeEnum.METADATASELECTED) {
			focusOnSelection();
		}
		if (arg1 == ModelChangeEnum.METADATAIMPORTED) {
			for (ModelAnnPanel box : annpanels) {
				box.updateValue();
			}
		}
	}
	
	private abstract class ModelAnnPanel extends JPanel {
		private static final long serialVersionUID = 1L;
		protected JLabel description;
		
		protected ModelAnnPanel(String title) {
			setBackground(SemGenSettings.lightblue);
			addMouseListener(new MAPMouseAdapter());
			setLayout(new BorderLayout(0,0));
			description = new JLabel(title);
			description.setBorder(BorderFactory.createEmptyBorder(0, 7, 0, 7));
			add(description, BorderLayout.WEST);
			add(Box.createGlue());
		}
		public abstract void giveFocus();
		public abstract void removeFocus();
		public abstract void updateValue();
		
		protected class MAPMouseAdapter extends MouseAdapter{
			@Override
			public void mouseClicked(MouseEvent e) {
				annpanels.get(metadatabench.getFocusIndex()).removeFocus();
				setMetadataIndex();
				giveFocus();
			}
		}
		
		protected void setMetadataIndex() {
			metadatabench.setMetadataSelectionIndex(annpanels.indexOf(this));
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
	}
	
	private class AnnTextPanel extends ModelAnnPanel {
		private static final long serialVersionUID = 1L;
		JTextField textbox = new JTextField();
		
		AnnTextPanel(String title) {
			super(title);
			textbox.addFocusListener(new MAPFocusListener());
			textbox.addMouseListener(new MAPMouseAdapter());
			textbox.addKeyListener(new MAPKeyboardListener(this));
			add(textbox);
		}
		
		public void setText(String text) {
			textbox.setText(text);
		}
		
		private String getText() {
			return textbox.getText();
		}
		
		public void giveFocus() {
			setMetadataIndex();
			setBackground(new Color(255,231,186));
			textbox.requestFocusInWindow();
			textbox.select(0, textbox.getText().length());
		}
		
		public void removeFocus() {
			setBackground(SemGenSettings.lightblue);
		}
		
		protected class MAPKeyboardListener implements KeyListener {
			AnnTextPanel target;
			
			protected MAPKeyboardListener(AnnTextPanel targ) {
				target = targ;
			}
			@Override
			public void keyPressed(KeyEvent arg0) {

			}

			@Override
			public void keyReleased(KeyEvent arg0) {
				setMetadataValue(target, target.getText());
			}

			@Override
			public void keyTyped(KeyEvent arg0) {
				
			}
		}

		@Override
		public void updateValue() {
			setText(metadatabench.getMetadataValuebyIndex(annpanels.indexOf(this)));
		}
	}
}
