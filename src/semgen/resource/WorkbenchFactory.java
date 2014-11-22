package semgen.resource;

import java.io.File;

public abstract class WorkbenchFactory<T extends Workbench> implements Runnable {
	protected File sourcefile;
	protected boolean cont = true;
	protected String status;
	protected T workbench;
	
	public WorkbenchFactory() {}

	protected abstract boolean makeWorkbench();
	
	public T getWorkbench() {
		return workbench;
	}
	
	public void run() {
		makeWorkbench();
	}
	
	protected void updateStatus(String state) {
		status = state;
	}
	
	public String getStatus() {
		return status;
	}
	
	public boolean isValid() {
		return cont;
	}
	protected void abort() {
		cont = false;
	}
}
