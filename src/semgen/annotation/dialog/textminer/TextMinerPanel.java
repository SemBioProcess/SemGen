package semgen.annotation.dialog.textminer;


import java.awt.BorderLayout;
import java.awt.Dimension;
import java.net.URI;

import javax.swing.JPanel;

import semgen.utilities.uicomponent.ExternalURLButton;
import semgen.utilities.uicomponent.MoreInfoButton;

public class TextMinerPanel extends JPanel{
	/**
	 * 
	 */
	private static final long serialVersionUID = -2531564431818333169L;
	public TextMinerCheckBox box;
	public String bioportalID;
	
	public TextMinerPanel(TextMinerCheckBox box, String onturi, String bioportalID, String termuri, String shortid){
		this.box = box;
		this.bioportalID = bioportalID;
		Dimension dim = new Dimension(700,35);
		this.setPreferredSize(dim);	
		this.setMaximumSize(dim);		
		this.setMinimumSize(dim);		
	
		setLayout(new BorderLayout());
		add(box, BorderLayout.WEST);
		JPanel moreinfopanel = new JPanel();
		MoreInfoButton mib = new MoreInfoButton(onturi,termuri,bioportalID, shortid);
		ExternalURLButton eub = new ExternalURLButton();
		eub.setTermURI(URI.create(termuri));
		moreinfopanel.add(mib);
		moreinfopanel.add(eub);
		add(moreinfopanel, BorderLayout.EAST);
	}
}