package semgen.annotation.common;

public interface AnnotationListener {
	public void onSearch();
	
	public void onCreateCustom();
	
	public void onWebLookup();
	
	public void onEditCustom();
	
	public void onSelection();
	
	public void onRemove();
}
