package semgen;

import java.lang.reflect.Method;

public class GenericThread implements Runnable {
	public boolean running;
	public Thread looper;
	public int value;
	public Object sourceobj;
	public String runningmethod;
	public Method method;

	public GenericThread(Object sourceobj, String runningmethod) {
		this.sourceobj = sourceobj;
		this.runningmethod = runningmethod;
		running = false;
	}

	public void start() {
		if (!running) {
			running = true;
			looper = new Thread(this);
			looper.start();
		}
	}

	public void run() {
		try {
			method = sourceobj.getClass().getDeclaredMethod(runningmethod,
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