package semsim.reading;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.XMLOutputter;

import semsim.SemSimLibrary;
import semsim.fileaccessors.JSimProjectAccessor;
import semsim.model.collection.SemSimModel;

/**
 * Class for creating SemSim model representations of the models in a JSim Project (.proj) file. 
 * @author mneal
 *
 */
public class JSimProjectFileReader {
	
	/**
	 * This method collects all annotations for an MML model using the RDF block 
	 * associated with it in its parent project file. The method returns whether  
	 * the model already contains SemSim annotations.
	 * @param semsimmodel The SemSim model to annotate, if annotations already
	 * exist in the JSim model
	 * @param ma Location of the JSim model
	 * @param sslib A SemSimLibrary instance
	 * @return Whether the JSim model already contains SemSim annotations.
	 */
	public static boolean getModelPreviouslyAnnotated(SemSimModel semsimmodel, JSimProjectAccessor ma,SemSimLibrary sslib){

			Document projdoc = ma.getJDOMDocument();
			Element ssael = getSemSimControlElementForModel(projdoc, ma.getModelName());
			
			// If there are no semsim annotations associated with the model, return false
			if(ssael != null){
				//Collect the annotations
				XMLOutputter xmloutputter = new XMLOutputter();
				
				// TODO: Move getRDFmarkup fxn somewhere else?
				Element rdfel = CellMLreader.getFirstRDFchildInElement(ssael);
				
				if(rdfel != null){
					SemSimRDFreader rdfreader = new SemSimRDFreader(ma, semsimmodel, xmloutputter.outputString(rdfel), sslib);
					
					rdfreader.getModelLevelAnnotations();
					rdfreader.getAllDataStructureAnnotations();
					rdfreader.getAllSemSimSubmodelAnnotations();
				}
				
				return true;
			}
		return false;
	}
	
	
	/**
	 * 
	 * @param projdoc A JDOM Document representing the JSim project file
	 * @return The "project" Element within the project file
	 */
	private static Element getProjectElement(Document projdoc){
		Element root = projdoc.getRootElement();
		return root.getChild("project");
	}
	
	
	/**
	 * @param projdoc JDOM Document representing a JSim project file
	 * @return The names of all models in the project file
	 */
	public static ArrayList<String> getNamesOfModelsInProject(Document projdoc){
		
		ArrayList<String> returnlist = new ArrayList<String>();
		Element projelement = getProjectElement(projdoc);
								
		if(projelement != null){
			@SuppressWarnings("unchecked")
			List<Element> modellist = projelement.getChildren("model");
			
			for(Element modelel : modellist) 
				returnlist.add(modelel.getAttributeValue("name"));
		
		}
		return returnlist;
	}
	
	
	/**
	 * Get the source code for a model in a JSim project file as an InputStream
	 * @param projdoc JDOM Document representing the project file
	 * @param modelname Name of the model in the project file
	 * @return Source code for the model as an InputStream
	 * @throws UnsupportedEncodingException
	 */
	public static InputStream getModelSourceCode(Document projdoc, String modelname) throws UnsupportedEncodingException{
		Element sourcecodeelemnt = getModelSourceCodeElement(projdoc, modelname);
		if(sourcecodeelemnt != null) {
			String modeltext = sourcecodeelemnt.getText();
			return new ByteArrayInputStream(modeltext.getBytes(StandardCharsets.UTF_8.name()));
		}
			
		return null;
	}
	
	
	/**
	 * Gett he JDOM Element containing the source code for a model in a JSim project file
	 * @param projdoc JDOM Document representing the project file
	 * @param modelname The name of a model in the project file
	 * @return JDOM Element that contains the source code for the given model within 
	 * the given project Document
	 */
	public static Element getModelSourceCodeElement(Document projdoc, String modelname){
		Element modelel = getModelElement(projdoc, modelname);
		@SuppressWarnings("unchecked")
		Iterator<Element> controlit = modelel.getChildren("control").iterator();
		
		while(controlit.hasNext()){
			Element controlel = controlit.next();
			
			if(controlel.getAttributeValue("name").equals("modelSource")){
				return controlel;
			}
		}
		
		return null;
	}
	

	/**
	 * Get the JDOM Element representing a given model in a project file
	 * @param projdoc JDOM Document representation of the project file
	 * @param modelname The name of the model in the project file
	 * @return The JDOM Element representing the given model within the 
	 * project file
	 */
	public static Element getModelElement(Document projdoc, String modelname){
		Element projelement = getProjectElement(projdoc);
		@SuppressWarnings("unchecked")
		Iterator<Element> modelit = projelement.getChildren("model").iterator();
		
		while(modelit.hasNext()){
			Element modelel = modelit.next();
			
			if(modelel.getAttributeValue("name").equals(modelname)){
				return modelel;
			}
		}
		return null;
	}
	
	
	/**
	 * Get the JDOM Element representing SemSim annotations for a model within a 
	 * project file
	 * @param projdoc JDOM Document of the project file
	 * @param modelname The name of the model to retrieve annotations for
	 * @return JDOM Element containing the SemSim annotations on the model
	 */
	public static Element getSemSimControlElementForModel(Document projdoc, String modelname){
		Element modelel = getModelElement(projdoc, modelname);
		@SuppressWarnings("unchecked")
		Iterator<Element> controlit = modelel.getChildren("control").iterator();
		
		while(controlit.hasNext()){
			Element controlel = controlit.next();
			
			if(controlel.getAttributeValue("name").equals(SemSimLibrary.SEMSIM_IN_JSIM_CONTROL_VALUE)){
				return controlel;
			}
		}
		
		return null;
	}
	
}
