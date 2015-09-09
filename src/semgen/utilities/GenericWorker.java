package semgen.utilities;

public class GenericWorker extends SemGenTask {
	private SemGenJob task;
	
	public GenericWorker(SemGenJob alg) {
		task = alg;
	}

	@Override
	protected Void doInBackground() throws Exception {
		task.run();
		cancel(!task.isValid());
		return null;
	}

}
