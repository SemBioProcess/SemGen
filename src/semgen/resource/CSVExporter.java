package semgen.resource;



import java.io.IOException;
import java.util.Arrays;

import javax.swing.filechooser.FileNameExtensionFilter;

import org.semanticweb.owlapi.model.OWLException;

import semgen.SemGenGUI;
import semgen.resource.file.SemGenOpenFileChooser;
import semsim.model.SemSimModel;
import semsim.model.computational.datastructures.DataStructure;
import semsim.writing.CaseInsensitiveComparator;

public class CSVExporter {
	public String savelocation;
	public String datatosave = "";
	public SemSimModel semsimmodel;

	public CSVExporter(SemSimModel semsimmodel){
		this.semsimmodel = semsimmodel;
	}
	public void exportCodewords() throws OWLException, IOException{
		datatosave = "Codeword, Units, Value (if static), Definition\n";
		
		String[] array = semsimmodel.getDataStructureNames().toArray(new String[]{});
		Arrays.sort(array, new CaseInsensitiveComparator());
		for(int i=0; i<array.length; i++){
			DataStructure ds = semsimmodel.getDataStructure(array[i]);
			if(ds.isDeclared() && !ds.getName().equals("")){
				String valueifstatic = "";
				if(ds.getComputation().getInputs().isEmpty()){
					String compcode = ds.getComputation().getComputationalCode();
					if(compcode!=null)
						valueifstatic = compcode.substring(compcode.indexOf("=")+1,compcode.length()).trim();
				}
				String desc = "";
				if(ds.getDescription()!=null)
					desc = ds.getDescription().replace("\n", "");
				String unitname = "";
				if(ds.hasUnits()){
					if(ds.getUnit().getName()!=null)
						unitname = ds.getUnit().getName();
				}
				datatosave = datatosave + ds.getName() + ", " + unitname + ", " + valueifstatic + ", " + desc + "\n";
			}
		}
		SemGenGUI.SaveAsAction(this, "", new FileNameExtensionFilter[]{SemGenOpenFileChooser.csvfilter});
	}
}
