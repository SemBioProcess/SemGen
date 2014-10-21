package semgen.annotation.buttontree;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;

public class BrowseTree extends JPanel {
	private static final long serialVersionUID = -6159443940192255692L;
	private String[] test = { "file" };
	public JTree tree;

	public BrowseTree() {

		DefaultMutableTreeNode top = new DefaultMutableTreeNode("Folder");
		createNodes(top);

		tree = new JTree(top);

		// Create a tree that allows one selection at a time.
		tree.getSelectionModel().setSelectionMode(
				TreeSelectionModel.SINGLE_TREE_SELECTION);
		tree.setVisible(true);
		tree.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		JScrollPane scroller = new JScrollPane(tree);

		this.add(scroller);
		this.setVisible(true);
	}

	public void createNodes(DefaultMutableTreeNode root) {
		for (int i = 0; i < test.length; i++) {
			DefaultMutableTreeNode temp = new DefaultMutableTreeNode(test[i]);
			root.add(temp);
		}
	}
}
