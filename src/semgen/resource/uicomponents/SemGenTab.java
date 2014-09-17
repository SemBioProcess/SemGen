package semgen.resource.uicomponents;

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import semgen.SemGenSettings;
import semgen.UniversalActions;
import semgen.resource.SemGenIcon;
import semgen.resource.SemGenFont;

abstract public class SemGenTab extends JPanel {

	private static final long serialVersionUID = 6580200707569122340L;
	public OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
	public static final int AnnotatorTab = 1;
	public static final int ExtractorTab = 2;
	public static final int MergerTab = 3;
	
	public JLabel removelabel = new JLabel();
	protected TabTitle tablabel = null;
	protected SemGenSettings settings;
	protected UniversalActions uacts;

	public SemGenTab(String title, ImageIcon icon, String tooltip, SemGenSettings sets, UniversalActions ua){
		tablabel = new TabTitle(this, title, icon, tooltip);	
		settings = new SemGenSettings(sets);
		uacts = ua;
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
	
	public void openDialog(JDialog dialog) {
		dialog.setLocationRelativeTo(this);
	}
	
	public abstract Boolean Initialize();
	
	public int closeTab() {
		return -1;
	}
		
	public void setClosePolicy(MouseListener ml) {
		tablabel.addMouseListener(ml);
	}
	public void setTitle(String title) {
		tablabel.titlelabel.setText(formatTabName(title));
	}
	
	abstract public int getTabType();
	
	abstract public boolean isSaved();
	
	abstract public void requestSave();
	
	abstract public void requestSaveAs();
	
	public class TabTitle extends JPanel implements MouseListener {
		private static final long serialVersionUID = 1L;
		SemGenTab parent;
		JLabel titlelabel;
		protected TabTitle(SemGenTab pcmp, String title, ImageIcon icon, String tooltip) {
			parent = pcmp;
			setOpaque(false);
			JLabel iconlabel = new JLabel(icon);
			
			removelabel.setOpaque(false);
			removelabel.setIcon(SemGenIcon.eraseiconsmall);
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
