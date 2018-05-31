package semgen.utilities;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Scanner;

import javax.swing.JOptionPane;

import org.semanticweb.owlapi.model.OWLException;

import semgen.utilities.file.SemGenSaveFileChooser;
import semsim.model.collection.SemSimModel;
import semsim.model.computational.datastructures.DataStructure;
import semsim.utilities.CaseInsensitiveComparator;

public class CSVExporter {;
	private String datatosave = "";
	private SemSimModel semsimmodel;

	public CSVExporter(SemSimModel semsimmodel){
		this.semsimmodel = semsimmodel;
	}
	public void exportCodewords() throws OWLException, IOException{
		datatosave = "Codeword, Units, Value (if static), Definition\n";
		
		String[] array = semsimmodel.getDataStructureNames().toArray(new String[]{});
		Arrays.sort(array, new CaseInsensitiveComparator());
		for(int i=0; i<array.length; i++){
			DataStructure ds = semsimmodel.getAssociatedDataStructure(array[i]);
			if(ds.isDeclared() && !ds.getName().equals("")){
				String valueifstatic = "";
				if(ds.getComputation().getInputs().isEmpty()){
					String compcode = ds.getComputation().getComputationalCode();
					if(!compcode.isEmpty())
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
		saveCSV();
	}
	
	public void saveCSV() {
		SemGenSaveFileChooser filec = new SemGenSaveFileChooser(new String[]{"csv"}, "csv"); 
		if (filec.SaveAsAction(semsimmodel)!=null) {
			Scanner scanner = new Scanner(datatosave);
			try {
				PrintWriter outfile = new PrintWriter(new FileWriter(filec.getSelectedFile()));
				String nextline;
				while (scanner.hasNextLine()) {
					nextline = scanner.nextLine();
					outfile.println(nextline);
				}
				outfile.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			scanner.close();
			JOptionPane.showMessageDialog(null, "Finished exporting .csv file");
		}
	}
}
