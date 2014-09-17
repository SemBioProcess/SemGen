package semgen.annotation.submodelpane;

import java.awt.Dimension;

import semgen.annotation.uicomponents.AnnotationObjectButton;
import semgen.resource.SemGenFont;

public class SubmodelButton extends AnnotationObjectButton{
	private static final long serialVersionUID = 1L;
	
	public SubmodelButton() {
		namelabel.setFont(SemGenFont.defaultBold());
		indicatorspanel.setPreferredSize(new Dimension(40, 17));
		indicatorspanel.repaint();
		makelabels();
	}
	
	@Override
	public void assignButton(String name, Boolean[] list) {
		setIdentifyingData(name);
		refreshAllCodes(list);
	}

	@Override
	public void refreshAllCodes(Boolean[] isAnn) {
		refreshFreeTextCode(isAnn[0]);
		refreshSingAnnCode(isAnn[1]);
	}
}
