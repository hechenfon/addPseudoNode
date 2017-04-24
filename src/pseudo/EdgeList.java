package pseudo;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import prefuse.data.Graph;
import prefuse.data.Table;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

/**
 * Created by keke on 12/14/2016.
 */
public class EdgeList {
    public static class LEdge {
        int n1;
        int n2;
        int weight;
    }

    LEdge [] edges;

    public static ArrayList<LEdge> toArray(Table nt) {
        ArrayList<LEdge> array = new ArrayList<>();
        for (int i = 0;i < nt.getRowCount();i++ ) {
            LEdge t = new LEdge();
            t.n1 = nt.getInt(i,"source");
            t.n2 = nt.getInt(i,"target");
            t.weight = nt.getInt(i,"WEIGHT");
            array.add(i,t);
        }
        return(array);
    }

    public static Table parse (File f) {
        try{
            BufferedReader reader = new BufferedReader(new FileReader(f)) ;
            Gson gson = new GsonBuilder().create();
            EdgeList el = gson.fromJson(reader, EdgeList.class);
            Table edgeTable= new Table();
            edgeTable.addColumn(Graph.DEFAULT_SOURCE_KEY, int.class);
            edgeTable.addColumn(Graph.DEFAULT_TARGET_KEY, int.class);
            edgeTable.addColumn("WEIGHT",int.class);
            int i=0;
            for (LEdge e: el.edges) if (e!=null) {
                //System.out.println(n.n1 + " " + n.n2);
                edgeTable.addRow();
                edgeTable.set(i, Graph.DEFAULT_SOURCE_KEY, e.n1);
                edgeTable.set(i, Graph.DEFAULT_TARGET_KEY, e.n2);
                edgeTable.set(i, "WEIGHT", e.weight);
                i++;
            }
            //System.out.println(i+"edges");
            return edgeTable;
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }



}
