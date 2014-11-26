package semgen.annotation.dialog.textminer;

import javax.swing.JCheckBox;

public class TextMinerCheckBox extends JCheckBox {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5844689436249332508L;
	public String uri;
	public String referenceonturi;

	public TextMinerCheckBox(String name, String uri, String refonturi) {
		super(name);
		this.uri = uri;
		referenceonturi = refonturi;
		this.setToolTipText(uri);
	}

}
