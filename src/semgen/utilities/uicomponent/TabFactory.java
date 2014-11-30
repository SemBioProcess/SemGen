/** 
 * Abstract class for defining a tab factory. Extending classes are required to
 * specify a class extending workbench.
 * */

package semgen.utilities.uicomponent;

import semgen.GlobalActions;
import semgen.SemGenSettings;
import semgen.utilities.Workbench;

public abstract class TabFactory<T extends Workbench> {
	protected SemGenSettings settings;
	protected GlobalActions globalactions;
	
	public TabFactory(SemGenSettings sets, GlobalActions actions) {
		settings = sets; globalactions = actions;
	}
	
	public abstract SemGenTab makeTab(T workbench);

}
