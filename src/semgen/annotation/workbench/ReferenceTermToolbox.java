package semgen.annotation.workbench;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;

import semgen.annotation.dialog.referencedialog.AddReferenceClassDialog;
import semgen.annotation.dialog.textminer.TextMinerDialog;
import semsim.SemSimConstants;
import semsim.model.physical.CompositePhysicalEntity;
import semsim.model.physical.PhysicalProperty;
import semsim.model.physical.ReferencePhysicalEntity;
import semsim.model.physical.ReferencePhysicalProcess;

public class ReferenceTermToolbox implements PropertyChangeListener {
	private HashMap<String, ReferencePhysicalEntity> rpeset = new HashMap<String, ReferencePhysicalEntity>();
	private HashMap<String, ReferencePhysicalProcess> rppset = new HashMap<String, ReferencePhysicalProcess>();
	private HashMap<String, CompositePhysicalEntity> cpeset = new HashMap<String, CompositePhysicalEntity>();
	private HashMap<String, PhysicalProperty> ppset = new HashMap<String, PhysicalProperty>();
	
	public ReferenceTermToolbox() {}
	
	public ReferencePhysicalEntity getRefPhysEntityByName(String name) {
		return rpeset.get(name);
	}
	
	public ReferencePhysicalProcess getRefPhysProcessByName(String name) {
		return rppset.get(name);
	}
	
	public CompositePhysicalEntity getCompPhysEntityByName(String name) {
		return cpeset.get(name);
	}
	
	public PhysicalProperty getPhysPropByName(String name) {
		return ppset.get(name);
	}
	
	public void addRefComponent(ReferencePhysicalEntity rpe) {
		rpeset.put(rpe.getName(), rpe);
	}
	
	public void addRefComponent(ReferencePhysicalProcess rpp) {
		rppset.put(rpp.getName(), rpp);
	}
	
	public void addRefComponent(CompositePhysicalEntity cpe) {
		cpeset.put(cpe.getName(), cpe);
	}
	
	public void addRefComponent(PhysicalProperty pp) {
		ppset.put(pp.getName(), pp);
	}
	
	private ReferencePhysicalEntity removeRefPhysEntity(String name) {
		return rpeset.remove(name);
	}
	
	private ReferencePhysicalProcess removeRefPhysProcess(String name) {
		return rppset.remove(name);
	}
	
	private CompositePhysicalEntity removeCompPhysEntity(String name) {
		return cpeset.remove(name);
	}
	
	private PhysicalProperty removePhysProp(String name) {
		return ppset.remove(name);
	}
	
	public ArrayList<String> getRefPhysEntityAsStrings() {
		return new ArrayList<String>(rpeset.keySet());
	}
	
	public ArrayList<String> getRefPhysProcessesAsStrings() {
		return new ArrayList<String>(rppset.keySet());
	}
	
	public ArrayList<String> getCompPhysEntityAsStrings() {
		return new ArrayList<String>(cpeset.keySet());
	}
	
	public ArrayList<String> getRefPhysPropertiesAsStrings() {
		return new ArrayList<String>(ppset.keySet());
	}
	
	public ArrayList<String> getAllCompsAsStrings() {
		ArrayList<String> list = new ArrayList<String>();
		list.addAll(rpeset.keySet());
		list.addAll(rppset.keySet());
		list.addAll(cpeset.keySet());
		list.addAll(ppset.keySet());
		return list;
	}
	
	public void removeTermRequest() {
		
	}

	public void addTermRequest() {
		new AddReferenceClassDialog(SemSimConstants.ALL_SEARCHABLE_ONTOLOGIES, 
				new Object[]{"Add as entity","Add as process","Close"}).packAndSetModality();
	}
	
	public void propertyChange(PropertyChangeEvent arg0) {
		
	}

	public void runTextMiner() {
		try {
			new TextMinerDialog();
		} 
		catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
	}
	
}
