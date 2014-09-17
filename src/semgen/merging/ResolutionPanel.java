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

import semgen.resource.SemGenIcon;
import semgen.resource.SemGenFont;
import semgen.resource.uicomponents.SemGenScrollPane;
import semsim.model.SemSimModel;
import semsim.model.computational.DataStructure;

public class ResolutionPanel extends JPanel implements ActionListener {

	private static final long serialVersionUID = -618244272904338963L;
	public MergerTab merger;

	public SemSimModel semsimmodel1, semsimmodel2;
	public DataStructure ds1, ds2;
	public JButton questionbutton = new JButton(SemGenIcon.questionicon);
	private ButtonGroup bg = new ButtonGroup();
	
	public ResolutionPanel(MergerTab mergr, DataStructure d1, DataStructure d2,
			SemSimModel semsimmod1, SemSimModel semsimmod2,
			String matchdescription) {

		this.merger = mergr;
		
		this.ds1 = d1;
		this.ds2 = d2;
		this.semsimmodel1 = semsimmod1;
		this.semsimmodel2 = semsimmod2;
		
		setLayout(new BorderLayout());
		formatComponent(this);
		setOpaque(true);

		JPanel annotationpanel = new JPanel(new BorderLayout());
		formatComponent(annotationpanel);
		annotationpanel.setOpaque(true);

		JLabel model1label = new JLabel(ds1.getDescription() + " (" + ds1.getName() + ")");
		formatComponent(model1label, null, Color.blue, SemGenFont.defaultBold());
		model1label.setBorder(BorderFactory.createEmptyBorder(3, 3, 0, 3));

		JLabel model2label = new JLabel(ds2.getDescription() + " (" + ds2.getName() + ")");
		formatComponent(model2label, null, Color.red, SemGenFont.defaultBold());
		model2label.setBorder(BorderFactory.createEmptyBorder(3, 3, 0, 3));

		JLabel mappedtolabel = new JLabel("  mapped to  ");
		formatComponent(mappedtolabel, Color.white, null, SemGenFont.defaultItalic(-1));
		
		JPanel annotationsubpanel = new JPanel();
		formatComponent(annotationsubpanel);
		annotationsubpanel.add(model1label); 
		annotationsubpanel.add(mappedtolabel);
		annotationsubpanel.add(model2label);
		annotationpanel.add(annotationsubpanel, BorderLayout.WEST);
		annotationpanel.add(Box.createGlue(), BorderLayout.EAST);

		JRadioButton rb1 = new JRadioButton("Use " + ds1.getName() + " (" + semsimmodel1.getName() + ")");

		JRadioButton rb2 = new JRadioButton("Use " + ds2.getName() + " (" + semsimmodel2.getName() + ")");
		rb2.setOpaque(false);
		
		JRadioButton rb3 = new JRadioButton("Ignore equivalency");
		rb1.setSelected(true);
		
		questionbutton.addActionListener(this);
		questionbutton.setBorderPainted(false);
		questionbutton.setContentAreaFilled(false);
		questionbutton.setMaximumSize(new Dimension(20, 20));
		
		JLabel equalslabel = new JLabel(matchdescription);
		equalslabel.setOpaque(false);
		equalslabel.setBorder(BorderFactory.createEmptyBorder(0,0,0,25));
		
		JPanel actionpanel = new JPanel();
		formatComponent(actionpanel);

		actionpanel.add(equalslabel);
				
		addRadioButton(rb1,bg, actionpanel, Color.blue, null);
		addRadioButton(rb2,bg, actionpanel, Color.red, null);
		addRadioButton(rb3,bg, actionpanel, null, "Preserve both codewords and their equations in the merged model");
		actionpanel.add(questionbutton);

		equalslabel.setFont(SemGenFont.defaultItalic());

		JPanel mainpanel = new JPanel(new BorderLayout());
		mainpanel.add(annotationpanel, BorderLayout.NORTH);
		mainpanel.add(actionpanel, BorderLayout.SOUTH);
		this.add(mainpanel, BorderLayout.NORTH);
		this.add(Box.createVerticalGlue(), BorderLayout.SOUTH);
	}
	
	private void formatComponent(JPanel comp) {
		comp.setAlignmentX(LEFT_ALIGNMENT);
		comp.setBackground(Color.white);
	}
	
	private void formatComponent(JLabel comp, Color bc, Color fc, Font f) {
		comp.setAlignmentX(LEFT_ALIGNMENT);
		comp.setBackground(bc);
		comp.setForeground(fc);
		comp.setFont(f);
	}
	
	private void addRadioButton(JRadioButton rb, ButtonGroup bg, JPanel pan, Color fc, String ttip) {
		rb.setBackground(Color.white);
		rb.setForeground(fc);

		
		if (ttip!=null) 
			rb.setToolTipText(ttip);
		else if(ds1.getComputation().getComputationalCode()!=null)
			rb.setToolTipText(ds1.getComputation().getComputationalCode());
		else 
			rb.setToolTipText("user-defined input");
		
		bg.add(rb);	
		rb.setActionCommand(String.valueOf(bg.getButtonCount()));
		pan.add(rb);
	}

	public int getSelectedAction() {
		return Integer.parseInt(bg.getSelection().getActionCommand());
	}
	
	public void actionPerformed(ActionEvent arg0) {
		if (arg0.getSource() == questionbutton) questionButtonAction();
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
		JOptionPane.showMessageDialog(this, scroller, "Information about resolution step", JOptionPane.PLAIN_MESSAGE);
	}

	public String makeStringListFromSet(Set<DataStructure> dsset, Boolean forInput) {	
		if (dsset.isEmpty()) {
			if (forInput) {
				return "  user-defined (external) input";
			}
			return "  nothing";
		}
		String stringlist = "  ";
		for (DataStructure ds : dsset) {
				stringlist = stringlist + ds.getDescription() + "\n ";
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
