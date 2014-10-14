package semgen;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;


public class LogViewer extends JDialog implements PropertyChangeListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8222166235631229362L;
	private JOptionPane optionPane;
	private Object[] options;
	private JScrollPane scrollpane;
	private JPanel panel;
	private JTextArea textarea;
	private Scanner logscanner;
	private String nextline = "";

	private int initwidth = 800;
	private int initheight = 650;

	public LogViewer() throws FileNotFoundException {

		this.setTitle("Session log file");
		this.setResizable(true);

		textarea = new JTextArea();
		textarea.setEditable(false);

		textarea.setBackground(new Color(250, 250, 250));
		textarea.setText(null);
		textarea.setMargin(new Insets(5, 15, 5, 5));
		textarea.setForeground(Color.DARK_GRAY);
		textarea.setFont(new Font("SansSerif", Font.PLAIN, 12));
		SemGenGUI.logfilewriter.flush();

		File logfile = new File(SemGenGUI.logfileloc);
		logscanner = new Scanner(logfile);
		while (logscanner.hasNextLine()) {
			nextline = logscanner.nextLine();
			textarea.append(nextline);
			textarea.append("\n");
			textarea.setCaretPosition(0);
		}

		panel = new JPanel();
		panel.add(textarea);

		scrollpane = new JScrollPane(panel);
		this.add(scrollpane);
		scrollpane.setPreferredSize(new Dimension(initwidth - 10, initheight - 10));
		scrollpane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollpane.getVerticalScrollBar().setUnitIncrement(12);
		scrollpane.getHorizontalScrollBar().setUnitIncrement(12);

		Object[] array = { scrollpane };

		optionPane = new JOptionPane(array, JOptionPane.PLAIN_MESSAGE,JOptionPane.OK_OPTION, null);
		optionPane.addPropertyChangeListener(this);
		options = new Object[] { "Close" };
		optionPane.setOptions(options);
		optionPane.setInitialValue(options[0]);

		setContentPane(optionPane);

		this.setModal(true);
		this.setSize(initwidth, initheight);
		this.pack();
		this.setLocationRelativeTo(null);
		this.setVisible(true);
	}

	public final void propertyChange(PropertyChangeEvent e) {

		String value = optionPane.getValue().toString();
		if (value == "Close") {
			this.setVisible(false);
		}
	}
}
