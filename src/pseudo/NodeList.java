package pseudo;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import prefuse.data.Table;
import java.applet.*;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.*;

/**
 * Created by keke on 12/14/2016.
 */
class NodeList{
    LNode[] sequences;

    public static class LNode {
        int nid;
        String label;
        int time;
        String isotype;
        int nRNAs;
        int nReads;
        int nMutations;
        String sequence;
        String origin;
    }

    public static Table sortNodesOnEdge (Table nt, Table et) {
        //sort node table based on the edge table, for edge table, root is at the very beginning
        int len = et.getRowCount()+1;
        int[] order = new int[len];
        int pos = 0;
        order[pos] = et.getInt(0,"source");
        for (int i = 0; i < et.getRowCount();i++) {
            int tid = et.getInt(i,"target");
            pos++;
            order[pos] = tid;
        }

        //sort node table based on the order list from edge table
        Table nt2 = new Table();
        nt2.addColumn("NID", int.class);
        nt2.addColumn("LABEL", String.class);
        nt2.addColumn("TIME", int.class);
        nt2.addColumn("ISOTYPE", String.class);
        nt2.addColumn("NRNAS", int.class);
        nt2.addColumn("NREADS", int.class);
        nt2.addColumn("NMUTATIONS", int.class);
        nt2.addColumn("SEQUENCE", String.class);
        nt2.addColumn("ORIGIN", Boolean.class);
        pos = 0;
        for (int ind: order) {
            for (int i = 0; i < nt.getRowCount();i++) {
                int cid = nt.getInt(i, "NID");
                if (cid == ind) {
                    nt2.addRow();
                    nt2.set(pos, "NID", nt.get(i,"NID"));
                    nt2.set(pos, "LABEL", nt.get(i,"LABEL"));
                    nt2.set(pos, "TIME", nt.get(i,"TIME"));
                    nt2.set(pos, "ISOTYPE", nt.get(i,"ISOTYPE"));
                    nt2.set(pos, "NRNAS", nt.get(i,"NRNAS"));
                    nt2.set(pos, "NREADS", nt.get(i,"NREADS"));
                    nt2.set(pos, "NMUTATIONS", nt.get(i,"NMUTATIONS"));
                    nt2.set(pos, "SEQUENCE", nt.get(i,"SEQUENCE"));
                    nt2.set(pos, "ORIGIN", nt.get(i,"ORIGIN"));
                }
            }
            pos++;
        }
        return(nt2);
    }


    public static ArrayList<LNode> toArray(Table nt) {
        ArrayList<LNode> array = new ArrayList<>();
        for (int i = 0;i < nt.getRowCount();i++ ) {
            LNode t = new LNode();
            t.nid = nt.getInt(i,"NID");
            t.isotype = nt.getString(i,"ISOTYPE");
            t.label = nt.getString(i,"LABEL");
            t.time = nt.getInt(i,"TIME");
            t.nRNAs = nt.getInt(i,"NRNAS");
            t.nReads = nt.getInt(i,"NREADS");
            t.nMutations = nt.getInt(i,"NMUTATIONS");
            t.sequence = nt.getString(i,"SEQUENCE");
            t.origin = nt.getString(i,"ORIGIN");
            array.add(i,t);
        }

        Collections.sort(array, new Comparator<LNode>() {
            @Override
            public int compare(LNode node1, LNode node2) {
                Integer aa = node1.nid;
                Integer bb = node2.nid;
                return aa.compareTo(bb);
            }
        });

        return(array);
    }
/*    public static Table addNode(Table org, LNode inf) {

        return();
    }*/

    public static Table parse(File f){
        try{
            BufferedReader reader = new BufferedReader(new FileReader(f));
            Gson gson = new GsonBuilder().create();
            //String js = "{'sequences':[{ 'isotype': 'IgM', 'label': '>5mC-PBMC-Acute-IgM_2_2_5', 'nMutations': 5, 'nRNAs': 2, 'nReads': 2, 'nid': 0, 'sequence': 'ACCTTTAGCAGCTATGCCATGAGTTGGGTCCGCCAGGCTCCAGGGAAGGGGCTGGAGTGGGTCTCAGCTATTAGTGGTAGTGGTGGTAGAACATACTACGCAGACTCCGTGAAGGGCCGGTTCACCATCTCCAGAGACAATTCCAAGAACACGGTGTATCTGCAAATGAACAGCCTGAGAGCCGAGGACACGGCCGTATATTACTGTGCGAAAGATGAAGACTTCCCCAATGATGTTTTTGATATTTGGGGCCAAGGGACAATGGTCACCGTCTCTTCAGGGAGTGCATCCGCCCCAACCCTTTTCCCCCTCGTCTCCTGTGAGAATTCC', 'time': 52}]}";
            //System.out.println(reader.readLine());
            //System.exit(0);
            //System.out.println(gson.fromJson(reader, NodeList.class));
            NodeList nl = gson.fromJson(reader, NodeList.class);

            //LNode [] ns = gson.fromJson(reader, LNode[].class);
            //return (nl);

            Table nodeTable = new Table();
            //Add columns to Node Table
            nodeTable.addColumn("NID", int.class);
            nodeTable.addColumn("LABEL", String.class);
            nodeTable.addColumn("TIME", int.class);
            nodeTable.addColumn("ISOTYPE", String.class);
            nodeTable.addColumn("NRNAS", int.class);
            nodeTable.addColumn("NREADS", int.class);
            nodeTable.addColumn("NMUTATIONS", int.class);
            nodeTable.addColumn("SEQUENCE", String.class);
            nodeTable.addColumn("ORIGIN", Boolean.class);
            //for (int i=0; i<nodeTable.getColumnCount(); i++)
            //    System.out.println("column "+i +" "+nodeTable.getColumnName(i));

            int i=0;
            for (LNode n: nl.sequences) {
                if (n!=null) {
                    nodeTable.addRow();

                    nodeTable.set(i, "NID", n.nid);
                    nodeTable.set(i, "LABEL", n.label);
                    nodeTable.set(i, "TIME", n.time);
                    nodeTable.set(i, "ISOTYPE", n.isotype.toUpperCase());
                    nodeTable.set(i, "NRNAS", n.nRNAs);
                    nodeTable.set(i, "NREADS", n.nReads);
                    nodeTable.set(i, "NMUTATIONS", n.nMutations);
                    nodeTable.set(i, "SEQUENCE", n.sequence.toUpperCase());
                    nodeTable.set(i, "ORIGIN", true);
                    i++;
                }
            }
            //System.out.println(i+" nodes");
            return nodeTable;
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

/*    public static String clearStr(String org) {
        String orgNew = org.replaceAll("\\'","");
        orgNew = org.replaceAll("\\"","");
        return(orgNew);
    }*/
}
