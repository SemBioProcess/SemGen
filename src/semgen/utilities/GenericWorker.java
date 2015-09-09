package semgen.utilities;


import javax.swing.JFrame;

import semgen.utilities.uicomponent.SemGenProgressBar;

public class GenericWorker extends SemGenTask {
	private SemGenJob task;
	
	public GenericWorker(SemGenJob alg) {
		task = alg;
		progframe = new SemGenProgressBar("Task Started", true);
	}

	public GenericWorker(SemGenJob alg, JFrame parent) {
		task = alg;
		progframe = new SemGenProgressBar("Task Started", true, parent);
	}
	
	@Override
	protected Void doInBackground() throws Exception {
		while (task.isValid()) {
			task.run();
			break;
		}
		if (!task.isValid()) {
			cancel(true);
		}
		return null;
	}

}
