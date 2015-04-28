package semgen.annotation.annotatorpane;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Observable;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import semgen.GlobalActions;
import semgen.SemGenSettings;
import semgen.annotation.workbench.AnnotatorWorkbench;
import semgen.annotation.workbench.drawers.SubModelToolDrawer;
import semgen.utilities.SemGenIcon;

public class SubmodelAnnotationPanel extends AnnotationPanel<SubModelToolDrawer> {
	private static final long serialVersionUID = 1L;
	
	protected AnnotatorButton loadsourcemodelbutton = new AnnotatorButton(SemGenIcon.annotatoricon, "Annotate source model for this imported sub-model");
	
	public SubmodelAnnotationPanel(AnnotatorWorkbench wb, SemGenSettings sets,
			GlobalActions gacts) {
		super(wb, wb.openSubmodelDrawer(), sets, gacts);

		drawUI();
	}

	@Override
	public void update(Observable o, Object arg) {
		
	}

	@Override
	protected void constructMainPanel(JPanel main) {
		
	}

	@Override
	protected void refreshUI() {
		
	}

	@Override
	protected void formatHeader() {
		if (drawer.isEditable()) {
			codewordlabel.addMouseListener(new SubmodelCodewordMouseBehavior());
			codewordlabel.addMouseListener(this);
		}
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
	protected void constructHumanDefinitionPanel() {

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
