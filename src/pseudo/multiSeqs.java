package pseudo;

import prefuse.data.Table;
import prefuse.data.column.Column;
import java.util.*;
import java.util.Map.Entry;


import java.util.Iterator;

/**
 * Created by hechenfon on 3/22/2017.
 * a collection of multiple sequences, and related methods, including mutation list etc
 */
public class multiSeqs {// either all sequences within a lineage or all descendants of a node

    private Table seqChars; //Table to store all sequences as characters;
    private Column seqs;  //store all sequence information
    private Column seqIsos; //store all isotype information
    private Column seqNid;  //store all NID information
    private int[][] mutMat; //store mutation matrix
    private mutObj allMut; //store all mutation information
    private HashMap<Integer,Double> mutScore; //store all mutation score



    public multiSeqs(Column colSeq) {
        //read in the sequences in Column format, generate table of characters for all sequences
        int nn = colSeq.getString(0).length();
        Table tmpChars = new Table();
        for (int i = 0; i < nn;i++) {
            tmpChars.addColumn(String.valueOf(i),String.class);
        }

        for (int i = 0; i < colSeq.getRowCount(); i++) {
            String tSeq = colSeq.getString(i);
            char[] tChar = tSeq.toCharArray();
            tmpChars.addRow();
            for (int j = 0; j < tChar.length; j++) {
                tmpChars.set(i,j,Character.toString(tChar[j]));
            }
        }
        this.seqChars = tmpChars;
    }
    public multiSeqs(Column colSeq, Column colIso) {
        //read in the sequences in Column format, generate table of characters for all sequences
        int nn = colSeq.getString(0).length();
        Table tmpChars = new Table();
        for (int i = 0; i < nn;i++) {
            tmpChars.addColumn(String.valueOf(i),String.class);
        }

        for (int i = 0; i < colSeq.getRowCount(); i++) {
            String tSeq = colSeq.getString(i);
            char[] tChar = tSeq.toCharArray();
            tmpChars.addRow();
            for (int j = 0; j < tChar.length; j++) {
                tmpChars.set(i,j,Character.toString(tChar[j]));
            }
        }
        this.seqs = colSeq;
        this.seqChars = tmpChars;
        this.seqIsos = colIso;
    }
    public multiSeqs(Column colSeq, Column colIso, Column colNid) {
        //read in the sequences in Column format, generate table of characters for all sequences
        int nn = colSeq.getString(0).length();
        Table tmpChars = new Table();
        for (int i = 0; i < nn;i++) {
            tmpChars.addColumn(String.valueOf(i),String.class);
        }

        for (int i = 0; i < colSeq.getRowCount(); i++) {
            String tSeq = colSeq.getString(i);
            char[] tChar = tSeq.toCharArray();
            tmpChars.addRow();
            for (int j = 0; j < tChar.length; j++) {
                tmpChars.set(i,j,Character.toString(tChar[j]));
            }
        }
        this.seqs = colSeq;
        this.seqChars = tmpChars;
        this.seqIsos = colIso;
        this.seqNid = colNid;
    }

    public boolean isOneStepMul(String rtSeq, String rtIso) {
        int[][] mutMat = calMutMat(rtSeq,rtIso);
        this.mutMat = mutMat;
        boolean onlyOne = true;

        mutObj curMuts = findAllMutNoSub(rtSeq,rtIso);

        if (curMuts.org.size() > 1) {
            onlyOne = false;
        }
        System.out.println("kkk"+curMuts.org.size());
        return(onlyOne);
    }

    public boolean isOneStep(String rtSeq, String rtIso) {
        int[][] mutMat = calMutMat(rtSeq,rtIso);
        this.mutMat = mutMat;

        boolean onlyOne = true;
        for (int i = 0; i < mutMat.length;i++) {
            int[] t = mutMat[i];
            HashMap<Integer,Integer> occ = tabArray(t);
            if (occ.size() > 1) {
                onlyOne = false;
                break;
            }
        }
        return(onlyOne);
    }

