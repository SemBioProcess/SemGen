package semgen.annotation.componentlistpanes;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.semanticweb.owlapi.model.OWLException;

import semgen.SemGenSettings;
import semgen.annotation.componentlistpanes.buttons.SubmodelButton;
import semgen.annotation.dialog.TextChangeDialog;
import semgen.annotation.workbench.AnnotatorWorkbench;
import semgen.annotation.workbench.AnnotatorWorkbench.modeledit;
import semgen.annotation.workbench.drawers.SubModelToolDrawer;
import semgen.utilities.SemGenIcon;

public class SubmodelListPane extends AnnotatorListPane<SubmodelButton, SubModelToolDrawer> implements ActionListener{
	private static final long serialVersionUID = 1L;

	private JButton addsubmodelbutton = new JButton(SemGenIcon.plusicon);
	private JButton removesubmodelbutton = new JButton(SemGenIcon.minusicon);
	
	public SubmodelListPane(AnnotatorWorkbench wb, SemGenSettings sets) {
		super(wb, sets, wb.openSubmodelDrawer());

		addsubmodelbutton.addActionListener(this);
		removesubmodelbutton.addActionListener(this);
	}

	public void updateButtonTable(){
		ArrayList<Integer> numlist = drawer.getSubmodelstoDisplay(settings.showImports());
		addPanelTitle("Sub-models ", numlist.size(), "No sub-models found");
		
		JPanel componentaddremovepanel = new JPanel();
		componentaddremovepanel.setOpaque(false);
		componentaddremovepanel.setLayout(new BoxLayout(componentaddremovepanel, BoxLayout.X_AXIS));
		componentaddremovepanel.add(addsubmodelbutton);
		componentaddremovepanel.add(removesubmodelbutton);
		buttonpane.add(componentaddremovepanel);
		
		for (Integer index : numlist) {
			SubmodelButton sbm = new ListButton(drawer.getCodewordName(index), drawer.isEditable(index));
			addButton(sbm, index);
			btnlist.put(sbm, index);
			sbm.toggleHumanDefinition(drawer.hasHumanReadableDef(index));
			sbm.toggleSingleAnnotation(drawer.hasSingularAnnotation(index));
			buttonpane.add(sbm);
		}
		buttonpane.add(Box.createGlue());
	}

	public void addNewSubmodelButton() throws OWLException {
		TextChangeDialog tcd = new TextChangeDialog("Add Submodel", "Enter a name for the new sub-model", "");
		String newname = tcd.getNewText();
		if(!newname.equals("")){
			workbench.addSubmodeltoModel(newname);
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object o = e.getSource();

		if (o == addsubmodelbutton){
			try {addNewSubmodelButton();
			} catch (OWLException e1) {e1.printStackTrace();}
		}
		if(o == removesubmodelbutton){
				int choice = JOptionPane.showConfirmDialog(this, 
						"Are you sure you want to remove component " + focusbutton.getText() + "?", "Confirm removal", JOptionPane.YES_NO_OPTION);
				if(choice == JOptionPane.YES_OPTION){
						workbench.removeSubmodelfromModel();
				}
		}
	}

	@Override
	public void updateUnique(Object arg) {
		if ((arg == modeledit.smnamechange) || (arg == modeledit.smlistchanged)){
			update();
		}
		if (focusbutton!=null) {
			removesubmodelbutton.setEnabled(!drawer.isFunctional());
		}
	}
	
	private class ListButton extends SubmodelButton {
		private static final long serialVersionUID = 1L;

		public ListButton(String name, boolean canedit) {
			super(name, canedit);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			
		}

		@Override
		public void mouseClicked(MouseEvent arg0) {
			changeButtonFocus(this);
			if (arg0.getSource()==humdeflabel) {
				workbench.requestFreetextChange();
			}
		}




	}
	
}
