package pseudo;

import java.util.HashMap;

/**
 * Created by hechenfon on 3/28/2017.
 * mutation object: stores all mutations
 * contains 3 HASHs, original - position - target
 */
public class mutObj {
    HashMap<Integer, String> org = new HashMap<Integer,String>();  //original, sub-seq
    HashMap<Integer, int[]> pos = new HashMap<Integer,int[]>();//mutation position, ['start pos','length']
    HashMap<Integer, String> tgt = new HashMap<Integer,String>();  //target, mutate to

    public mutObj() {

    }
}
