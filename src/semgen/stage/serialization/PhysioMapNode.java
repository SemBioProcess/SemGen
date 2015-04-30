package semgen.stage.serialization;

import java.util.ArrayList;

import semgen.SemGen;
import semsim.model.computational.datastructures.DataStructure;
import semsim.model.physical.PhysicalEntity;
import semsim.model.physical.PhysicalProcess;

public class PhysioMapNode extends Node {
	
	public ArrayList<Object> sources;
	public ArrayList<Object> sinks;
	public ArrayList<Object> mediators;
	
	public String nodeType;

	public PhysioMapNode(DataStructure ds, String pmcName) {
		super(pmcName);
		
		sources = new ArrayList<Object>();
		sinks = new ArrayList<Object>();
		mediators = new ArrayList<Object>();
				
		this.nodeType = ds.getPropertyType(SemGen.semsimlib).toString();
				
		if(ds.getPhysicalProperty().getPhysicalPropertyOf() instanceof PhysicalProcess) {
			PhysicalProcess proc = (PhysicalProcess) ds.getPhysicalProperty().getPhysicalPropertyOf();
			for(PhysicalEntity ent : proc.getSourcePhysicalEntities()) {
				sources.add(ent.getName());
			}
			for(PhysicalEntity ent : proc.getSinkPhysicalEntities()) {
				sinks.add(ent.getName());
			}
			for(PhysicalEntity ent : proc.getMediatorPhysicalEntities()) {
				mediators.add(ent.getName());
			}
		}
	}
}
