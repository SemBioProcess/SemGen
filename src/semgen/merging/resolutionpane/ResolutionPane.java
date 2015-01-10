package semgen.merging.resolutionpane;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

import semgen.merging.ResolutionPanel;
import semgen.merging.workbench.DataStructureDescriptor;
import semgen.merging.workbench.MergerWorkbench;
import semgen.merging.workbench.MergerWorkbench.MergeEvent;
import semgen.merging.workbench.ModelOverlapMap.ResolutionChoice;
import semgen.utilities.SemGenError;
import semgen.utilities.uicomponent.SemGenScrollPane;

public class ResolutionPane extends JPanel implements Observer, ActionListener {
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
		
		for (int i = 0; i < workbench.getMappingCount(); i++) {
			addMapping(i);
			add(new JSeparator());
		}
		
		validate();
		repaint();
	}
	
	private void addMapping(int index) {
		if (index!=0) add(new JSeparator());
		ResolutionPanel resbtn = new ResolutionPanel(this, workbench.getDSDescriptors(index).getLeft(), 
				workbench.getDSDescriptors(index).getRight(),
				workbench.getOverlapMapModelNames(), 
				workbench.getMapPairType(index));
		
		resbtn.addMouseListener(new ResPaneMouse(resbtn));
		add(resbtn);
	}
	
	public void actionPerformed(ActionEvent arg0) {
		Pair<DataStructureDescriptor,DataStructureDescriptor> dstarg = workbench.getDSDescriptors(selection);
		ResolutionInformationTextPane ritp = new ResolutionInformationTextPane(
				dstarg.getLeft(), dstarg.getRight(), workbench.getOverlapMapModelNames());
		
		SemGenScrollPane scroller = new SemGenScrollPane(ritp);
		scroller.setPreferredSize(new Dimension(600, 600));
		scroller.getVerticalScrollBar().setUnitIncrement(12);
		JOptionPane.showMessageDialog(this, scroller, "Information about resolution step", JOptionPane.PLAIN_MESSAGE);
	}
	
	@Override
	public void update(Observable arg0, Object arg1) {
		if (arg1 == MergeEvent.mappingadded) {
			addMapping(workbench.getMappingCount());
			validate();
			repaint();
		}
		if (arg1 == MergeEvent.mapfocuschanged) {
			loadCodewordMap();
		}
	}
	
	public boolean pollResolutionList() {
		ArrayList<ResolutionChoice> choicelist = new ArrayList<ResolutionChoice>();
		for (ResolutionPanel panel : resolvelist) {
			if (panel.getSelection().equals(ResolutionChoice.noselection)) return false;
			choicelist.add(panel.getSelection());
		}
		workbench.applyCodewordSelections(choicelist);
		return true;
	}
	
	class ResPaneMouse extends MouseAdapter {
		ResolutionPanel mousee;
		ResPaneMouse(ResolutionPanel target) {
			mousee = target;
		}
		
		public void mouseClicked(MouseEvent e) {
			selection = resolvelist.indexOf(mousee);
		}
	}
	
}
