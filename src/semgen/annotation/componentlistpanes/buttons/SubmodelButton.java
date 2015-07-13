package semgen.annotation.componentlistpanes.buttons;

import java.awt.Dimension;
import java.awt.event.ActionListener;

import semgen.utilities.SemGenFont;


public abstract class SubmodelButton extends AnnotationObjectButton implements ActionListener {
	private static final long serialVersionUID = 1L;
	

	public SubmodelButton(String title, boolean editable) {
		super(title, editable);
		namelabel.setFont(SemGenFont.defaultBold());
		Dimension dim = new Dimension(40, 17);
		indicatorspanel.setPreferredSize(dim);
		indicatorspanel.setMinimumSize(dim);
		indicatorspanel.setMaximumSize(dim);
		
		drawButton();
	}

}
