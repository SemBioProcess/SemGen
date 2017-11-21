package semsim.reading;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;

import semsim.SemSimLibrary;
import semsim.model.collection.SemSimModel;
import semsim.reading.ModelClassifier.ModelType;

public class JSimProjectFileReader {
	
	// This method collects all annotations for an MML model using the RDF block
	// associated with it in its parent project file. The method returns whether 
	// the model has already been annotated to some degree.
	public static boolean getModelPreviouslyAnnotated(SemSimModel semsimmodel, ModelAccessor ma,SemSimLibrary sslib){
		
		if(ma.modelIsPartOfJSimProjectFile()){
			
			Document projdoc = ModelReader.getJDOMdocumentFromFile(ma.getFile());
			Element ssael = getSemSimControlElementForModel(projdoc, ma.getFileName());
			
			// If there are no semsim annotations associated with the model, return false
			if(ssael == null){
				return false;
			}
			// Otherwise collect the annotations
			else{
				XMLOutputter xmloutputter = new XMLOutputter();
				
				// TODO: Move getRDFmarkup fxn somewhere else?
				Element rdfel = CellMLreader.getRDFmarkupForElement(ssael);
				
				SemSimRDFreader rdfreader = new SemSimRDFreader(ma, semsimmodel, xmloutputter.outputString(rdfel), ModelType.MML_MODEL_IN_PROJ, sslib);
				
				rdfreader.getModelLevelAnnotations();
				rdfreader.getAllDataStructureAnnotations();
				rdfreader.getAllSemSimSubmodelAnnotations();
				
				return true;
			}
		}
		return false;
	}
	
	public static Document getDocument(InputStream file){
		Document doc = null;
		SAXBuilder builder = new SAXBuilder();
		
		try{ 
			doc = builder.build(file);
		}
		catch(JDOMException | IOException e) {
			e.printStackTrace();
		}
		return doc;
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
	
	
	protected static InputStream getModelSourceCode(Document projdoc, String modelname) throws UnsupportedEncodingException{
		
		if(getModelSourceCodeElement(projdoc, modelname) != null) {
			String modeltext = getModelSourceCodeElement(projdoc, modelname).getText();
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
