package semgen.annotation.common;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import semgen.annotation.dialog.SemSimComponentSelectionDialog;
import semgen.annotation.workbench.SemSimTermLibrary;
import semsim.SemSimConstants;
import semsim.annotation.SemSimRelation;

public abstract class CustomTermOptionPane extends JPanel implements ActionListener, KeyListener {
	private static final long serialVersionUID = 1L;
	protected SemSimTermLibrary library;
	protected Integer termindex = -1;
	
	protected JTextField mantextfield;
	protected JTextArea descriptionarea;
	protected ArrayList<ObjectPropertyEditor> objecteditors = new ArrayList<ObjectPropertyEditor>();
	
	protected JButton createbtn = new JButton("Create");
	protected JButton cancelbtn = new JButton("Cancel");
	protected JLabel msgbox = new JLabel("Please enter a valid name");
	
	public CustomTermOptionPane(SemSimTermLibrary lib) {
		library = lib;
		makeSharedComponents();
		makeUnique();
		finishPanel();
	}
	
	public CustomTermOptionPane(SemSimTermLibrary lib, Integer libindex) {
		library = lib;
		termindex = libindex;
		
		createbtn.setText("Modify");
		makeSharedComponents();
		makeUnique();
		finishPanel();
	}

	private void makeSharedComponents() {
		mantextfield = new JTextField();
		mantextfield.setEditable(true);
		mantextfield.setForeground(Color.blue);
		mantextfield.setPreferredSize(new Dimension(450, 28));
		mantextfield.addKeyListener(this);
		
		descriptionarea = new JTextArea();
		descriptionarea.setForeground(Color.blue);
		descriptionarea.setLineWrap(true);
		descriptionarea.setWrapStyleWord(true);
		
		JScrollPane descscroller = new JScrollPane(descriptionarea);
		descscroller.setPreferredSize(new Dimension(450,100));
		
		JPanel namepanel = new JPanel();
		namepanel.add(new JLabel("Name: "));
		namepanel.add(mantextfield);
		
		JPanel descriptionpanel = new JPanel();
		descriptionpanel.add(new JLabel("Description: "));
		descriptionpanel.add(descscroller);
		
		if (termindex!=-1) {
			mantextfield.setText(library.getComponentName(termindex));
			descriptionarea.setText(library.getComponentDescription(termindex));
			msgbox.setText("");
		}
		
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		add(namepanel);
		add(descriptionpanel);
	}
	
	protected void makeUnique() {
		ArrayList<Integer> versionrels = library.getIndiciesofReferenceRelations(termindex, SemSimConstants.BQB_IS_VERSION_OF_RELATION);
		objecteditors.add(new CustomEntityEditor(library, SemSimConstants.BQB_IS_VERSION_OF_RELATION, versionrels));
	}
	
	protected void finishPanel() {
		for (ObjectPropertyEditor editor : objecteditors) {
			add(editor);
		}
		createbtn.setEnabled(termindex!=-1);
		createbtn.addActionListener(this);
		cancelbtn.addActionListener(this);
		JPanel confirmpan = new JPanel();
		confirmpan.setLayout(new BoxLayout(confirmpan, BoxLayout.X_AXIS));
		confirmpan.add(msgbox);
		confirmpan.add(createbtn);
		confirmpan.add(cancelbtn);
		
		add(confirmpan);
		add(Box.createVerticalGlue());
		validate();
	}
	
	public String getTitle() {
		if (termindex==-1) return "Create Custom Physical Entity";
		return "Edit " + library.getComponentName(termindex);
	}
	
	protected void createTerm() {
		termindex = library.createCustomPhysicalEntity(mantextfield.getText(), descriptionarea.getText());
		for (ObjectPropertyEditor ope : objecteditors) {
			ope.setRelationships(termindex);
		}
	}
	
	protected void modifyTerm() {
		library.modifyCustomPhysicalEntity(termindex, mantextfield.getText(), descriptionarea.getText());
		for (ObjectPropertyEditor ope : objecteditors) {
			ope.setRelationships(termindex);
		}
	}
	
	public int getSelection() {
		return termindex;
	}
	
	@Override
	public void keyPressed(KeyEvent arg0) {
		
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		
	}

	@Override
	public void keyTyped(KeyEvent arg0) {
		String name = mantextfield.getText().trim();
		if (!name.isEmpty()) {
			int namechk = library.libraryHasName(name);
			if (namechk==-1 || namechk==termindex) {
				createbtn.setEnabled(true);
				msgbox.setText("");
			}
			else {
				createbtn.setEnabled(false);
				msgbox.setText("That name already exists");
			}
			return;
		}
		createbtn.setEnabled(false);
		msgbox.setText("Please enter a valid name");
	}
	
	private class CustomEntityEditor extends ObjectPropertyEditor {
		private static final long serialVersionUID = 1L;

		public CustomEntityEditor(SemSimTermLibrary lib, SemSimRelation rel,
				ArrayList<Integer> complist) {
			super(lib, rel, complist);
		}

		@Override
		protected void showSelectionDialog() {
			ArrayList<Integer> entities = library.getSortedReferencePhysicalEntityIndicies();
			ArrayList<Integer> preselected = new ArrayList<Integer>();
			for (Integer i : components) {
				preselected.add(entities.indexOf(i));
			}
			
			String dialogname = "Annotate " + mantextfield.getText() + " with " + relation.getURIFragment() + " relations.";
			SemSimComponentSelectionDialog seldialog = new SemSimComponentSelectionDialog(dialogname, library.getComponentNames(entities), preselected);
			
			preselected = seldialog.getSelections();
			
			setElements(preselected, entities);
		}

	}
	
}
