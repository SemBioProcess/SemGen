package semgen.annotation;

import semgen.SemGenGUI;
import semgen.SemGenGUI.NewAnnotatorTask;
import semgen.annotation.composites.CompositeAnnotationPanel;
import semgen.annotation.composites.SemSimComponentAnnotationPanel;
import semgen.annotation.composites.StructuralRelationPanel;
import semgen.resource.SemGenFont;
import semgen.resource.SemGenIcon;
import semgen.resource.uicomponent.SemGenSeparator;
import semsim.Annotatable;
import semsim.model.Importable;
import semsim.model.SemSimComponent;
import semsim.model.SemSimModel;
import semsim.model.annotation.StructuralRelation;
import semsim.model.computational.DataStructure;
import semsim.model.computational.MappableVariable;
import semsim.model.physical.PhysicalEntity;
import semsim.model.physical.PhysicalModelComponent;
import semsim.model.physical.Submodel;
import semsim.model.physical.FunctionalSubmodel;
import semsim.owl.SemSimOWLFactory;
import semsim.writing.CaseInsensitiveComparator;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ListIterator;
import java.util.Set;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Font;

import javax.swing.*;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import org.semanticweb.owlapi.model.OWLException;

public class AnnotationDialog extends JPanel implements MouseListener{

	private static final long serialVersionUID = -7946871333815617810L;
	public AnnotatorTab annotator;
	public SemSimModel semsimmodel;
	public SemSimComponent smc;
	public AnnotationObjectButton thebutton;
	public CompositeAnnotationPanel compositepanel;
	public JPanel mainpanel;
	public SemSimComponentAnnotationPanel singularannpanel;
	private JPanel humandefpanel;
	private JLabel codewordlabel;
	public AnnotationDialogClickableTextPane subtitlefield;
	public AnnotationDialogClickableTextPane nestedsubmodelpane;
	private JLabel compositelabel;
	private JLabel singularannlabel;
	public AnnotationDialogClickableTextPane humandefpane;
	private JButton humapplyelsewherebutton;
	public JLabel humremovebutton;
	private JLabel copyannsbutton = new JLabel(SemGenIcon.copyicon);
	private JLabel loadsourcemodelbutton = new JLabel(SemGenIcon.annotatoricon);
	public SelectorDialogForCodewordsOfSubmodel sdfcoc;
	public SelectorDialogForSubmodelsOfSubmodel sdfcomp;
	public Set<DataStructure> cdwdsfromcomps;
	public String codeword;
	
	public int indent = 15;

