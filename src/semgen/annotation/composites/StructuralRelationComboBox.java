package semgen.annotation.composites;

import java.awt.Dimension;
import java.awt.Font;

import javax.swing.JComboBox;

import semgen.SemGenGUI;
import semsim.model.annotation.StructuralRelation;

public class StructuralRelationComboBox extends JComboBox{
	/**
	 * 
	 */
	private static final long serialVersionUID = -4455216268075370509L;

	public StructuralRelationComboBox(StructuralRelation rel){
		this.addItem("contained_in");
		this.addItem("part_of");
		this.setSelectedItem(rel.getURI().getFragment());
		this.setFont(new Font("SansSerif", Font.ITALIC, SemGenGUI.defaultfontsize));
		this.setPreferredSize(new Dimension(135,30));
		this.setMaximumSize(new Dimension(135,30));
	}
}
