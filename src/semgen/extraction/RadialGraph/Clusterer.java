package semgen.extraction.RadialGraph;
/*
 * Copyright (c) 2003, the JUNG Project and the Regents of the University
 * of California
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * http://jung.sourceforge.net/license.txt for a description.
 */
//package edu.uci.ics.jung.samples;



import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.commons.collections15.Transformer;
import org.apache.commons.collections15.functors.ChainedTransformer;
import org.apache.commons.collections15.functors.ConstantTransformer;
import org.apache.commons.collections15.functors.MapTransformer;
import org.apache.commons.collections15.map.LazyMap;

import semgen.extraction.ExtractorJCheckBox;
import semgen.extraction.ExtractorTab;
import semgen.resource.SemGenFont;
import semgen.resource.uicomponent.SemGenScrollPane;
import semgen.resource.uicomponent.SemGenTab;
import semsim.model.computational.datastructures.DataStructure;
import semsim.model.physical.PhysicalModelComponent;
import semsim.model.physical.PhysicalProcess;
import edu.uci.ics.jung.algorithms.cluster.EdgeBetweennessClusterer;
import edu.uci.ics.jung.algorithms.layout.AggregateLayout;
import edu.uci.ics.jung.algorithms.layout.CircleLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.util.Relaxer;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseMultigraph;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.renderers.DefaultVertexLabelRenderer;
import edu.uci.ics.jung.visualization.renderers.VertexLabelAsShapeRenderer;

/**
 * This simple app demonstrates how one can use our algorithms and visualization
 * libraries in unison. In this case, we generate use the Zachary karate club
 * data set, widely known in the social networks literature, then we cluster the
 * vertices using an edge-betweenness clusterer, and finally we visualize the
 * graph using Fruchtermain-Rheingold layout and provide a slider so that the
 * user can adjust the clustering granularity.
 * 
 * @author Scott White
 */
@SuppressWarnings("serial")
public class Clusterer extends JFrame {

	VisualizationViewer<String, Number> vv;

	@SuppressWarnings("unchecked")
	Map<String, Paint> vertexPaints = LazyMap.<String, Paint> decorate(
			new HashMap<String, Paint>(), new ConstantTransformer(Color.white));
	@SuppressWarnings("unchecked")
	Map<Number, Paint> edgePaints = LazyMap.<Number, Paint> decorate(
			new HashMap<Number, Paint>(), new ConstantTransformer(Color.blue));
	public static JFrame jf;
	public JSplitPane splitpane;
	public JPanel clusterpanel;
	public JPanel sempanel = new JPanel();
	public SemGenScrollPane semscroller;
	public SparseMultigraph<String, Number> mygraph;
	public ExtractorTab extractor;
	public ExtractorJCheckBox selectioncheckbox;
	public AggregateLayout<String, Number> layout;
	public static JToggleButton groupVertices;
	public int nummodules = 0;
	public int initsempanelwidth = 250;

	public final Color[] similarColors = { new Color(216, 134, 134),
			new Color(135, 137, 211), new Color(134, 206, 189),
			new Color(206, 176, 134), new Color(194, 204, 134),
			new Color(145, 214, 134), new Color(133, 178, 209),
			new Color(103, 148, 255), new Color(60, 220, 220),
			new Color(30, 250, 100) };

	public Clusterer(SparseMultigraph<String, Number> mygraph, ExtractorTab extractor) throws IOException {
		this.mygraph = mygraph;
		this.extractor = extractor;
		semscroller = new SemGenScrollPane(sempanel);
		sempanel.setLayout(new BoxLayout(sempanel, BoxLayout.Y_AXIS));
		sempanel.setOpaque(true);
		setUpView();
	}


