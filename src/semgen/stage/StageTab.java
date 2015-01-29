package semgen.stage;

import java.util.Observer;

import semgen.GlobalActions;
import semgen.SemGenSettings;
import semgen.utilities.SemGenIcon;
import semgen.utilities.uicomponent.SemGenTab;

public class StageTab extends SemGenTab {

	public StageTab(SemGenSettings sets, GlobalActions globalacts, StageWorkbench bench) {
		super("Stage", SemGenIcon.annotatemodelicon, "Stage for facilitating SemGen tasks", sets, globalacts);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void loadTab() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isSaved() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void requestSave() {
		// TODO Auto-generated method stub

	}

	@Override
	public void requestSaveAs() {
		// TODO Auto-generated method stub

	}

	@Override
	public void addObservertoWorkbench(Observer obs) {
		// TODO Auto-generated method stub

	}

}
