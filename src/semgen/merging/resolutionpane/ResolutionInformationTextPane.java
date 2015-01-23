package semgen.merging.resolutionpane;

import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

import org.apache.commons.lang3.tuple.Pair;

import semgen.merging.workbench.DataStructureDescriptor;
import semgen.merging.workbench.DataStructureDescriptor.Descriptor;

public class ResolutionInformationTextPane extends JTextPane {
	private static final long serialVersionUID = 1L;

	public ResolutionInformationTextPane(DataStructureDescriptor ds1, DataStructureDescriptor ds2, 
			Pair<String, String> modnames) {
		setCaretPosition(0);
		createTextPane(
				ds1.getDescriptorValue(Descriptor.name), 
				ds2.getDescriptorValue(Descriptor.name), 
				ds1.getDescriptorValue(Descriptor.description), 
				ds2.getDescriptorValue(Descriptor.description), 
				ds1.getDescriptorValue(Descriptor.inputs), 
				ds2.getDescriptorValue(Descriptor.inputs), 
				ds1.getDescriptorValue(Descriptor.inputfor), 
				ds2.getDescriptorValue(Descriptor.inputfor),
				modnames);
	}
	
	private void createTextPane(String name1, String name2, String term1, String term2,
			String hasinput1list, String hasinput2list, String isinputfor1list,
			String isinputfor2list,
			Pair<String, String> modnames) {
		String isinputforbothlist = isinputfor1list + isinputfor2list;
		
		String samecdwdtext = "";
		if (name1.equals(name2)) {
			samecdwdtext = "You will be prompted to rename " + name2
					+ " (from " + modnames.getRight() + ") and ";
		}
		String[] initString = {
				"If " + name1 + " (" + modnames.getLeft() + ") is kept:\n ", // bold
				term1 + " will be computed from...\n\n", // regular
				hasinput1list + "\n", // italic
				"\n  ...and will be used to compute... \n\n", // regular
				isinputforbothlist + "\n", // italic
				"\nIf " + name2 + " (" + modnames.getRight() + ") is kept:\n ",// bold
				term2 + " will be computed from...\n\n", // regular
				hasinput2list + "\n", // italic
				"\n  ...and will be used to compute... \n\n", // regular
				isinputforbothlist + "\n", // italic
				"\nIf data structures are kept disjoint:\n ", // bold
				samecdwdtext + term1 + " (" + name1 + 
				", " + name1
						+ ") will be computed from...\n\n", // regular
				hasinput1list + "\n", // italic
				"\n  ...and will be used to compute... \n\n", // regular
				isinputfor1list + "\n\n", // italic
				term2 + "\n (" + name2 + ", " + modnames.getRight()
						+ ") will be computed from...\n\n", // regular
				hasinput2list + "\n", // italic
				"\n  ...and will be used to compute... \n\n", // regular
				isinputfor2list + "\n", // italic
		};

		String[] initStyles = { "bold", "regular", "italic", "regular",
				"italic", "bold", "regular", "italic", "regular", "italic",
				"bold", "regular", "italic", "regular", "italic", "regular",
				"italic", "regular", "italic" };

		StyledDocument doc = getStyledDocument();
		addStylesToDocument(doc);

		try {
			for (int i = 0; i < initString.length; i++) {
				doc.insertString(doc.getLength(), initString[i],
						doc.getStyle(initStyles[i]));
			}
		} catch (BadLocationException ble) {
			System.err.println("Couldn't insert initial text into text pane.");
		}
	}

	protected void addStylesToDocument(StyledDocument doc) {
		// Initialize styles.
		Style def = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);

		Style regular = doc.addStyle("regular", def);
		StyleConstants.setFontFamily(def, "SansSerif");

		Style s = doc.addStyle("italic", regular);
		StyleConstants.setItalic(s, true);

		s = doc.addStyle("bold", regular);
		StyleConstants.setBold(s, true);

		s = doc.addStyle("small", regular);
		StyleConstants.setFontSize(s, 10);

		s = doc.addStyle("large", regular);
		StyleConstants.setFontSize(s, 16);

		s = doc.addStyle("icon", regular);
		StyleConstants.setAlignment(s, StyleConstants.ALIGN_CENTER);

		s = doc.addStyle("button", regular);
		StyleConstants.setAlignment(s, StyleConstants.ALIGN_CENTER);

	}
}
