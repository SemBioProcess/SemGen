package semgen.extraction;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import semgen.SemGenSettings;
import semgen.utilities.SemGenIcon;
import semgen.utilities.uicomponent.SemGenTabToolbar;

public class ExtractorToolbar extends SemGenTabToolbar implements ActionListener {
	private static final long serialVersionUID = 1L;
	
	protected SemGenToolbarButton extractoritembatchcluster = new SemGenToolbarButton(SemGenIcon.extractoricon);
	protected SemGenToolbarButton extractoritemopenann = new SemGenToolbarButton(SemGenIcon.annotatoricon);
	
	public ExtractorToolbar(SemGenSettings sets) {
		super(sets);
		setMaximumSize(new Dimension(9999999,30));
		extractoritembatchcluster.setToolTipText("Performs clustering analysis on model");
		extractoritembatchcluster.addActionListener(this);

		extractoritemopenann.setToolTipText("Open model in Annotator");
		extractoritemopenann.addActionListener(this);
		
		add(extractoritembatchcluster);
		add(extractoritemopenann);
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
	}

}
