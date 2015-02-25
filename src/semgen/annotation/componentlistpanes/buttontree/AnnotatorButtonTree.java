package semgen.annotation.componentlistpanes.buttontree;

import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.UIDefaults;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.text.BadLocationException;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;
import javax.swing.Painter;

import semgen.SemGenSettings;
import semgen.annotation.AnnotatorTab;
import semgen.annotation.componentlistpanes.AnnotationObjectButton;
import semgen.annotation.componentlistpanes.codewords.CodewordButton;
import semgen.annotation.componentlistpanes.submodels.SubmodelButton;
import semgen.utilities.SemGenFont;
import semgen.utilities.SemGenIcon;
import semsim.model.SemSimComponent;
import semsim.model.SemSimModel;
import semsim.model.computational.datastructures.DataStructure;
import semsim.model.computational.datastructures.MappableVariable;
import semsim.model.physical.Submodel;
import semsim.model.physical.object.FunctionalSubmodel;
import semsim.writing.CaseInsensitiveComparator;

public class AnnotatorButtonTree extends JTree implements TreeSelectionListener{
	private static final long serialVersionUID = 7868541704010347520L;
	public AnnotatorTab ann;
	public Set<DataStructure> looseds;
	public DefaultMutableTreeNode focusnode;
	public Map<AnnotationObjectButton, DefaultMutableTreeNode> nodebuttonmap = new HashMap<AnnotationObjectButton, DefaultMutableTreeNode>();
	protected SemGenSettings settings;
	
	public AnnotatorButtonTree(AnnotatorTab ann, SemGenSettings sets, DefaultMutableTreeNode root){
		super(root);
		
		settings = sets;
		this.ann = ann;
		
		UIDefaults dialogTheme = new UIDefaults();
		dialogTheme.put("Tree:TreeCell[Focused+Selected].backgroundPainter", new MyPainter());
		dialogTheme.put("Tree:TreeCell[Enabled+Selected].backgroundPainter", new MyPainter());
		putClientProperty("Nimbus.Overrides.InheritDefaults", true);
		putClientProperty("Nimbus.Overrides", dialogTheme);
		
		MyRenderer renderer = new MyRenderer();
		renderer.setIconTextGap(0);
	    setCellRenderer(renderer);
		setLargeModel(true);
		addTreeSelectionListener(this);
		this.addMouseListener(new NodeClickEvent());
		looseds = new HashSet<DataStructure>();
		looseds.addAll(ann.semsimmodel.getDataStructures());
		
		for(String subname : getSortedNamesOfSemSimComponents(ann.semsimmodel.getSubmodels())){
    		Submodel sub = ann.semsimmodel.getSubmodel(subname);
    		traverseSubmodelBranch(root, sub);
		}
		
		for(String dsname : getSortedNamesOfSemSimComponents(looseds)){
			AnnotationObjectButton cbutton = ann.codewordbuttontable.get(dsname);
			((CodewordButton)cbutton).refreshAllCodes();
			DefaultMutableTreeNode dsnode = new DefaultMutableTreeNode(cbutton);
			root.add(dsnode);
		}
		
		getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		setVisible(true);
		if(ann.focusbutton!=null) focusnode = nodebuttonmap.get(ann.focusbutton);
		update(root, this);
	}
	
	private void traverseSubmodelBranch(DefaultMutableTreeNode curroot, Submodel sub){
		SubmodelButton smb = ann.submodelbuttontable.get(sub.getName());
		DefaultMutableTreeNode submodelnode = new DefaultMutableTreeNode(smb);
		
		if(settings.showImports() || ann.getSubmodelButtonVisibility(smb)){
			curroot.add(submodelnode);
			nodebuttonmap.put(smb, submodelnode);
			smb.refreshAllCodes();
		}
		
		// Get all the data structures in the submodel
		for(String name : getSortedNamesOfSemSimComponents(sub.getAssociatedDataStructures())){
			DataStructure ds = ann.semsimmodel.getDataStructure(name);
        	if(ann.codewordbuttontable.containsKey(name)){
        		AnnotationObjectButton cbutton = ann.codewordbuttontable.get(name);
        		cbutton.namelabel.setText(name.substring(name.lastIndexOf(".")+1)); // Needed to keep codeword names short
        		DefaultMutableTreeNode dsnode = new DefaultMutableTreeNode(ann.codewordbuttontable.get(name));
        		
        			submodelnode.add(dsnode);
        			nodebuttonmap.put(ann.codewordbuttontable.get(name), dsnode);
        		        	}
			if(sub instanceof FunctionalSubmodel && ds instanceof MappableVariable){
				looseds.remove(ds);
			}
		}
	}

	class MyPainter implements Painter<Object>{
		public void paint(Graphics2D arg0, Object arg1, int arg2, int arg3) {
			arg0.setColor(SemGenSettings.lightblue);
			arg0.fillRect( 0, 0, arg2, arg3 );
		}
	}
	
	public void update( final DefaultMutableTreeNode node, final JTree tree ) {
	    SwingUtilities.invokeLater( new Runnable() {
	        public void run() {
	        	((DefaultTreeModel) tree.getModel()).nodeChanged(node);
	        }
	    });
	}
	
	class MyRenderer extends DefaultTreeCellRenderer {
		private static final long serialVersionUID = 1L;

		public Component getTreeCellRendererComponent(
	                        JTree tree,
	                        Object value,
	                        boolean sel,
	                        boolean expanded,
	                        boolean leaf,
	                        int row,
	                        boolean hasFocus) {

	        super.getTreeCellRendererComponent(
	                        tree, value, sel,
	                        expanded, leaf, row,
	                        hasFocus);
            
	        Object userObj = ((DefaultMutableTreeNode)value).getUserObject();
	        
	        if(userObj instanceof AnnotationObjectButton){
	        	return (AnnotationObjectButton)userObj;
	        }
	        else if(userObj instanceof SemSimModel){
	        	JLabel label = new JLabel(((SemSimModel)userObj).getName());
	        	label.setIcon(SemGenIcon.homeiconsmall);
	        	label.setFont(SemGenFont.defaultBold(1));
	        	JPanel panel = new JPanel();
	        	panel.setOpaque(false);
	        	panel.add(label);
	        	return panel;
	        }
	        else 
	        	return new JLabel("???");
	    }
	}
	
	public ArrayList<String> getSortedNamesOfSemSimComponents(Set<? extends SemSimComponent> set){
		ArrayList<String> ar = new ArrayList<String>();
		for(SemSimComponent ssc : set ){
			ar.add(ssc.getName());
		}
		Collections.sort(ar, new CaseInsensitiveComparator());
		return ar;
	}
	
	private void changeNode() {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) this.getLastSelectedPathComponent();

	    if (node == null) //Nothing is selected.   
	    	return;

	    Object nodeObj = node.getUserObject();
	    
	    if(nodeObj instanceof AnnotationObjectButton){
	    	try {
				ann.annotationObjectAction((AnnotationObjectButton)nodeObj);
			} catch (BadLocationException | IOException e) {
				e.printStackTrace();
			} 
	    }
	    else if(nodeObj instanceof SemSimModel){
	    	ann.showSelectAnnotationObjectMessage();
	    }
	}
	
	public void valueChanged(TreeSelectionEvent arg0) {
		changeNode();
	}
	
	private class NodeClickEvent extends MouseAdapter {
		public void mouseClicked(MouseEvent arg0) {
			changeNode();
		}
	}
}
