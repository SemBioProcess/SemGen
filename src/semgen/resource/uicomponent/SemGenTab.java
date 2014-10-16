package semgen.resource.uicomponent;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import semgen.SemGenSettings;
import semgen.resource.SemGenFont;
import semgen.resource.SemGenIcon;

public class SemGenTab extends JPanel {

	private static final long serialVersionUID = 6580200707569122340L;
	protected SemGenSettings settings;
	public Component tool;

	protected TabTitle tablabel = null;

	public SemGenTab(String title, ImageIcon icon, String tooltip, SemGenSettings sets){
		tablabel = new TabTitle(this, title, icon, tooltip);	
		settings = new SemGenSettings(sets);
	}
	
	public TabTitle getTabLabel() {
		return tablabel;
	}
	
	public static String formatTabName(String filename){
		String tabname = filename; 
		if(tabname.length()>=30)
			tabname = tabname.substring(0, 30) + "...";
		return tabname;
	}
	
	public int closeTab() {
		return -1;
	}
		
	public void setClosePolicy(MouseListener ml) {
		tablabel.removelabel.addMouseListener(ml);
	}
	
	public void setTabName(String title) {
		tablabel.titlelabel.setText(formatTabName(title));
	}
	
	public class TabTitle extends JPanel implements MouseListener {
		private static final long serialVersionUID = 1L;
		SemGenTab parent;
		JLabel titlelabel;
		JLabel removelabel = new JLabel(SemGenIcon.eraseiconsmall);
		
		protected TabTitle(SemGenTab pcmp, String title, ImageIcon icon, String tooltip) {
			parent = pcmp;
			setOpaque(false);
			JLabel iconlabel = new JLabel(icon);
			
			removelabel.setOpaque(false);
			removelabel.addMouseListener(this);
			removelabel.setEnabled(false);
			removelabel.setBorder(BorderFactory.createEmptyBorder(1,1,1,1));
			titlelabel = new JLabel(formatTabName(title));
			titlelabel.setFont(SemGenFont.defaultPlain());
			
			add(iconlabel);
			add(titlelabel);
			add(removelabel);
			setToolTipText(tooltip);
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
	
		public void mouseReleased(MouseEvent arg0) {}

		@Override
		public void mouseClicked(MouseEvent arg0) {}
	}
}
