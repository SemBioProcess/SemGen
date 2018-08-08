package semgen.annotation.dialog.termlibrary;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import semgen.annotation.common.CustomTermOptionPane;
import semgen.annotation.workbench.SemSimTermLibrary;

public class CustomPhysicalForceOptionPane extends CustomTermOptionPane implements TableModelListener{

	private static final long serialVersionUID = 1L;
	private ArrayList<ParticipantEditor> editors;

//	public CustomPhysicalForceOptionPane(SemSimTermLibrary lib) {
//		super(lib);
//	}
	
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
		return "Specify sources and sinks for force";
	}
	
	
	protected void setParticipantTableData() {
		LinkedHashMap<Integer,Double> tempsrc = new LinkedHashMap<Integer,Double>();
		LinkedHashMap<Integer,Double> tempsink = new LinkedHashMap<Integer,Double>();

		for(Integer sourceint : library.getIndiciesOfForceSources(termindex)){
			tempsrc.put(sourceint, Double.valueOf(-1));
		}
		for(Integer sinkint : library.getIndiciesOfForceSinks(termindex)){
			tempsink.put(sinkint, Double.valueOf(-1));
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
		System.out.println("Modifying existing force");
		library.editForce(termindex);
		setForceParticipants();
	}
	
	private void setForceParticipants() {
		System.out.println("Setting participants");
		library.setForceSources(termindex, editors.get(0).getParticipants());
		library.setForceSinks(termindex, editors.get(1).getParticipants());
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