    private String mutToStr(int j) {
        //based on the coding of mute type, transfer back integers to char
        if (j == 1) {
            return("A");
        }
        else if (j==2) {
            return("G");
        }
        else if (j == 3) {
            return("C");
        }
        else if (j == 4) {
            return("T");
        }
        else if (j == 5) {
            return("N");
        }
        else if (j == 6) {
            return("-");
        }
        else {
            return null;
        }
    }


    public mutObj findAllMutNoSub(String rtSeq, String rtIso) {
        //only allow point mutation/insertion/deletion right now
        mutObj allMut = new mutObj();
        int[][] mutMat = calMutMat(rtSeq,rtIso);
        int[] colSum = colSum(mutMat);

        int[] rowMutTp = new int[mutMat.length]; //record tabulate of each row, one mutation will be ignored(descendant has one-step mutation to the parent node)

        for (int i = 0;i < mutMat.length;i++) {
            int[] t = mutMat[i];
            HashMap<Integer,Integer> occ = tabArray(t);
            rowMutTp[i] = occ.size();
            //allow for continuous mutations
            if (rowMutTp[i] > 1) {
                int mutNum = 0;
                for (int j = 0;j < colSum.length-1;j++) {
                    if (mutMat[i][j] > 0) {
                        mutNum++;
                        List<Integer> nzPos = new ArrayList<Integer>();
                        List<String> nzNts = new ArrayList<String>();
                        nzPos.add(j);
                        nzNts.add(mutToStr(mutMat[i][j]));
                        j++;
                        while (j < colSum.length-1 && mutMat[i][j] > 0) {
                            nzPos.add(j);
                            nzNts.add(mutToStr(mutMat[i][j]));
                            j++;
                        }
                        //find all possible mutations
                        int len = nzPos.size();

                        int st = nzPos.get(0);

                        allMut.org.put(mutNum,rtSeq.substring(st,st+len));
                        allMut.pos.put(mutNum,new int[]{st,len});
                        String tgtSub = "";
                        for (int e = 0; e < len; e++) {
                            tgtSub += nzNts.get(e);
                        }
                        allMut.tgt.put(mutNum,tgtSub);
                        //System.out.println("aaa"+rtSeq.substring(st,st+len)+"===>"+tgtSub);
                    }
                }
                int ii = mutMat[i][colSum.length-1];
                if (ii != 0) {
                    mutNum++;
                    String tgIso = "";
                    if (ii == 11) {
                        tgIso = "IGA";
                    }
                    else if (ii == 12) {
                        tgIso = "IGD";
                    }
                    else if (ii == 13) {
                        tgIso = "IGE";
                    }
                    else if (ii == 14) {
                        tgIso = "IGG";
                    }
                    else if (ii == 15) {
                        tgIso = "IGM";
                    }
                    allMut.org.put(mutNum,rtIso);
                    allMut.pos.put(mutNum,new int[]{colSum.length-1,1});
                    allMut.tgt.put(mutNum,tgIso);

                }

            }
        }
        this.allMut = allMut;
        return (allMut);
    }



