package semgen.annotation;

import java.awt.Component;
import java.awt.Font;
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
import javax.swing.tree.TreeSelectionModel;

import javax.swing.Painter;

import semgen.SemGenGUI;
import semsim.model.SemSimComponent;
import semsim.model.SemSimModel;
import semsim.model.computational.DataStructure;
import semsim.model.computational.MappableVariable;
import semsim.model.physical.FunctionalSubmodel;
import semsim.model.physical.Submodel;

public class AnnotatorButtonTree extends JTree implements TreeSelectionListener{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7868541704010347520L;
	public Annotator ann;
	public Set<DataStructure> looseds;
	public DefaultMutableTreeNode focusnode;
	public Map<AnnotationObjectButton, DefaultMutableTreeNode> nodebuttonmap = new HashMap<AnnotationObjectButton, DefaultMutableTreeNode>();

	public AnnotatorButtonTree(Annotator ann, DefaultMutableTreeNode root){
		super(root);
		
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
		DefaultMutableTreeNode submodelnode = new DefaultMutableTreeNode(ann.submodelbuttontable.get(sub.getName()));
		
		if(SemGenGUI.annotateitemshowimports.isSelected() || ann.getSubmodelButtonVisibility(ann.submodelbuttontable.get(sub.getName()))){
			curroot.add(submodelnode);
			nodebuttonmap.put(ann.submodelbuttontable.get(sub.getName()), submodelnode);
			ann.submodelbuttontable.get(sub.getName()).refreshAllCodes();
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

	
	
	class MyPainter implements Painter{
		public void paint(Graphics2D arg0, Object arg1, int arg2, int arg3) {
			arg0.setColor(SemGenGUI.lightblue);
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
	        	label.setIcon(SemGenGUI.homeiconsmall);
	        	label.setFont(new Font("SansSerif", Font.BOLD, SemGenGUI.defaultfontsize+1));
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
		Arrays.sort(ar, SemGenGUI.cic);
		return ar;
	}
	
	public void valueChanged(TreeSelectionEvent arg0) {
		
	    DefaultMutableTreeNode node = (DefaultMutableTreeNode) this.getLastSelectedPathComponent();

	    if (node == null)
	    	//Nothing is selected.     
	    return;

	    Object nodeObj = node.getUserObject();
	    
	    if(nodeObj instanceof AnnotationObjectButton){
	    	try {
				ann.annotationObjectAction((AnnotationObjectButton)nodeObj);
			} catch (BadLocationException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
	    }
	    else if(nodeObj instanceof SemSimModel){
	    	ann.showSelectAnnotationObjectMessage();
	    }
	}
}
