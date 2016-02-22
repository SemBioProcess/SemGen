package semsim.reading;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

public class JSimProjectFileReader {

	public static final String semSimAnnotationControlValue = "SemSimAnnotation";
	
	public static Document getDocument(File file){
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
			List<Element> modellist = projelement.getChildren("model");
			
			for(Element modelel : modellist) 
				returnlist.add(modelel.getAttributeValue("name"));
		
		}
		return returnlist;
	}
	
	
	protected static String getModelCode(Document projdoc, String modelname){
		
		Element modelel = getModelElement(projdoc, modelname);
		Iterator<Element> controlit = modelel.getChildren("control").iterator();
		
		while(controlit.hasNext()){
			Element controlel = controlit.next();
			
			if(controlel.getAttributeValue("name").equals("modelSource")){
				return controlel.getText();
			}
		}
		
		return null;
	}
	

	public static Element getModelElement(Document projdoc, String modelname){
		Element projelement = getProjectElement(projdoc);
		Iterator<Element> modelit = projelement.getChildren("model").iterator();
		
		while(modelit.hasNext()){
			Element modelel = modelit.next();
			
			if(modelel.getAttributeValue("name").equals(modelname)){
				return modelel;
			}
		}
		return null;
	}
	
	
	public static Element getSemSimAnnotationControlElementForModel(Document projdoc, String modelname){
		Element modelel = getModelElement(projdoc, modelname);
		Iterator<Element> controlit = modelel.getChildren("control").iterator();
		
		while(controlit.hasNext()){
			Element controlel = controlit.next();
			
			if(controlel.getAttributeValue("name").equals(semSimAnnotationControlValue)){
				return controlel;
			}
		}
		
		// If we're here we need to create a new control element and attach it to the model element
		Element newel = new Element("control");
		newel.setAttribute(new Attribute("name", semSimAnnotationControlValue));
		modelel.addContent(newel);
		
		return newel;
	}
	
}
