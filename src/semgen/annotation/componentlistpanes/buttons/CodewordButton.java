package semgen.annotation.componentlistpanes.buttons;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JLabel;

import semgen.annotation.workbench.drawers.CodewordToolDrawer.CodewordCompletion;
import semgen.utilities.SemGenFont;
import semsim.definitions.PropertyType;

public abstract class CodewordButton extends AnnotationObjectButton {
	private static final long serialVersionUID = -7555259367118052593L;
	protected JLabel singularannlabel = new JLabel("_");
	private JLabel compannlabel = new JLabel("_");
	private PropertyMarker propoflabel = new PropertyMarker(Color.white, null);
	
	private Color constitutivecolor = new Color(255, 127, 14, 255);
	private Color entitycolor = new Color(31, 119, 180);
	private Color processcolor = new Color(63, 196, 63);
	private Color forcecolor = Color.PINK;
	
	public CodewordButton(String name, boolean canedit, boolean showmarkers) {
		super(name, canedit);
		indicatorspanel.setPreferredSize(new Dimension(50, 18));
	
		makeIndicator(compannlabel, "C", "Indicates status of codeword's composite annotation");
		compannlabel.addMouseListener(new IndicatorMouseListener(compannlabel));
		
		makeIndicator(singularannlabel, "S", "Click to set singular reference annotation");
		singularannlabel.addMouseListener(new ClickableMouseListener(singularannlabel));
		
		if(editable) compannlabel.setForeground(editablelabelcolor);
		else compannlabel.setForeground(noneditablelabelcolor);
		
		drawButton();
		namelabel.setFont(SemGenFont.defaultItalic());
		propoflabel.addMouseListener(this);
		propoflabel.setVisible(showmarkers);
		indicatorssuperpanel.add(propoflabel, BorderLayout.EAST);
	}
	
	public void refreshCompositeAnnotationCode(CodewordCompletion cwc) {
		if (cwc!=CodewordCompletion.noAnnotations) {
			compannlabel.setFont(SemGenFont.Bold("Serif", -2));
		}
		else {
			compannlabel.setFont(SemGenFont.Plain("Serif", -2));
		}
		
		compannlabel.setText(cwc.getCode());
	}

	public void toggleSingleAnnotation(boolean hasdef) {
		toggleIndicator(singularannlabel, hasdef);
	}
	
	public void togglePropertyMarkers(boolean showmarkers) {
		propoflabel.setVisible(showmarkers);
	}
	
	public void refreshPropertyOfMarker(PropertyType ptype){
		Color col =  constitutivecolor;
		String tooltip;
		if(ptype == PropertyType.PropertyOfPhysicalEntity){
			col = entitycolor;
			tooltip = "<html>Codeword identified as a property of a physical <i>entity</i></html>";
		}
		else if(ptype == PropertyType.PropertyOfPhysicalProcess){
			col = processcolor;
			tooltip = "<html>Codeword identified as a property of a physical <i>process</i></html>";
		}
		else if(ptype == PropertyType.PropertyOfPhysicalForce){
			col = forcecolor;
			tooltip = "<html>Codeword identified as a property of a physical <i>force</i></html>";
		}
		else{
			tooltip = "<html>Codeword identified as a property of a <i>constitutive</i> relation.</htm>";
		}
		propoflabel.updateType(col, tooltip);
		indicatorssuperpanel.add(propoflabel, BorderLayout.EAST);
		validate();
	}
}