	public AnnotationDialog(AnnotatorTab ann, AnnotationObjectButton aob) throws IOException{
		annotator = ann;
		thebutton = aob;
		codeword = aob.getName();
		semsimmodel = annotator.semsimmodel;

		if(aob instanceof SubmodelButton)
			smc = ((SubmodelButton)aob).sub;
		else smc = ((CodewordButton)aob).ds;
		
		setBackground(SemGenGUI.lightblue);
		setLayout(new BorderLayout());

		codewordlabel = new JLabel(codeword);
		codewordlabel.setBorder(BorderFactory.createEmptyBorder(5, indent, 5, 10));
		codewordlabel.setFont(SemGenFont.defaultBold(3));
		
		humapplyelsewherebutton = new JButton();
		humremovebutton = new JLabel(SemGenIcon.eraseiconsmall);

		FormatButton(humremovebutton, "Remove this annotation", thebutton.editable);
		
		copyannsbutton.setToolTipText("Copy annotations to all mapped variables");
		copyannsbutton.addMouseListener(this);
		copyannsbutton.setBorder(BorderFactory.createEmptyBorder(1,1,1,1));
		
		loadsourcemodelbutton.setToolTipText("Annotate source model for this imported sub-model");
		loadsourcemodelbutton.addMouseListener(this);
		loadsourcemodelbutton.setBorder(BorderFactory.createEmptyBorder(1,1,1,1));

		humapplyelsewherebutton.setVisible(false);

		humandefpanel = new JPanel();
		humandefpanel.setBackground(SemGenGUI.lightblue);
		humandefpanel.setBorder(BorderFactory.createEmptyBorder(0, indent, 0, 0));

		JPanel humandefsubpanel = new JPanel();
		humandefsubpanel.setBackground(SemGenGUI.lightblue);
		humandefsubpanel.setLayout(new BoxLayout(humandefsubpanel, BoxLayout.X_AXIS));
		humandefpane = new AnnotationDialogClickableTextPane("[unspecified]",this, indent, thebutton.editable);
		humandefpane.setAlignmentX(JTextArea.LEFT_ALIGNMENT);
		humandefpanel.setLayout(new BorderLayout());
		humandefsubpanel.add(humandefpane);
		humandefsubpanel.add(humremovebutton);
		humandefpanel.add(humandefsubpanel, BorderLayout.WEST);
		humandefpanel.add(Box.createGlue(), BorderLayout.EAST);
		
		JPanel subtitlepanel = new JPanel();
		subtitlepanel.setLayout(new BoxLayout(subtitlepanel, BoxLayout.Y_AXIS));
		
		JPanel codewordspanel = new JPanel();
		JEditorPane eqpane = null;
		
		// If viewing a submodel
		if(thebutton instanceof SubmodelButton){
			if(thebutton.editable) codewordlabel.addMouseListener(this);
			subtitlefield = new AnnotationDialogClickableTextPane("", this, 2*indent, (thebutton.editable && !(thebutton.ssc instanceof FunctionalSubmodel)));
			nestedsubmodelpane = new AnnotationDialogClickableTextPane("", this, 2*indent, (thebutton.editable && !(thebutton.ssc instanceof FunctionalSubmodel)));
			
			codewordspanel.setLayout(new BorderLayout());
			codewordspanel.add(subtitlefield, BorderLayout.NORTH);
			codewordspanel.add(Box.createHorizontalGlue(), BorderLayout.EAST);
			codewordspanel.setBackground(SemGenGUI.lightblue);
			subtitlepanel.add(codewordspanel);
			
			JPanel nestedsubmodelpanel = new JPanel();
			nestedsubmodelpanel.setLayout(new BorderLayout());
			nestedsubmodelpanel.add(nestedsubmodelpane, BorderLayout.WEST);
			nestedsubmodelpanel.add(Box.createGlue(), BorderLayout.EAST);
			nestedsubmodelpanel.setBackground(SemGenGUI.lightblue);
			subtitlepanel.add(nestedsubmodelpanel);
		}
		// If viewing a codeword, get the equation and units associated with the codeword
		else{
			String code = "";
			if(semsimmodel.getDataStructure(codeword).getComputation()!=null){
				code = semsimmodel.getDataStructure(codeword).getComputation().getComputationalCode();
			}
			eqpane = new JEditorPane();
			eqpane.setEditable(false);
			eqpane.setText(code);
			eqpane.setFont(SemGenFont.defaultItalic(-1));
			eqpane.setOpaque(false);
			eqpane.setAlignmentX(JEditorPane.LEFT_ALIGNMENT);
			eqpane.setBorder(BorderFactory.createEmptyBorder(2, 2*indent, 2, 2));
			eqpane.setBackground(new Color(0,0,0,0));

			String units = "dimensionless";
			if(((DataStructure)thebutton.ssc).hasUnits())
				units = ((DataStructure)thebutton.ssc).getUnit().getName();
			codewordlabel.setText(codewordlabel.getText() + " (" + units + ")");
			compositepanel = new CompositeAnnotationPanel(BoxLayout.Y_AXIS, this);
		}
		subtitlepanel.setBorder(BorderFactory.createEmptyBorder(0, indent, 0, indent));
		subtitlepanel.setBackground(SemGenGUI.lightblue);
		
		singularannlabel = new JLabel("Singular annotation");
		singularannlabel.setFont(SemGenFont.defaultBold());
		singularannlabel.setBorder(BorderFactory.createEmptyBorder(10, indent, 5, 0));

		refreshData();

		mainpanel = new JPanel();
		mainpanel.setLayout(new BoxLayout(mainpanel, BoxLayout.Y_AXIS));
		
		Box mainheader = Box.createHorizontalBox();
		mainheader.setBackground(SemGenGUI.lightblue);
		mainheader.setAlignmentX(LEFT_ALIGNMENT);
		
		codewordlabel.setAlignmentX(JLabel.LEFT_ALIGNMENT);
		mainheader.add(codewordlabel);

		if(smc instanceof MappableVariable){
			MappableVariable mvar = (MappableVariable)smc;
			if(!mvar.getMappedTo().isEmpty() || !mvar.getMappedFrom().isEmpty()){
				mainheader.add(copyannsbutton);
				codewordlabel.setBorder(BorderFactory.createEmptyBorder(5, indent, 5, 10));
			}
		}
		// If we're looking at an imported submodel
		else if(smc instanceof Submodel){
			Submodel fsub = (Submodel)smc;
			if(fsub.isImported()){
				mainheader.add(loadsourcemodelbutton);
				codewordlabel.setBorder(BorderFactory.createEmptyBorder(5, indent, 5, 10));
			}
		}
		mainheader.add(Box.createGlue());
		
		mainpanel.add(mainheader);
		mainpanel.add(humandefpanel);
		
		if(thebutton instanceof SubmodelButton){
			subtitlefield.setAlignmentX(JPanel.LEFT_ALIGNMENT);
			codewordspanel.setAlignmentX(JPanel.LEFT_ALIGNMENT);
			mainpanel.add(subtitlefield);
			nestedsubmodelpane.setAlignmentX(JPanel.LEFT_ALIGNMENT);
			mainpanel.add(nestedsubmodelpane);
			mainpanel.add(new SemGenSeparator());
		}
		else{
			mainpanel.add(eqpane);
			mainpanel.add(new SemGenSeparator());
			
			compositelabel = new JLabel("Composite annotation");
			compositelabel.setFont(SemGenFont.defaultBold());
			compositelabel.setBorder(BorderFactory.createEmptyBorder(10, indent, 0, 0));
			
			mainpanel.add(compositelabel);
			
			mainpanel.add(Box.createGlue());
			mainpanel.add(compositepanel);
			mainpanel.add(Box.createGlue());
			mainpanel.add(new SemGenSeparator());
		}
		humandefpanel.setAlignmentX(JPanel.LEFT_ALIGNMENT);
		singularannpanel.setAlignmentX(JPanel.LEFT_ALIGNMENT);
		mainpanel.add(singularannlabel);
		mainpanel.add(singularannpanel);
		mainpanel.setBackground(SemGenGUI.lightblue);
		
		add(mainpanel, BorderLayout.NORTH);
		add(Box.createVerticalGlue(), BorderLayout.SOUTH);
		
		setVisible(true);
		ann.dialogscrollpane.scrollToLeft();
		this.validate();
		this.repaint();
	}

