package semgen.resource;

import java.lang.reflect.Method;

public class GenericThread implements Runnable {
	private boolean running = false;
	private Object sourceobj;
	private String runningmethod;

	public GenericThread(Object sourceobj, String runningmethod) {
		this.sourceobj = sourceobj;
		this.runningmethod = runningmethod;
	}

	public void start() {
		if (!running) {
			running = true;
			Thread looper = new Thread(this);
			looper.start();
		}
	}

	public void run() {
		try {
			Method method = sourceobj.getClass().getDeclaredMethod(runningmethod,
					(Class[]) null);
			method.invoke(sourceobj, (Object[]) null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void stop(){
		running = false;
	}
}