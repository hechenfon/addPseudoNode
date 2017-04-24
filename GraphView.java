package linv;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import prefuse.Constants;
import prefuse.Display;
import prefuse.Visualization;
import prefuse.action.ActionList;
import prefuse.action.RepaintAction;
import prefuse.action.assignment.*;
import prefuse.action.filter.GraphDistanceFilter;
import prefuse.action.layout.graph.ForceDirectedLayout;
import prefuse.activity.Activity;
import prefuse.controls.*;
import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.Table;
import prefuse.data.Tuple;
import prefuse.data.event.TupleSetListener;
import prefuse.data.tuple.TupleSet;
import prefuse.render.DefaultRendererFactory;
import prefuse.render.EdgeRenderer;
import prefuse.render.LabelRenderer;
import prefuse.util.ColorLib;
import prefuse.util.FontLib;
import prefuse.util.ui.UILib;
import prefuse.visual.AggregateItem;
import prefuse.visual.EdgeItem;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualGraph;
import prefuse.visual.VisualItem;
import prefuse.visual.expression.InGroupPredicate;

public class GraphView extends JFrame implements ActionListener, ItemListener {
    Visualization m_vis=null;
    Graph m_graph =null;
    JCheckBoxMenuItem sizeItem, colorItem, lenItem;
    ActionList draw, animate;
    JTextArea nodeInfo;

    public GraphView() {
        Toolkit tk = getToolkit();
        Dimension screenSize = tk.getScreenSize();
        setTitle("Lineage Visualization");
        setSize(screenSize);
        setBackground(Color.BLACK);
        addMenu();
        setLayout(new BorderLayout());

        //setLocationRelativeTo(null);
        //createPopupMenu();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }


    public void addMenu() {
        JMenuBar menuBar;
        JMenu menu;
        JMenuItem menuItem;

        menuBar = new JMenuBar();
        setJMenuBar(menuBar);

        menu = new JMenu("File");
        menuBar.add(menu);

        menuItem = new JMenuItem("Open", KeyEvent.VK_O);
        menuItem.getAccessibleContext().setAccessibleDescription(
                "Open");
        menuItem.addActionListener(this);
        menu.add(menuItem);
        menuItem = new JMenuItem("Exit", KeyEvent.VK_E);
        menuItem.getAccessibleContext().setAccessibleDescription(
                "Exit");
        menuItem.addActionListener(this);
        menu.add(menuItem);

        menu = new JMenu ("Settings");
        menuBar.add(menu);
        JCheckBoxMenuItem cmItem = new JCheckBoxMenuItem("size->nRNAs", false);
        menu.add(cmItem);
        cmItem.addItemListener(this);
        sizeItem = cmItem;
        cmItem = new JCheckBoxMenuItem("color->nMuts", true);
        menu.add(cmItem);
        cmItem.addItemListener(this);
        colorItem = cmItem;
        cmItem = new JCheckBoxMenuItem("length->EDist", false);
        menu.add(cmItem);
        lenItem = cmItem;
        cmItem.addItemListener(this);
    }
    public void itemStateChanged(ItemEvent e){
        if (m_vis !=null) {
            updateActionLists();
            m_vis.run("draw");
        }
    }
    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();
        if (cmd.equals("Open")) {
            m_graph = openFiles();
            if (m_graph!=null){
                createViz(m_graph);
            };
        }
        else if (cmd.equals("Exit")){
            System.exit(0);
        }
    }

    private Graph openFiles(){
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Load a node file");
        int returnVal = fc.showOpenDialog(null);
        if (returnVal != JFileChooser.APPROVE_OPTION) {
            return null;
        }
        File nf = fc.getSelectedFile();
        Table nt = NodeList.parse(nf);
        if (nt==null){
            System.err.println("failed to load the node file");
            return null;
        }
        fc.setDialogTitle("Load an edge file");
        returnVal = fc.showOpenDialog(null);
        if (returnVal != JFileChooser.APPROVE_OPTION) {
            return null;
        }
        File ef = fc.getSelectedFile();
        Table et = EdgeList.parse(ef);
        System.out.println("done");
        if (et==null){
            System.err.println("failed to load the edge file");
            return null;
        }

        return new Graph(nt, et, true);
    }

