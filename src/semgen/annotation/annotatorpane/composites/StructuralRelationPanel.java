package semgen.annotation.annotatorpane.composites;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;

import semgen.annotation.annotatorpane.AnnotationPanel;
import semgen.resource.SemGenFont;
import semsim.model.annotation.StructuralRelation;

public class StructuralRelationPanel extends JPanel{
	private static final long serialVersionUID = 4319031091828000135L;
	public StructuralRelation structuralRelation;

	public StructuralRelationPanel(StructuralRelation relation){
		structuralRelation = relation;
		setLayout(new BorderLayout());
		
		JLabel partOf = new JLabel("part_of");
		partOf.setFont(SemGenFont.defaultItalic());
		partOf.setPreferredSize(new Dimension(135,30));
		partOf.setMaximumSize(new Dimension(135,30));
		
		add(partOf, BorderLayout.WEST);
		add(Box.createGlue(), BorderLayout.EAST);
	}
}
