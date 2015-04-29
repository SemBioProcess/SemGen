package semgen.annotation.annotatorpane;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Observable;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import semgen.GlobalActions;
import semgen.SemGenSettings;
import semgen.annotation.workbench.AnnotatorWorkbench;
import semgen.annotation.workbench.drawers.SubModelToolDrawer;
import semgen.utilities.SemGenIcon;
import semgen.utilities.uicomponent.SemGenSeparator;

public class SubmodelAnnotationPanel extends AnnotationPanel<SubModelToolDrawer> {
	private static final long serialVersionUID = 1L;
	
	protected AnnotatorButton loadsourcemodelbutton = new AnnotatorButton(SemGenIcon.annotatoricon, "Annotate source model for this imported sub-model");
	private AnnotationClickableTextPane nestedsubmodelpane;
	private AnnotationClickableTextPane subtitlefield;
	
	public SubmodelAnnotationPanel(AnnotatorWorkbench wb, SemGenSettings sets,
			GlobalActions gacts) {
		super(wb, wb.openSubmodelDrawer(), sets, gacts);

		drawUI();
	}

	@Override
	protected void formatHeader(Box mainheader) {
		if (drawer.isEditable()) {
			codewordlabel.addMouseListener(new SubmodelCodewordMouseBehavior());
			codewordlabel.addMouseListener(this);
		}
		if(drawer.isImported()){
			mainheader.add(loadsourcemodelbutton);
			codewordlabel.setBorder(BorderFactory.createEmptyBorder(5, indent, 5, 10));
		}
	}
	
	@Override
	protected void createUniqueElements() {
		subtitlefield = new AnnotationClickableTextPane("", 2*indent, drawer.isEditable() && !drawer.isFunctional());
		nestedsubmodelpane = new AnnotationClickableTextPane("", 2*indent, drawer.isEditable() && !drawer.isFunctional());
		
		subtitlefield.setAlignmentX(JPanel.LEFT_ALIGNMENT);
		mainpanel.add(subtitlefield);
		nestedsubmodelpane.setAlignmentX(JPanel.LEFT_ALIGNMENT);
		mainpanel.add(nestedsubmodelpane);
		mainpanel.add(new SemGenSeparator());
		
	}

	@Override
	protected void refreshData() {
		
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		
	}

	@Override
	public void mousePressed(MouseEvent e) {
		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		
	}


	@Override
	public void updateUnique(Observable o, Object arg) {
		
	}
	
	private class SubmodelCodewordMouseBehavior extends MouseAdapter {
		public void mouseEntered(MouseEvent e) {
			codewordlabel.setForeground(Color.blue);
			codewordlabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
		}
		
		public void mouseExited(MouseEvent e) {
			codewordlabel.setForeground(Color.black);
			codewordlabel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		}
		
		public void mouseClicked(MouseEvent arg0) {
			String currentname =  drawer.getCodewordName();
			while (true) {
				String newcompname = JOptionPane.showInputDialog(null, "Rename component", currentname);
				if(newcompname!=null && !newcompname.equals(currentname)){
					if (workbench.submitSubmodelName(newcompname)) break;
					
					JOptionPane.showMessageDialog(null, "That name is either invalid or already taken");
				}
			}
		}
	}


}
