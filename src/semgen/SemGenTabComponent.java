package semgen;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.HeadlessException;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import semgen.annotation.Annotator;
import semgen.extraction.ExtractorTab;
import semgen.merging.Merger;

public class SemGenTabComponent extends JPanel implements MouseListener{

	private static final long serialVersionUID = 6580200707569122340L;
	public Component tool;
	public JLabel removelabel;

	public SemGenTabComponent(String title, Component tool){
		this.tool = tool;
		this.setOpaque(false);
		JLabel iconlabel = new JLabel();
		ImageIcon icon = null;
		if(tool instanceof Annotator) icon = SemGenGUI.annotatoricon;
		else if(tool instanceof ExtractorTab) icon = SemGenGUI.extractoricon;
		else if(tool instanceof Merger) icon = SemGenGUI.mergeicon;
		iconlabel.setIcon(icon);
		
		removelabel = new JLabel();
		removelabel.setOpaque(false);
		removelabel.setIcon(SemGenGUI.eraseiconsmall);
		removelabel.addMouseListener(this);
		removelabel.setEnabled(false);
		removelabel.setBorder(BorderFactory.createEmptyBorder(1,1,1,1));
		JLabel titlelabel = new JLabel(title);
		titlelabel.setFont(new Font("SansSerif", Font.PLAIN, SemGenGUI.defaultfontsize));
		
		add(iconlabel);
		add(titlelabel);
		add(removelabel);
	}

	public void mouseClicked(MouseEvent arg0) {
		try {
			SemGenGUI.closeTabAction(tool);
		} catch (HeadlessException e) {
			e.printStackTrace();
		}
	}

	public void mouseEntered(MouseEvent arg0) {
		Object x = arg0.getSource();
		if(x == removelabel){
			((JLabel)x).setEnabled(true);
		}

	}

	public void mouseExited(MouseEvent arg0) {
		Object x = arg0.getSource();
		if(x == removelabel){
			((JLabel)x).setEnabled(false);
			((JLabel)x).setBorder(BorderFactory.createEmptyBorder(1,1,1,1));
		}
	}

	public void mousePressed(MouseEvent arg0) {
		Object x = arg0.getSource();
		if(x == removelabel){
			((JLabel)x).setBorder(BorderFactory.createLineBorder(Color.blue,1));
		}
	}

	public void mouseReleased(MouseEvent arg0) {
	}
}
