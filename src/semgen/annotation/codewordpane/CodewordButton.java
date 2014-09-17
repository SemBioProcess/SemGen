package semgen.annotation.codewordpane;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.JLabel;

import semgen.annotation.uicomponents.AnnotationObjectButton;
import semgen.resource.SemGenFont;
import semsim.SemSimConstants;
import semsim.model.computational.DataStructure;

public class CodewordButton extends AnnotationObjectButton {
	private static final long serialVersionUID = -7555259367118052593L;
	private JLabel compannlabel = new JLabel("_");
	public DataStructure ds;
	public PropertyMarker propoflabel = new PropertyMarker(Color.white, null);
	private Color dependencycolor = new Color(205, 92, 92, 255);
	private Color processgreen = new Color(50,205,50);

	public CodewordButton() {
		namelabel.setFont(SemGenFont.defaultItalic());
		
		compannlabel.setName("C");
		compannlabel.setToolTipText("Indicates status of codeword's composite annotation");
		lbls.add(compannlabel);
		makelabels();	
		
		indicatorssuperpanel.add(propoflabel, BorderLayout.EAST);
	}
	
	public void refreshAllCodes(Boolean[] isAnn){
		refreshFreeTextCode(isAnn[0]);
		refreshSingAnnCode(isAnn[1]);
		refreshCompositeAnnotationCode(isAnn[2]);
	}
	
	@Override
	public void assignButton(String name, Boolean[] list) {
		setIdentifyingData(name);
		refreshAllCodes(list);
	}
	
	public void refreshCompositeAnnotationCode(boolean isAnn){
		annotationAdded(compannlabel, isAnn);
	}
	
	public void togglePropertyofLabel(boolean vis) {
		propoflabel.setVisible(vis);
	}
	
	public boolean refreshPropertyOfMarker(){
		indicatorssuperpanel.remove(propoflabel);
		Color oldcolor = propoflabel.color;
		Color col = Color.white; 
		String tooltip = null;
		switch (ds.getPropertyType()) {
			case SemSimConstants.PROPERTY_OF_PHYSICAL_ENTITY:
				col = Color.black;
				tooltip = "<html>Codeword identified as a property of a physical <i>entity</i></html>";
				break;
			case SemSimConstants.PROPERTY_OF_PHYSICAL_PROCESS:
				col = processgreen;
				tooltip = "<html>Codeword identified as a property of a physical <i>process</i></html>";
				break;
			default:
				col = dependencycolor;
				tooltip = "<html>Codeword identified as a property of a <i>constitutive</i> relation.</htm>";
				break;
		}
		propoflabel = new PropertyMarker(col, tooltip);
		propoflabel.addMouseListener(new annBtnlMouseAdaptor());
		indicatorssuperpanel.add(propoflabel, BorderLayout.EAST);
		validate();
		
		return (oldcolor!=col);
		// Indicates to re-scroll to this button if true;
	}
	
	public String getCompositeAnnotationCodeForButton(){
		if(ds.getPhysicalProperty()!=null){
			if(ds.getPhysicalProperty().hasRefersToAnnotation()){
				if(ds.getPhysicalProperty().getPhysicalPropertyOf()!=null) return "P+X";
				else return "P+_";
			}
			else{
				if(ds.getPhysicalProperty().getPhysicalPropertyOf()!=null) return "_+X";
			}
		}
		return "_";
	}
}
