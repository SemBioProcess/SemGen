package semgen.annotation.componentlistpanes;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Observer;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.border.TitledBorder;

import semgen.SemGenSettings;
import semgen.annotation.componentlistpanes.buttons.AnnotationObjectButton;
import semgen.annotation.workbench.AnnotatorWorkbench;
import semgen.annotation.workbench.drawers.AnnotatorDrawer;
import semgen.utilities.SemGenFont;
import semgen.utilities.uicomponent.SemGenScrollPane;

public abstract class AnnotatorListPane<T extends AnnotationObjectButton, D extends AnnotatorDrawer<?>> extends SemGenScrollPane implements KeyListener, Observer {
	private static final long serialVersionUID = 1L;
	protected AnnotatorWorkbench workbench;
	protected D drawer;
	protected SemGenSettings settings;
	protected ArrayList<T> btnarray = new ArrayList<T>();
	protected LinkedHashMap<T, Integer> btnlist = new LinkedHashMap<T, Integer>();
	protected JPanel buttonpane = new JPanel();
	protected T focusbutton;
	
	public AnnotatorListPane(AnnotatorWorkbench wb, SemGenSettings sets, D tooldrawer) {
		workbench = wb;
		settings = sets;
		drawer = tooldrawer;
		
		workbench.addObserver(this);
		drawer.addObserver(this);
		settings.addObserver(this);
		
		buttonpane.setBackground(Color.white);
		buttonpane.setLayout(new BoxLayout(buttonpane, BoxLayout.Y_AXIS));
		setViewportView(buttonpane);
		
		InputMap im = getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		// Override up and down key functions so user can use arrows to move between codewords
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "none");
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "none");
		scrollToTop();
	}
	
	public void addPanelTitle(String type, int totalcount, String zerocountmsg) {
		setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.gray), 
				type + "(" + totalcount + ")", TitledBorder.CENTER, TitledBorder.TOP, SemGenFont.defaultBold(2)));
		if (totalcount == 0 && type.equals("Codeword ")) {
			getViewport().add(new JLabel(zerocountmsg));
		}
	}
	
	public void update() {
		destroy();
		updateButtonTable();
		
		if (drawer.getSelectedIndex()!=-1) {
			changeButtonFocus(btnarray.get(drawer.getSelectedIndex()));
		}

		buttonpane.validate();
		buttonpane.repaint();
	}
		
	protected void changeButtonFocus(T focus) {
		requestFocusInWindow();
		if(focusbutton!=null){
			focusbutton.setBackground(Color.white);
		}
		focusbutton = focus;
		focusbutton.setBackground(SemGenSettings.lightblue);
		scrollToComponent(focusbutton);
		drawer.setSelectedIndex(btnlist.get(focus));
	}
	
	protected void addButton(T btn, Integer index) {
		btnlist.put(btn, index);
		btnarray.add(btn);
		buttonpane.add(btn);
		btn.addMouseListener(btn);
	}
	

	
	public void keyPressed(KeyEvent e) {
		int id = e.getKeyCode();
		int index = btnarray.indexOf(focusbutton);
		// Up arrow key
		if (id == 38) {
			index++;
			if(index == btnarray.size()) index = 0;
		}
		// Down arrow key
		if (id == 40) {
			index--;
			if(index!=-1) index = btnarray.size()-1;
		}
		changeButtonFocus(btnarray.get(index));
	}

	public void keyReleased(KeyEvent e) {}
	public void keyTyped(KeyEvent e) {}
	
	protected abstract void updateButtonTable();
	
	public void destroy() {
		btnlist.clear();
		btnarray.clear();
		buttonpane.removeAll();
	}
}
