package semgen.annotation;


import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

import semgen.SemGenGUI;
import semsim.model.physical.Submodel;


public class SubmodelButton extends AnnotationObjectButton{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public Submodel sub;
	public SubmodelButton(Annotator ann, Submodel ssc, boolean compannfilled, String companntext, boolean noncompannfilled,
			boolean humdeffilled, boolean depfilled, boolean editable) {
		super(ann, ssc, compannfilled, companntext, noncompannfilled, humdeffilled, depfilled, editable);
		sub = (Submodel)ssc;
		compannlabel.setVisible(false);
		namelabel.setFont(new Font("SansSerif", Font.BOLD, SemGenGUI.defaultfontsize));
		indicatorspanel.setPreferredSize(new Dimension(40, 17));
		indicatorspanel.setMinimumSize(new Dimension(40, 17));
		indicatorspanel.setMaximumSize(new Dimension(40, 17));
		indicatorspanel.repaint();
		refreshAllCodes();
	}
	
	public boolean refreshAllCodes(){
		refreshSingularAnnotationCode();
		refreshFreeTextCode();
		annotater.updateTreeNode();
		return true;
	}
}
