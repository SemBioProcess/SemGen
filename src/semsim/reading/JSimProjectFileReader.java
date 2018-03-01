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
import semsim.fileaccessors.JSIMProjectAccessor;
import semsim.model.collection.SemSimModel;

public class JSimProjectFileReader {
	
	// This method collects all annotations for an MML model using the RDF block
	// associated with it in its parent project file. The method returns whether 
	// the model has already been annotated to some degree.
	public static boolean getModelPreviouslyAnnotated(SemSimModel semsimmodel, JSIMProjectAccessor ma,SemSimLibrary sslib){

			Document projdoc = ma.getJDOMDocument();
			Element ssael = getSemSimControlElementForModel(projdoc, ma.getModelName());
			
			// If there are no semsim annotations associated with the model, return false
			if(ssael != null){
				//Collect the annotations
				XMLOutputter xmloutputter = new XMLOutputter();
				
				// TODO: Move getRDFmarkup fxn somewhere else?
				Element rdfel = CellMLreader.getRDFmarkupForElement(ssael);
				
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
	
	private static Element getProjectElement(Document projdoc){
		Element root = projdoc.getRootElement();
		return root.getChild("project");
	}
	
	
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
	
	
	public static InputStream getModelSourceCode(Document projdoc, String modelname) throws UnsupportedEncodingException{
		Element sourcecodeelemnt = getModelSourceCodeElement(projdoc, modelname);
		if(sourcecodeelemnt != null) {
			String modeltext = sourcecodeelemnt.getText();
			return new ByteArrayInputStream(modeltext.getBytes(StandardCharsets.UTF_8.name()));
		}
			
		return null;
	}
	
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
	
	
	public static Element getSemSimControlElementForModel(Document projdoc, String modelname){
		Element modelel = getModelElement(projdoc, modelname);
		@SuppressWarnings("unchecked")
		Iterator<Element> controlit = modelel.getChildren("control").iterator();
		
		while(controlit.hasNext()){
			Element controlel = controlit.next();
			
			if(controlel.getAttributeValue("name").equals(SemSimLibrary.SemSimInJSimControlValue)){
				return controlel;
			}
		}
		
		return null;
	}
	
}
