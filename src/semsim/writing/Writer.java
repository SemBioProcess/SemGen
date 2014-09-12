package semsim.writing;

import semsim.model.SemSimModel;

public interface Writer {
	public String writeToString(SemSimModel model);
}
