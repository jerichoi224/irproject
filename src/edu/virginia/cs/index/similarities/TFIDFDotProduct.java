package edu.virginia.cs.index.similarities;

import org.apache.lucene.search.similarities.BasicStats;
import org.apache.lucene.search.similarities.SimilarityBase;

public class TFIDFDotProduct extends SimilarityBase {
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
        double N = stats.getNumberOfDocuments();
        return (float)((1 + Math.log(termFreq))*Math.log((N + 1)/stats.getDocFreq()));
    }

    @Override
    public String toString() {
        return "TF-IDF Dot Product";
    }
}
