package semgen.annotation.annotationtree;

import java.awt.Component;
import java.awt.Graphics2D;
import java.io.IOException;
import java.util.Arrays;
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
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import javax.swing.Painter;

import semgen.SemGenGUI;
import semgen.annotation.AnnotatorTab;
import semgen.annotation.codewordpane.CodewordButton;
import semgen.annotation.uicomponents.AnnotationObjectButton;
import semgen.annotation.workbench.AnnotatorWorkbench;
import semgen.annotation.workbench.CodewordAnnotations;
import semgen.annotation.workbench.SubModelAnnotations;
import semgen.resource.SemGenIcon;
import semgen.resource.SemGenFont;
import semgen.resource.SemGenResource;
import semsim.model.SemSimComponent;
import semsim.model.SemSimModel;
import semsim.model.computational.DataStructure;
import semsim.model.computational.MappableVariable;
import semsim.model.physical.FunctionalSubmodel;
import semsim.model.physical.Submodel;
import semsim.writing.CaseInsensitiveComparator;

public class AnnotatorButtonTree extends JTree implements TreeSelectionListener{
	
	private static final long serialVersionUID = 7868541704010347520L;
	public DefaultMutableTreeNode focusnode;
	public Map<AnnotationObjectButton, DefaultMutableTreeNode> nodebuttonmap = new HashMap<AnnotationObjectButton, DefaultMutableTreeNode>();
	private SubModelAnnotations sma;
	private CodewordAnnotations cwa;
	
	public AnnotatorButtonTree(DefaultMutableTreeNode root, AnnotatorWorkbench canvas){
		super(root);
		cwa = canvas.cwa;
		sma = canvas.sma;
		
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
		
		for(String subname : getSortedNamesOfSemSimComponents(semsimmodel.getSubmodels())){
    		Submodel sub = semsimmodel.getSubmodel(subname);
    		traverseSubmodelBranch(root, sub);
		}
		
		for(String dsname : getSortedNamesOfSemSimComponents(looseds)){
			AnnotationObjectButton cbutton = codewordbuttontable.get(dsname);
			((CodewordButton)cbutton).refreshAllCodes();
			DefaultMutableTreeNode dsnode = new DefaultMutableTreeNode(cbutton);
			root.add(dsnode);
		}
		
		getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		setVisible(true);
		if(ann.focusbutton!=null) focusnode = nodebuttonmap.get(focusbutton);
		
		
		update(root, this);
	}
	
	public void displayTree() {
		TreeNode firstChild = ((DefaultMutableTreeNode) getModel().getRoot()).getChildAt(0);
		setSelectionPath(new TreePath(((DefaultMutableTreeNode)firstChild).getPath()));
	}
	
	private void traverseSubmodelBranch(DefaultMutableTreeNode curroot){
		DefaultMutableTreeNode submodelnode = new DefaultMutableTreeNode(submodelbuttontable.get(sub.getName()));
		
		if(SemGenGUI.annotateitemshowimports.isSelected() || submodelbuttontable.get(sub.getName()).getSubmodelButtonVisibility()){
			curroot.add(submodelnode);
			nodebuttonmap.put(submodelbuttontable.get(sub.getName()), submodelnode);
			submodelbuttontable.get(sub.getName()).refreshAllCodes();
		}
		
		// Get all the data structures in the submodel
		for(String name : getSortedNamesOfSemSimComponents(sub.getAssociatedDataStructures())){
			DataStructure ds = semsimmodel.getDataStructure(name);
        	if(codewordbuttontable.containsKey(name)){
        		AnnotationObjectButton cbutton = codewordbuttontable.get(name);
        		cbutton.namelabel.setText(name.substring(name.lastIndexOf(".")+1)); // Needed to keep codeword names short
        		DefaultMutableTreeNode dsnode = new DefaultMutableTreeNode(codewordbuttontable.get(name));
        		
        		submodelnode.add(dsnode);
        		nodebuttonmap.put(codewordbuttontable.get(name), dsnode);
        	}
			if(sub instanceof FunctionalSubmodel && ds instanceof MappableVariable){
				looseds.remove(ds);
			}
		}
	}

	class MyPainter implements Painter<Object>{
		public void paint(Graphics2D arg0, Object arg1, int arg2, int arg3) {
			arg0.setColor(SemGenResource.lightblue);
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
	
	public void valueChanged(TreeSelectionEvent arg0) {
	    DefaultMutableTreeNode node = (DefaultMutableTreeNode) this.getLastSelectedPathComponent();

	    if (node == null) return;//Nothing is selected. 

	    Object nodeObj = node.getUserObject();
	    
	    if(nodeObj instanceof AnnotationObjectButton){
	    	try {
				annotationObjectAction((AnnotationObjectButton)nodeObj);
			} catch (BadLocationException|IOException e) {
				e.printStackTrace();
			}
	    }
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
	        else return new JLabel("???");
	    }
	}
	
	public String[] getSortedNamesOfSemSimComponents(Set<? extends SemSimComponent> set){
		String[] ar = new String[set.size()];
		int i = 0;
		for(SemSimComponent ssc : set ){
			ar[i] = ssc.getName();
			i++;
		}
		Arrays.sort(ar, new CaseInsensitiveComparator());
		return ar;
	}
	

}
