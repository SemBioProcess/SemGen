package semsim.reading;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.jdom.Attribute;
import org.jdom.Content;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.semanticweb.owlapi.model.OWLException;

import semsim.model.collection.SemSimModel;

public class JSimProjectFileReader extends ModelReader{

	private Document doc;
	private Element root;
	private Element projelement;
	public static final String semSimAnnotationControlValue = "SemSimAnnotation";
	
	public JSimProjectFileReader(File file) {
		super(file);
		
		SAXBuilder builder = new SAXBuilder();
		
		try{ 
			doc = builder.build(file);
			root = doc.getRootElement();
			projelement = root.getChild("project");
		}
		catch(JDOMException | IOException e) {
			e.printStackTrace();
		}
	}
	
	public Document getDocument(){
		return doc;
	}
	
	public ArrayList<String> getNamesOfModelsInProject(){
		
		ArrayList<String> returnlist = new ArrayList<String>();
								
		if(projelement != null){
			List<Element> modellist = projelement.getChildren("model");
			
			for(Element modelel : modellist) 
				returnlist.add(modelel.getAttributeValue("name"));
		
		}
		return returnlist;
	}
	
	
	public String getModelCode(String modelname){
		
		Element modelel = getModelElement(modelname);
		Iterator<Element> controlit = modelel.getChildren("control").iterator();
		
		while(controlit.hasNext()){
			Element controlel = controlit.next();
			
			if(controlel.getAttributeValue("name").equals("modelSource")){
				return controlel.getText();
			}
		}
		
		return null;
	}
	

	public Element getModelElement(String modelname){
		Iterator<Element> modelit = projelement.getChildren("model").iterator();
		
		while(modelit.hasNext()){
			Element modelel = modelit.next();
			
			if(modelel.getAttributeValue("name").equals(modelname)){
				return modelel;
			}
		}
		return null;
	}
	
	
	public Element getSemSimAnnotationsControlElementForModel(String modelname){
		Element modelel = getModelElement(modelname);
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

	
	@Override
	public SemSimModel read() throws IOException, InterruptedException,
			OWLException, CloneNotSupportedException, XMLStreamException {
		return null;
	}

}
