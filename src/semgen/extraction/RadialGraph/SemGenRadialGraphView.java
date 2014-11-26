package semgen.extraction.RadialGraph;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.ToolTipManager;

import prefuse.Constants;
import prefuse.Display;
import prefuse.Visualization;
import prefuse.action.ActionList;
import prefuse.action.GroupAction;
import prefuse.action.ItemAction;
import prefuse.action.RepaintAction;
import prefuse.action.animate.ColorAnimator;
import prefuse.action.animate.PolarLocationAnimator;
import prefuse.action.animate.QualityControlAnimator;
import prefuse.action.animate.VisibilityAnimator;
import prefuse.action.assignment.ColorAction;
import prefuse.action.assignment.FontAction;
import prefuse.action.layout.CollapsedSubtreeLayout;
import prefuse.action.layout.graph.RadialTreeLayout;
import prefuse.activity.SlowInSlowOutPacer;
import prefuse.controls.ControlAdapter;
import prefuse.controls.DragControl;
import prefuse.controls.HoverActionControl;
import prefuse.controls.PanControl;
import prefuse.controls.ToolTipControl;
import prefuse.controls.ZoomControl;
import prefuse.controls.ZoomToFitControl;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.Table;
import prefuse.data.Tuple;
import prefuse.data.event.TupleSetListener;
import prefuse.data.expression.Predicate;
import prefuse.data.expression.parser.ExpressionParser;
import prefuse.data.io.GraphMLReader;
import prefuse.data.query.SearchQueryBinding;
import prefuse.data.search.PrefixSearchTupleSet;
import prefuse.data.search.SearchTupleSet;
import prefuse.data.tuple.DefaultTupleSet;
import prefuse.data.tuple.TupleSet;
import prefuse.render.AbstractShapeRenderer;
import prefuse.render.DefaultRendererFactory;
import prefuse.render.EdgeRenderer;
import prefuse.render.LabelRenderer;
import prefuse.util.ColorLib;
import prefuse.util.FontLib;
import prefuse.util.ui.JSearchPanel;
import prefuse.util.ui.UILib;
import prefuse.visual.EdgeItem;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualItem;
import prefuse.visual.expression.InGroupPredicate;
import prefuse.visual.sort.TreeDepthItemSorter;
import semgen.SemGenSettings;
import semgen.extraction.ExtractorTab;
import semsim.model.SemSimModel;

/**
 * Demonstration of a node-link tree viewer
 * 
 * @version 1.0
 * @author <a href="http://jheer.org">jeffrey heer</a>
 */
public class SemGenRadialGraphView extends Display {
	private static final long serialVersionUID = 8464331927479988729L;

	SemGenSettings settings;
	
	public static final String DATA_FILE = "/socialnet.xml";
	private static final String tree = "tree";
	private static final String treeNodes = "tree.nodes";
	private static final String treeEdges = "tree.edges";
	private static final String linear = "linear";
	private static final String neighbors = "neighbors";
	private static final String nonneighbors = "nonneighbors";

