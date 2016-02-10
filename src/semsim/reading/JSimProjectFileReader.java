package semsim.reading;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.stream.XMLStreamException;

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
	
	public JSimProjectFileReader(File file) {
		super(file);
		
		SAXBuilder builder = new SAXBuilder();
		
		try{ 
			doc = builder.build(srcfile);
			root = doc.getRootElement();
			projelement = root.getChild("project");
		}
		catch(JDOMException | IOException e) {
			e.printStackTrace();
		}
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
		Iterator<Element> modelit = projelement.getChildren("model").iterator();
		
		while(modelit.hasNext()){
			Element modelel = modelit.next();
			
			if(modelel.getAttributeValue("name").equals(modelname)){
				
				Iterator<Element> controlit = modelel.getChildren("control").iterator();
				
				while(controlit.hasNext()){
					Element controlel = controlit.next();
					
					if(controlel.getAttributeValue("name").equals("modelSource")){
						return controlel.getText();
					}
				}
			}
		}
		
		return null;
	}

	
	@Override
	public SemSimModel readFromFile() throws IOException, InterruptedException,
			OWLException, CloneNotSupportedException, XMLStreamException {
		// TODO Auto-generated method stub
		return null;
	}

}
