package semgen;

import java.awt.Color;

import javax.swing.UIManager;

import semgen.SemGenGUI;

public class SemGen {
	
	/** Main method for running an instance of SemGen */
	public static void main(String[] args) {
		try{

			UIManager.put("nimbusOrange", new Color(51,98,140));
			UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
		    for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
		        if ("Nimbus".equals(info.getName())) {
		            UIManager.setLookAndFeel(info.getClassName());
		            break;
		        }
			}
			SemGenGUI.frame = new SemGenGUI();
		}
		catch(Exception e){e.printStackTrace();}
	}
}
