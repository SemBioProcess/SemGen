package semgen.annotation;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import org.openjena.atlas.lib.Pair;

import semgen.SemGenSettings;
import semgen.annotation.workbench.AnnotatorWorkbench;
import semgen.annotation.workbench.ModelAnnotationsBench;
import semgen.utilities.SemGenFont;
import semgen.utilities.SemGenIcon;
import semgen.utilities.uicomponent.SemGenScrollPane;

public class ModelAnnotationsView extends SemGenScrollPane implements Observer {
	private static final long serialVersionUID = 1L;
	ModelAnnotationsBench modannbench;
	SemGenSettings settings;
	ArrayList<MetadataBox> metadataarray = new ArrayList<MetadataBox>();
	JPanel viewport = new JPanel();
	
	ModelAnnotationsView(AnnotatorWorkbench wb, SemGenSettings sets) {
		viewport.setLayout(new BoxLayout(viewport, BoxLayout.Y_AXIS));
		modannbench = wb.getModelAnnotationsWorkbench();
		settings = sets;
		
		String name = modannbench.getFullModelName();
		if (name.isEmpty()) name = wb.getCurrentModelName();
		setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createEtchedBorder(), name, 
				TitledBorder.LEFT, 
				TitledBorder.TOP, 
				SemGenFont.defaultBold(2)
				));
		createUI();
	}
	
	private void createUI() {
		viewport.setBackground(Color.white);
		drawList();
		setViewportView(viewport);	
	}
	
	private void drawList() {
		for (Pair<String, Boolean> pair : modannbench.getModelAnnotationFilledPairs()) {
			MetadatawithCheck box = new MetadatawithCheck(pair.getLeft(), pair.getRight());
			metadataarray.add(box);
			viewport.add(box, Component.LEFT_ALIGNMENT);
		}
		viewport.add(Box.createGlue());
		validate();
	}
	
	@Override
	public void update(Observable arg0, Object arg1) {

	}
       
	abstract class MetadataBox extends JPanel {
		private static final long serialVersionUID = 1L;
		MetadataBox() {
			super(new BorderLayout(0, 0));
			setBackground(Color.white);
			this.addMouseListener(new MouseMetadataAdapter(this));
		}
		
		class MouseMetadataAdapter extends MouseAdapter {
			JComponent target;
			MouseMetadataAdapter(JComponent targ) {
				target = targ;
			}
			
			public void mouseEntered(MouseEvent e) {
				target.setOpaque(true);
				target.setBackground(new Color(255,231,186));
				target.setCursor(new Cursor(Cursor.HAND_CURSOR));
			}

			public void mouseExited(MouseEvent e) {
				target.setOpaque(false);
				target.setBackground(null);
				target.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			}
		};
	};
	
	class MetadatawithCheck extends MetadataBox {
		private static final long serialVersionUID = 1L;
		JLabel indicator = new JLabel();
		
		MetadatawithCheck(String label, Boolean check) {
			setIndicator(check);

			JLabel text = new JLabel(label);
			text.setAlignmentX(LEFT_ALIGNMENT);
			add(Box.createGlue(), BorderLayout.WEST);
			add(text ,BorderLayout.CENTER);
			
			indicator.setSize(12, 12);
			add(indicator,BorderLayout.EAST);
			validate();
		}
		
		public void setIndicator(boolean checkmark) {
			if (checkmark) indicator.setIcon(SemGenIcon.onicon);
			else indicator.setIcon(SemGenIcon.officon);
		}
		
		
	};
}
