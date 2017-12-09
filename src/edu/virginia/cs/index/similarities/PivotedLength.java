package edu.virginia.cs.index.similarities;

import org.apache.lucene.search.similarities.BasicStats;
import org.apache.lucene.search.similarities.SimilarityBase;

public class PivotedLength extends SimilarityBase {
    /**
     * Returns a score for a single term in the document.
     *
     * @param stats
     *            Provides access to corpus-level statistics
     * @param termFreq
     * @param docLength
     */
    @Override
    protected float score(BasicStats stats, float termFreq, float docLength) {
        double s = 0.75;
        double N = stats.getNumberOfDocuments();
        double avgDocLength = stats.getAvgFieldLength();
        return (float)((1+(Math.log(1+(Math.log(termFreq)))))/(1-s+(s*docLength/avgDocLength))*
                (Math.log((N+1)/stats.getDocFreq())));
    }

    @Override
    public String toString() {
        return "Pivoted Length Normalization";
    }

}
