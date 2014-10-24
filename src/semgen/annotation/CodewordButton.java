package semgen.annotation;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.*;

import semgen.SemGenGUI;
import semgen.annotation.composites.PropertyMarker;
import semsim.SemSimConstants;
import semsim.model.computational.DataStructure;

public class CodewordButton extends AnnotationObjectButton implements MouseListener {
	private static final long serialVersionUID = -7555259367118052593L;
	
	public enum cwCompletion {noAnnotations, hasPhysProp, hasPhysEnt, hasAll}
	public DataStructure ds;

	public CodewordButton(Annotator ann, DataStructure ssc, boolean compannfilled,
			String companntext, boolean noncompannfilled, boolean humdeffilled,
			boolean depfilled, boolean editable) {
		super(ann, ssc, compannfilled, companntext, noncompannfilled, humdeffilled, depfilled, editable);
		ds = (DataStructure)ssc;
		namelabel.setFont(new Font("SansSerif", Font.ITALIC, SemGenGUI.defaultfontsize));
		refreshAllCodes();
	}
	
	public boolean refreshAllCodes(){
		refreshCompositeAnnotationCode();
		refreshSingularAnnotationCode();
		refreshFreeTextCode();
		annotater.updateTreeNode();
		return refreshPropertyOfMarker();
	}
	
	public void refreshCompositeAnnotationCode() {
		String companncode = setCompositeAnnotationCodeforButton();
		compannlabel.setText(companncode);
		if(companncode.equals("_")) annotationNotAdded(compannlabel);
		else annotationAdded(compannlabel, true);
	}
	
	
	public boolean refreshPropertyOfMarker(){
		indicatorssuperpanel.remove(propoflabel);
		Color oldcolor = propoflabel.color;
		Color col = Color.white; 
		String tooltip = null;
		int type = SemGenGUI.getPropertyType(ds);
		if(type == SemSimConstants.PROPERTY_OF_PHYSICAL_ENTITY){
			col = Color.black;
			tooltip = "<html>Codeword identified as a property of a physical <i>entity</i></html>";
		}
		else if(type == SemSimConstants.PROPERTY_OF_PHYSICAL_PROCESS){
			col = SemGenGUI.processgreen;
			tooltip = "<html>Codeword identified as a property of a physical <i>process</i></html>";
		}
		else{
			col = SemGenGUI.dependencycolor;
			tooltip = "<html>Codeword identified as a property of a <i>constitutive</i> relation.</htm>";
		}
		propoflabel = new PropertyMarker(col, tooltip);
		propoflabel.addMouseListener(this);
		indicatorssuperpanel.add(propoflabel, BorderLayout.EAST);
		validate();
		propoflabel.setVisible(SemGenGUI.annotateitemshowmarkers.isSelected());
		
		if(oldcolor!=col && SemGenGUI.annotateitemsortbytype.isSelected()){
			return true;  // Indicates to re-scroll to this button
		}
		return false;  // Indicates not to re-scroll to this button
	}
	
	//Generates the codes in the codeword panel (northwest panel) indicating whether a 
	//codeword has Physical Property and/or Physical Entity annotations
	private String setCompositeAnnotationCodeforButton() {
		switch (getCompositeAnnotationCodeForButton()) {
		case hasPhysProp:
			return "P+_";
		case hasPhysEnt:
			return "_+X";
		case hasAll:
			return "P+X";
		default:
			return "_";
		}
	}
	
	public cwCompletion getCompositeAnnotationCodeForButton(){
		if(ds.getPhysicalProperty()!=null){
			if(ds.getPhysicalProperty().hasRefersToAnnotation()){
				if(ds.getPhysicalProperty().getPhysicalPropertyOf()!=null) 
					return cwCompletion.hasAll;//Property with Physical Entity
				else return cwCompletion.hasPhysProp; //Property only
			}
			else{
				if(ds.getPhysicalProperty().getPhysicalPropertyOf()!=null) 
					return cwCompletion.hasPhysEnt; //Physical Entity Only
			}
		}
		return cwCompletion.noAnnotations;
	}
}
