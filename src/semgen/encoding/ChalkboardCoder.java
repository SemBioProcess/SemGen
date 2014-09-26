package semgen.encoding;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
<<<<<<< HEAD
import java.util.HashSet;
import java.util.Hashtable;
=======
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
import java.util.Set;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.semanticweb.owlapi.model.OWLException;

import semgen.SemGenGUI;
import semsim.SemSimConstants;
import semsim.model.SemSimModel;
<<<<<<< HEAD
=======
import semsim.model.computational.DataStructure;
import semsim.model.physical.CompositePhysicalEntity;
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
import semsim.model.physical.CustomPhysicalEntity;
import semsim.model.physical.CustomPhysicalProcess;
import semsim.model.physical.PhysicalEntity;
import semsim.model.physical.PhysicalProcess;
import semsim.model.physical.PhysicalProperty;
import semsim.owl.SemSimOWLFactory;

public class ChalkboardCoder {
<<<<<<< HEAD
	public static Hashtable<String,CustomPhysicalEntity> nounnamesandphysents;
	private static int x;
	public static Set<Element> nouncollection = new HashSet<Element>();

	public static void translate(SemSimModel semsimmodel, File outputfile) throws JDOMException, IOException, OWLException {		
=======

	public static Set<PhysicalEntity> physicalents;
	public static Hashtable<String,CustomPhysicalEntity> nounnamesandphysents;
	public static int x;
	public static Set<String> allrateprops;
	public static Set<String> allstateprops;
	public static Map<String,CompositePhysicalEntity> nounnamesandcompositeanns = new HashMap<String, CompositePhysicalEntity>();
	public static Set<Element> nouncollection;

	
	public static void translate(SemSimModel semsimmodel, File outputfile) throws JDOMException, IOException, OWLException {
		
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
		// Create top of CB xml file
		Element root = new Element("BioDModel");
		root.setAttribute("BioDMLversion", "0.02");
		root.setAttribute("AuthorApp", "Chalkboard");
		root.setAttribute("ModelName", semsimmodel.getName());
		
		Document doc = new Document(root);
		
		Element nounlist = new Element("nounList");
		Element verblist = new Element("verbList");
<<<<<<< HEAD
		
		root.addContent(nounlist);
		root.addContent(verblist);

=======
		//Element socketlist = new Element("socketList");
		
		root.addContent(nounlist);
		root.addContent(verblist);
		//root.addContent(socketlist);
		
		nouncollection = new HashSet<Element>();
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
		Set<Element> verbcollection = new HashSet<Element>();
		
		Set<CustomPhysicalProcess> setofverbobjects = new HashSet<CustomPhysicalProcess>();
		
		nounnamesandphysents = new Hashtable<String,CustomPhysicalEntity>();
		
		x = 1;
		
<<<<<<< HEAD
		Set<String> allflowrates = SemSimOWLFactory.getAllSubclasses(SemGenGUI.semsimlib.OPB, SemSimConstants.OPB_NAMESPACE + "OPB_00573", false);

=======
		physicalents = semsimmodel.getPhysicalEntities();
		Set<String> allflowrates = SemSimOWLFactory.getAllSubclasses(SemGenGUI.OPB, SemSimConstants.OPB_NAMESPACE + "OPB_00573", false);
		allrateprops = SemSimOWLFactory.getAllSubclasses(SemGenGUI.OPB, SemSimConstants.OPB_NAMESPACE + "OPB00093", false);
		allstateprops = SemSimOWLFactory.getAllSubclasses(SemGenGUI.OPB, SemSimConstants.OPB_NAMESPACE + "OPB_00569", false);
		
		Float count = (float) 1;
		Float frac;
		
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
		/* For each data structure
		if has a property that is a flow, get entity-flow physiomap
		else
			if associated with an entity, use the entity as the node
			else use the computational component
			
			collect its inputs and what it's an input for
			for inputs
				see if physical entity associated with input
					if yes, assert connection between input physical entity and the current node
					else assert connection between input data structure and the current node
			for what its an input for
				see if physical entity associated with what its an input for
					if yes, assert connection between the antecedent physical entity and the node
					else assert connection between what its an input for and the current node
		*/
		
		
		for(PhysicalProperty prop : semsimmodel.getPhysicalProperties()){
			if(prop.hasRefersToAnnotation()){
				if(allflowrates.contains(prop.getFirstRefersToReferenceOntologyAnnotation().getReferenceURI().toString())){  // If we have terms annotated against OPB flow rates
<<<<<<< HEAD
=======
					DataStructure datastructure = prop.getAssociatedDataStructure();
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
					PhysicalProcess proc = (PhysicalProcess) prop.getPhysicalPropertyOf();
					String processname = "Process " + Integer.toString(x);
					
					// Get the inputs to the flow term
					String verbID = Integer.toString(x);
					String desc = null;
					if(prop.getPhysicalPropertyOf().hasRefersToAnnotation()){
						desc = prop.getPhysicalPropertyOf().getFirstRefersToReferenceOntologyAnnotation().getValueDescription();
					}
					else desc = processname;
					CustomPhysicalProcess verbobject = new CustomPhysicalProcess(processname, desc);
					verbobject.ID = Integer.toString(x);
					setofverbobjects.add(verbobject);
					x++;
					
					// Associate the source physical entities with the process
					for(PhysicalEntity input : proc.getSourcePhysicalEntities()){
						CustomPhysicalEntity noun = collectNounData(input, verbID);
						if(noun!=null && !verbobject.setofinputs.contains(noun)){
							verbobject.setofinputs.add(noun);
						}
					}
					// Get the sinks
					for(PhysicalEntity output : proc.getSinkPhysicalEntities()){
						CustomPhysicalEntity noun = collectNounData(output, verbID);
						
						// Add the noun to the set of outputs
						if(noun!=null && !verbobject.setofoutputs.contains(noun)){
							verbobject.setofoutputs.add(noun);
						}
					}
					
					// Get the mediators
					for(PhysicalEntity mediator : proc.getMediatorPhysicalEntities()){
						CustomPhysicalEntity noun = collectNounData(mediator, verbID);
						
						// Add the noun to the set of outputs
						if(noun!=null && !verbobject.setofmediators.contains(noun)){
							verbobject.setofmediators.add(noun);
						}
					}
				}
			}
		}
<<<<<<< HEAD
				
=======
		
		// Remove redundant physical entity annotations from the role players list
		// When add inputs and outputs, test to see if the noun added should be
//		for(String nm : nounnamesandphysents.keySet()){
//			CustomPhysicalEntity pe = nounnamesandphysents.get(nm);
//			PhysicalModelButton[] entcac = OWLMethods.getCompositePropOfAnnotationComponents(ontology, pe.getDescription());
//			ArrayList<PhysicalModelButton> ental = new ArrayList<PhysicalModelButton>();
//			for(int y=0; y<entcac.length;y++){
//				ental.add(entcac[y]);
//			}
//			nounnamesandcompositeanns.put(nm, ental);
//		}
		
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
		// Set the input/output relationships in the XML document
		for(CustomPhysicalProcess verb : setofverbobjects){
			Element verbel = new Element("verb").setAttribute("name",verb.getName());
			
			for(CustomPhysicalEntity input : verb.setofinputs){
				verbel.addContent(new Element("start").setAttribute("nounID",input.getName()));
			}
			for(CustomPhysicalEntity output : verb.setofoutputs){
				verbel.addContent(new Element("end").setAttribute("nounID",output.getName()));
			}
			for(CustomPhysicalEntity mediator : verb.setofmediators){
				verbel.addContent(new Element("mediator").setAttribute("nounID",mediator.getName()));
			}
			verbcollection.add(verbel);
		}
		
		nounlist.addContent(nouncollection);
		verblist.addContent(verbcollection);
		
		XMLOutputter xmlOutput = new XMLOutputter();
		xmlOutput.setFormat(Format.getPrettyFormat());
		xmlOutput.output(doc, new FileWriter(outputfile));
	}
	
<<<<<<< HEAD
	private static CustomPhysicalEntity collectNounData(PhysicalEntity ent, String verbID){
		CustomPhysicalEntity noun = null;
		String compositename = ent.getName();
		if(!nounnamesandphysents.keySet().contains(compositename)){
			String nounID = Integer.toString(x);
=======
	
	
	public static CustomPhysicalEntity collectNounData(PhysicalEntity ent, String verbID){
		CustomPhysicalEntity noun = null;
		String compositename = ent.getName();
		String nounID = null; 
		if(!nounnamesandphysents.keySet().contains(compositename)){
			nounID = Integer.toString(x);
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
			noun = new CustomPhysicalEntity(ent.getName(), ent.getName());
			Element nounel = new Element("noun");
			nounel.setAttribute("ID", nounID);
			nounel.setAttribute("name",compositename);
			nouncollection.add(nounel);
			nounnamesandphysents.put(compositename, noun);
			x++;
		}
		else{
			noun = nounnamesandphysents.get(compositename);
		}
		return noun;
	}
}
