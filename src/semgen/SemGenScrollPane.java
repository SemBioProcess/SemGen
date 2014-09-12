package semgen;

import java.awt.Component;
import javax.swing.JScrollPane;

public class SemGenScrollPane extends JScrollPane {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2703381146331909737L;

	public SemGenScrollPane(Component view) {
		super(view);
		this.getVerticalScrollBar().setUnitIncrement(12);
		this.getHorizontalScrollBar().setUnitIncrement(12);
	}

	public SemGenScrollPane() {
		this.getVerticalScrollBar().setUnitIncrement(12);
		this.getHorizontalScrollBar().setUnitIncrement(12);
	}
}
