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
	
	public static enum ModelType{
		SEMSIM_MODEL, 
		SBML_MODEL, 
		CELLML_MODEL, 
		MML_MODEL, 
		MML_MODEL_IN_PROJ, 
		OMEX_ARCHIVE,
		CASA_FILE,
		UNKNOWN;
	}
	
	public static ModelType classify(ModelAccessor accessor) throws JDOMException, IOException{
		return classify(accessor.getModelwithBaseFile());
	}
	
	
	public static ModelType classify(File file) throws JDOMException, IOException {
		return classify(file.getPath());
	}
	
	/**
	 * Return the type of the model based on the file extension
	 * @throws IOException 
	 * @throws JDOMException 
	 **/
	public static ModelType classify(String file) throws JDOMException, IOException{
		ModelType type = ModelType.UNKNOWN;
				if (file.toLowerCase().endsWith(".omex")){
					type = ModelType.OMEX_ARCHIVE;
				}
				else if(file.toLowerCase().endsWith(".proj")){
					type = ModelType.MML_MODEL_IN_PROJ;
				}
				else if (file.toLowerCase().endsWith(".mod")){
					type = ModelType.MML_MODEL;
				}
				else if (file.toLowerCase().endsWith(".owl")) {
					type =  ModelType.SEMSIM_MODEL;
				}
				else if(file.toLowerCase().endsWith(".cellml")){
					type =  ModelType.CELLML_MODEL;
				}
				else if(file.toLowerCase().endsWith(".xml") || file.endsWith(".sbml")){
					type =  ModelType.SBML_MODEL;
				}
				else if(file.toLowerCase().endsWith(".rdf")){
					type = ModelType.CASA_FILE;
				}

		return type;
	}
	
	/**
	 * Verifies the the indicated document is well-formed SBML
	 * @param file to check
	 * @return
	 */
	
	public static Boolean isValidSBML(File file){
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
	
	/**
	 * Verifies the the indicated document is well-formed CellML
	 * @param file to check
	 * @return
	 */
	public static Boolean isValidCellMLmodel(File file) throws JDOMException, IOException{
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
	
	public static boolean hasValidOMEXmodelFileFormat(String format) {
		return format.matches(".*/sbml.*$") || format.endsWith("cellml");
	}
	
	public static boolean hasValidOMEXannotationFileFormat(String format) {
		return format.endsWith("rdf+xml");
	}
}
