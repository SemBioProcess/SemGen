package semgen.annotation.componentlistpanes.codewords;

import java.awt.BorderLayout;
import java.awt.Color;

import semgen.SemGen;
import semgen.SemGenSettings;
import semgen.annotation.AnnotatorTab;
import semgen.annotation.annotatorpane.composites.PropertyMarker;
import semgen.annotation.componentlistpanes.AnnotationObjectButton;
import semgen.utilities.SemGenFont;
import semsim.SemSimConstants;
import semsim.model.computational.datastructures.DataStructure;

public class CodewordButton extends AnnotationObjectButton {
	private static final long serialVersionUID = -7555259367118052593L;
	
	public enum cwCompletion {noAnnotations, hasPhysProp, hasPhysEnt, hasAll}
	public Color dependencycolor = new Color(205, 92, 92, 255);
	public Color entityblack = Color.black;
	public Color processgreen = new Color(50,205,50);
	public DataStructure ds;
	
	public CodewordButton(AnnotatorTab ann, SemGenSettings sets, DataStructure ssc, boolean compannfilled,
			String companntext, boolean noncompannfilled, boolean humdeffilled,
			boolean editable) {
		super(ann, sets, ssc, noncompannfilled, humdeffilled, editable);
		ds = ssc;
		
		if (compannfilled) {
			annotationAdded(compannlabel, true);
			compannlabel.setText(companntext);
		}
		
		else {annotationNotAdded(compannlabel);}
		namelabel.setFont(SemGenFont.defaultItalic());
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
		int type = ds.getPropertyType(SemGen.semsimlib);
		if(type == SemSimConstants.PROPERTY_OF_PHYSICAL_ENTITY){
			col = Color.black;
			tooltip = "<html>Codeword identified as a property of a physical <i>entity</i></html>";
		}
		else if(type == SemSimConstants.PROPERTY_OF_PHYSICAL_PROCESS){
			col = processgreen;
			tooltip = "<html>Codeword identified as a property of a physical <i>process</i></html>";
		}
		else{
			col = dependencycolor;
			tooltip = "<html>Codeword identified as a property of a <i>constitutive</i> relation.</htm>";
		}
		propoflabel = new PropertyMarker(col, tooltip);
		propoflabel.addMouseListener(new AOBMouseListener(propoflabel));
		indicatorssuperpanel.add(propoflabel, BorderLayout.EAST);
		validate();
		propoflabel.setVisible(settings.useDisplayMarkers());
		
		// Indicate whether to re-scroll to this button
		return (oldcolor!=col && settings.organizeByPropertyType()); 
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