	public SemSimModel semsimmodel;
    public final static Predicate isinputfilter = ExpressionParser.predicate("input==true");
    public final static Predicate isvar2inputfilter = ExpressionParser.predicate("var2input==true");

    
	public SemGenRadialGraphView(SemGenSettings sets, Graph g, String label, SemSimModel semsimmodel) {
		super(new Visualization());
		settings = sets;
		this.semsimmodel = semsimmodel;

		// -- set up visualization --
		m_vis.add(tree, g);
		m_vis.setInteractive(treeEdges, null, false);

		// -- set up renderers --
		LabelRenderer m_nodeRenderer = new LabelRenderer(label);
		m_nodeRenderer.setRenderType(AbstractShapeRenderer.RENDER_TYPE_DRAW_AND_FILL);
		m_nodeRenderer.setHorizontalAlignment(Constants.CENTER);
		m_nodeRenderer.setRoundedCorner(8, 8);
		EdgeRenderer m_edgeRenderer = new EdgeRenderer(Constants.EDGE_TYPE_LINE,
				prefuse.Constants.EDGE_ARROW_REVERSE);
		m_edgeRenderer.setArrowType(prefuse.Constants.EDGE_ARROW_REVERSE);
		m_edgeRenderer.setArrowHeadSize(8, 8);

		// MAYBE HERE?
		DefaultRendererFactory rf = new DefaultRendererFactory(m_nodeRenderer);
		rf.add(new InGroupPredicate(treeEdges), m_edgeRenderer);
		m_vis.setRendererFactory(rf);

		// -- set up processing actions --
		// colors
		ItemAction nodeColor = new NodeColorAction(treeNodes);
        ItemAction borderColor = new BorderColorAction(treeNodes);
        m_vis.putAction("borderColor", borderColor);
		ItemAction textColor = new TextColorAction(treeNodes);
		m_vis.putAction("textColor", textColor);
		ItemAction arrowColorStroke = new ArrowColorStrokeAction(treeEdges);
		m_vis.putAction("arrowStrokeColor", arrowColorStroke);
		ItemAction arrowColorFill = new ArrowColorFillAction(treeEdges);
		m_vis.putAction("arrowFillColor", arrowColorFill);

		ItemAction fontStyle = new NodeFontAction(treeNodes, FontLib.getFont("Verdana",11));

		// recolor
		ActionList recolor = new ActionList();
		recolor.add(borderColor);
		recolor.add(nodeColor);
		recolor.add(textColor);
		recolor.add(arrowColorStroke);
		recolor.add(arrowColorFill);
		m_vis.putAction("recolor", recolor);

		// repaint
		ActionList repaint = new ActionList();
		repaint.add(recolor);
		repaint.add(new RepaintAction());
		m_vis.putAction("repaint", repaint);

		// animate paint change
		ActionList animatePaint = new ActionList(400);
		animatePaint.add(new ColorAnimator(treeNodes));
		animatePaint.add(new RepaintAction());
		m_vis.putAction("animatePaint", animatePaint);

		// create the tree layout action
		RadialTreeLayout treeLayout = new RadialTreeLayout(tree);
		treeLayout.setAutoScale(true);
		m_vis.putAction("treeLayout", treeLayout);

		CollapsedSubtreeLayout subLayout = new CollapsedSubtreeLayout(tree);
		m_vis.putAction("subLayout", subLayout);
		
		// create the filtering and layout
		ActionList filter = new ActionList();
		filter.add(new TreeRootAction(tree));
		filter.add(fontStyle);
		filter.add(treeLayout);
		filter.add(subLayout);
		filter.add(textColor);
		filter.add(borderColor);
		filter.add(nodeColor);
		filter.add(arrowColorStroke);
		filter.add(arrowColorFill);
		m_vis.putAction("filter", filter);


		
		// animated transition
		ActionList animate = new ActionList(700);
		animate.setPacingFunction(new SlowInSlowOutPacer());
		animate.add(new QualityControlAnimator());
		animate.add(new VisibilityAnimator(tree));
		animate.add(new PolarLocationAnimator(treeNodes, linear));
		animate.add(new ColorAnimator(treeNodes));
		animate.add(new RepaintAction());
		m_vis.putAction("animate", animate);
		m_vis.alwaysRunAfter("filter", "animate");
		// ------------------------------------------------

		// initialize the display
		setSize(settings.getAppWidth()-ExtractorTab.leftpanewidth-50, settings.getAppHeight()-235);
		setItemSorter(new TreeDepthItemSorter());
		addControlListener(new DragControl());
		addControlListener(new ZoomToFitControl());
		addControlListener(new ZoomControl());
		addControlListener(new PanControl());
		addControlListener(new SemGenRadialGraphViewFocusControl(2, "filter"));
		addControlListener(new HoverActionControl("repaint"));
		addControlListener(new ToolTipControl("tooltip"));
		addControlListener(new NeighborHighlightControl(m_vis, neighbors, nonneighbors));
		
		ToolTipManager.sharedInstance().setDismissDelay(30000);

		// ------------------------------------------------

		// filter graph and perform layout
		m_vis.run("filter");

		// maintain a set of items that should be interpolated linearly
		// this isn't absolutely necessary, but makes the animations nicer
		// the PolarLocationAnimator should read this set and act accordingly
		m_vis.addFocusGroup(linear, new DefaultTupleSet());
		m_vis.getGroup(Visualization.FOCUS_ITEMS).addTupleSetListener(
				new TupleSetListener() {
					public void tupleSetChanged(TupleSet t, Tuple[] add, Tuple[] rem) {
						TupleSet linearInterp = m_vis.getGroup(linear);
						if (add.length < 1)
							return;
						linearInterp.clear();
						for (Node n = (Node) add[0]; n != null; n = n.getParent())
							linearInterp.addTuple(n);
					}
				});

		SearchTupleSet search = new PrefixSearchTupleSet();
		m_vis.addFocusGroup(Visualization.SEARCH_ITEMS, search);
		search.addTupleSetListener(new TupleSetListener() {
			public void tupleSetChanged(TupleSet t, Tuple[] add, Tuple[] rem) {
				m_vis.cancel("animatePaint");
				m_vis.run("recolor");
				m_vis.run("animatePaint");
			}
		});
	}

