package edu.virginia.cs.index.similarities;

import org.apache.lucene.search.similarities.BasicStats;
import org.apache.lucene.search.similarities.SimilarityBase;

public class OkapiBM25 extends SimilarityBase {
    double k1;
    double k2;
    double b;
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

        k1 = 1.2;
        k2 = 750;
        b = 0.75;
        double N = stats.getNumberOfDocuments();
        double df = stats.getDocFreq();
        double n = stats.getAvgFieldLength();
        double t1 = (Math.log((N-df+0.5)/(df+0.5)));
        double t2 = ((k1+1)*termFreq)/(k1*(1-b+b*(docLength/n))+termFreq);
        double t3 = ((k2+1)*1)/(k2+1);
        return (float)(t1 * t2 * t3);

//        b = 0.75;
//        double N = stats.getNumberOfDocuments();
//        double df = stats.getDocFreq();
//        double avdl = stats.getAvgFieldLength();
//        double lamda = (double)(1/(b+(b/avdl)+1));
//        double weight = (Math.log((N-df+0.5)/(df+0.5))) * lamda;
//        double term1 = termFreq/((b/avdl)*docLength + b + termFreq);
//        return (float)(term1 * weight);
    }

    @Override
    public String toString() {
        return "Okapi BM25";
    }
    public void setK1(double k){
        k1 = k;
    }
    public void setB(double b){this.b = b;}
    public double getB(){return b;}
    public double getK1(){return k1;}
}
