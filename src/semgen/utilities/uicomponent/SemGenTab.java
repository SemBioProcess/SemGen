package semgen.utilities.uicomponent;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import semgen.GlobalActions;
import semgen.SemGenSettings;
import semgen.utilities.GenericWorker;
import semgen.utilities.SemGenJob;
import semsim.fileaccessors.ModelAccessor;
import semgen.utilities.SemGenFont;
import semgen.utilities.SemGenIcon;

public abstract class SemGenTab extends JPanel {

	private static final long serialVersionUID = 6580200707569122340L;
	protected SemGenSettings settings;
	public Component tool;
	protected GlobalActions globalactions;

	protected TabTitle tablabel = null;

	public SemGenTab(String title, ImageIcon icon, String tooltip, SemGenSettings sets, GlobalActions acts){
		createTabTitleLabel(title, icon, tooltip);
		settings = new SemGenSettings(sets);
		globalactions=acts;
	}
	
	protected void createTabTitleLabel(String title, ImageIcon icon, String tooltip) {
		tablabel = new TabTitle(this, title, icon, tooltip);	
	}
	
	public abstract void loadTab();
	
	public TabTitle getTabLabel() {
		return tablabel;
	}
	
	public static String formatTabName(String filename){
		String tabname = filename; 
		if(tabname.length()>=30)
			tabname = tabname.substring(0, 30) + "...";
		return tabname;
	}
	
	public boolean closeTab() {
		return true;
	}
		
	public void setClosePolicy(MouseListener ml) {
		tablabel.removelabel.addMouseListener(ml);
	}
	
	public void setTabName(String title) {
		tablabel.titlelabel.setText(formatTabName(title));
	}
	
	public void addMouseListenertoTabLabel(MouseListener listener) {
		tablabel.addMouseListener(listener);
	}
	
	public boolean isModelLoaded() {
		return true;
	}
	
	abstract public boolean isSaved();
	
	abstract public void requestSave();
	
	abstract public void requestSaveAs();
	
	abstract public void requestExport();
	
	abstract public void requestEditModelLevelMetadata();
	
	public boolean modelAccessorMatches(ModelAccessor accessor) {
		return false;
	}
	
	public void executeMonitoredThread(SemGenJob sga) {
		GenericWorker worker = new GenericWorker(sga);
		worker.run();
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
