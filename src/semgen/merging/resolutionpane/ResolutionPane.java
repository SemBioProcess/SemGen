package semgen.merging.resolutionpane;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import javax.swing.BoxLayout;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;

import org.apache.commons.lang3.tuple.Pair;

import semgen.merging.workbench.DataStructureDescriptor;
import semgen.merging.workbench.Merger.ResolutionChoice;
import semgen.merging.workbench.MergerWorkbench;
import semgen.merging.workbench.MergerWorkbench.MergeEvent;
import semgen.utilities.SemGenError;
import semgen.utilities.uicomponent.SemGenScrollPane;

public class ResolutionPane extends JPanel implements Observer {
	private static final long serialVersionUID = 1L;
	private MergerWorkbench workbench;
	private ArrayList<ResolutionPanel> resolvelist = new ArrayList<ResolutionPanel>();
	private int selection = -1;
	
	public ResolutionPane(MergerWorkbench bench) {
		workbench = bench;
		workbench.addObserver(this);
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setBackground(Color.white);
	}
	
	private void loadCodewordMap() {
		removeAll();
		if (!workbench.hasSemanticOverlap()) {
				SemGenError.showError("SemGen did not find any semantic equivalencies between the models", "Merger message");
				return;
		}
		
		for (int i = 1; i < workbench.getMappingCount(); i++) {
			addMapping(i);
		}
		
		validate();
		repaint();
	}
	
	private void addMapping(int index) {
		if (index!=0) add(new JSeparator());
		ResolutionPanel resbtn = new ResolutionPanel(workbench.getDSDescriptors(index).getLeft(), 
				workbench.getDSDescriptors(index).getRight(),
				workbench.getOverlapMapModelNames(), 
				workbench.getMapPairType(index));
		
		resbtn.addMouseListener(new ResPaneMouse(resbtn));
		resolvelist.add(resbtn);
		add(resbtn);
	}

	@Override
	public void update(Observable arg0, Object arg1) {
		if (arg1 == MergeEvent.mappingadded) {
			addMapping(workbench.getMappingCount()-1);
			validate();
			repaint();
		}
		if (arg1 == MergeEvent.mapfocuschanged) {
			loadCodewordMap();
		}
	}
	
	public ArrayList<ResolutionChoice>  pollResolutionList() {
		ArrayList<ResolutionChoice> choicelist = new ArrayList<ResolutionChoice>();
		for (ResolutionPanel panel : resolvelist) {
			if (panel.getSelection().equals(ResolutionChoice.noselection)) return null;
			choicelist.add(panel.getSelection());
		}
		return choicelist;
	}
	
	private void showInformation(Pair<DataStructureDescriptor,DataStructureDescriptor> dstarg) {
		ResolutionInformationTextPane ritp = new ResolutionInformationTextPane(
					dstarg.getLeft(), dstarg.getRight(), workbench.getOverlapMapModelNames());
			
			SemGenScrollPane scroller = new SemGenScrollPane(ritp);
			scroller.setPreferredSize(new Dimension(600, 600));
			scroller.getVerticalScrollBar().setUnitIncrement(12);
			JOptionPane.showMessageDialog(this, scroller, "Information about resolution step", JOptionPane.PLAIN_MESSAGE);
	}
	
	class ResPaneMouse extends MouseAdapter {
		ResolutionPanel mousee;
		ResPaneMouse(ResolutionPanel target) {
			mousee = target;
		}
		
		public void mouseClicked(MouseEvent e) {
			selection = resolvelist.indexOf(mousee);
			showInformation(workbench.getDSDescriptors(selection+1));
		}
	}
	
}