	public void FormatComponents(JPanel panel, JLabel label, int panelindent) {
		panel.setLayout(new BoxLayout(panel,BoxLayout.X_AXIS));
		panel.setBackground(SemGenGUI.lightblue);
		panel.setBorder(BorderFactory.createEmptyBorder(0, panelindent, 0, 15));
		panel.setAlignmentX(Component.LEFT_ALIGNMENT);
		label.setBorder(BorderFactory.createEmptyBorder(10, panelindent/2, 3, 15));
		label.setFont(SemGenFont.defaultBold());
	}

	public void FormatButton(JLabel label, String tooltip, Boolean enabled) {
		label.addMouseListener(this);
		label.setToolTipText(tooltip);
		label.setEnabled(enabled);
		label.setBorder(BorderFactory.createEmptyBorder(1,1,1,1));
	}

	public void refreshData() {
		// If a submodel, refresh the associated codewords
		if(thebutton instanceof SubmodelButton)
			refreshSubmodelData();
		// Otherwise we're looking at a codewordbutton - get the composite annotation
		else refreshCompositeAnnotation();
		refreshHumanReadableDefinition();
		refreshSingularAnnotation();
		validate();
		repaint();
	}
	
	public void refreshSubmodelData(){
		
		String subtitletext = "Click to assign codewords to this component";
		String editcomptext = "Click to assign sub-models";
		
		Submodel submod = semsimmodel.getSubmodel(thebutton.getName());
		
		if(thebutton.ssc instanceof FunctionalSubmodel){
			subtitletext = "No codewords associated with submodel";
			editcomptext = "No submodels associated with this submodel";
		}
		
		Set<DataStructure> associatedcodewords = submod.getAssociatedDataStructures();
		Set<Submodel> associatedcomponents = submod.getSubmodels();
		
		// Include the codewords that are in the subcomponents in the list of associated codewords
		ArrayList<DataStructure> listofdss = new ArrayList<DataStructure>();
		listofdss.addAll(associatedcodewords);
		cdwdsfromcomps = SemSimOWLFactory.getCodewordsAssociatedWithNestedSubmodels(submod);
		associatedcodewords.addAll(cdwdsfromcomps);
		if(!associatedcodewords.isEmpty())
			setSubmodelDataOnScreen(submod, subtitlefield, associatedcodewords, "Codewords");
		else{
			subtitlefield.setCustomText(subtitletext);
			subtitlefield.setForeground(Color.gray);
		}
		if(!associatedcomponents.isEmpty()){
			setSubmodelDataOnScreen(submod, nestedsubmodelpane, associatedcomponents, "Sub-components");
		}
		else{
			nestedsubmodelpane.setCustomText(editcomptext);
			nestedsubmodelpane.setForeground(Color.gray);
		}
		annotator.dialogscrollpane.scrollToLeft();
	}
	
