package semgen.annotation;

import java.awt.Dimension;
import semgen.resource.SemGenFont;
import semsim.model.physical.Submodel;


public class SubmodelButton extends AnnotationObjectButton{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public Submodel sub;
	public SubmodelButton(AnnotatorTab ann, Submodel ssc, boolean compannfilled, String companntext, boolean noncompannfilled,
			boolean humdeffilled, boolean depfilled, boolean editable) {
		super(ann, ssc, compannfilled, companntext, noncompannfilled, humdeffilled, depfilled, editable);
		sub = (Submodel)ssc;
		compannlabel.setVisible(false);
		namelabel.setFont(SemGenFont.defaultBold());
		Dimension dim = new Dimension(40, 17);
		indicatorspanel.setPreferredSize(dim);
		indicatorspanel.setMinimumSize(dim);
		indicatorspanel.setMaximumSize(dim);
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
