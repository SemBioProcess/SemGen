package semgen.merging;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.apache.commons.lang3.tuple.Pair;

import semgen.merging.workbench.DataStructureDescriptor;
import semgen.merging.workbench.DataStructureDescriptor.Descriptor;
import semgen.merging.workbench.Merger.ResolutionChoice;
import semgen.utilities.SemGenFont;
import semgen.utilities.SemGenIcon;

public class ResolutionPanel extends JPanel {
	private static final long serialVersionUID = -618244272904338963L;

	private ButtonGroup bg = new ButtonGroup();
	private JButton questionbutton = new JButton(SemGenIcon.questionicon);
	private ResolutionChoice selection = ResolutionChoice.noselection;

	public ResolutionPanel(ActionListener listener, DataStructureDescriptor ds1, 
			DataStructureDescriptor ds2, 
			Pair<String, String> modnames, String matchdescription) {
		this.setLayout(new BorderLayout());
		this.setAlignmentX(LEFT_ALIGNMENT);
		this.setBackground(Color.white);
		this.setOpaque(true);
		
		JLabel model1label = new JLabel(ds1.getDescriptorValue(Descriptor.description) + 
				" (" + ds1.getDescriptorValue(Descriptor.name) + ")");

		model1label.setFont(SemGenFont.defaultBold());
		model1label.setForeground(Color.blue);
		model1label.setAlignmentX(LEFT_ALIGNMENT);
		model1label.setBorder(BorderFactory.createEmptyBorder(3, 3, 0, 3));

		JLabel model2label = new JLabel(ds2.getDescriptorValue(Descriptor.description) + 
				" (" + ds2.getDescriptorValue(Descriptor.name) + ")");
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
		
		JPanel annotationpanel = new JPanel(new BorderLayout());
		annotationpanel.setBackground(Color.white);
		annotationpanel.setAlignmentX(LEFT_ALIGNMENT);
		annotationpanel.setOpaque(true);
		annotationpanel.add(annotationsubpanel, BorderLayout.WEST);
		annotationpanel.add(Box.createGlue(), BorderLayout.EAST);

		JRadioButton rb1 = new JRadioButton("Use " + ds1.getDescriptorValue(Descriptor.name) 
				+ " (" + modnames.getLeft() + ")");
		formatButton(rb1, Color.blue, ResolutionChoice.first);
		
		if(ds1.getDescriptorValue(Descriptor.computationalcode)!=null)
			rb1.setToolTipText(ds1.getDescriptorValue(Descriptor.computationalcode));
		else rb1.setToolTipText("user-defined input");
		
		JRadioButton rb2 = new JRadioButton("Use " + ds2.getDescriptorValue(Descriptor.name) 
				+ " (" + modnames.getRight() + ")");
		formatButton(rb2, Color.red, ResolutionChoice.second);
		if(ds2.getDescriptorValue(Descriptor.computationalcode)!=null)
			rb2.setToolTipText(ds2.getDescriptorValue(Descriptor.computationalcode));
		else rb2.setToolTipText("user-defined input");
		rb2.setToolTipText(ds2.getDescriptorValue(Descriptor.computationalcode));

		JRadioButton rb3 = new JRadioButton("Ignore equivalency");
		formatButton(rb3, null, ResolutionChoice.ignore);
		rb3.setToolTipText("Preserve both codewords and their equations in the merged model");
		
		questionbutton.addActionListener(listener);
		questionbutton.setBorderPainted(false);
		questionbutton.setContentAreaFilled(false);
		questionbutton.setMaximumSize(new Dimension(20, 20));

		JPanel actionpanel = new JPanel();
		actionpanel.setBackground(Color.white);
		actionpanel.setAlignmentX(LEFT_ALIGNMENT);

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
}
