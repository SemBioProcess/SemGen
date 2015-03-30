package semgen.merging.resolutionpane;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.apache.commons.lang3.tuple.Pair;

import semgen.merging.workbench.DataStructureDescriptor;
import semgen.merging.workbench.DataStructureDescriptor.Descriptor;
import semgen.merging.workbench.Merger.ResolutionChoice;
import semgen.utilities.SemGenIcon;

public class ResolutionPanel extends JPanel {
	private static final long serialVersionUID = -618244272904338963L;

	private ButtonGroup bg = new ButtonGroup();
	private JButton questionbutton = new JButton(SemGenIcon.questionicon);
	private ResolutionChoice selection = ResolutionChoice.noselection;

	public ResolutionPanel(DataStructureDescriptor ds1, 
			DataStructureDescriptor ds2, 
			Pair<String, String> modnames, String matchdescription) {
		this.setLayout(new BorderLayout());
		this.setAlignmentX(LEFT_ALIGNMENT);
		this.setBackground(Color.white);
		this.setOpaque(true);
		
		// Create text for radio buttons
		String model1optiontext = modnames.getLeft() + ":  " + ds1.getDescriptorValue(Descriptor.description) + 
				" (" + ds1.getDescriptorValue(Descriptor.name) + ")";
		String model2optiontext = modnames.getRight() + ":  " + ds2.getDescriptorValue(Descriptor.description) + 
				" (" + ds2.getDescriptorValue(Descriptor.name) + ")";

		JRadioButton rb1 = new JRadioButton(model1optiontext);
		formatButton(rb1, Color.blue, ResolutionChoice.first);
		
		if(ds1.getDescriptorValue(Descriptor.computationalcode)!=null)
			rb1.setToolTipText(ds1.getDescriptorValue(Descriptor.computationalcode));
		else rb1.setToolTipText("user-defined input");
		
		JRadioButton rb2 = new JRadioButton(model2optiontext);
		formatButton(rb2, Color.red, ResolutionChoice.second);
		
		if(ds2.getDescriptorValue(Descriptor.computationalcode)!=null)
			rb2.setToolTipText(ds2.getDescriptorValue(Descriptor.computationalcode));
		else rb2.setToolTipText("user-defined input");

		JRadioButton rb3 = new JRadioButton("Ignore equivalency");
		formatButton(rb3, null, ResolutionChoice.ignore);
		rb3.setToolTipText("Preserve both codewords and their equations in the merged model");

		JPanel actionpanel = new JPanel();
		actionpanel.setBackground(Color.white);
		actionpanel.setLayout(new BoxLayout(actionpanel, BoxLayout.Y_AXIS));
		actionpanel.setAlignmentX(Box.LEFT_ALIGNMENT);
		actionpanel.add(rb1);
		actionpanel.add(rb2);
		actionpanel.add(rb3);

		// Info panel includes the mapping type and the question mark button for 
		// helping the user anticipate the consequences of their resolution step decision
		JPanel infopanel = new JPanel();
		infopanel.setLayout(new BoxLayout(infopanel, BoxLayout.Y_AXIS));
		infopanel.setBackground(Color.white);
		infopanel.setAlignmentX(CENTER_ALIGNMENT);
		infopanel.setOpaque(true);
		
		// Create label that describes the type of mapping for the resolution step
		JLabel mappingtypelabel = new JLabel("Mapping type: " + matchdescription);
		mappingtypelabel.setOpaque(false);
		mappingtypelabel.setBorder(BorderFactory.createEmptyBorder(0,0,0,25));
		mappingtypelabel.setFont(new Font("SansSerif", Font.ITALIC, 12));
		mappingtypelabel.setAlignmentX(CENTER_ALIGNMENT);

		
		questionbutton.setBorderPainted(false);
		questionbutton.setContentAreaFilled(false);
		questionbutton.setMaximumSize(new Dimension(20, 20));
		questionbutton.setAlignmentX(CENTER_ALIGNMENT);
		
		infopanel.add(mappingtypelabel);
		infopanel.add(Box.createVerticalGlue());
		infopanel.add(questionbutton);
		infopanel.add(Box.createVerticalGlue());

		JPanel mainpanel = new JPanel(new BorderLayout());
		mainpanel.setBackground(Color.white);
		mainpanel.add(actionpanel, BorderLayout.WEST);
		mainpanel.add(infopanel, BorderLayout.EAST);
		mainpanel.setBorder(BorderFactory.createEmptyBorder(5,5,10,5));
		this.add(mainpanel, BorderLayout.NORTH);
		this.add(Box.createVerticalGlue(), BorderLayout.SOUTH);
	}
	
	private void formatButton(JRadioButton button, Color foreground, final ResolutionChoice choice) {
		button.setForeground(foreground);
		button.setBackground(Color.white);
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				selection = choice;
			}
		});
		bg.add(button);
	}
	
	public ResolutionChoice getSelection() {
		return selection;
	}
	
	public void addMouseListener(MouseListener listener) {
		questionbutton.addMouseListener(listener);
	}
}
