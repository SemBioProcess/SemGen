package semgen.resource;

import java.io.File;

public interface Workbench {
	public void loadModel(boolean autoannotate);
	
	public boolean getModelSaved();
	
	public void setModelSaved(boolean val);
	
	public String getCurrentModelName();
	
	public String getModelSourceFile();
	
	public File saveModel();
	
	public File saveModelAs();
}
