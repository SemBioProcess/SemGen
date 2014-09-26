package semgen.annotation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;

import javax.xml.rpc.ServiceException;

import org.jdom.JDOMException;
import org.semanticweb.owlapi.model.OWLException;

import JSim.util.Xcept;

import semgen.SemGenGUI;
import semsim.reading.ModelClassifier;

public class BatchSBML {
	
	public BatchSBML() throws FileNotFoundException, IOException, OWLException, InterruptedException, URISyntaxException, JDOMException, Xcept, ServiceException{
		
		File outputdir = new File("/Users/max_neal/Documents/workspace/PhysiomeKBgenerator/semsim_models/");
		File[] outputfilesarray = outputdir.listFiles();
		Set<String> outputfiles = new HashSet<String>();
		for(int y=0;y<outputfilesarray.length;y++){
			outputfiles.add(outputfilesarray[y].getName());
		}
		
		File sbmldir = new File("/Users/max_neal/Documents/workspace/PhysiomeKBgenerator/sbml_models/");
		File[] sbmlfiles = sbmldir.listFiles();
		System.out.println("sbmlfiles: " + sbmlfiles.length);
		for(int x=0;x<sbmlfiles.length;x++){
			if(sbmlfiles[x].getAbsolutePath().endsWith(".xml")){
				File outfile = new File("/Users/max_neal/Documents/workspace/PhysiomeKBgenerator/semsim_models/" + sbmlfiles[x].getName().replace(".xml", ".owl"));
				if(!outputfiles.contains(outfile.getName())){
					if(ModelClassifier.classify(sbmlfiles[x]) == ModelClassifier.SBML_MODEL){
						System.out.println("Processing " + sbmlfiles[x].getName());
						Annotator ann = SemGenGUI.AnnotateAction(sbmlfiles[x], true);
						ann.fileURI = outfile.toURI();
						SemGenGUI.SaveAction(ann, ModelClassifier.SBML_MODEL);
					}
					else{
						System.out.println(sbmlfiles[x] + " is not valid SBML");
					}
				}
				else{
					System.out.println(outfile.getName() + " already exists");
				}
			}
			else{System.out.println(sbmlfiles[x].getName() + " did not end in xml");}
		}
		System.out.println("***Finished batch processing of SBML models***");
	}
}
