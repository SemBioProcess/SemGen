package semgen.annotation;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import org.openjena.atlas.lib.Pair;

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
		setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createEtchedBorder(), wb.getCurrentModelName(), 
				TitledBorder.LEFT, 
				TitledBorder.TOP, 
				SemGenFont.defaultBold(2)
				));
		
		drawList();
	}
	
	public void drawList() {
		for (Pair<String, Boolean> pair : modannbench.getModelAnnotationFilledPairs()) {
			MetadatawithCheck box = new MetadatawithCheck(pair.getLeft(), pair.getRight());
			metadataarray.add(box);
			add(box, Component.LEFT_ALIGNMENT);
		}
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
		
		MetadatawithCheck(String label, Boolean check) {
			JLabel text = new JLabel(label);
			text.setAlignmentX(LEFT_ALIGNMENT);
			add(Box.createGlue(), BorderLayout.EAST);
			add(text ,BorderLayout.CENTER);
			setIndicator(check);
			indicator.setSize(12, 12);
			add(indicator,BorderLayout.WEST);
			validate();
		}
		
		public void setIndicator(boolean checkmark) {
			if (checkmark) indicator.setIcon(SemGenIcon.onicon);
			else indicator.setIcon(SemGenIcon.officon);
		}
	};
}