	public void refreshCompositeAnnotation(){
		compositepanel.refreshUI();
	}
	
	public void refreshHumanReadableDefinition(){
		// Refresh the human readable definition
		String comment = "";
		if(smc.getDescription()!=null){
			comment = smc.getDescription();
		}
		// Get the human readable definition for the codeword
		if (!comment.equals("") && comment!=null) {
			humandefpane.setCustomText(comment);
			humandefpane.setForeground(Color.blue);
			// Refresh the indicator icons next to the codeword in the bottom left of the Annotator
			thebutton.annotationAdded(thebutton.humdeflabel, false);
			humapplyelsewherebutton.setEnabled(true);
			humremovebutton.setEnabled(thebutton.editable);
		} else {
			String msg = "Click to set free-text description";
			if(SemSimComponentIsImported(smc)) msg = "No free-text description specified";
			
			humandefpane.setCustomText(msg);
			humandefpane.setForeground(Color.gray);
			// Refresh the indicator icons next to the codeword in the bottom left of the Annotator
			thebutton.annotationNotAdded(thebutton.humdeflabel);
			humapplyelsewherebutton.setEnabled(false);
			humremovebutton.setEnabled(false);
		}
		annotator.updateTreeNode();
	}
	
	public void refreshSingularAnnotation(){
		// Get the singular annotation for the codeword
		singularannpanel = new SemSimComponentAnnotationPanel(this, (Annotatable)smc);
		singularannpanel.setBorder(BorderFactory.createEmptyBorder(0, indent+5, 0, 0));
		annotator.updateTreeNode();
	}
	
	public void setSubmodelDataOnScreen(Submodel sub, AnnotationDialogClickableTextPane pane, Set<? extends SemSimComponent> associatedsscs, String title){	
		if(thebutton.ssc instanceof FunctionalSubmodel) pane.setForeground(Color.black);
		else pane.setForeground(Color.blue);
		
		String text = "";
		if(!associatedsscs.isEmpty()){
			// Weed out null data structures and sub-models
			ArrayList<SemSimComponent> templist = new ArrayList<SemSimComponent>();
			for(SemSimComponent ssc : associatedsscs){
				if(ssc!=null) templist.add(ssc);
			}
			String[] sarray = new String[templist.size()];
			for(int y=0;y<sarray.length;y++){
				sarray[y] = templist.get(y).getName();
			}
			Arrays.sort(sarray, new CaseInsensitiveComparator());
			for(String s : sarray){
				String name = s;
				if(sub instanceof FunctionalSubmodel){ // Get rid of prepended submodel names if submodel is functional
					name = name.substring(name.lastIndexOf(".")+1);
				}
				if(cdwdsfromcomps.contains(semsimmodel.getDataStructure(s)))
					text = text + ", " + "{" + name + "}";
				else
					text = text + ", " + name;
			}
		}
		text = title + ": " + text.substring(2);
		pane.setCustomText(text);
	}
	
	