	public void setUpView() throws IOException {
		setTitle(SemGenTab.formatTabName(extractor.semsimmodel.getName()));
		layout = new AggregateLayout<String, Number>(new SemGenFRLayout<String, Number>(mygraph));

		vv = new VisualizationViewer<String, Number>(layout);
		// this class will provide both label drawing and vertex shapes
		VertexLabelAsShapeRenderer<String, Number> vlasr = new VertexLabelAsShapeRenderer<String, Number>(vv.getRenderContext());

		// customize the render context
		vv.getRenderContext().setVertexLabelTransformer(
		// this chains together Transformers so that the html tags
		// are prepended to the toString method output
				new ChainedTransformer<String, String>(new Transformer[] {
						new ToStringLabeller<String>(),
						new Transformer<String, String>() {
							public String transform(String input) {
								return input;
							}
						} }));
		vv.getRenderContext().setVertexShapeTransformer(vlasr);
		vv.getRenderContext().setVertexLabelRenderer(new DefaultVertexLabelRenderer(Color.red));
		vv.getRenderContext().setEdgeDrawPaintTransformer(new ConstantTransformer(Color.yellow));
		vv.getRenderContext().setEdgeStrokeTransformer(new ConstantTransformer(new BasicStroke(2.5f)));

		// customize the renderer
		vv.getRenderer().setVertexLabelRenderer(vlasr);

		vv.setBackground(Color.white);
		// Tell the renderer to use our own customized color rendering
		vv.getRenderContext().setVertexFillPaintTransformer(MapTransformer.<String, Paint> getInstance(vertexPaints));
		vv.getRenderContext().setVertexDrawPaintTransformer(new Transformer<String, Paint>() {
					public Paint transform(String v) {
						if (vv.getPickedVertexState().isPicked(v)) {
							if (selectioncheckbox != null) {
								extractor.clusterpanel.remove(selectioncheckbox);
							}
							Set<DataStructure> dsuris = new HashSet<DataStructure>();
							for (String dsname : vv.getPickedVertexState().getPicked()) {
								dsuris.add(extractor.semsimmodel.getDataStructure(dsname));
							}
							Component[] clusters = extractor.clusterpanel.checkboxpanel.getComponents();
							extractor.clusterpanel.checkboxpanel.removeAll();
							for (int x = -1; x < clusters.length; x++) {
								if (x == -1 && selectioncheckbox==null) {
									selectioncheckbox = new ExtractorJCheckBox("Selected node(s)", dsuris);
									selectioncheckbox.addItemListener(extractor);
									extractor.clusterpanel.checkboxpanel.add(selectioncheckbox);
								} else if(x > -1){
									extractor.clusterpanel.checkboxpanel.add(clusters[x]);
								}
							}
							refreshModulePanel();
							return Color.cyan;
						} else {
							if (vv.getPickedVertexState().getPicked().isEmpty()) {
								if (selectioncheckbox != null) {
									extractor.clusterpanel.checkboxpanel.remove(selectioncheckbox);
									selectioncheckbox = null;
								}
							}
							refreshModulePanel();
							return Color.BLACK;
						}
					}
				});

		vv.getRenderContext().setEdgeDrawPaintTransformer(
				MapTransformer.<Number, Paint> getInstance(edgePaints));

		vv.getRenderContext().setEdgeStrokeTransformer(
				new Transformer<Number, Stroke>() {
					protected final Stroke THIN = new BasicStroke(1);
					protected final Stroke THICK = new BasicStroke(2);

					public Stroke transform(Number e) {
						Paint c = edgePaints.get(e);
						if (c == Color.LIGHT_GRAY)
							return THIN;
						else
							return THICK;
					}
				});

		// add restart button
		JButton scramble = new JButton("Shake");
		scramble.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				Layout layout = vv.getGraphLayout();
				layout.initialize();
				Relaxer relaxer = vv.getModel().getRelaxer();
				if (relaxer != null) {
					relaxer.stop();
					relaxer.prerelax();
					relaxer.relax();
				}
			}
		});

		DefaultModalGraphMouse gm = new DefaultModalGraphMouse();
		vv.setGraphMouse(gm);

		groupVertices = new JToggleButton("Group Clusters");

		// Create slider to adjust the number of edges to remove when clustering
		final JSlider edgeBetweennessSlider = new JSlider(JSlider.HORIZONTAL);
		edgeBetweennessSlider.setBackground(Color.WHITE);
		edgeBetweennessSlider.setPreferredSize(new Dimension(350, 50));
		edgeBetweennessSlider.setPaintTicks(true);
		edgeBetweennessSlider.setMaximum(mygraph.getEdgeCount());
		edgeBetweennessSlider.setMinimum(0);
		edgeBetweennessSlider.setValue(0);
		if (mygraph.getEdgeCount() > 10) {
			edgeBetweennessSlider.setMajorTickSpacing(mygraph.getEdgeCount() / 10);
		} else {
			edgeBetweennessSlider.setMajorTickSpacing(1);
		}
		edgeBetweennessSlider.setPaintLabels(true);
		edgeBetweennessSlider.setPaintTicks(true);

		// I also want the slider value to appear
		final JPanel eastControls = new JPanel();
		eastControls.setOpaque(true);
		eastControls.setLayout(new BoxLayout(eastControls, BoxLayout.Y_AXIS));
		eastControls.add(Box.createVerticalGlue());
		eastControls.add(edgeBetweennessSlider);

		final String COMMANDSTRING = "Edges removed for clusters: ";
		final String eastSize = COMMANDSTRING + edgeBetweennessSlider.getValue();

		final TitledBorder sliderBorder = BorderFactory.createTitledBorder(eastSize);
		eastControls.setBorder(sliderBorder);
				eastControls.add(Box.createVerticalGlue());

		groupVertices.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				clusterAndRecolor(layout, edgeBetweennessSlider.getValue(),similarColors, e.getStateChange() == ItemEvent.SELECTED);
				vv.repaint();
			}
		});

		

		edgeBetweennessSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSlider source = (JSlider) e.getSource();
				if (!source.getValueIsAdjusting()) {
					int numEdgesToRemove = source.getValue();
					clusterAndRecolor(layout, numEdgesToRemove, similarColors, groupVertices.isSelected());
					sliderBorder.setTitle(COMMANDSTRING + edgeBetweennessSlider.getValue());
					eastControls.repaint();
					vv.validate();
					vv.repaint();
				}
			}
		});

		
		clusterAndRecolor(layout, 0, similarColors, groupVertices.isSelected());

		clusterpanel = new JPanel();
		clusterpanel.setLayout(new BoxLayout(clusterpanel, BoxLayout.Y_AXIS));
		GraphZoomScrollPane gzsp = new GraphZoomScrollPane(vv);
		clusterpanel.add(gzsp);
		JPanel south = new JPanel();
		JPanel grid = new JPanel(new GridLayout(2, 1));
		grid.add(scramble);
		grid.add(groupVertices);
		south.add(grid);
		south.add(eastControls);
		JPanel p = new JPanel();
		p.setBorder(BorderFactory.createTitledBorder("Mouse Mode"));
		p.add(gm.getModeComboBox());
		south.add(p);
		clusterpanel.add(south);
		clusterpanel.add(Box.createGlue());
		semscroller = new SemGenScrollPane(sempanel);
		splitpane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,semscroller,clusterpanel);
		splitpane.setDividerLocation(initsempanelwidth);
		splitpane.setDividerLocation(initsempanelwidth+10);
		this.add(splitpane);
		this.setPreferredSize(new Dimension(950,800));
		this.pack();
		this.setLocationRelativeTo(null);
		this.setVisible(true);
	}

	public String clusterAndRecolor(AggregateLayout<String, Number> layout, int numEdgesToRemove, Color[] colors, boolean groupClusters) {

		setCursor(new Cursor(Cursor.WAIT_CURSOR));
		String moduletable = "";
		Graph<String, Number> g = layout.getGraph();
		layout.removeAll();

		EdgeBetweennessClusterer<String, Number> clusterer = new EdgeBetweennessClusterer<String, Number>(numEdgesToRemove);
		Set<Set<String>> clusterSet = clusterer.transform(g);
		List<Number> edges = clusterer.getEdgesRemoved();
		sempanel.removeAll();
		int i = 0;
		// Set the colors of each node so that each cluster's vertices have the same color
		extractor.clusterpanel.checkboxpanel.removeAll();
		for (Iterator<Set<String>> cIt = clusterSet.iterator(); cIt.hasNext();) {
			moduletable = moduletable + "\nCLUSTER " + (i + 1);
			Set<String> vertices = cIt.next();
			Color c = colors[i % colors.length];
			Set<DataStructure> datastrs = new HashSet<DataStructure>();
			for(String vertex : vertices){
				datastrs.add(extractor.semsimmodel.getDataStructure(vertex));
			}

			JLabel modulelabel = new JLabel("Cluster " + (i + 1));
			modulelabel.setOpaque(true);
			modulelabel.setFont(SemGenFont.defaultBold());
			modulelabel.setBackground(c);
			modulelabel.setAlignmentX(LEFT_ALIGNMENT);
			sempanel.add(modulelabel);

			// Update the semantics panel
			Set<String> addedterms = new HashSet<String>();
			for (String ver : vertices) {
				if(extractor.semsimmodel.getDataStructure(ver).hasPhysicalProperty()){
					if(extractor.semsimmodel.getDataStructure(ver).getPhysicalProperty().getPhysicalPropertyOf()!=null){
						PhysicalModelComponent pmc = extractor.semsimmodel.getDataStructure(ver).getPhysicalProperty().getPhysicalPropertyOf();
						String name = null;
						if(pmc.hasRefersToAnnotation()){
							name = pmc.getFirstRefersToReferenceOntologyAnnotation().getValueDescription();
						}
						else{
							name = pmc.getName();
						}
						if(!addedterms.contains(name)){
							addedterms.add(name);
							JTextArea enttext = new JTextArea(name);
							enttext.setOpaque(true);
							enttext.setBackground(c);
							enttext.setFont(SemGenFont.defaultPlain(-2));
							if(pmc instanceof PhysicalProcess){
								enttext.setFont(SemGenFont.defaultItalic(-2));
								name = "Process: " + name;
							}
							else name = "Entity: " + name;
							enttext.setWrapStyleWord(true);
							enttext.setLineWrap(true);
							enttext.setBorder(BorderFactory.createEmptyBorder(7, 7, 0, 0));
							enttext.setAlignmentX(LEFT_ALIGNMENT);
							sempanel.add(enttext);
							moduletable = moduletable + "\n   " + name;
						}
					}
				}
			}
			sempanel.validate();
			sempanel.repaint();
			semscroller.repaint();
			semscroller.validate();
			this.repaint();
			this.validate();

			colorCluster(vertices, c);
			ExtractorJCheckBox box = new ExtractorJCheckBox("Cluster " + (i + 1), datastrs);
			box.setBackground(c);
			box.setOpaque(true);
			box.addItemListener(extractor);
			extractor.clusterpanel.checkboxpanel.add(box);
			if (groupClusters == true) groupCluster(layout, vertices);
			i++;
		}
		refreshModulePanel();

		for (Number e : g.getEdges()) {
			if (edges.contains(e)) edgePaints.put(e, Color.lightGray);
			else edgePaints.put(e, Color.black);
		}
		nummodules = i;
		setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		return moduletable;
	}

	private void colorCluster(Set<String> vertices, Color c) {
		for (String v : vertices) vertexPaints.put(v, c);
	}

	private void groupCluster(AggregateLayout<String, Number> layout,
			Set<String> vertices) {
		if (vertices.size() < layout.getGraph().getVertexCount()) {
			Point2D center = layout.transform(vertices.iterator().next());
			Graph<String, Number> subGraph = SparseMultigraph
					.<String, Number> getFactory().create();
			for (String v : vertices) 
				subGraph.addVertex(v);
			Layout<String, Number> subLayout = new CircleLayout<String, Number>(subGraph);
			subLayout.setInitializer(vv.getGraphLayout());
			subLayout.setSize(new Dimension(Math.round(700 / nummodules), Math.round(700 / nummodules)));
			subLayout.setSize(new Dimension(175, 175));

			layout.put(subLayout, center);
			vv.repaint();
		}
	}

	public void refreshModulePanel() {
		int numclusters = 0;
		if(vv.getPickedVertexState().getPicked().isEmpty()){
			numclusters = extractor.clusterpanel.checkboxpanel.getComponentCount();
		}
		else{
			numclusters = extractor.clusterpanel.checkboxpanel.getComponentCount()-1;
		}
		
		extractor.clusterpanel.titlelabel.setText("Clusters (" + numclusters + ")");
		extractor.validate();
		extractor.repaint();
		extractor.clusterpanel.validate();
		extractor.clusterpanel.repaint();
		extractor.clusterpanel.scroller.validate();
		extractor.clusterpanel.scroller.repaint();
	}
}
