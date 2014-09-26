package semgen.extraction;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;

import semsim.owl.SemSimOWLFactory;


public class ExtractorEntitiesTree extends JPanel implements ActionListener, ItemListener{

	public JTree tree;
	public String result;
	public File testfile;
	public JTree testtree;
	
	public ExtractorEntitiesTree(File file) {

		testfile = file;

		Set<DefaultMutableTreeNode> roots = makeTreeFromQueryResult();
		// tree = new JTree((DefaultMutableTreeNode[])roots.toArray(new
		// DefaultMutableTreeNode[]{}));
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
		// scroller = new JScrollPane(tree);

		this.add(tree);
		this.setVisible(true);
	}

	public Set<DefaultMutableTreeNode> makeTreeFromQueryResult() {
		SAXBuilder builder = new SAXBuilder();
		Document doc = new Document();
		try {
			doc = builder.build(testfile);
		} catch (JDOMException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		Element root = doc.getRootElement();

		// System.out.println(root.toString());
		List rootchildren = root.getChildren();
		Iterator iterator = rootchildren.iterator();
		Namespace heirns = doc.getRootElement().getNamespace("hier");
		Namespace rdfns = doc.getRootElement().getNamespace("rdf");
		Hashtable urisandparents = new Hashtable();
		Hashtable uriswithmultparents = new Hashtable();
		Hashtable uriswithsingleparents = new Hashtable();
		while (iterator.hasNext()) {
			Element el = (Element) iterator.next();

			String clsname = el.getAttributeValue("about", rdfns);
			// Confusing: Parents for FMA classes, but children in query result
			List children = el.getChildren();
			Iterator childit = children.iterator();
			Set<String> parentset = new HashSet();

			while (childit.hasNext()) {
				Element parentel = (Element) childit.next();
				String parenturi = parentel
						.getAttributeValue("resource", rdfns);
				parentset.add(parenturi);
			}
			urisandparents.put(clsname, parentset);
			// if(parentset.size()==1){
			// uriswithsingleparents.put(clsname, parentset);
			// }
			// else if(parentset.size()>1){
			// uriswithmultparents.put(clsname, parentset);
			// }
		}
		Hashtable urisandnodes = new Hashtable();
		Set<DefaultMutableTreeNode> nodeset = new HashSet();
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
					Enumeration en = childnode.children();
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

		Set<DefaultMutableTreeNode> roots = new HashSet();
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

	public void itemStateChanged(ItemEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void actionPerformed(ActionEvent arg0) {
		// TODO Auto-generated method stub
		
	}

}