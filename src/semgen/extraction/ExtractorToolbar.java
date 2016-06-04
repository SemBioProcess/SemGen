package semgen.extraction;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;

import semgen.SemGenSettings;
import semgen.utilities.SemGenIcon;
import semgen.utilities.uicomponent.SemGenTabToolbar;

public class ExtractorToolbar extends SemGenTabToolbar implements ActionListener {
	private static final long serialVersionUID = 1L;
	
	protected SemGenToolbarButton extractoritembatchcluster = new SemGenToolbarButton(SemGenIcon.clusteranalysisicon);
	protected SemGenToolbarButton extractoritemopenann = new SemGenToolbarButton(SemGenIcon.annotatoricon);
	protected JCheckBox includeMappedFromCheckBox = new JCheckBox("Include mappings");

	public ExtractorToolbar(SemGenSettings sets) {
		super(sets);
		setMaximumSize(new Dimension(9999999,30));
		extractoritembatchcluster.setToolTipText("Performs clustering analysis on model");
		extractoritembatchcluster.addActionListener(this);

		extractoritemopenann.setToolTipText("Open model in Annotator");
		extractoritemopenann.addActionListener(this);
		
		includeMappedFromCheckBox.setBorder(BorderFactory.createEmptyBorder());
		includeMappedFromCheckBox.setSelected(false);
		
		add(extractoritembatchcluster);
		add(extractoritemopenann);
		add(includeMappedFromCheckBox);
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
	}

}