	public Boolean validateNewComponentName(String newname){
		if(!newname.equals("") && !annotator.submodelbuttontable.containsKey(newname) &&
			!annotator.codewordbuttontable.containsKey(newname) && !newname.contains("--"))
			return true;
		else return false;
	}
	
	
	public void updateCompositeAnnotationFromUIComponents() throws OWLException{
		annotator.setModelSaved(false);
		DataStructure ds = (DataStructure)smc;

		ArrayList<PhysicalModelComponent> pmclist = new ArrayList<PhysicalModelComponent>();
		ArrayList<StructuralRelation> structuralrellist = new ArrayList<StructuralRelation>();
		compositepanel.validate();
		
		for(int j=0; j<compositepanel.getComponentCount(); j++){
			if(compositepanel.getComponent(j) instanceof SemSimComponentAnnotationPanel){
				Annotatable smc = ((SemSimComponentAnnotationPanel)compositepanel.getComponent(j)).smc;
				if(smc instanceof PhysicalModelComponent) pmclist.add((PhysicalModelComponent)smc);
			}
			if(compositepanel.getComponent(j) instanceof StructuralRelationPanel)
				structuralrellist.add(((StructuralRelationPanel)compositepanel.getComponent(j)).structuralRelation);
		}
		
		// If one property and one property target (works for single entities and processes)
		if(pmclist.size()==2){
			ds.getPhysicalProperty().setPhysicalPropertyOf(pmclist.get(1));
		}
		else if(pmclist.size()>2){
			pmclist.remove(0);
			ArrayList<PhysicalEntity> entlist = new ArrayList<PhysicalEntity>();
			ListIterator<PhysicalModelComponent> iterator = pmclist.listIterator();
			while(iterator.hasNext()) entlist.add((PhysicalEntity)iterator.next());
			ds.getPhysicalProperty().setPhysicalPropertyOf(semsimmodel.addCompositePhysicalEntity(entlist, structuralrellist));
		}
		else if(pmclist.size()==1){
			ds.getPhysicalProperty().setPhysicalPropertyOf(null);
			// If there is only a property panel present
		}
		
		if(thebutton.refreshAllCodes()){
			if(SemGenGUI.annotateitemsortbytype.isSelected() && !SemGenGUI.annotateitemtreeview.isSelected()) // Not able to sort codewords by marker in tree view yet
				annotator.AlphabetizeAndSetCodewords();
			if(!SemGenGUI.annotateitemtreeview.isSelected())
				annotator.codewordscrollpane.scrollToComponent(thebutton);
		}
	}
	
	public void showSingularAnnotationEditor(){
		if (annotator.noncompeditor != null)
			annotator.noncompeditor.dispose();
		annotator.noncompeditor = new SingularAnnotationEditor(this, new Object[]{"Annotate","Close"});
	}	
	
	
	public boolean SemSimComponentIsImported(SemSimComponent comp){
		// If semsim component is imported, change message
		boolean imported = false;
		if(comp instanceof Importable){
			if(((Importable)comp).isImported()) imported = true;;
		}
		else if(comp instanceof DataStructure){
			if(((DataStructure)comp).isImportedViaSubmodel()) imported = true;
		}
		return imported;
	}

	
	public void mouseClicked(MouseEvent arg0) {
		if (arg0.getComponent() == humremovebutton) {
			if(thebutton.editable){
				smc.setDescription(null);
				refreshHumanReadableDefinition();
				humremovebutton.setEnabled(false);
				annotator.setModelSaved(false);
			}
		}
		
		if(arg0.getComponent()==codewordlabel && thebutton instanceof SubmodelButton){
			String newcompname = JOptionPane.showInputDialog(SemGenGUI.desktop, "Rename component", annotator.focusbutton.namelabel.getText());
			if(newcompname!=null && !newcompname.equals(codewordlabel.getText())){
				Boolean newnameapproved = validateNewComponentName(newcompname);
				while(!newnameapproved){
					JOptionPane.showMessageDialog(SemGenGUI.desktop, "That name is either invalid or already taken");
					newcompname = JOptionPane.showInputDialog(SemGenGUI.desktop, "Rename component", newcompname);
					newnameapproved = validateNewComponentName(newcompname);
				}
				thebutton.ssc.setName(newcompname);
				annotator.setModelSaved(false);
				annotator.submodelbuttontable.remove(thebutton.namelabel.getText());
				annotator.submodelbuttontable.put(newcompname, (SubmodelButton)thebutton);
				thebutton.setIdentifyingData(newcompname);
				annotator.changeButtonFocus(thebutton, thebutton, null);
				annotator.focusbutton = thebutton;
			}
		}
		// Actions for when user clicks on an AnnotationDialogTextArea
		if (arg0.getComponent() == subtitlefield) {
			if(sdfcoc!=null){sdfcoc.dispose();}
			Submodel sub = ((SubmodelButton)thebutton).sub;
			sdfcoc = new SelectorDialogForCodewordsOfSubmodel(
					this,
					semsimmodel.getDataStructures(), 
					null,
					sub, 
					sub.getAssociatedDataStructures(),
					SemSimOWLFactory.getCodewordsAssociatedWithNestedSubmodels(sub),
					false,
					"Select codewords");
		}
		
		if( arg0.getComponent() == nestedsubmodelpane){
			if(sdfcomp!=null){sdfcomp.dispose();}
			Submodel sub = (Submodel)thebutton.ssc;
			sdfcomp = new SelectorDialogForSubmodelsOfSubmodel(
					this,
					semsimmodel.getSubmodels(),
					sub,
					sub, 
					sub.getSubmodels(),
					null,
					true,
					"Select components");
		}
		
		if (arg0.getComponent() == this.humandefpane) {
			if (annotator.humdefeditor != null) {
				annotator.humdefeditor.dispose();
			}
			
			annotator.humdefeditor = new HumanDefEditor(smc, this, true);
			annotator.humdefeditor.setVisible(true);
		}
		
		if(arg0.getComponent() == copyannsbutton){
			int x = JOptionPane.showConfirmDialog(SemGenGUI.desktop, "Really copy annotations to mapped variables?", "Confirm", JOptionPane.YES_NO_OPTION);
			if(x==JOptionPane.YES_OPTION){
				MappableVariable thevar = (MappableVariable)smc;
				
				for(MappableVariable targetvar : AnnotationCopier.copyAllAnnotationsToMappedVariables(annotator, thevar)){
					annotator.codewordbuttontable.get(targetvar.getName()).refreshAllCodes();
				}
				
				// Update the codeword button markers, re-sort if needed
				if(SemGenGUI.annotateitemsortbytype.isSelected()){
					annotator.AlphabetizeAndSetCodewords();
					annotator.codewordscrollpane.scrollToComponent(thebutton);
				}
			}
		}
		
		// Activated if user selects the Annotator icon within the AnnotationDialog (used for imported submodels)
		if(arg0.getComponent() == loadsourcemodelbutton){
			File file = new File(annotator.sourcefile.getParent() + "/" + ((Submodel)smc).getHrefValue());

			if(file.exists()){
				NewAnnotatorTask task = new SemGenGUI.NewAnnotatorTask(new File[]{file}, false);
				task.execute();
			}
			else{JOptionPane.showMessageDialog(SemGenGUI.desktop, "Could not locate source file for this sub-model.", "ERROR", JOptionPane.ERROR_MESSAGE);}
		}
	}