    public mutObj findAllMut(String rtSeq, String rtIso) {
        //only allow point mutation/insertion/deletion right now
        mutObj allMut = new mutObj();
        int[][] mutMat = calMutMat(rtSeq,rtIso);
        int[] colSum = colSum(mutMat);

        int[] rowMutTp = new int[mutMat.length]; //record tabulate of each row, one mutation will be ignored(descendant has one-step mutation to the parent node)
/*
        for (int i = 0; i < mutMat.length;i++) {
            int[] t = mutMat[i];
            HashMap<Integer,Integer> occ = tabArray(t);
            rowMutTp[i] = occ.size();
        }
*/

        for (int i = 0;i < mutMat.length;i++) {
            int[] t = mutMat[i];
            HashMap<Integer,Integer> occ = tabArray(t);
            rowMutTp[i] = occ.size();
            //allow for continuous mutations, subsets of continuous mutations are also valid candidates
            if (rowMutTp[i] > 1) {
                int mutNum = 0;
                for (int j = 0;j < colSum.length-1;j++) {
                    if (mutMat[i][j] > 0) {
                        List<Integer> nzPos = new ArrayList<Integer>();
                        List<String> nzNts = new ArrayList<String>();
                        nzPos.add(j);
                        nzNts.add(mutToStr(mutMat[i][j]));
                        j++;
                        while (j < colSum.length-1 && mutMat[i][j] > 0) {
                            nzPos.add(j);
                            nzNts.add(mutToStr(mutMat[i][j]));
                            j++;
                        }


                        //find all possible mutations
                        int len = nzPos.size();

                        int st = nzPos.get(0);
                        //only allow point mutation, <=> window size can only be 1
                        for (int w = 1; w <= 1; w++) {
                        //for (int w = 1; w <= len; w++) {
                            for (int s = 0; s <= len-w; s++) {
                                String tgtSub = "";
                                for (int e = s; e < s+w; e++) {
                                    //tgtSub.concat(nzNts.get(e));
                                    tgtSub += nzNts.get(e);
                                }
                                mutNum++;
                                String orgSub = rtSeq.substring(st+s,st+s+w);
                                allMut.org.put(mutNum,orgSub);
                                allMut.pos.put(mutNum,new int[]{st+s,w});
                                allMut.tgt.put(mutNum,tgtSub);
                            }
                        }
                    }
                }
                int ii = mutMat[i][colSum.length-1];
                if (ii != 0) {
                    mutNum++;
                    String tgIso = "";
                    if (ii == 11) {
                        tgIso = "IGA";
                    }
                    else if (ii == 12) {
                        tgIso = "IGD";
                    }
                    else if (ii == 13) {
                        tgIso = "IGE";
                    }
                    else if (ii == 14) {
                        tgIso = "IGG";
                    }
                    else if (ii == 15) {
                        tgIso = "IGM";
                    }
                    allMut.org.put(mutNum,rtIso);
                    allMut.pos.put(mutNum,new int[]{colSum.length-1,1});
                    allMut.tgt.put(mutNum,tgIso);
                }

            }
        }
        this.allMut = allMut;
        return (allMut);
    }

    public Entry<Integer,Double> maxScoreMut(mutObj allMut) {
        /*score the mutation based on the criteria,
         now - only count the number of seqs with certain mutation (method: x10000 for different type)
        */
        HashMap<Integer,String> org = allMut.org;
        HashMap<Integer,int[]> pos = allMut.pos;
        HashMap<Integer,String> tgt = allMut.tgt;

        HashMap<Integer,Double> mutScore = new HashMap<Integer,Double>();


        for (Integer i : org.keySet()) {
            int[] mutPos = pos.get(i);
            String tgtSub = tgt.get(i);
            //1. count on the number of descendants sharing this mutation
            Double score = score1(mutPos, tgtSub) * Math.pow(10,4*5);
            Double ss = score;
            //2. count on the number of insert/delete/substitute within this mutation
            score = score + score2(mutPos, tgtSub) * Math.pow(10,4*4);
            //3. count on number of mutations shared by descendants with this mutation
        //    score = score + score3(mutPos, tgtSub) * Math.pow(10,4*3);
            //4. count on average distance between all pairs of descendants with this mutation
            //5. count on average distance from the root node to all descendants with this mutation

            if (i == 4){
                System.out.println("123");
            }

            mutScore.put(i,score);
        }

        //sort scores by value-descending order
        //HashMap<Integer,Double> sortedHash = sortHash(mutScore);
        Entry<Integer,Double> maxMut = maxHash(mutScore);

        return(maxMut);
    }

