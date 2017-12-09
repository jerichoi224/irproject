package edu.virginia.cs.index.similarities;

import org.apache.lucene.search.similarities.BasicStats;
import org.apache.lucene.search.similarities.LMSimilarity;

public class DirichletPrior extends LMSimilarity {

    private LMSimilarity.DefaultCollectionModel model; // this would be your reference model
    private float queryLength = 0; // will be set at query time automatically
    private double m;

    public DirichletPrior() {
        model = new LMSimilarity.DefaultCollectionModel();
        m = 2500;
    }

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
        m = 71;
        double p_s =  ((termFreq + m * model.computeProbability(stats))/(docLength + m));
        double a_d = (m)/(m + docLength);
        return (float)(Math.log(p_s/(a_d*model.computeProbability(stats))));
    }

    @Override
    public String getName() {
        return "Dirichlet Prior";
    }
    public double getM(){
        return m;
    }
    public void setM(double M){
        m = M;
    }
    @Override
    public String toString() {
        return getName();
    }

    public void setQueryLength(float length) {
        queryLength = length;
    }
    public float getQueryLength(){return queryLength;}
}
