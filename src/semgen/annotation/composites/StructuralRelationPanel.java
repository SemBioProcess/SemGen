package semgen.annotation.composites;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.semanticweb.owlapi.model.OWLException;

import semgen.SemGenGUI;
import semgen.annotation.AnnotationDialog;
import semsim.model.annotation.StructuralRelation;

public class StructuralRelationPanel extends JPanel{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4319031091828000135L;
	public AnnotationDialog anndialog;
	public StructuralRelation structuralRelation;


	public StructuralRelationPanel(AnnotationDialog anndialog, StructuralRelation relation){
		this.anndialog = anndialog;
		structuralRelation = relation;
		setLayout(new BorderLayout());
		
		JLabel partOf = new JLabel("part_of");
		partOf.setFont(new Font("SansSerif", Font.ITALIC, SemGenGUI.defaultfontsize));
		partOf.setPreferredSize(new Dimension(135,30));
		partOf.setMaximumSize(new Dimension(135,30));
		
		add(partOf, BorderLayout.WEST);
		add(Box.createGlue(), BorderLayout.EAST);
	}
}