    private double score3(int[] pos, String tgt) {
        //3. count on the number of mutations shared by descendants with this mutation
        int seqNum = seqs.getRowCount();
        ArrayList<int[]> tmpMutMat = new ArrayList<int[]>();

        double score = 0;
        if (pos[0] < seqs.getString(0).length()) {
            for (int i = 0; i < seqNum; i++) {
                String tgSeq = seqs.getString(i);
                String subSeq = tgSeq.substring(pos[0], pos[0] + pos[1]);
                if (subSeq.equals(tgt)) {
                    tmpMutMat.add(mutMat[i]);
                }
            }
        }
        else {
            for (int i = 0; i < seqNum; i++) {
                if (seqIsos.getString(i).equals(tgt)) {
                    tmpMutMat.add(mutMat[i]);
                }
            }
        }

        for (int i = 0;i <= tmpMutMat.get(0).length;i++) {
            if (i != pos[0]) {
                ArrayList<Integer> tmpArr = new ArrayList<Integer>();
                for (int j = 0; j < tmpMutMat.size();j++) {
                    int cc = tmpMutMat.get(i)[j];
                    if (cc > 0) {
                        tmpArr.add(cc);
                    }
                }
                HashMap<Integer,Integer> tab = tabArray(tmpArr);
                if (tab.size() == 1) {
                    score++;
                }
            }
        }
        return(score);
    }

    private double score2(int[]pos, String tgt) {
        //2. count on the number of insert/delete/substitute within this mutation
        double score = 0;
        if (pos[0] < seqs.getString(0).length()) {
            score = pos[1];
        }
        else {
            score = 100; //weighted more on isotype-switch
        }
        return(score);
    }


    private double score1(int[] pos, String tgt) {
        //1. count the number of seqs with certain mutation
        int seqNum = seqs.getRowCount();
        double score = 0;
        if (pos[0] < seqs.getString(0).length()) {
            for (int i = 0; i < seqNum; i++) {
                String tgSeq = seqs.getString(i);
                String subSeq = tgSeq.substring(pos[0], pos[0] + pos[1]);
                if (subSeq.equals(tgt)) {
                    score++;
                }
            }
        }
        else {
            for (int i = 0; i < seqNum; i++) {
                if (seqIsos.getString(i).equals(tgt)) {
                    score++;
                }
            }
        }
        return(score);
    }

    public boolean withinOrg (String ss, String ii) {
        //check both isotype and sequence
        boolean isOrg = false;
        for (int i = 0;i < seqs.getRowCount();i++) {
            String seq = seqs.getString(i);
            String iso = seqIsos.getString(i);
            if (ss.equals(seq) && ii.equals(iso)) {
                isOrg = true;
                break;
            }
        }
        return(isOrg);
    }

    public int findOrgNID(String ss) {
        int oID = -1;
        for (int i = 0;i < seqs.getRowCount();i++) {
            String seq = seqs.getString(i);
            if (ss.equals(seq)) {
                oID = seqNid.getInt(i);
            }
        }
        return(oID);
    }

    public HashMap<Integer,Integer> findMutDescend (Entry<Integer,Double> selMut) {
        //find all descendant nodes bearing certain mutation
        HashMap<Integer,String> org = allMut.org;
        HashMap<Integer,int[]> pos = allMut.pos;
        HashMap<Integer,String> tgt = allMut.tgt;

        Integer selMutID = selMut.getKey();
        int[] maxMutPos = pos.get(selMutID);
        String maxMutOrg = org.get(selMutID);
        String maxMutTgt = tgt.get(selMutID);

        HashMap<Integer,Integer> allMutDes = new HashMap<Integer,Integer>();
        int seqLen = seqs.getString(0).length();

        int seqNum = seqs.getRowCount();

        //System.out.println("aaa"+maxMutPos[0]+";"+seqLen);
        //System.out.println("bbb"+tgt);
        if (maxMutPos[0] < seqLen) {
            for (int i = 0; i < seqNum; i++) {
                String tgSeq = seqs.getString(i);
                String subSeq = tgSeq.substring(maxMutPos[0], maxMutPos[0] + maxMutPos[1]);
                if (subSeq.equals(maxMutTgt)) {
                    allMutDes.put(seqNid.getInt(i),1);
                }
            }
        }
        else { //for isotype case
            for (int i = 0; i < seqNum; i++) {
                if (seqIsos.getString(i).equals(maxMutTgt)) {
                    allMutDes.put(seqNid.getInt(i),1);
                }
            }
        }
        return (allMutDes);
    }


