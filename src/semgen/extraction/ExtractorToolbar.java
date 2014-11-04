package semgen.extraction;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import semgen.SemGenSettings;
import semgen.SemGenGUI.NewAnnotatorTask;
import semgen.resource.SemGenIcon;
import semgen.resource.uicomponent.SemGenTabToolbar;

public class ExtractorToolbar extends SemGenTabToolbar implements ActionListener {
	private static final long serialVersionUID = 1L;
	
	private SemGenToolbarButton extractoritembatchcluster = new SemGenToolbarButton(SemGenIcon.extractoricon);
	private SemGenToolbarButton extractoritemopenann = new SemGenToolbarButton(SemGenIcon.annotatoricon);
	private ExtractorTab extractor;
	
	public ExtractorToolbar(SemGenSettings sets, ExtractorTab tab) {
		super(sets);
		extractor = tab;
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
		Object o = arg0.getSource();
		if (o == extractoritembatchcluster) {
				try {
					extractor.batchCluster();
				} catch (IOException e1) {e1.printStackTrace();}
		}

		if (o == extractoritemopenann) {
				try {
					NewAnnotatorTask task = new NewAnnotatorTask(extractor.sourcefile, false);
					task.execute();
				} catch (Exception e1) {e1.printStackTrace();} 
			}	
	}

}