	public void mouseEntered(MouseEvent e) {
		Component component = e.getComponent();
		if (component instanceof AnnotationDialogClickableTextPane) {
			// Paint underline on mouse entered
			AnnotationDialogClickableTextPane jtp = (AnnotationDialogClickableTextPane) component;
	        MutableAttributeSet attrs = jtp.getInputAttributes();
	        StyleConstants.setUnderline(attrs, true);
	        StyledDocument doc = jtp.getStyledDocument();
	        doc.setCharacterAttributes(0, doc.getLength() + 1, attrs, false);
			component.setCursor(new Cursor(Cursor.HAND_CURSOR));
		}
		if(component instanceof JLabel){
			((JLabel)component).setCursor(new Cursor(Cursor.HAND_CURSOR));
		}
		if(component==codewordlabel && thebutton instanceof SubmodelButton){
			codewordlabel.setForeground(Color.blue);
			codewordlabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
		}
	}

	public void mouseExited(MouseEvent e) {
		Component component = e.getComponent();
		if (component instanceof AnnotationDialogClickableTextPane) {
			// Remove underline on mouse exit
			AnnotationDialogClickableTextPane jtp = (AnnotationDialogClickableTextPane) component;
	        MutableAttributeSet attrs = jtp.getInputAttributes();
	        StyleConstants.setUnderline(attrs, false);
	        StyledDocument doc = jtp.getStyledDocument();
	        doc.setCharacterAttributes(0, doc.getLength() + 1, attrs, false);
			component.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		}
		if(component instanceof JLabel){
			((JLabel)component).setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		}
		if(component==codewordlabel && thebutton instanceof SubmodelButton){
			codewordlabel.setForeground(Color.black);
			codewordlabel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		}
	}

	public void mousePressed(MouseEvent arg0) {
		Component component = arg0.getComponent();
		if (component instanceof JLabel && component!=codewordlabel) {
			((JLabel)component).setBorder(BorderFactory.createLineBorder(Color.blue,1));
		}
	}
	public void mouseReleased(MouseEvent arg0) {
		Component component = arg0.getComponent();
		if (component instanceof JLabel && component!=codewordlabel) {
			((JLabel)component).setBorder(BorderFactory.createEmptyBorder(1,1,1,1));
		}
	}
}