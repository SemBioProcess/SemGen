package semgen.extraction.graph;



import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseEvent;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JTextArea;

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
import prefuse.controls.FocusControl;
import prefuse.controls.HoverActionControl;
import prefuse.controls.PanControl;
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
import prefuse.visual.VisualItem;
import prefuse.visual.expression.InGroupPredicate;
import prefuse.visual.sort.TreeDepthItemSorter;
import semgen.extraction.ExtractorTab;
import semsim.model.SemSimModel;

/**
 * Demonstration of a node-link tree viewer
 * 
 * @version 1.0
 * @author <a href="http://jheer.org">jeffrey heer</a>
 */
public class PhysioMapRadialGraphView extends Display {
	private static final long serialVersionUID = 8464331927479988729L;

	public static final String DATA_FILE = "/socialnet.xml";
	private static final String tree = "tree";
	private static final String treeNodes = "tree.nodes";
	private static final String treeEdges = "tree.edges";
	private static final String linear = "linear";

	private LabelRenderer m_nodeRenderer;
	private EdgeRenderer m_edgeRenderer;

	public Graph g;
	public SemSimModel semsimmodel;
	private Dimension DispSize;
	
	 public final static Predicate isprocessfilter = ExpressionParser.predicate("process==true");

	public PhysioMapRadialGraphView(Graph g, String label, SemSimModel semsimmodel, Dimension DispSize) {
		super(new Visualization());
		this.g = g;

		this.semsimmodel = semsimmodel;

		// -- set up visualization --
		m_vis.add(tree, g);
		m_vis.setInteractive(treeEdges, null, false);

		// -- set up renderers --
		m_nodeRenderer = new LabelRenderer(label);
		m_nodeRenderer.setRenderType(AbstractShapeRenderer.RENDER_TYPE_DRAW_AND_FILL);
		m_nodeRenderer.setHorizontalAlignment(Constants.CENTER);
		m_nodeRenderer.setRoundedCorner(8, 8);
		m_edgeRenderer = new EdgeRenderer(Constants.EDGE_TYPE_LINE,
				prefuse.Constants.EDGE_ARROW_REVERSE);
		m_edgeRenderer.setArrowType(prefuse.Constants.EDGE_ARROW_REVERSE);
		m_edgeRenderer.setArrowHeadSize(8, 8);
		

		// MAYBE HERE?
		DefaultRendererFactory rf = new DefaultRendererFactory(m_nodeRenderer);
		rf.add(new InGroupPredicate(treeEdges), m_edgeRenderer);
		m_vis.setRendererFactory(rf);
		//m_vis.

		// -- set up processing actions --
		// colors
		ItemAction nodeColor = new NodeColorAction(treeNodes);
		ItemAction borderColor = new BorderColorAction(treeNodes);
        m_vis.putAction("borderColor", borderColor);
		ItemAction textColor = new TextColorAction(treeNodes);
		m_vis.putAction("textColor", textColor);
		ItemAction edgeColor = new ColorAction(treeEdges,
				VisualItem.STROKECOLOR, ColorLib.rgb(0, 0, 0));
		ItemAction arrowColor = new ArrowColorAction(treeEdges);
		m_vis.putAction("arrowColor", arrowColor);

		FontAction fonts = new FontAction(treeNodes, FontLib.getFont("Verdana",12));
		fonts.add("ingroup('_focus_')", FontLib.getFont("Verdana", 12));

		// recolor
		// When recolor, do these actions
		ActionList recolor = new ActionList();
		recolor.add(nodeColor);
		recolor.add(borderColor);
		recolor.add(textColor);
		recolor.add(arrowColor);
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
		filter.add(fonts);
		filter.add(treeLayout);
		filter.add(borderColor);
		filter.add(subLayout);
		filter.add(textColor);
		filter.add(nodeColor);
		filter.add(edgeColor);
		filter.add(arrowColor);
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

		setSize(DispSize.height-ExtractorTab.leftpanewidth-50, DispSize.width-235);
		setItemSorter(new TreeDepthItemSorter());
		addControlListener(new DragControl());
		addControlListener(new ZoomToFitControl());
		addControlListener(new ZoomControl());
		addControlListener(new PanControl());
		addControlListener(new FocusControl(1, "filter"));
		addControlListener(new HoverActionControl("repaint"));

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

	public JPanel demo() {
		return demo(DATA_FILE, "name");
	}

	public JPanel demo(String datafile, final String label) {
		Graph g = new Graph();
		g.addNodeRow();

		try {
			g = new GraphMLReader().readGraph(datafile);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		return demo(g, label);
	}

	public JPanel demo(Graph g, final String label) {
		// create a new radial tree view
		final PhysioMapRadialGraphView gview = new PhysioMapRadialGraphView(g, label, semsimmodel, DispSize);
		Visualization vis = gview.getVisualization();
		

		// create a search panel for the tree map
		SearchQueryBinding sq = new SearchQueryBinding(
				(Table) vis.getGroup(treeNodes), label, (SearchTupleSet) vis.getGroup(Visualization.SEARCH_ITEMS));
		JSearchPanel search = sq.createSearchPanel();
		search.setShowResultCount(true);
		search.setBorder(BorderFactory.createEmptyBorder(5, 5, 4, 0));
		search.setFont(FontLib.getFont("Verdana", Font.PLAIN, 11));

		final JTextArea title = new JTextArea();
		title.setPreferredSize(new Dimension(450, 500));
		title.setMaximumSize(new Dimension(450, 500));
		title.setMinimumSize(new Dimension(450, 500));
		
		title.setAlignmentY(CENTER_ALIGNMENT);
		title.setLineWrap(true);
		title.setWrapStyleWord(true);
		title.setBorder(BorderFactory.createEmptyBorder(3, 0, 0, 0));
		title.setFont(FontLib.getFont("Verdana", Font.PLAIN, 11));

		gview.addControlListener(new ControlAdapter() {
			public void itemEntered(VisualItem item, MouseEvent e) {
				if (item.canGetString(label)){ 
				}
			}

			public void itemExited(VisualItem item, MouseEvent e) {
				title.setText(null);
			}
		});

		Box searchbox = new Box(BoxLayout.X_AXIS);
		searchbox.add(Box.createHorizontalStrut(10));
		searchbox.add(search);
		searchbox.add(Box.createHorizontalStrut(3));
		
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(searchbox, BorderLayout.NORTH);
		panel.add(gview, BorderLayout.CENTER);
		panel.add(Box.createGlue(), BorderLayout.SOUTH);

		Color BACKGROUND = Color.WHITE;
		Color FOREGROUND = Color.DARK_GRAY;
		UILib.setColor(panel, BACKGROUND, FOREGROUND);

		return panel;
	}

	// ------------------------------------------------------------------------

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
			super(group, VisualItem.FILLCOLOR, ColorLib
					.rgba(202, 225, 255, 250));
			add("_hover", ColorLib.gray(220, 230));
			add("ingroup('_search_')", ColorLib.rgb(255, 190, 190));
			add("ingroup('_focus_')", ColorLib.rgb(205, 197, 191));
			add(isprocessfilter, ColorLib.rgba(255, 193, 193, 255));
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

	public static class ArrowColorAction extends ColorAction {
		public ArrowColorAction(String group) {
			super(group, VisualItem.FILLCOLOR, ColorLib.rgb(0, 0, 0));
		}
	} // end of inner class ArrowColorAction
	
	public static class BorderColorAction extends ColorAction {
        public BorderColorAction(String group) {
            super(group, VisualItem.STROKECOLOR, ColorLib.rgba(0, 0, 255, 255));
			add(isprocessfilter, ColorLib.rgba(176, 23, 31, 255));
        }
    }

} // end of class RadialGraphView
