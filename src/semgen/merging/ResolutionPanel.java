package semgen.merging;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

import semgen.SemGenGUI;
import semgen.resource.SemGenFont;
import semgen.resource.SemGenIcon;
import semgen.resource.uicomponent.SemGenScrollPane;
import semsim.model.SemSimModel;
import semsim.model.computational.DataStructure;


public class ResolutionPanel extends JPanel implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -618244272904338963L;

	public SemSimModel semsimmodel1;
	public SemSimModel semsimmodel2;
	public DataStructure ds1;
	public DataStructure ds2;
	public Boolean manualmapping;
	public JRadioButton rb1;
	public JRadioButton rb2;
	public JRadioButton rb3;
	public JButton questionbutton = new JButton(SemGenIcon.questionicon);

	public ResolutionPanel(DataStructure ds1, DataStructure ds2,
			SemSimModel semsimmodel1, SemSimModel semsimmodel2,
			String matchdescription, Boolean manualmapping) {

		this.ds1 = ds1;
		this.ds2 = ds2;
		this.semsimmodel1 = semsimmodel1;
		this.semsimmodel2 = semsimmodel2;
		this.manualmapping = manualmapping;

		this.setLayout(new BorderLayout());
		this.setAlignmentX(LEFT_ALIGNMENT);
		this.setBackground(Color.white);
		this.setOpaque(true);

		JPanel annotationpanel = new JPanel(new BorderLayout());
		annotationpanel.setBackground(Color.white);
		annotationpanel.setAlignmentX(LEFT_ALIGNMENT);
		annotationpanel.setOpaque(true);

		JLabel model1label = new JLabel(ds1.getDescription() + " (" + ds1.getName() + ")");

		model1label.setFont(SemGenFont.defaultBold());
		model1label.setForeground(Color.blue);
		model1label.setAlignmentX(LEFT_ALIGNMENT);
		model1label.setBorder(BorderFactory.createEmptyBorder(3, 3, 0, 3));

		JLabel model2label = new JLabel(ds2.getDescription() + " (" + ds2.getName() + ")");
		model2label.setFont(SemGenFont.defaultBold());
		model2label.setForeground(Color.red);
		model2label.setAlignmentX(LEFT_ALIGNMENT);
		model2label.setBorder(BorderFactory.createEmptyBorder(3, 3, 0, 3));

		JLabel mappedtolabel = new JLabel("  mapped to  ");
		mappedtolabel.setBackground(Color.white);
		mappedtolabel.setFont(SemGenFont.defaultItalic(-1));
		
		JPanel annotationsubpanel = new JPanel();
		annotationsubpanel.setBackground(Color.white);
		annotationsubpanel.add(model1label);
		annotationsubpanel.add(mappedtolabel);
		annotationsubpanel.add(model2label);

		annotationpanel.add(annotationsubpanel, BorderLayout.WEST);
		annotationpanel.add(Box.createGlue(), BorderLayout.EAST);

		ButtonGroup bg = new ButtonGroup();
		rb1 = new JRadioButton("Use " + ds1.getName() + " (" + semsimmodel1.getName() + ")");
		rb1.setForeground(Color.blue);
		rb1.setBackground(Color.white);
		if(ds1.getComputation().getComputationalCode()!=null)
			rb1.setToolTipText(ds1.getComputation().getComputationalCode());
		else rb1.setToolTipText("user-defined input");
		rb2 = new JRadioButton("Use " + ds2.getName() + " (" + semsimmodel2.getName() + ")");
		rb2.setForeground(Color.red);
		rb2.setBackground(Color.white);
		rb2.setOpaque(false);
		if(ds2.getComputation().getComputationalCode()!=null)
			rb2.setToolTipText(ds2.getComputation().getComputationalCode());
		else rb2.setToolTipText("user-defined input");
		rb2.setToolTipText(ds2.getComputation().getComputationalCode());
		
		rb3 = new JRadioButton("Ignore equivalency");
		rb3.setToolTipText("Preserve both codewords and their equations in the merged model");
		rb3.setBackground(Color.white);
		rb1.setSelected(true);
		
		bg.add(rb1);
		bg.add(rb2);
		bg.add(rb3);

		questionbutton.addActionListener(this);
		questionbutton.setBorderPainted(false);
		questionbutton.setContentAreaFilled(false);
		questionbutton.setMaximumSize(new Dimension(20, 20));

		JPanel actionpanel = new JPanel();
		actionpanel.setBackground(Color.white);
		actionpanel.setAlignmentX(LEFT_ALIGNMENT);

		JPanel actionsubpanel = new JPanel();
		actionsubpanel.setLayout(new BorderLayout());

		JLabel equalslabel = new JLabel(matchdescription);
		equalslabel.setOpaque(false);
		equalslabel.setBorder(BorderFactory.createEmptyBorder(0,0,0,25));

		actionpanel.add(equalslabel);
		actionpanel.add(rb1);
		actionpanel.add(rb2);
		actionpanel.add(rb3);
		actionpanel.add(questionbutton);

		equalslabel.setFont(new Font("SansSerif", Font.ITALIC, 12));

		JPanel mainpanel = new JPanel(new BorderLayout());
		mainpanel.add(annotationpanel, BorderLayout.NORTH);
		mainpanel.add(actionpanel, BorderLayout.SOUTH);
		this.add(mainpanel, BorderLayout.NORTH);
		this.add(Box.createVerticalGlue(), BorderLayout.SOUTH);
	}

	public void actionPerformed(ActionEvent arg0) {
		Object o = arg0.getSource();
		if (o == questionbutton) questionButtonAction();
	}

	public void questionButtonAction() {
		String term1 = ds1.getDescription();
		String term2 = ds2.getDescription();
		Set<String> hasinput1fulldesc = new HashSet<String>();
		Set<String> hasinput2fulldesc = new HashSet<String>();

		for (DataStructure input1 : ds1.getComputation().getInputs()) {
			hasinput1fulldesc.add(input1.getDescription());
		}
		for (DataStructure input2 : ds2.getComputation().getInputs()) {
			hasinput2fulldesc.add(input2.getDescription());
		}
		String hasinput1list = makeStringListFromSet(ds1.getComputation().getInputs(), true);
		String hasinput2list = makeStringListFromSet(ds2.getComputation().getInputs(), true);

		Set<DataStructure> inputsforboth = new HashSet<DataStructure>();
		inputsforboth.addAll(ds1.getUsedToCompute());
		inputsforboth.addAll(ds2.getUsedToCompute());
		String isinputforbothlist = makeStringListFromSet(inputsforboth, false);
		String isinputfor1list = makeStringListFromSet(ds1.getUsedToCompute(), false);
		String isinputfor2list = makeStringListFromSet(ds2.getUsedToCompute(), false);
		JTextPane textpane = createTextPane(term1, term2, hasinput1list,
				hasinput2list, isinputfor1list, isinputfor2list,
				isinputforbothlist);
		
		SemGenScrollPane scroller = new SemGenScrollPane(textpane);
		textpane.setCaretPosition(0);
		scroller.setPreferredSize(new Dimension(600, 600));
		scroller.getVerticalScrollBar().setUnitIncrement(12);
		JOptionPane.showMessageDialog(SemGenGUI.desktop, scroller, "Information about resolution step", JOptionPane.PLAIN_MESSAGE);
	}

	public String makeStringListFromSet(Set<DataStructure> dsset, Boolean forInput) {
		String stringlist = "  ";
		int n = 0;
		for (DataStructure ds : dsset) {
			if (n == 0) {
				stringlist = ds.getDescription();
			} else {
				stringlist = stringlist + "\n" + "  " + ds.getDescription();
			}
			n++;
		}
		if (dsset.isEmpty()) {
			if (forInput) {
				stringlist = "  user-defined (external) input";
			} else {
				stringlist = "  nothing";
			}
		}
		return stringlist;
	}

	private JTextPane createTextPane(String term1, String term2,
			String hasinput1list, String hasinput2list, String isinputfor1list,
			String isinputfor2list, String isinputforbothlist) {
		String samecdwdtext = "";
		if (ds1.getName().equals(ds2.getName())) {
			samecdwdtext = "You will be prompted to rename " + ds2.getName()
					+ " (from " + semsimmodel2.getName() + ") and ";
		}
		String[] initString = {
				"If " + ds1.getName() + " (" + semsimmodel1.getName() + ") is kept:\n ", // bold
				term1 + " will be computed from...\n\n", // regular
				hasinput1list + "\n", // italic
				"\n  ...and will be used to compute... \n\n", // regular
				isinputforbothlist + "\n", // italic
				"\nIf " + ds2.getName() + " (" + semsimmodel2.getName() + ") is kept:\n ",// bold
				term2 + " will be computed from...\n\n", // regular
				hasinput2list + "\n", // italic
				"\n  ...and will be used to compute... \n\n", // regular
				isinputforbothlist + "\n", // italic
				"\nIf data structures are kept disjoint:\n ", // bold
				samecdwdtext + term1 + " (" + ds1.getName() + ", " + semsimmodel1.getName()
						+ ") will be computed from...\n\n", // regular
				hasinput1list + "\n", // italic
				"\n  ...and will be used to compute... \n\n", // regular
				isinputfor1list + "\n\n", // italic
				term2 + "\n (" + ds2.getName() + ", " + semsimmodel2.getName()
						+ ") will be computed from...\n\n", // regular
				hasinput2list + "\n", // italic
				"\n  ...and will be used to compute... \n\n", // regular
				isinputfor2list + "\n", // italic
		};

		String[] initStyles = { "bold", "regular", "italic", "regular",
				"italic", "bold", "regular", "italic", "regular", "italic",
				"bold", "regular", "italic", "regular", "italic", "regular",
				"italic", "regular", "italic" };

		JTextPane textPane = new JTextPane();
		StyledDocument doc = textPane.getStyledDocument();
		addStylesToDocument(doc);

		try {
			for (int i = 0; i < initString.length; i++) {
				doc.insertString(doc.getLength(), initString[i],
						doc.getStyle(initStyles[i]));
			}
		} catch (BadLocationException ble) {
			System.err.println("Couldn't insert initial text into text pane.");
		}
		return textPane;
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
