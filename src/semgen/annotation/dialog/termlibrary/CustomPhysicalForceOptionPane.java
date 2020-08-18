package semgen.annotation.dialog.termlibrary;

import java.awt.event.ActionEvent;
import java.util.ArrayList;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import semgen.annotation.common.CustomTermOptionPane;
import semsim.annotation.SemSimTermLibrary;

/**
 * CustomTermOptionPane subclass for specifying the sources and sinks 
 * for a physical energy differential
 * @author Christopher Thompson
 *
 */
public class CustomPhysicalForceOptionPane extends CustomTermOptionPane implements TableModelListener{

	private static final long serialVersionUID = 1L;
	private ArrayList<ParticipantEditor> editors;

	public CustomPhysicalForceOptionPane(SemSimTermLibrary lib, int forceindex) {
		super(lib, forceindex);
	}

	@Override
	protected void makeUnique() {
		editors = new ArrayList<ParticipantEditor>();
		editors.add(new ParticipantEditor("Source Participants", library, this));
		editors.add(new ParticipantEditor("Sink Participants", library, this));
		
		setParticipantTableData();
		
		this.namepanel.setVisible(false);
		this.descriptionpanel.setVisible(false);
	}
	
	
	@Override
	public String getTitle() {
		return "Specify sources and sinks for energy differential";
	}
	
	
	protected void setParticipantTableData() {
		ArrayList<Integer> tempsrc = new ArrayList<Integer>();
		ArrayList<Integer> tempsink = new ArrayList<Integer>();

		for(Integer sourceint : library.getIndicesOfEnergyDiffSources(termindex)){
			tempsrc.add(sourceint);
		}
		for(Integer sinkint : library.getIndicesOfEnergyDiffSinks(termindex)){
			tempsink.add(sinkint);
		}
		editors.get(0).setTableData(tempsrc); 
		editors.get(1).setTableData(tempsink);
		
		for (ParticipantEditor editor : editors) {
			editor.addTableModelListener(this);
			add(editor);
		}
	}
	
	
	@Override
	protected void finishPanel() {
		createbtn.setEnabled(true);
		createbtn.setText("Modify");
		createbtn.addActionListener(this);
		cancelbtn.addActionListener(this);

		confirmpan.setLayout(new BoxLayout(confirmpan, BoxLayout.X_AXIS));
		confirmpan.setAlignmentY(Box.TOP_ALIGNMENT);
		confirmpan.add(createbtn);
		confirmpan.add(cancelbtn);
		
		add(confirmpan);
		add(Box.createVerticalGlue());
		validate();
	}
	
	
	@Override
	protected Integer createTerm() {
		return null;
	}
	
	
	@Override
	protected void modifyTerm() {
		library.editEnergyDifferential(termindex);
		setEnergyDifferentialParticipants();
	}
	
	private void setEnergyDifferentialParticipants() {
		library.setEnergyDifferentialSources(termindex, editors.get(0).getParticipants());
		library.setEnergyDifferentialSinks(termindex, editors.get(1).getParticipants());
	}
	
	
	
	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void tableChanged(TableModelEvent arg0) {
		// TODO Auto-generated method stub
		
	}

}
