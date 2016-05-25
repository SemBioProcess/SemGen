package semgen.extraction;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;

import semsim.owl.SemSimOWLFactory;


public class ExtractorEntitiesTree extends JPanel {

	private static final long serialVersionUID = 1L;
	private JTree tree;
	private File testfile;

	public ExtractorEntitiesTree(File file) {

		testfile = file;

		Set<DefaultMutableTreeNode> roots = makeTreeFromQueryResult();
		DefaultMutableTreeNode[] testarray = (DefaultMutableTreeNode[]) roots
				.toArray(new DefaultMutableTreeNode[] {});
		DefaultMutableTreeNode bodyroot = new DefaultMutableTreeNode(
				"Body part");
		if (testarray.length > 1) {
			for (int i = 0; i < testarray.length; i++) {
				bodyroot.add(testarray[i]);
			}
		} else if (testarray.length == 1) {
			bodyroot.add(testarray[0]);
		}
		tree = new JTree(bodyroot);
		tree.getSelectionModel().setSelectionMode(
				TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);

		this.add(tree);
		this.setVisible(true);
	}

	public Set<DefaultMutableTreeNode> makeTreeFromQueryResult() {
		SAXBuilder builder = new SAXBuilder();
		Document doc = new Document();
		try {
			doc = builder.build(testfile);
		} catch (JDOMException | IOException e) {
			e.printStackTrace();
		}

		Element root = doc.getRootElement();

		List<?> rootchildren = root.getChildren();
		Iterator<?> iterator = rootchildren.iterator();
		doc.getRootElement().getNamespace("hier");
		Namespace rdfns = doc.getRootElement().getNamespace("rdf");
		Hashtable<String, Set<String>> urisandparents = new Hashtable<String, Set<String>>();
		new Hashtable<Object, Object>();
		new Hashtable<Object, Object>();
		while (iterator.hasNext()) {
			Element el = (Element) iterator.next();

			String clsname = el.getAttributeValue("about", rdfns);
			// Confusing: Parents for FMA classes, but children in query result
			List<?> children = el.getChildren();
			Iterator<?> childit = children.iterator();
			Set<String> parentset = new HashSet<String>();

			while (childit.hasNext()) {
				Element parentel = (Element) childit.next();
				String parenturi = parentel
						.getAttributeValue("resource", rdfns);
				parentset.add(parenturi);
			}
			urisandparents.put(clsname, parentset);
		}
		Hashtable<String, DefaultMutableTreeNode> urisandnodes = new Hashtable<String, DefaultMutableTreeNode>();
		new HashSet<DefaultMutableTreeNode>();
		for (String child : (Set<String>) urisandparents.keySet()) {
			Set<String> parents = (Set<String>) urisandparents.get(child);
			for (String parent : parents) {
				// Check if either child or parent already have nodes assigned
				DefaultMutableTreeNode childnode = new DefaultMutableTreeNode(
						SemSimOWLFactory.getIRIfragment(child));
				DefaultMutableTreeNode parentnode = new DefaultMutableTreeNode(
						SemSimOWLFactory.getIRIfragment(parent));
				if (urisandnodes.keySet().contains(child)) {
					childnode = (DefaultMutableTreeNode) urisandnodes
							.get(child);
				} else {
					urisandnodes.put(child, childnode);
				}
				if (urisandnodes.keySet().contains(parent)) {
					parentnode = (DefaultMutableTreeNode) urisandnodes
							.get(parent);
				} else {
					urisandnodes.put(parent, parentnode);
				}

				if (childnode.getParent() != null) {
					DefaultMutableTreeNode tempnode = (DefaultMutableTreeNode) childnode
							.clone();
					// copy over the parents of the node
					// tempnode.setParent((DefaultMutableTreeNode)childnode.getParent());
					// and copy over the children
					Enumeration<?> en = childnode.children();
					while (en.hasMoreElements()) {
						DefaultMutableTreeNode onechild = (DefaultMutableTreeNode) en
								.nextElement();

						tempnode.add(onechild);
					}
					childnode = tempnode;
				}
				parentnode.add(childnode);
			}
		}

		Set<DefaultMutableTreeNode> roots = new HashSet<DefaultMutableTreeNode>();
		for (String key : (Set<String>) urisandnodes.keySet()) {
			System.out.println("Looking at; " + key);
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) urisandnodes
					.get(key);
			if (node.getParent() == null) {
				roots.add(node);

				System.out.println(node.toString() + " has how many children? "
						+ node.getChildCount());
			}
		}
		return roots;
	}

}