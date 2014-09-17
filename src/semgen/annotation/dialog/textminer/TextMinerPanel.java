package semgen.annotation.dialog.textminer;


import java.awt.BorderLayout;
import java.awt.Dimension;
import java.net.URI;

import javax.swing.JPanel;

import semgen.resource.uicomponents.ExternalURLButton;
import semgen.resource.uicomponents.MoreInfoButton;

public class TextMinerPanel extends JPanel{

	private static final long serialVersionUID = -2531564431818333169L;
	public TextMinerCheckBox box;
	public String bioportalID;
	
	public TextMinerPanel(TextMinerCheckBox box, String onturi, String bioportalID, String termuri){
		Dimension dim = new Dimension(700,35);
		setPreferredSize(dim);	
		setMaximumSize(dim);		
		setMinimumSize(dim);		
		
		setLayout(new BorderLayout());
		add(box, BorderLayout.WEST);
		JPanel moreinfopanel = new JPanel();
		MoreInfoButton mib = new MoreInfoButton(onturi,termuri,bioportalID);
		ExternalURLButton eub = new ExternalURLButton();
		eub.setTermURI(URI.create(termuri));
		moreinfopanel.add(mib);
		moreinfopanel.add(eub);
		add(moreinfopanel, BorderLayout.EAST);
	}
}
