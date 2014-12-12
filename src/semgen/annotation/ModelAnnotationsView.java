package semgen.annotation;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import semgen.SemGenSettings;
import semgen.annotation.workbench.AnnotatorWorkbench;
import semgen.annotation.workbench.ModelAnnotationsBench;
import semgen.utilities.SemGenFont;
import semgen.utilities.SemGenIcon;

public class ModelAnnotationsView extends JPanel implements Observer {
	private static final long serialVersionUID = 1L;
	ModelAnnotationsBench modannbench;
	SemGenSettings settings;
	ArrayList<MetadataBox> metadataarray = new ArrayList<MetadataBox>();
	
	ModelAnnotationsView(AnnotatorWorkbench wb, SemGenSettings sets) {
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		modannbench = wb.getModelAnnotationsWorkbench();
		settings = sets;
		JLabel title = new JLabel(wb.getCurrentModelName());
		title.setFont(SemGenFont.defaultBold(2));
		add(title);
	}
	
	public void drawList() {
		
		//metadataarray.add(new MetadatawithCheck("Description", modannbench));
	}
	
	@Override
	public void update(Observable arg0, Object arg1) {

	}
       
	abstract class MetadataBox extends JPanel {
		private static final long serialVersionUID = 1L;
		
	};
	
	class MetadatawithCheck extends MetadataBox {
		private static final long serialVersionUID = 1L;
		JLabel indicator = new JLabel();
		
		MetadatawithCheck(String label, boolean check) {
			add(new JLabel(label),BorderLayout.WEST);
			indicator.setSize(12, 12);
			add(indicator,BorderLayout.WEST);
		}
		
		public void setIndicator(boolean checkmark) {
			if (checkmark) indicator.setIcon(SemGenIcon.onicon);
			else indicator.setIcon(SemGenIcon.officon);
		}
	};
}
