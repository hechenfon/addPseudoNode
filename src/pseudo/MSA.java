package pseudo;

/**
 * Created by hechenfon on 3/22/2017.
 * by calling bioJava to do Multiple Sequence Alignment
 */
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.biojava.nbio.alignment.Alignments;
import org.biojava.nbio.alignment.template.AlignedSequence;
import org.biojava.nbio.alignment.template.Profile;
import org.biojava.nbio.core.sequence.DNASequence;
import org.biojava.nbio.core.sequence.compound.AminoAcidCompound;
import org.biojava.nbio.core.sequence.io.FastaReaderHelper;
import org.biojava.nbio.core.util.ConcurrencyTools;

public class MSA {
    public List<AlignedSequence> MSA(List<DNASequence> seqs) {
        Profile algnRes = Alignments.getMultipleSequenceAlignment(seqs);
        List<AlignedSequence> algnSeq = algnRes.getAlignedSequences();
        return(algnSeq);
    }
}
