package pseudo;

import java.util.HashMap;

/**
 * Created by hechenfon on 3/30/2017.
 * store sequence unit
 *  - sequence
 *  - isotype
 */
public class seqUnit {
    int parID;  //parent ID
    int psID;  //pseudoNode ID
    int allID; //according to NID, add from the last NID
    String seq;
    String iso;
    HashMap<Integer,Integer> child; //Nids of all descendants for current seq
    boolean orExist; //True: the generated seq is within the original pool

    public seqUnit(String seq, String iso) {
        this.seq = seq;
        this.iso = iso;
    }
}
