package semgen.annotation.routines;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.jdom.JDOMException;
import org.semanticweb.owlapi.model.OWLException;

import semgen.GlobalActions;
import semgen.annotation.workbench.AnnotatorWorkbench;
import semsim.reading.ModelClassifier;

public class BatchSBML {
	GlobalActions globalactions;
	
	public BatchSBML(GlobalActions gactions, boolean autoannotate) throws IOException, OWLException, JDOMException{
		globalactions = gactions;
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
						AnnotatorWorkbench annotator = new AnnotatorWorkbench();
						annotator.initialize(sbmlfiles[x], autoannotate);
						annotator.saveModelAs();
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
