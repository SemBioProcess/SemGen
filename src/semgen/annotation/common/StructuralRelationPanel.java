package semgen.annotation.common;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;

import semgen.SemGenSettings;
import semgen.utilities.SemGenFont;

public class StructuralRelationPanel extends JPanel{
	private static final long serialVersionUID = 4319031091828000135L;
	private static final Dimension dims = new Dimension(135, 15);

	public StructuralRelationPanel(){
		setLayout(new BorderLayout());
		setMaximumSize(dims);
		JLabel partOf = new JLabel("part_of");
		partOf.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		partOf.setFont(SemGenFont.defaultItalic());
		partOf.setPreferredSize(dims);
		partOf.setMaximumSize(dims);
		setBackground(SemGenSettings.lightblue);
		
		add(partOf, BorderLayout.WEST);
		add(Box.createGlue(), BorderLayout.EAST);
	}
}
