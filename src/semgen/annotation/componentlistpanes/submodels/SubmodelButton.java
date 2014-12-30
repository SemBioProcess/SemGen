package semgen.annotation.componentlistpanes.submodels;

import java.awt.Dimension;

import semgen.SemGenSettings;
import semgen.annotation.AnnotatorTab;
import semgen.annotation.componentlistpanes.AnnotationObjectButton;
import semgen.utilities.SemGenFont;
import semsim.model.physical.Submodel;


public class SubmodelButton extends AnnotationObjectButton{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public Submodel sub;
	public SubmodelButton(AnnotatorTab ann, SemGenSettings sets, Submodel ssc, boolean noncompannfilled,
			boolean humdeffilled, boolean editable) {
		super(ann, sets, ssc, noncompannfilled, humdeffilled, editable);
		sub = ssc;
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
