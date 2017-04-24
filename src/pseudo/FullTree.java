package pseudo;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.biojava.nbio.core.exceptions.CompoundNotFoundException;

import prefuse.data.Table;
import prefuse.data.column.Column;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

/**
 * Created by hechenfon on 3/9/2017.
 * this class is to take COLT c++ tree and add pseudonodes based on multiple sequence alignment from MUSCLE
 */
public class FullTree {
    Table allNs;
    Table allEs;

    public FullTree(String nf, String ef) throws CompoundNotFoundException, IOException {
        File nff = new File(nf);

        File eff = new File(ef);
        if (nff.exists() && eff.exists()) {
            Table nt1 = NodeList.parse(new File(nf));
            Table et = EdgeList.parse(new File(ef));
            Table nt = NodeList.sortNodesOnEdge(nt1, et); //sort all the nodes based on the edge list
                                                            //better sort by the distance from the root (linAnalyzer.findRoot)

            this.allNs = nt;
            this.allEs = et;

            int psID = 0; //ID of pseudonodes

            //start the main loop, inner/outer loops
            int ckID = 0;  //row on the node list
            int addID = 0;
            linAnalyzer tmpLin = new linAnalyzer(nt,et);

            while(ckID < nt.getRowCount()) {//outer loop
                int NID = nt.getInt(ckID,"NID"); //SHOULD BE root node nid
                System.out.println(">>>>>>>>>>>>>>>adding from node"+ckID+"vs"+NID);
                String rtSeq = nt.getString(ckID,"SEQUENCE");
                String rtIso = nt.getString(ckID,"ISOTYPE");

                Table selDesc = tmpLin.findDescendTable(NID);
                Column descSeq = selDesc.getColumn("SEQUENCE");
                Column descIso = selDesc.getColumn("ISOTYPE");
                Column descNid = selDesc.getColumn("NID");

                if (selDesc.getRowCount()>0) { //exclude leaf node
                    multiSeqs ms = new multiSeqs(descSeq, descIso, descNid);
                    //boolean aa = ms.isOneStepMul(rtSeq, rtIso);
                    while (!ms.isOneStep(rtSeq, rtIso)) {//inner loop
                        int mmID = tmpLin.orgLastID + addID;
                        System.out.println("generating pseudo" + psID + ";"+"total:"+mmID);

                        mutObj tmpMut = ms.findAllMut(rtSeq, rtIso);

                        Entry<Integer, Double> maxMut = ms.maxScoreMut(tmpMut);
                        seqUnit newSeq = ms.genMutSeq(rtSeq, rtIso, maxMut);

                        if (!ms.withinOrg(newSeq.seq, newSeq.iso)) {
                            addID++;
                            newSeq.child = ms.findMutDescend(maxMut);
                            newSeq.parID = NID;
                            newSeq.psID = psID;
                            newSeq.orExist = false;
                            newSeq.allID = tmpLin.orgLastID + addID;
                            tmpLin.addAndUpdate(NID, newSeq);
                            psID++;
                        }
                        else {
                            int orID = ms.findOrgNID(newSeq.seq);
                            newSeq.child = ms.findMutDescend(maxMut);
                            newSeq.parID = NID;
                            newSeq.allID = orID;
                            newSeq.orExist = true;

                            tmpLin.addAndUpdate(NID,newSeq);
                        }
                        selDesc = tmpLin.findDescendTable(NID);
                        descSeq = selDesc.getColumn("SEQUENCE");
                        descIso = selDesc.getColumn("ISOTYPE");
                        descNid = selDesc.getColumn("NID");

                        ms = new multiSeqs(descSeq, descIso, descNid);

                    }
                }
                ckID++;
            }
            //test-shrink the network or not
            tmpLin.shrinkNet();

            //write out structure
            tmpLin.writeNS("data/check_263021/263021gp.n");
            tmpLin.writeES("data/check_263021/263021gp.e");

            System.out.print("123");
        }
    }

    public static void main(String[] args) {
        //String nodeFile = args[0];
        //String edgeFile = args[1];

        String nodeFile = "D:/sync_labwork/Box Sync/Jiang/tree_manu_prep/for.pseudonodes/addPseudoNode/data/check_263021/263021g.n";
        String edgeFile = "D:/sync_labwork/Box Sync/Jiang/tree_manu_prep/for.pseudonodes/addPseudoNode/data/check_263021/263021g.e";

        try {
            FullTree ft = new FullTree(nodeFile, edgeFile);
        } catch (CompoundNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
