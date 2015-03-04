/** 
 * Class for creating all icons used by SemGen
 * */

package semgen.utilities;

import javax.swing.ImageIcon;

import semgen.SemGenGUI;

public class SemGenIcon { 
	public static final ImageIcon plusicon = createImageIcon("icons/plus.gif");
	public static final ImageIcon minusicon = createImageIcon("icons/minus.gif");
	public static final ImageIcon loadingicon = createImageIcon("icons/blackspinnerclear.gif");
	public static final ImageIcon loadingiconsmall = createImageIcon("icons/preloader20x20.gif");
	public static final ImageIcon blankloadingicon = createImageIcon("icons/blackspinnerclearempty.gif");
	public static final ImageIcon blankloadingiconsmall = createImageIcon("icons/blackspinnersmallempty.gif");
	public static final ImageIcon searchicon = createImageIcon("icons/Search2020.png");
	public static final ImageIcon copyicon = createImageIcon("icons/Copy2020.png");
	public static final ImageIcon createicon = createImageIcon("icons/Create2020.png");
	public static final ImageIcon eraseicon = createImageIcon("icons/Erase2020.png");
	public static final ImageIcon eraseiconsmall = createImageIcon("icons/Erase1313.png");
	public static final ImageIcon modifyicon = createImageIcon("icons/Modify2020.png");
	public static final ImageIcon loadsourceofimporticon = createImageIcon("icons/Load2020.png");
	public static final ImageIcon homeicon = createImageIcon("icons/Home2020.png");
	public static final ImageIcon homeiconsmall = createImageIcon("icons/Home1515.png");
	public static final ImageIcon extractoricon = createImageIcon("icons/extractoricon2020.png");
	public static final ImageIcon annotatoricon = createImageIcon("icons/annotatoricon2020.png");
	public static final ImageIcon annotatoriconsmall = createImageIcon("icons/annotatoricon1515.png");
	public static final ImageIcon codericon = createImageIcon("icons/codericon2020.png");
	public static final ImageIcon mergeicon = createImageIcon("icons/mergeicon2020.png");
	public static final ImageIcon moreinfoicon = createImageIcon("icons/moreinfoicon2020.png");
	public static final ImageIcon externalURLicon = createImageIcon("icons/externalURL2020.png");
	public static final ImageIcon expendcontracticon = createImageIcon("icons/expandcontracticon1.gif");
	public static final ImageIcon questionicon = createImageIcon("icons/questionicon.gif");	
	public static final ImageIcon replaceicon = createImageIcon("icons/replaceicon.png");	
	public static final ImageIcon importicon = createImageIcon("icons/import_wiz.png");
	public static final ImageIcon exporticon = createImageIcon("icons/exportpref_obj.png");
	public static final ImageIcon treeicon = createImageIcon("icons/hierarchicalLayout.gif");
	public static final ImageIcon setsourceicon = createImageIcon("icons/rebase.gif");	
	public static final ImageIcon onicon = createImageIcon("icons/onicon.gif");
	public static final ImageIcon officon = createImageIcon("icons/officon.gif");
	public static final ImageIcon annotatemodelicon = createImageIcon("icons/annotatemodel.gif");
	public static final ImageIcon checkmarkicon = createImageIcon("icons/passed.png");
	public static final ImageIcon semgenbigicon = createImageIcon("icons/semgeniconbig.png");
	
	/** Returns an ImageIcon, or null if the path was invalid. */
	private static ImageIcon createImageIcon(String path) {
		java.net.URL imgURL = SemGenGUI.class.getResource(path);
		if (imgURL == null) {
			System.err.println("Couldn't find file: " + path);
			return null;
		} 
		return new ImageIcon(imgURL);
	}
}
