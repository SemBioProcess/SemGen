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

	/**
	 * 
	 */
	private static final long serialVersionUID = -7555259367118052593L;
	
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
	
	private String setCompositeAnnotationCodeforButton() {
		switch (getCompositeAnnotationCodeForButton()) {
		case 1:
			return "P+_";
		case 2:
			return "_+X";
		case 3:
			return "P+X";
		default:
			return "_";
		}
	}
	
	public int getCompositeAnnotationCodeForButton(){
		if(ds.getPhysicalProperty()!=null){
			if(ds.getPhysicalProperty().hasRefersToAnnotation()){
				if(ds.getPhysicalProperty().getPhysicalPropertyOf()!=null) 
					return 3;//Property with Physical Entity
				else return 1; //Property only
			}
			else{
				if(ds.getPhysicalProperty().getPhysicalPropertyOf()!=null) 
					return 2; //Physical Entity Only
			}
		}
		return 0;
	}
}
