package semgen.annotation.annotatorpane.composites;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;

import semgen.utilities.SemGenFont;
import semsim.annotation.StructuralRelation;

public class StructuralRelationPanel extends JPanel{
	private static final long serialVersionUID = 4319031091828000135L;
	public StructuralRelation structuralRelation;
	private static final Dimension dims = new Dimension(135, 15);

	public StructuralRelationPanel(StructuralRelation relation){
		structuralRelation = relation;
		setLayout(new BorderLayout());
		
		JLabel partOf = new JLabel("part_of");
		partOf.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

		partOf.setFont(SemGenFont.defaultItalic());
		partOf.setPreferredSize(dims);
		partOf.setMaximumSize(dims);
		
		add(partOf, BorderLayout.WEST);
		add(Box.createGlue(), BorderLayout.EAST);
	}
}