    public HashMap<Integer,Double> scoreAllMut(mutObj allMut) {
        //score the mutation based on the criteria, now - only count the number of seqs with certain mutation (method: x10000 for different type)
        HashMap<Integer,String> org = allMut.org;
        HashMap<Integer,int[]> pos = allMut.pos;
        HashMap<Integer,String> tgt = allMut.tgt;

        HashMap<Integer,Double> mutScore = new HashMap<Integer,Double>();
        for (Integer i : org.keySet()) {
            String orgSub = org.get(i);
            int[] mutPos = pos.get(i);
            String tgtSub = tgt.get(i);

            Double score = score1(mutPos, tgtSub) * Math.pow(10,4*5);
            mutScore.put(i,score);
        }

        //sort scores by value-descending order
        HashMap<Integer,Double> sortedHash = sortHash(mutScore);
        //Entry<Integer,Double> maxMut = maxHash(mutScore);
        return(sortedHash);
    }

    private HashMap<Integer,Double> sortHash(HashMap<Integer,Double> orgHash) {
        //sort hash map by value
        Set<Entry<Integer,Double>> entries = orgHash.entrySet();
        List<Entry<Integer, Double>> listOfEntries = new ArrayList<Entry<Integer, Double>>(entries);
        Collections.sort(listOfEntries, valueComparator);
        LinkedHashMap<Integer, Double> sortedByValue = new LinkedHashMap<Integer, Double>(listOfEntries.size());
        for(Entry<Integer, Double> entry : listOfEntries){
            sortedByValue.put(entry.getKey(), entry.getValue());
        }

        return(sortedByValue);
    }

    private Entry<Integer, Double> maxHash(HashMap<Integer,Double> orgHash) {
        //max hash map by value
        Set<Entry<Integer,Double>> entries = orgHash.entrySet();
        List<Entry<Integer, Double>> listOfEntries = new ArrayList<Entry<Integer, Double>>(entries);
        Collections.sort(listOfEntries, valueComparator);
        LinkedHashMap<Integer, Double> sortedByValue = new LinkedHashMap<Integer, Double>(listOfEntries.size());

        for(Entry<Integer, Double> entry : listOfEntries){
            sortedByValue.put(entry.getKey(), entry.getValue());
        }
        Entry<Integer, Double> maxMut = listOfEntries.get(0);
        return(maxMut);
    }

    private Comparator<Entry<Integer, Double>> valueComparator = new Comparator<Entry<Integer,Double>>() {
        @Override
        public int compare(Entry<Integer, Double> e1, Entry<Integer, Double> e2) {
            Double v1 = e1.getValue();
            Double v2 = e2.getValue();
            return v2.compareTo(v1);
        }
    };


    public seqUnit genMutSeq(String rtSeq, String rtIso, Entry<Integer,Double> maxMut) {
        //select the highest scored mutation, generate the mutated sequences/isotypes based on mutation information from the rootSeq/rootIso
        HashMap<Integer,String> org = allMut.org;
        HashMap<Integer,int[]> pos = allMut.pos;
        HashMap<Integer,String> tgt = allMut.tgt;

        Integer selMutID = maxMut.getKey();
        int[] maxMutPos = pos.get(selMutID);
        String maxMutOrg = org.get(selMutID);
        String maxMutTgt = tgt.get(selMutID);

        seqUnit newSeq = new seqUnit(rtSeq,rtIso);

        //replace the root seq with mutation
        int colLen = mutMat[0].length-1;
        try {
            if (maxMutPos[0] < colLen) {
                char[] seqChars = rtSeq.toCharArray();
                char[] repChars = maxMutTgt.toCharArray();
                for (int i = maxMutPos[0]; i < maxMutPos[0] + maxMutPos[1]; i++) {
                    seqChars[i] = repChars[i - maxMutPos[0]];
                }
                String repSeq = new String(seqChars);
                newSeq.seq = repSeq;
            } else {
                newSeq.iso = maxMutTgt;
            }
            return (newSeq);
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }

    }



