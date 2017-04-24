package pseudo;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import prefuse.data.Graph;
import prefuse.data.Table;
import prefuse.data.column.Column;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Created by hechenfon on 3/23/2017.
 * Class to analyze lineage structure for interested info
 */
public class linAnalyzer {
    public static Table ns = new Table(); //store all nodes information
    public static Table es = new Table(); //store all edges information

    int orgLastID; //the last NID in the original structure


    public linAnalyzer(Table ns, Table es) {
        this.ns = ns;
        this.es = es;
        int maxID = 0;
        for (int i = 0;i < ns.getRowCount();i++) {
            int curID = ns.getInt(i,"NID");
            if (curID > maxID) {
                maxID = curID;
            }
        }
        this.orgLastID = maxID;
    }


    public static Integer findRoot() {
        //find the root nid based on edge table - node without incoming edge
        int rtNID = -1;
        HashMap<Integer,Integer> allTgt = new HashMap<>();

        for (int i = 0; i < es.getRowCount();i++) {
            int tid = es.getInt(i, "target");
            allTgt.put(tid,1);
        }
        for (int i = 0; i < es.getRowCount();i++) {
            int sid = es.getInt(i, "source");
            if (!allTgt.containsKey(sid)) {
                rtNID = sid;
            }
        }
        return(rtNID);
    }



    private static int getRowInd(Integer nid) {
        int rowID = 0;
        for (int i = 0;i < ns.getRowCount();i++) {
            int curID = ns.getInt(i,"NID");
            if (curID == nid) {
                rowID = i;
                break;
            }
        }
        return(rowID);
    }

    public void addAndUpdate(Integer nid, seqUnit newSeq) {
        if (!newSeq.orExist) {
            //add new node ID, update both the nodes table and edges table - nid: the nodeID to add newSeq as child(directly after);
            //update the node Table
            ns.addRow();
            //String seqName = ">P"+String.valueOf(newSeq.allID);
            String seqName = ">P";
            int parRowID = getRowInd(nid);
            for (int i = ns.getRowCount() - 1; i > 0; i--) {
                if (i > parRowID + 1) {
                    ns.set(i, "NID", ns.getInt(i - 1, "NID"));
                    ns.set(i, "LABEL", ns.get(i - 1, "LABEL"));
                    ns.set(i, "TIME", ns.get(i - 1, "TIME"));
                    ns.set(i, "ISOTYPE", ns.get(i - 1, "ISOTYPE"));
                    ns.set(i, "NRNAS", ns.getInt(i - 1, "NRNAS"));
                    ns.set(i, "NREADS", ns.getInt(i - 1, "NREADS"));
                    ns.set(i, "NMUTATIONS", ns.getInt(i - 1, "NMUTATIONS"));
                    ns.set(i, "SEQUENCE", ns.get(i - 1, "SEQUENCE"));
                    ns.set(i, "ORIGIN", ns.get(i - 1, "ORIGIN"));
                } else if (i == parRowID + 1) {
                    ns.set(i, "NID", newSeq.allID);
                    ns.set(i, "LABEL", seqName);
                    ns.set(i, "TIME", 0);
                    ns.set(i, "ISOTYPE", newSeq.iso);
                    ns.set(i, "NRNAS", 0);
                    ns.set(i, "NREADS", 0);
                    ns.set(i, "NMUTATIONS", 1);
                    ns.set(i, "SEQUENCE", newSeq.seq);
                    ns.set(i, "ORIGIN",false);
                }
            }
            //update the edge Table
            int orRowNum = es.getRowCount();
            es.addRow();
            es.set(orRowNum, "source", newSeq.parID);
            es.set(orRowNum, "target", newSeq.allID);
            es.set(orRowNum, "WEIGHT", 1);

            for (int i = 0; i < orRowNum; i++) {
                int sid = es.getInt(i, "source");
                int tid = es.getInt(i, "target");

                if (newSeq.child.containsKey(tid)) {
                    es.set(i, "source", newSeq.allID);
                }
            }
            System.out.println("aaa");
        }
        else {
            //modify on existing node ID, only need to update the edges table - nid: not used here
            //update the edge Table
            int orRowNum = es.getRowCount();
            for (int i = 0; i < orRowNum; i++) {
                int sid = es.getInt(i, "source");
                int tid = es.getInt(i, "target");
                if (newSeq.child.containsKey(tid) && tid != newSeq.allID) {
                    es.set(i, "source", newSeq.allID);
                }
            }
            System.out.println("bbb");
        }
    }

