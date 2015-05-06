package semgen.annotation.annotatorpane;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Observable;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import semgen.GlobalActions;
import semgen.SemGenSettings;
import semgen.annotation.common.AnnotationClickableTextPane;
import semgen.annotation.dialog.SemSimComponentSelectionDialog;
import semgen.annotation.dialog.TextChangeDialog;
import semgen.annotation.workbench.AnnotatorWorkbench;
import semgen.annotation.workbench.AnnotatorWorkbench.modeledit;
import semgen.annotation.workbench.drawers.CodewordToolDrawer;
import semgen.annotation.workbench.drawers.SubModelToolDrawer;
import semgen.utilities.SemGenIcon;
import semgen.utilities.uicomponent.SemGenSeparator;

public class SubmodelAnnotationPanel extends AnnotationPanel<SubModelToolDrawer> {
	private static final long serialVersionUID = 1L;
	
	protected AnnotatorButton loadsourcemodelbutton = new AnnotatorButton(SemGenIcon.annotatoricon, "Annotate source model for this imported sub-model");
	private AnnotationClickableTextPane nestedsubmodelpane;
	private AnnotationClickableTextPane subtitlefield;
	private CodewordToolDrawer cwdrawer;
	
	public SubmodelAnnotationPanel(AnnotatorWorkbench wb, SemGenSettings sets,
			GlobalActions gacts) {
		super(wb, wb.openSubmodelDrawer(), sets, gacts);
		cwdrawer = workbench.openCodewordDrawer();
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
		
		refreshSubmodelData();
		subtitlefield.setAlignmentX(JPanel.LEFT_ALIGNMENT);

		nestedsubmodelpane.setAlignmentX(JPanel.LEFT_ALIGNMENT);
		if (!drawer.isFunctional()) {
			subtitlefield.addMouseListener(this);
			nestedsubmodelpane.addMouseListener(this);
		}
		mainpanel.add(nestedsubmodelpane);
		mainpanel.add(subtitlefield);
		mainpanel.add(new SemGenSeparator());
		
	}

	private void refreshSubmodelData(){	
		String subtitletext = "Click to assign codewords to this component";
		String editcomptext = "Click to assign sub-models";
		
		if(drawer.isFunctional()){
			subtitletext = "No codewords associated with submodel";
			editcomptext = "No submodels associated with this submodel";
		}
		
		//	Include the codewords that are in the subcomponents in the list of associated codewords
		ArrayList<String> assoccomp = drawer.getDataStructureNames();
		ArrayList<String> assocsmds = drawer.getAssociatedSubModelDataStructureNames();

		boolean isempty = assoccomp.isEmpty() && assocsmds.isEmpty();
		if (!isempty) {
			subtitletext = generateSubcomponentText(assoccomp);
			if (!assocsmds.isEmpty()) {
				if (!assoccomp.isEmpty()) {
					subtitletext = subtitletext + ", ";
				}
				for (String s : assoccomp) {
						while (assocsmds.contains(s)) {
							assocsmds.remove(s);
						}
				}
				subtitletext = subtitletext + "{" + generateSubcomponentText(assocsmds) + "}"; 	
			}
		}
		setSubmodelDataOnScreen(subtitlefield, "Codewords: " + subtitletext, isempty);
		
		assoccomp = drawer.getAssociatedSubmodelNames();
		
		isempty = assoccomp.isEmpty();
		if(!isempty){
			editcomptext = generateSubcomponentText(assoccomp);
		}
		setSubmodelDataOnScreen(nestedsubmodelpane, "Sub-components: " + editcomptext, isempty);
	}
	
	private String generateSubcomponentText(ArrayList<String> list) {
		if (list.isEmpty()) return ""; 
		String txt = list.get(0);
		for (int i = 1; i< list.size(); i++) {
			txt = txt + ", " + list.get(i);
		}
		return txt;
	}
	
	public void setSubmodelDataOnScreen(AnnotationClickableTextPane pane, String text, boolean empty){	
		if (empty) pane.setForeground(Color.gray);
		else if(drawer.isEditable()) pane.setForeground(Color.black);
		else pane.setForeground(Color.blue);
		
		pane.setText(text);		
	}
	
	protected void associateSubmodels() {
		ArrayList<Integer> sms = drawer.getSubmodelsWithoutFocus();
		SemSimComponentSelectionDialog dialog = new SemSimComponentSelectionDialog(
				"Select Submodels", drawer.getComponentNamesfromIndicies(sms), drawer.getAssociatedSubmodelIndicies(sms));
		
		drawer.addSubmodelstoSubmodel(dialog.getSelections());
		refreshSubmodelData();
	}
	
	protected void associateDatastructures() {
		ArrayList<Integer> dsind = cwdrawer.getCodewordstoDisplay(new Boolean[]{settings.showImports(), false, false});
		ArrayList<String> dsnames = new ArrayList<String>();
		for (Integer i : dsind) {
			dsnames.add(cwdrawer.getCodewordName(i));
		}
		SemSimComponentSelectionDialog dialog = new SemSimComponentSelectionDialog(
				"Select Data Structures", dsnames, workbench.getSelectedSubmodelDSIndicies());
		
		workbench.addDataStructurestoSubmodel(dialog.getSelections());
		refreshSubmodelData();

	}

	@Override
	public void mouseEntered(MouseEvent e) {}

	@Override
	public void mouseExited(MouseEvent e) {}

	@Override
	public void mousePressed(MouseEvent e) {}

	@Override
	public void mouseReleased(MouseEvent e) {}

	@Override
	public void mouseClicked(MouseEvent e) {
		super.mouseClicked(e);
		// Activated if user selects the Annotator icon within the AnnotationDialog (used for imported submodels)
		if(e.getComponent() == nestedsubmodelpane){
			associateSubmodels();
		}
		if(e.getComponent() == subtitlefield){
			associateDatastructures();
		}
		if(e.getComponent() == loadsourcemodelbutton){
			File file = workbench.getSourceSubmodelFile();

			if(file.exists()){
				globalacts.NewAnnotatorTab(file);
			}
			else{JOptionPane.showMessageDialog(this, "Could not locate source file for this sub-model.", "ERROR", JOptionPane.ERROR_MESSAGE);}
		}
		
	}
	
	@Override
	public void updateUnique(Observable o, Object arg) {
		if (arg == modeledit.smnamechange) {
			codewordlabel.setText(drawer.getCodewordName());
		}
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
				TextChangeDialog tcd = new TextChangeDialog("Rename component", currentname, currentname);
				String newcompname = tcd.getNewText();
				if(newcompname.equals(currentname) && !newcompname.equals("")) break;
				
				if (workbench.submitSubmodelName(newcompname)) {
					drawer.setSubmodelName(newcompname);
					break;
				}
					
				JOptionPane.showMessageDialog(null, "That name is either invalid or already taken");
			}
		}
	}
}
