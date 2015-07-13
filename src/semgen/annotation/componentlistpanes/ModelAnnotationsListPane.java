package semgen.annotation.componentlistpanes;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
import semgen.annotation.workbench.drawers.ModelAnnotationsBench;
import semgen.utilities.SemGenFont;
import semgen.utilities.SemGenIcon;
import semgen.utilities.uicomponent.SemGenScrollPane;

public class ModelAnnotationsListPane extends SemGenScrollPane implements Observer {
	private static final long serialVersionUID = 1L;
	ModelAnnotationsBench metadatabench;
	SemGenSettings settings;
	ArrayList<MetadataBox> metadataarray = new ArrayList<MetadataBox>();
	JPanel viewport = new JPanel();

	
	public ModelAnnotationsListPane(AnnotatorWorkbench wb, SemGenSettings sets) {
		viewport.setLayout(new BoxLayout(viewport, BoxLayout.Y_AXIS));
		metadatabench = wb.getModelAnnotationsWorkbench();
		wb.addObservertoModelAnnotator(this);
		settings = sets;
		
		String name = metadatabench.getFullModelName();
		if (name==null || name.isEmpty()) name = wb.getCurrentModelName();
		createBorder(name);
		createUI();
	}
	
	private void createBorder(String name) {
		
		setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createEtchedBorder(), name, 
				TitledBorder.LEFT, 
				TitledBorder.TOP, 
				SemGenFont.defaultBold(2)
				));
	}
	
	private void createUI() {
		viewport.setBackground(Color.white);
		drawList();
		setMaximumSize(new Dimension(360, (metadataarray.size()+2)*18));
		setViewportView(viewport);	
	}
	
	private void drawList() {
		
		for (Pair<String, Boolean> pair : metadatabench.getModelAnnotationFilledPairs()) {
			MetadatawithCheck box = new MetadatawithCheck(pair.getLeft(), pair.getRight());
			metadataarray.add(box);
			box.setMaximumSize(new Dimension(360, 12));
			viewport.add(box, Component.LEFT_ALIGNMENT);	
			viewport.add(Box.createGlue());
		}
		
		validate();
	}
	
	@Override
	public void update(Observable arg0, Object arg1) {
		if (arg1 == ModelAnnotationsBench.ModelChangeEnum.METADATACHANGED) {
			int index = metadatabench.getFocusIndex();
			if (index==0) createBorder(metadatabench.getFullModelName());
			metadataarray.get(index).setIndicator(metadatabench.focusHasValue());
		}
	}
     
	abstract class MetadataBox extends JPanel {
		private static final long serialVersionUID = 1L;
		MetadataBox() {
			super(new BorderLayout(0, 0));
			setBackground(Color.white);
			this.addMouseListener(new MouseMetadataAdapter(this));
		}
		
		public abstract void setIndicator(boolean status);
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
			if (checkmark) indicator.setIcon(SemGenIcon.checkmarkicon);
			else indicator.setIcon(SemGenIcon.eraseiconsmall);
		}
	};
	
	class MouseMetadataAdapter extends MouseAdapter {
		MetadataBox target;
			MouseMetadataAdapter(MetadataBox targ) {
				target = targ;
			}
			
			public void mouseClicked(MouseEvent e) {
				metadatabench.notifyOberserversofMetadataSelection(metadataarray.indexOf(target));
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
}
