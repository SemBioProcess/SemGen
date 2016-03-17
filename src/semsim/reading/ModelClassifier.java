package semsim.reading;

import java.io.File;
import java.io.IOException;

import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.JDOMParseException;
import org.jdom.input.SAXBuilder;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLReader;

public class ModelClassifier {
	
	public static final int SEMSIM_MODEL = 0;
	public static final int SBML_MODEL = 1;
	public static final int CELLML_MODEL = 2;
	public static final int MML_MODEL = 3;
	public static final int MML_MODEL_IN_PROJ = 4;
	
	
	public static int classify(File file){
		return classify(new ModelAccessor(file));
	}
	
	public static int classify(ModelAccessor accessor){
		int type = -1;
		try{
			
			if(accessor.modelIsPartOfArchive()){
				
				if(accessor.modelIsPartOfJSimProjectFile()){
					type = MML_MODEL_IN_PROJ;
				}
			}
			else{
				File file = accessor.getFileThatContainsModel();
				if (file.toString().toLowerCase().endsWith(".mod")){
					type = MML_MODEL;
				}
				else if (file.toString().endsWith(".owl")) {
					type =  SEMSIM_MODEL;
				}
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
		}
		catch(JDOMException | IOException e) {
			e.printStackTrace();
		} 
		return type;
	}
	
	private static Boolean isValidSBML(File file){
		System.out.println("Testing SBML validity");
		try{
			SBMLDocument sbmldoc = SBMLReader.read(file);
			if (sbmldoc.getNumErrors()>0){
			      System.err.println(file.getName() + " is not valid SBML");
			      sbmldoc.printErrors(System.err);
			}
			else {
				return true;
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return false;
	}
	
	
	private static Boolean isCellMLmodel(File file) throws JDOMException, IOException{
		SAXBuilder builder = new SAXBuilder();
		try{
			Document doc = builder.build(file);
			String nsuri = doc.getRootElement().getNamespace().getURI();
			if(nsuri.contains("http://www.cellml.org/cellml/")){
				return true;
			}
		}
		catch(JDOMParseException e){
			e.printStackTrace();
		}
		return false;
	}
}