    public HashMap<Integer,Integer> findDescendID (int par) {
        //given a node, find all descendant nodes (nodes directly link downstream) for this node
        //ArrayList<Integer> desc = new ArrayList<Integer>();
        HashMap<Integer,Integer> desc = new HashMap();
        Column srcNodes = es.getColumn("source");
        for (int i = 0;i < es.getRowCount();i++) {
            int tSrc = es.getInt(i,"source");
            if (tSrc == par) {
                int tEnd = es.getInt(i, "target");
                //desc.add(tEnd);
                desc.put(tEnd, 1);
            }
        }
        return(desc); //only return descendant ID
    }

    public Table findDescendTable (int par) {
        //given a node, find all descendant nodes (nodes directly link downstream) for this node
        //return network Unit
        HashMap<Integer,Integer> desc = new HashMap();
        Column srcNodes = es.getColumn("source");
        Table tmpNode = new Table();

        tmpNode.addColumn("NID", int.class);
        tmpNode.addColumn("LABEL", String.class);
        tmpNode.addColumn("TIME", int.class);
        tmpNode.addColumn("ISOTYPE", String.class);
        tmpNode.addColumn("NRNAS", int.class);
        tmpNode.addColumn("NREADS", int.class);
        tmpNode.addColumn("NMUTATIONS", int.class);
        tmpNode.addColumn("SEQUENCE", String.class);
        tmpNode.addColumn("ORIGIN", Boolean.class);

        int j = 0;
        for (int i = 0;i < es.getRowCount();i++) {
            int tSrc = es.getInt(i,"source");
            if (tSrc == par) {
                int tEnd = es.getInt(i, "target");
                desc.put(tEnd, 1);
            }
        }

        //based on the IDs, re-generate the Table
        j = 0;
        for (int i = 0; i < ns.getRowCount();i++) {
            int nid = ns.getInt(i,"NID");
            if (desc.containsKey(nid)) {
                tmpNode.addRow();
                tmpNode.set(j, "NID", ns.get(i,"NID"));
                tmpNode.set(j, "LABEL", ns.get(i,"LABEL"));
                tmpNode.set(j, "TIME", ns.get(i,"TIME"));
                tmpNode.set(j, "ISOTYPE", ns.get(i,"ISOTYPE"));
                tmpNode.set(j, "NRNAS", ns.get(i,"NRNAS"));
                tmpNode.set(j, "NREADS", ns.get(i,"NREADS"));
                tmpNode.set(j, "NMUTATIONS", ns.get(i,"NMUTATIONS"));
                tmpNode.set(j, "SEQUENCE", ns.get(i,"SEQUENCE"));
                tmpNode.set(j, "ORIGIN", ns.get(i,"ORIGIN"));
                j++;
            }
        }
        return(tmpNode);
    }

    public Column getSeq(HashMap<Integer,Integer> ids) {
        //get sequence column info from selected ids
        Table selTab = new Table();
        selTab.addColumn("NID",int.class);
        selTab.addColumn("SEQUENCE",String.class);
        selTab.addColumn("ISOTYPE",String.class);

        int j = 0;
        for(int i = 0; i < ns.getRowCount();i++) {
            int nid = ns.getInt(i,"NID");
            if (ids.containsKey(nid)) {
                j++;
                selTab.addRow();
                selTab.set(j,"NID",nid);
                selTab.set(j,"SEQUENCE",ns.get(i,"SEQUENCE"));
                selTab.set(j,"ISOTYPE",ns.get(i,"ISOTYPE"));
            }
        }
        return(selTab.getColumn("SEQUENCE"));
    }

    public Column getIso(HashMap<Integer,Integer> ids) {
        //get isotype column info from selected ids
        Table selTab = new Table();
        selTab.addColumn("PID",int.class);
        selTab.addColumn("SEQUENCE",String.class);
        selTab.addColumn("ISOTYPE",String.class);

        int j = 0;
        for(int i = 0; i < ns.getRowCount();i++) {
            int pid = ns.getInt(i,"PID");
            if (ids.containsKey(pid)) {
                j++;
                selTab.addRow();
                selTab.set(j,"PID",pid);
                selTab.set(j,"SEQUENCE",ns.get(i,"SEQUENCE"));
                selTab.set(j,"ISOTYPE",ns.get(i,"ISOTYPE"));
            }
        }
        return(selTab.getColumn("ISOTYPE"));
    }

    public static void writeNS(String file) throws IOException {
        //current visualization tool-> nodes file needes the NID in ascent order - need to fix this bug
        ArrayList<NodeList.LNode> nsArray = NodeList.toArray(ns);
        try (FileWriter wter = new FileWriter(file)) {
            Gson outJson = new GsonBuilder().setPrettyPrinting().create();
            outJson.toJson(nsArray, wter);
        }catch(Exception e){
            e.printStackTrace();
        }

    }