    private void createViz(Graph g){
        // create a new, empty visualization for our data
        m_vis = new Visualization();
        LabelRenderer nodeRenderer = new LabelRenderer(VisualItem.LABEL);
        //LabelRenderer nodeRenderer = new LabelRenderer("LABEL");
        nodeRenderer.setHorizontalPadding(7);
        nodeRenderer.setVerticalPadding(7);
        nodeRenderer.setRoundedCorner(10,10);

        EdgeRenderer edgeRenderer = new EdgeRenderer(Constants.EDGE_TYPE_LINE, Constants.EDGE_ARROW_FORWARD);
        edgeRenderer.setArrowHeadSize(10, 10);
        //edgeRenderer.setVerticalPadding(7);

        DefaultRendererFactory rf = new DefaultRendererFactory(nodeRenderer,edgeRenderer);
        m_vis.setRendererFactory(rf);
        rf.add(new InGroupPredicate("WEIGHT"), new LabelRenderer(VisualItem.LABEL));
        rf.setDefaultRenderer(nodeRenderer);

        // --------------------------------------------------------------------
        // register the data with a visualization
        // adds graph to visualization and sets renderer label field
        //setGraph(g, label);
        DefaultRendererFactory drf = (DefaultRendererFactory) m_vis.getRendererFactory();
        ((LabelRenderer)drf.getDefaultRenderer()).setTextField("NID");
        m_vis.removeGroup("graph");
        VisualGraph vg = m_vis.addGraph("graph", g);
        VisualItem f = (VisualItem)vg.getNode(0);
        m_vis.getGroup(Visualization.FOCUS_ITEMS).setTuple(f);

        // fix selected focus nodes
        TupleSet focusGroup = m_vis.getGroup(Visualization.FOCUS_ITEMS);
        focusGroup.addTupleSetListener(new TupleSetListener() {
            @Override
            public void tupleSetChanged(TupleSet ts, Tuple[] add, Tuple[] rem) {
                String s="Clicked Node:\n";
                for (int i = 0; i < add.length; i++) {
                    s += "\nNID: "+add[i].get("NID");
                    s += "\nLABEL: " + add[i].get("LABEL");
                    s += "\nTIMESTAMP: " + add[i].get("TIME");
                    s += "\nISOTYPE: " + add[i].get("ISOTYPE");
                    s += "\nNRNAs " + add[i].get("NRNAS");
                    s+=  "\nNREADs: "+ add[i].get("NREADS");
                    s+=  "\nNMutations: "+ add[i].get("NMUTATIONS");
                    s+=  "\nSEQUENCE: "+ add[i].get("SEQUENCE");
                    //((VisualItem)add[i]).setFixed(true);
                    //((VisualItem)rem[i]).setFixed(false);
                    //		item.setHighlighted(true);
                }
                nodeInfo.setText("");
                nodeInfo.append(s);
                for (int i = 0; i < rem.length; i++) {
                    //item.setHighlighted(false);
                }
                //m_vis.run("layout");
            }
        });

        /*
        TupleSet selectedGroup = m_vis.getGroup(Visualization.SELECTED_ITEMS);
        selectedGroup.addTupleSetListener(new TupleSetListener() {
            @Override
            public void tupleSetChanged(TupleSet ts, Tuple[] add, Tuple[] rem)
            {
                for ( int i=0; i<rem.length; ++i )
                {

                    ((VisualItem)add[i]).setFixed(false);
                    new PopUpMenu();
                }
                for ( int i=0; i<add.length; ++i ) {
                    ((VisualItem)add[i]).setFixed(true);
                    ((VisualItem)rem[i]).setFixed(false);
                    new PopUpMenu();
                }
                if ( ts.getTupleCount() == 0 ) {
                    ts.addTuple(rem[0]);
                    ((VisualItem)rem[0]).setFixed(false);
                }
                m_vis.run("draw");
            }
        });
*/
        // --------------------------------------------------------------------
        // create actions to process the visual data

        updateActionLists();

        // --------------------------------------------------------------------
        // set up a display to show the visualization

        Display display = new Display(m_vis);
        display.setSize(1000,1000);
        display.pan(500, 500);
        display.setForeground(Color.GRAY);
        display.setBackground(Color.WHITE);
        //PopupMenuController popup = new PopupMenuController(m_vis);
        //display.addControlListener(popup);
        //display.addControlListener(new TooltipControl(m_vis));

        // main display controls
        display.addControlListener(new FocusControl(1));
        display.addControlListener(new DragControl());
        display.addControlListener(new PanControl());
        display.addControlListener(new ZoomControl());
        display.addControlListener(new WheelZoomControl());
        display.addControlListener(new ZoomToFitControl());
        display.addControlListener(new NeighborHighlightControl());
        display.addControlListener(new NodeToolTipControl(""));
        display.addControlListener(new LineControl());
        display.setForeground(Color.GRAY);
        display.setBackground(Color.white);
        m_vis.run("draw");
        add(display, BorderLayout.CENTER);
        nodeInfo = new JTextArea(100, 100);
        JScrollPane scrollPane = new JScrollPane(nodeInfo);
        nodeInfo.setText("Clicked Node:");
        nodeInfo.setEditable(false);
        nodeInfo.setLineWrap(true);
        add(scrollPane, BorderLayout.EAST);
        revalidate();
        //repaint();
    }

