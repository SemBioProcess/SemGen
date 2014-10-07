package semsim.reading;

import java.io.File;
import java.io.IOException;

import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.JDOMParseException;
import org.jdom.input.SAXBuilder;
import org.sbml.libsbml.SBMLDocument;
import org.sbml.libsbml.SBMLReader;

public class ModelClassifier {
	
	public static final int SEMSIM_MODEL = 0;
	public static final int SBML_MODEL = 1;
	public static final int CELLML_MODEL = 2;
	public static final int MML_MODEL = 3;
	
	public static int classify(File file){
		int type = -1;
		try{
			// get the model's language (SBML, CellML, MML, etc.)
			if (file.toString().toLowerCase().endsWith(".mod")){
				type = MML_MODEL;
			}
			else if (file.toString().endsWith(".owl")) type =  SEMSIM_MODEL;
			else if(isCellMLmodel(file)){
				type =  CELLML_MODEL;
			}
			else if(isValidSBML(file)){
				type =  SBML_MODEL;
			}
			else if(file.toString().toLowerCase().endsWith(".xml")){
				if(isValidSBML(file)) type =  SBML_MODEL;
				else
					if(isCellMLmodel(file)) type =  CELLML_MODEL;
			}
			
		}
		catch(Exception e) {
			e.printStackTrace();
		} 
		return type;
	}

	
	private static Boolean isValidSBML(File file){
		System.out.println("Testing SBML validity");
		try{
			SBMLDocument sbmldoc = new SBMLReader().readSBMLFromFile(file.getAbsolutePath());
			if (sbmldoc.getNumErrors()>0){
			      System.err.println(file.getName() + " is not valid SBML");
			      sbmldoc.printErrors();
			      return false;
			}
			else {
				return true;
			}
		}
		catch(Exception e){e.printStackTrace();}
		return false;
	}
	
	
	private static Boolean isCellMLmodel(File file) throws JDOMException, IOException{
		SAXBuilder builder = new SAXBuilder();
		try{
			Document doc = builder.build(file);
			String nsuri = doc.getRootElement().getNamespace().getURI();
			if(nsuri.contains("http://www.cellml.org/cellml/")){return true;}
		}
		catch(JDOMParseException e){
			e.printStackTrace();
		}
		return false;
	}
}
