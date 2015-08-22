package semgen.stage.janet;

import java.io.FileNotFoundException;

import semgen.stage.serialization.SearchResultSet;

class callJanetThread implements Runnable {
    private int c;
    private SearchResultSet[] resultSets;
    private String searchString;

    public callJanetThread(int a, int b) {
        c = a + b;
    }

    
    public callJanetThread(SearchResultSet[] resultSets, String searchString) {
    	 resultSets =janet_main.processSearch(resultSets,searchString);
    }
    
    
    
    public void run() {
        System.out.println(c);
    }

}


 
 
/*
	public static SearchResultSet[] threadWorks(SearchResultSet[] resultSets, String searchString) {
	Thread t1 = new Thread(new Runnable() {
		     public void run() {

		    	// PMR results here
				 SearchResultSet[] resultSets = null;
				 resultSets =janet_main.processSearch(resultSets,searchString);
		    	
		     }
		     public SearchResultSet[] getresultSets() {
		         return resultSets;
		     }

		});  
		t1.start();
		
		callJanetThread foo = new callJanetThread();
		new Thread(foo).start();
		
		
		return null; 
*/