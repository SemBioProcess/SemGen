package semgen.resource;

import java.io.File;
import java.util.Observable;

public abstract class Workbench extends Observable{
	public abstract void initialize();
	
	public boolean getModelSaved() {
		return false;
	}
	
	public abstract void setModelSaved(boolean val);
	
	public abstract String getCurrentModelName();
	
	public abstract String getModelSourceFile();
	
	public abstract File saveModel();
	
	public abstract File saveModelAs();
}
