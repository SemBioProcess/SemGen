package semgen.resource;

import java.lang.reflect.Method;

public class GenericThread implements Runnable {
	public boolean running;
	public Object sourceobj;
	public String runningmethod;

	public GenericThread(Object sourceobj, String runningmethod) {
		this.sourceobj = sourceobj;
		this.runningmethod = runningmethod;
		running = false;
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