    public static void writeES(String file) throws IOException {
        ArrayList<EdgeList.LEdge> nsArray = EdgeList.toArray(es);
        try (FileWriter wter = new FileWriter(file)) {
            Gson outJson = new GsonBuilder().setPrettyPrinting().create();
            outJson.toJson(nsArray, wter);
        }
    }

    public static void shrinkNet() {
        //shrink the intermediate nodes, only retain branching nodes/leaf nodes and original nodes
        try {
            int rtNID = findRoot();
            HashMap<Integer,Integer> tmpHash = new HashMap<>();
            HashMap<Integer,Integer> tmpPar = new HashMap<>(); //store parent node
            HashMap<Integer,Boolean> remID = new HashMap<>();
            for (int i = 0;i < ns.getRowCount();i++) {
                int id = ns.getInt(i,"NID");
                if (ns.getString(i,"ORIGIN").equals("true")) {
                    tmpHash.put(id, 2);
                }
                else {
                    tmpHash.put(id, 0);
                }
            }
            for (int i = 0; i < es.getRowCount();i++) {
                int sid = es.getInt(i, "source");
                int tid = es.getInt(i, "target");
                tmpHash.put(sid,tmpHash.get(sid)+1);
                tmpPar.put(tid, sid);
            }
            //set nodes within the original nodes and leaf/branching nodes
            Iterator it = tmpHash.entrySet().iterator();
            while(it.hasNext()) {
                Map.Entry<Integer,Integer> pair = (Map.Entry<Integer,Integer>)it.next();
                int nid = pair.getKey();
                int sz = pair.getValue();
                if (sz == 0 || sz > 1) {
                    remID.put(nid,true);
                }
                else {
                    remID.put(nid,false);
                }
            }

            //shrink the edges
            Table edgeTable2= new Table();
            edgeTable2.addColumn(Graph.DEFAULT_SOURCE_KEY, int.class);
            edgeTable2.addColumn(Graph.DEFAULT_TARGET_KEY, int.class);
            edgeTable2.addColumn("WEIGHT",int.class);
            HashMap<Integer, Integer>withinEdge = new HashMap<>(); //stores whether nid is in the edge table-for update node table

            int jj = 0;
            for (int i = 0;i < ns.getRowCount();i++) {
                int curNID = ns.getInt(i,"NID");
                if (remID.get(curNID) && curNID != rtNID) {
                    int tmpMut = 0;
                    int curPar = tmpPar.get(curNID);
                    int tNID = curNID;
                    while (!remID.get(curPar)) {
                        tmpMut++;
                        tNID = curPar;
                        curPar = tmpPar.get(tNID);
                    }
                    edgeTable2.addRow();
                    edgeTable2.set(jj, Graph.DEFAULT_SOURCE_KEY, curPar);
                    edgeTable2.set(jj, Graph.DEFAULT_TARGET_KEY, curNID);
                    withinEdge.put(curPar,1);
                    withinEdge.put(curNID,1);
                    edgeTable2.set(jj, "WEIGHT", tmpMut);
                    jj++;
                }
            }

            //shrink the nodeTable
            Table nodeTable2 = new Table();
            nodeTable2.addColumn("NID", int.class);
            nodeTable2.addColumn("LABEL", String.class);
            nodeTable2.addColumn("TIME", int.class);
            nodeTable2.addColumn("ISOTYPE", String.class);
            nodeTable2.addColumn("NRNAS", int.class);
            nodeTable2.addColumn("NREADS", int.class);
            nodeTable2.addColumn("NMUTATIONS", int.class);
            nodeTable2.addColumn("SEQUENCE", String.class);
            nodeTable2.addColumn("ORIGIN", Boolean.class);

            jj = 0;
            for(int i = 0; i < ns.getRowCount();i++) {
                int nid = ns.getInt(i,"NID");
                if (withinEdge.containsKey(nid)) {
                    nodeTable2.addRow();
                    nodeTable2.set(jj,"NID",ns.getInt(i,"NID"));
                    nodeTable2.set(jj, "LABEL", ns.get(i,"LABEL"));
                    nodeTable2.set(jj, "TIME", ns.get(i,"TIME"));
                    nodeTable2.set(jj, "ISOTYPE", ns.get(i,"ISOTYPE"));
                    nodeTable2.set(jj, "NRNAS", ns.get(i,"NRNAS"));
                    nodeTable2.set(jj, "NREADS", ns.get(i,"NREADS"));
                    nodeTable2.set(jj, "NMUTATIONS", ns.get(i,"NMUTATIONS"));
                    nodeTable2.set(jj, "SEQUENCE", ns.get(i,"SEQUENCE"));
                    nodeTable2.set(jj, "ORIGIN", ns.get(i,"ORIGIN"));
                    jj++;
                }
            }

            es = edgeTable2;
            //ns = nodeTable2;

        }catch(Exception e){
            e.printStackTrace();
        }
    }

}

