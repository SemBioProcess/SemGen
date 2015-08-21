package semgen.stage.janet;



public class waitOnKeyStrokes{

	public static boolean autoComplete(String searchString) {
		boolean callQuery = false;
		// TODO Auto-generated method stub
		int lengthSearchString = searchString.length();
		
		if(lengthSearchString >3){ //atleast 3 alphabets in string
			callQuery = true;
			try {
			    Thread.sleep(0);                 //1000 milliseconds is one second.
			} catch(InterruptedException ex) {
			    Thread.currentThread().interrupt();
			}
		}
		
		return callQuery; 
	}
	
}
	