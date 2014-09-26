package semgen.annotation.uicomponents;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import semgen.resource.SemGenFont;
import semgen.resource.SemGenResource;

public abstract class AnnotationObjectButton extends JPanel implements FocusListener {
	private static final long serialVersionUID = 1L;

	public Boolean editable;
	protected JLabel namelabel = new JLabel();
	protected JLabel humdeflabel = new JLabel("_");
	protected JLabel singularannlabel = new JLabel("_");;
	protected ArrayList<JLabel> lbls = new ArrayList<JLabel>();
	
	public JPanel indicatorspanel = new JPanel();
	public JPanel indicatorssuperpanel = new JPanel();

	public int maxHeight = 35, ipph = 18;
	
	public AnnotationObjectButton() {
		setLayout(new BorderLayout());
		setFocusable(true);
		addFocusListener(this);
		setMaximumSize(new Dimension(999999, maxHeight));
		
		((BorderLayout)getLayout()).setVgap(0);
		((BorderLayout)getLayout()).setHgap(0);
		
		namelabel.setFont(SemGenFont.defaultPlain());
		namelabel.setOpaque(false);
		namelabel.setBorder(BorderFactory.createEmptyBorder(0, 3, 0, 10));
		namelabel.setBackground(new Color(0,0,0,0));
		addMouseListener(new annBtnlMouseAdaptor());
				
		indicatorspanel.setPreferredSize(new Dimension(50, ipph));
		indicatorspanel.setLayout(new BoxLayout(indicatorspanel, BoxLayout.X_AXIS));
		indicatorspanel.setAlignmentY(TOP_ALIGNMENT);
		indicatorspanel.setOpaque(false);	

		indicatorspanel.setOpaque(false);
		
		indicatorssuperpanel.setOpaque(false);
		indicatorssuperpanel.setLayout(new BorderLayout());
		indicatorssuperpanel.add(Box.createGlue(), BorderLayout.WEST);	

		add(Box.createGlue(), BorderLayout.EAST);
		add(namelabel, BorderLayout.CENTER);

		setBackground(Color.white);
		setBorder(BorderFactory.createEmptyBorder(1, 2, 1, 2));
		setOpaque(true);
		setForeground(Color.black);
		setVisible(true);
	}
	
	public abstract void assignButton(String name, Boolean[] list);
	
	public void makelabels() {
		lbls.add(singularannlabel);
		lbls.add(humdeflabel);

		for (JLabel lbl : lbls) {
			lbl.setFont(SemGenFont.Plain("Serif",-3));
			lbl.setAlignmentY(JComponent.CENTER_ALIGNMENT);
			lbl.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
			if(editable){	
				lbl.addMouseListener(new propLabelMouseAdaptor());
			}
			
			indicatorspanel.add(lbl);
		}	
			
		singularannlabel.setName("S");
		singularannlabel.setToolTipText("Click to set singular reference annotation");
		
		humdeflabel.setName("F");
		humdeflabel.setToolTipText("Click to set free-text description");
		indicatorssuperpanel.add(indicatorspanel, BorderLayout.CENTER);
		add(indicatorssuperpanel, BorderLayout.WEST);
	}

	public void setIdentifyingData(String name){
		this.setName(name);
		namelabel.setText(name);
	}
	
	public abstract void refreshAllCodes(Boolean[] anneds);
		
	public void refreshFreeTextCode(boolean isAnn){
		annotationAdded(humdeflabel, isAnn);
	}

	public void refreshSingAnnCode(boolean isAnn){
		annotationAdded(singularannlabel, isAnn);
	}
	
	public void annotationAdded(JLabel label, Boolean isann) {
		label.setFont(SemGenFont.Bold("Serif",-2));
		label.setForeground(Color.gray);
		if (isann) {
			label.setText(label.getText());
			if(editable) label.setForeground(Color.blue);
		}
		else {
			label.setText("_");
		}
		validate();
		repaint();
	}

	protected void changeBackground(Color col) {
		if (getBackground()!=SemGenResource.lightblue)
				setBackground(col);
	}
		
	public class annBtnlMouseAdaptor extends MouseAdapter {
		public void mouseClicked(MouseEvent e) {
			requestFocusInWindow();
		}
		
		public void mouseEntered(MouseEvent e) {
			changeBackground(Color.lightGray);
		}

		public void mouseExited(MouseEvent e) {
			changeBackground(Color.white);
		}
	}
	
	protected class propLabelMouseAdaptor extends MouseAdapter {
		public void mouseEntered(MouseEvent e) {
			JComponent label = (JComponent)e.getComponent();
			label.setOpaque(true);
			label.setBackground(new Color(255,231,186));
			label.setCursor(new Cursor(Cursor.HAND_CURSOR));
			changeBackground(Color.lightGray);
		}

		public void mouseExited(MouseEvent e) {
			JComponent label = (JComponent)e.getComponent();
			label.setOpaque(false);
			label.setBackground(null);
			label.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			changeBackground(Color.white);
			label.firePropertyChange(label.getName(), 0, 1);
		}
		
		public void mouseClicked(MouseEvent e) {
			requestFocusInWindow();		
		}
	}
	
	public void focusGained(FocusEvent e) {
		setBackground(SemGenResource.lightblue);
	}

	public void focusLost(FocusEvent e) {
		setBackground(Color.white);
	}
	
}