	public JPanel demo(ExtractorTab eTab) {
		return demo(DATA_FILE, "name", eTab);
	}

	public JPanel demo(String datafile, final String label, ExtractorTab eTab) {
		Graph g = new Graph();
		g.addNodeRow();

		try {
			g = new GraphMLReader().readGraph(datafile);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return demo(g, label, eTab);
	}

	public JPanel demo(Graph g, final String label, ExtractorTab eTab) {
		// create a new radial tree view
		final SemGenRadialGraphView gview = new SemGenRadialGraphView(settings, g, label, semsimmodel);
		Visualization vis = gview.getVisualization();
		
		// create a search panel for the tree map
		SearchQueryBinding sq = new SearchQueryBinding(
				(Table) vis.getGroup(treeNodes), label, (SearchTupleSet) vis.getGroup(Visualization.SEARCH_ITEMS));
		JSearchPanel search = sq.createSearchPanel();
		search.setShowResultCount(true);
		search.setBorder(BorderFactory.createEmptyBorder(5, 5, 4, 0));
		search.setFont(FontLib.getFont("Verdana", Font.PLAIN, 11));

		// Used to be for showing annotations for data structures on mouseEntered event
		gview.addControlListener(new PopupControlAdapter(g, eTab));

		Box searchbox = new Box(BoxLayout.X_AXIS);
		searchbox.add(Box.createHorizontalStrut(10));
		searchbox.add(search);
		searchbox.add(Box.createHorizontalStrut(3));
		searchbox.add(eTab.vizsourcebutton);
		searchbox.add(eTab.extractbutton);
		
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBackground(Color.white);
		panel.add(searchbox, BorderLayout.NORTH);
		panel.add(gview, BorderLayout.CENTER);
		panel.add(Box.createGlue(), BorderLayout.SOUTH);

		Color BACKGROUND = Color.WHITE;
		Color FOREGROUND = Color.DARK_GRAY;
		UILib.setColor(search, BACKGROUND, FOREGROUND);
		UILib.setColor(gview, BACKGROUND, FOREGROUND);

		return panel;
	}

	public static class PopupControlAdapter extends ControlAdapter implements ActionListener {
		public Graph g;
		public String focusname;
		public ExtractorTab eTab;
		public PopupControlAdapter(Graph g, ExtractorTab extractorTab){
			this.g = g;
			this.eTab = extractorTab;
		}
		public void itemClicked(VisualItem item, MouseEvent e) {
			e.consume();
			focusname = item.getString("codeword");
			if(e.getModifiers()==InputEvent.BUTTON3_MASK | e.getModifiersEx()==128
					&& eTab.retrieveNodeByLabel(g, focusname).getBoolean(ExtractorTab.VAR2INPUT)){
				JPopupMenu nodePopupMenu = new JPopupMenu();
				JMenuItem expand = new JMenuItem("Add inputs (expand)", 'a');
				expand.addActionListener(this);
				nodePopupMenu.add(expand);
				nodePopupMenu.show(e.getComponent(), e.getX(), e.getY());
			}
		}
		// Expand selected node to include direct inputs
		public void actionPerformed(ActionEvent e) {
			eTab.codewordspanel.termandcheckboxmap.get(focusname).setSelected(true);
			eTab.codewordspanel.scroller.scrollToComponent(eTab.codewordspanel.termandcheckboxmap.get(focusname));
			try {
				eTab.visualize(eTab.primeextraction(), false);
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
	}
	
	public class NeighborHighlightControl extends ControlAdapter {
		private Visualization visu;
		String activity = "repaint";
		String sourceGroupName;
		String nonGroupName;
		TupleSet sourceTupleSet;
		TupleSet nonTupleSet;

		public NeighborHighlightControl(Visualization vis, String group, String nongroup) {
			visu = vis;
			sourceGroupName = group;
			nonGroupName = nongroup;

			try {
				visu.addFocusGroup(sourceGroupName);
				visu.addFocusGroup(nonGroupName);
			} catch (Exception e) {
				System.out.println("Error while adding focus groups to visualization " + e.getMessage());
			}
			sourceTupleSet = visu.getFocusGroup(sourceGroupName);
			nonTupleSet = visu.getFocusGroup(nonGroupName);
		}

		public void itemEntered(VisualItem item, MouseEvent e) {
			if (item instanceof NodeItem)
				setNeighbourHighlight((NodeItem) item);
            visu.run("repaint");
		}

		public void itemExited(VisualItem item, MouseEvent e) {
			if (item instanceof NodeItem) {
				sourceTupleSet.clear();
				nonTupleSet.clear();
			}
		}

		protected void setNeighbourHighlight(NodeItem centerNode) {

			Iterator<Node> alledges = visu.getGroup(treeEdges).tuples();
			while(alledges.hasNext())
				nonTupleSet.addTuple((EdgeItem) alledges.next());
			
			Iterator<?> iterInEdges = centerNode.inEdges();
			while (iterInEdges.hasNext()) {
				EdgeItem edge = (EdgeItem) iterInEdges.next();
				sourceTupleSet.addTuple(edge);
				nonTupleSet.removeTuple(edge);
			}

			Iterator<?> iterOutEdges = centerNode.outEdges();
			while (iterOutEdges.hasNext()) {
				EdgeItem edge = (EdgeItem) iterOutEdges.next();
				sourceTupleSet.addTuple(edge);
				nonTupleSet.removeTuple(edge);
			}
		}

	} // end of class NeighborHighlightControlForDirectedGraphs

	/**
	 * Switch the root of the tree by requesting a new spanning tree at the
	 * desired root
	 */
	public static class TreeRootAction extends GroupAction {
		public TreeRootAction(String graphGroup) {
			super(graphGroup);
		}

		public void run(double frac) {
			TupleSet focus = m_vis.getGroup(Visualization.FOCUS_ITEMS);
			if (focus == null || focus.getTupleCount() == 0)
				return;

			Graph g = (Graph) m_vis.getGroup(m_group);
			Node f = null;
			@SuppressWarnings("unchecked")
			Iterator<Node> tuples = focus.tuples();
			while (tuples.hasNext()
					&& !g.containsTuple(f = tuples.next())) {
				f = null;
			}
			if (f == null)
				return;
			g.getSpanningTree(f);
		}
	}

	/**
	 * Set node fill colors
	 */
	public static class NodeColorAction extends ColorAction {
		public NodeColorAction(String group) {
			super(group, VisualItem.FILLCOLOR, ColorLib.rgba(255, 255, 255, 255));
			add("_hover", ColorLib.gray(220, 230));
			add("ingroup('_search_')", ColorLib.rgb(255, 190, 190));
			add("ingroup('_focus_')", ColorLib.rgb(198, 229, 229));
			add(isvar2inputfilter, ColorLib.rgba(202, 255, 112, 255));
		}
	} // end of inner class NodeColorAction

	/**
	 * Set node text colors
	 */
	public static class TextColorAction extends ColorAction {
		public TextColorAction(String group) {
			super(group, VisualItem.TEXTCOLOR, ColorLib.gray(0));
			add("_hover", ColorLib.rgb(255, 0, 0));
		}
	} // end of inner class TextColorAction
	
	public static class NodeFontAction extends FontAction {
		public NodeFontAction(String group, Font font){
			super(group, font);
			add("ingroup('_focus_')", FontLib.getFont("Verdana", 11));
			add(isinputfilter, FontLib.getFont("Verdana", Font.ITALIC, 11));
		}
	}
	
	public static class ArrowColorStrokeAction extends ColorAction {
		public ArrowColorStrokeAction(String group){
			super(group, VisualItem.STROKECOLOR, ColorLib.rgb(51, 0, 204));
			add("ingroup('" + neighbors + "')", ColorLib.rgba(51, 0, 204, 255));
			add("ingroup('" + nonneighbors + "')", ColorLib.rgba(51,0,204,40));
		}
	}

	public static class ArrowColorFillAction extends ColorAction {
		public ArrowColorFillAction(String group) {
			super(group, VisualItem.FILLCOLOR, ColorLib.rgb(51, 0, 204));
			add("ingroup('" + neighbors + "')", ColorLib.rgba(51, 0, 204, 255));
			add("ingroup('" + nonneighbors + "')", ColorLib.rgba(51, 0, 204, 40));
		}
	} // end of inner class TextColorAction

	public static class BorderColorAction extends ColorAction {
        public BorderColorAction(String group) {
            super(group, VisualItem.STROKECOLOR, ColorLib.rgba(0, 0, 0, 100));
        }
    }
} // end of class RadialGraphView