    private int[][] calMutMat (String rtSeq, String rtIso) {
        //based on input parent seq and all child sequences, generate distance table
        int matRowSize = seqs.getRowCount();
        int matColSize = rtSeq.length();

        int[][] mutMat = new int[matRowSize][matColSize+1]; //store all mutation information ,LAST COLUMN of the matrix is for ISOTYPE
        char[] rtChars = rtSeq.toCharArray();

        for (int i=0; i<matRowSize; i++) {
            String tgSeq = seqs.getString(i);
            String tgIso = seqIsos.getString(i);
            char[] tgChars = tgSeq.toCharArray();
            for (int j = 0; j < rtChars.length; j++) {
                if (rtChars[j] == tgChars[j]) {
                    mutMat[i][j] = 0;
                }
                else {
                    if (tgChars[j] == 'A') {
                        mutMat[i][j] = 1;
                    }
                    else if (tgChars[j] == 'G') {
                        mutMat[i][j] = 2;
                    }
                    else if (tgChars[j] == 'C') {
                        mutMat[i][j] = 3;
                    }
                    else if (tgChars[j] == 'T') {
                        mutMat[i][j] = 4;
                    }
                    else if (tgChars[j] == 'N') {
                        mutMat[i][j] = 5;
                    }
                    else if (tgChars[j] == '-') {
                        mutMat[i][j] = 6;
                    }
                }
            }
            //System.out.println(rtIso+';'+tgIso);
            if (rtIso.equals(tgIso)) {
                mutMat[i][matColSize] = 0;
            }
            else {
                if (tgIso.equals("IGA")) {
                    mutMat[i][matColSize] = 11;
                }
                else if (tgIso.equals("IGD")) {
                    mutMat[i][matColSize] = 12;
                }
                else if (tgIso.equals("IGE")) {
                    mutMat[i][matColSize] = 13;
                }
                else if (tgIso.equals("IGG")) {
                    mutMat[i][matColSize] = 14;
                }
                else if (tgIso.equals("IGM")) {
                    mutMat[i][matColSize] = 15;
                }
            }
        }
        return mutMat;
    }

    private int[] rowSum(int[][] info) {
        int nRow = info.length;
        int nCol = info[0].length;
        int[] sum = new int[nRow];
        for (int i = 0;i < nRow; i++) {
            int t = 0;
            for (int j = 0;j < nCol;j++) {
                t = t+info[i][j];
            }
            sum[i] = t;
        }
        return(sum);
    }

    private int[] colSum(int[][] info) {
        int nRow = info.length;
        int nCol = info[0].length;
        int[] sum = new int[nCol];
        for (int i = 0;i < nCol; i++) {
            int t = 0;
            for (int j = 0;j < nRow;j++) {
                t = t+info[j][i];
            }
            sum[i] = t;
        }
        return(sum);
    }

    private HashMap<Integer,Integer> tabArray(int[] tmpCol) {
        //find occurrence of each type of mutation within each column
        HashMap<Integer,Integer> mutOcc = new HashMap<>();
        for (int i = 0; i < tmpCol.length;i++) {
            if (tmpCol[i] != 0) {
                if (mutOcc.containsKey(tmpCol[i])) {
                    mutOcc.put(tmpCol[i],mutOcc.get(tmpCol[i])+1);
                }
                else {
                    mutOcc.put(tmpCol[i],1);
                }
            }
        }
        return(mutOcc);
    }

    private HashMap<Integer,Integer> tabArray(ArrayList<Integer> tmpCol) {
        //find occurrence of each type of mutation within each column
        HashMap<Integer,Integer> mutOcc = new HashMap<>();
        for (int i = 0; i < tmpCol.size();i++) {
            if (tmpCol.get(i) != 0) {
                if (mutOcc.containsKey(tmpCol.get(i))) {
                    mutOcc.put(tmpCol.get(i),mutOcc.get(tmpCol.get(i))+1);
                }
                else {
                    mutOcc.put(tmpCol.get(i),1);
                }
            }
        }
        return(mutOcc);
    }

    public Table getOrgSeqs () {
        return (seqChars);
    }
}

