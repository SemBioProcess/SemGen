package semgen.annotation.componentlistpanes;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.border.TitledBorder;

import org.apache.commons.collections15.bidimap.DualHashBidiMap;

import semgen.SemGenSettings;
import semgen.SemGenSettings.SettingChange;
import semgen.annotation.componentlistpanes.buttons.AnnotationObjectButton;
import semgen.annotation.workbench.AnnotatorWorkbench;
import semgen.annotation.workbench.AnnotatorWorkbench.ModelEdit;
import semgen.annotation.workbench.drawers.AnnotatorDrawer;
import semgen.utilities.SemGenFont;
import semgen.utilities.uicomponent.SemGenScrollPane;

public abstract class AnnotatorListPane<T extends AnnotationObjectButton, D extends AnnotatorDrawer<?>> extends SemGenScrollPane implements KeyListener, Observer {
	private static final long serialVersionUID = 1L;
	protected AnnotatorWorkbench workbench;
	protected D drawer;
	protected SemGenSettings settings;
	protected ArrayList<T> btnarray = new ArrayList<T>();
	protected DualHashBidiMap<T, Integer> btnlist = new DualHashBidiMap<T, Integer>();
	protected JPanel buttonpane = new JPanel();
	protected T focusbutton;
	
	public AnnotatorListPane(AnnotatorWorkbench wb, SemGenSettings sets, D tooldrawer) {
		workbench = wb;
		settings = sets;
		drawer = tooldrawer;
		
		workbench.addObserver(this);
		drawer.addObserver(this);
		settings.addObserver(this);
		addKeyListener(this);
		
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
		
		int index = drawer.getSelectedIndex();
		if (index !=-1 && btnarray.contains(index)) {
			changeButtonFocus(btnarray.get(index));
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
		if (!btnarray.contains(focusbutton)) return;
		int index = btnarray.indexOf(focusbutton);

		//The scrollpane scrolls scrolls one component short of where it should when the down key is pressed
		int scrollindex = index; 
		// Up arrow key
		if (id == KeyEvent.VK_UP) {
			index--;
			if(index==-1) {
				index = btnarray.size()-1;	
				scrollToBottom();
			}
			if (index!=0) {
				scrollindex = index-1;
				}
				else scrollToTop();
		}
		// Down arrow key
		if (id == KeyEvent.VK_DOWN) {
			index++;
			if(index == btnarray.size()) {
				index = 0;
				scrollToTop();
			}
			else {
				if (index!=btnarray.size()-1) {
				scrollindex = index+1;
				}
				else scrollToBottom();
			}
		}
		changeButtonFocus(btnarray.get(index));
		scrollToComponent(btnarray.get(scrollindex));
	}

	public void keyReleased(KeyEvent e) {}
	public void keyTyped(KeyEvent e) {}
	
	public void destroy() {
		btnlist.clear();
		btnarray.clear();
		buttonpane.removeAll();
	}
	
	public void update(Observable o, Object arg) {
		if (!settings.useTreeView()) {
			if (arg==SettingChange.TOGGLETREE && settings.useTreeView()) {
				destroy();
			}
			if (o==drawer) {
				if (focusbutton!=null) {
					if (arg==ModelEdit.FREE_TEXT_CHANGED) {
						focusbutton.toggleHumanDefinition(drawer.hasHumanReadableDef());
					}
				}
			}
			updateUnique(arg);
		}
	}
	
	protected abstract void refreshChangedButtons();
	protected abstract void updateButtonTable();
	protected abstract void updateUnique(Object arg);
}