    private void updateActionLists(){
        int hops = 30;
        final GraphDistanceFilter filter = new GraphDistanceFilter("graph", hops);

        ColorAction nfill;
        if (colorItem.getState()==true)
            nfill = new DataColorAction("graph.nodes", "NMUTATIONS",
                    Constants.NOMINAL, VisualItem.FILLCOLOR,
                    ColorLib.getInterpolatedPalette(ColorLib.rgb(0,200, 255), ColorLib.rgb(200,0, 0)));
        else
            nfill = new ColorAction("graph.nodes", VisualItem.FILLCOLOR, ColorLib.color(Color.blue));
        nfill.add(VisualItem.FIXED, ColorLib.rgb(255,200,125));
        nfill.add(VisualItem.HIGHLIGHT, ColorLib.rgb(0,120,255));

        ColorAction textfill = new ColorAction("graph.nodes", VisualItem.TEXTCOLOR, ColorLib.rgb(255,255,255));
        textfill.add(VisualItem.FIXED, ColorLib.rgb(0,0,0));
        nfill.add(VisualItem.HIGHLIGHT, ColorLib.rgb(255,200,125));

        ActionList size = new ActionList();
        if (sizeItem.getState()==true)
            size.add(new DataSizeAction("graph.nodes", "SIZE1"));
        else
            size.add(new DataSizeAction("graph.nodes", "SIZE0"));
        //	DataColorAction efill = new DataColorAction(edges, "weight", Constants.NOMINAL, VisualItem.FILLCOLOR, ColorLib.getInterpolatedPalette(ColorLib.rgb(200,0, 0), ColorLib.rgb(0,200, 255)));

        ColorAction efill = new ColorAction("graph.edges", VisualItem.FILLCOLOR, ColorLib.rgb(200,200,255));
        efill.add(VisualItem.FIXED, ColorLib.rgb(255,100,100));
        efill.add(VisualItem.HIGHLIGHT, ColorLib.rgb(255,200,125));

        ColorAction edgeLineColor = new ColorAction("graph.edges", VisualItem.STROKECOLOR, ColorLib.rgb(200,200,200));
        edgeLineColor.add(VisualItem.FIXED, ColorLib.rgb(255,100,100));
        edgeLineColor.add(VisualItem.HIGHLIGHT, ColorLib.rgb(255,200,125));

        draw = new ActionList();
        draw.add(filter);
        draw.add(new FontAction("graph.nodes", FontLib.getFont("Tahoma", 12)));
        draw.add(nfill);
        draw.add(efill);
        draw.add(size);
        draw.add(textfill);
        draw.add(edgeLineColor);

        animate = new ActionList(Activity.INFINITY);
        if (lenItem.getState()==true)
            animate.add(new ForceDirectedVarSpringLayout("graph", true));
        else
            animate.add(new ForceDirectedLayout("graph", true, false));
        //ForceDirectedLayout fd = new ForceDirectedLayout("graph", true, false);
        //fd.setMaxTimeStep(100);
        //animate.add(fd);
        animate.add(efill);
        animate.add(nfill);
        animate.add(textfill);
        animate.add(size);
        animate.add(edgeLineColor);
        animate.add(new RepaintAction());
        m_vis.removeAction("draw");
        m_vis.removeAction("layout");
        m_vis.putAction("draw", draw);
        m_vis.putAction("layout", animate);
        m_vis.runAfter("draw", "layout");
    }


    class LineControl extends ControlAdapter {
        VisualItem start, end;
        Node tempNode;
        public LineControl() {
            super();
            start = end = null;
        }

        public void itemClicked(VisualItem item, java.awt.event.MouseEvent e) {
            if (item.isInGroup("graph.nodes")) {
                if (start == null) {
                    start = item;/*
                    if (m_graph!=null) {
                        tempNode = m_graph.addNode();
                        m_graph.addEdge((Node) item.getSourceTuple(), tempNode);
                    }*/
                } else if (item!=start && end == null) {
                    end = item;
                    //create link
                }
            } else {/*
                if (start!=null && tempNode!=null) {
                    m_graph.removeNode(tempNode);
                }*/
                start = end = null; // clear
                tempNode =null;
            }
            System.out.println("start:"+start);
            System.out.println("end:"+end);
        }

        public void mouseMoved(java.awt.event.MouseEvent e) {
            System.out.println(e.getPoint());
        }
    }



    class NodeToolTipControl extends ToolTipControl{
        public NodeToolTipControl(String st){
            super(st);
        }
        public void itemEntered(VisualItem item, java.awt.event.MouseEvent e) {
            System.out.println("node tool tip, entered");

            if (!item.isInGroup("graph.nodes"))
                return;
            Display d = (Display)e.getSource();
            String s ="<html><font color=\"#800080\" " +
                    "size=\"10\" face=\"Verdana\">";
            //s += "\nTIMESTAMP: " + item.get("TIME");
            s += "TYPE:" + item.get("ISOTYPE");
            s += ", RNAs: " + item.get("NRNAS");
            s+=  ", READs: "+ item.get("NREADS");
            s+=  ", Muts: "+ item.get("NMUTATIONS");
            //s+=  "\nSEQUENCE: "+ item.get("SEQUENCE");
            s += "</font></html>";
            d.setToolTipText(s);
        }

    }

    /*
    public static void main(String[] args) {

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                GraphView ex = new GraphView();
                ex.setVisible(true);
            }
        });
    }
    */
}