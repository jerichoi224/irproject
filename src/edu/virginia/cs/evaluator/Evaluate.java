package edu.virginia.cs.evaluator;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.DefaultSimilarity;

import edu.virginia.cs.index.ResultDoc;
import edu.virginia.cs.index.Searcher;
import edu.virginia.cs.index.similarities.*;

public class Evaluate {
	/**
	 * Format for judgements.txt is:
	 * 
	 * line 0: <query 1 text> line 1: <space-delimited list of relevant URLs>
	 * line 2: <query 2 text> line 3: <space-delimited list of relevant URLs>
	 * ...
	 * Please keep all these constants!
	 */

	Searcher _searcher = null;
	ArrayList<Double> map = new ArrayList<Double>();
	ArrayList<Double> patk = new ArrayList<Double>();
	ArrayList<Double> mrr = new ArrayList<Double>();
	ArrayList<Double> dcg = new ArrayList<Double>();


	public static void setSimilarity(Searcher searcher, String method) {
		if(method == null)
			return;
		else if(method.equals("--dp"))
			searcher.setSimilarity(new DirichletPrior());
		else if(method.equals("--jm"))
			searcher.setSimilarity(new JelinekMercer());
		else if(method.equals("--ok"))
			searcher.setSimilarity(new OkapiBM25());
		else if(method.equals("--pl"))
			searcher.setSimilarity(new PivotedLength());
		else if(method.equals("--tfidf"))
			searcher.setSimilarity(new TFIDFDotProduct());
		else if(method.equals("--bdp"))
			searcher.setSimilarity(new BooleanDotProduct());
		else
		{
			System.out.println("[Error]Unknown retrieval function specified!");
			printUsage();
			System.exit(1);
		}
	}

	public static void printUsage() {
		System.out.println("To specify a ranking function, make your last argument one of the following:");
		System.out.println("\t--dp\tDirichlet Prior");
		System.out.println("\t--jm\tJelinek-Mercer");
		System.out.println("\t--ok\tOkapi BM25");
		System.out.println("\t--pl\tPivoted Length Normalization");
		System.out.println("\t--tfidf\tTFIDF Dot Product");
		System.out.println("\t--bdp\tBoolean Dot Product");
	}

	//Please implement P@K, MRR and NDCG accordingly
	public void evaluate(String method, String indexPath, String judgeFile) throws IOException {
		_searcher = new Searcher(indexPath);		
		setSimilarity(_searcher, method);
		
		String line = null, judgement = null;
		int k = 10;
		double max = 0;
		double maxb = 0;
		double maxk = 0;


		BufferedReader br = new BufferedReader(new FileReader(judgeFile));
		double meanAvgPrec = 0.0, p_k = 0.0, mRR = 0.0, nDCG = 0.0;
		double numQueries = 0.0;
		String q = "";
		double maxDiff = 0;
		int length = 0;
		while ((line = br.readLine()) != null) {
			judgement = br.readLine();
//			setSimilarity(_searcher, "--dp");
//			 double pl = AvgPrec(line, judgement);
//			setSimilarity(_searcher, "--ok");
//			double ok = AvgPrec(line, judgement);
//			length += line.split(" ").length;
//			if(pl > ok){
//				if(pl - ok > maxDiff){
//					maxDiff = pl - ok;
//					q = line;
//				}
//				System.out.println(line);
//				System.out.println(pl - ok);
//				System.out.println();
//			}
			meanAvgPrec += AvgPrec(line, judgement);

			//compute corresponding AP
			//compute corresponding P@K
			p_k += Prec(line, judgement, k);
			//compute corresponding MRR
			mRR += RR(line, judgement);
			//compute corresponding NDCG
			nDCG += NDCG(line, judgement, k);
				++numQueries;
		}
//		System.out.println(length/numQueries);
//		System.out.println("Query: " + q);
//		System.out.println("Diff: " + maxDiff);
		br.close();

//		System.out.print(meanAvgPrec / numQueries+"\n");//this is the final MAP performance of your selected ranker
		System.out.print("\nMAP: " + meanAvgPrec / numQueries);//this is the final MAP performance of your selected ranker
		System.out.print("\nP@" + k + ": " + p_k / numQueries);//this is the final P@K performance of your selected ranker
		System.out.print("\nMRR: " + mRR / numQueries);//this is the final MRR performance of your selected ranker
		System.out.print("\nNDCG: " + nDCG / numQueries); //this is the final NDCG performance of your selected ranker
//		return meanAvgPrec;
	}

	double AvgPrec(String query, String docString) {
		ArrayList<ResultDoc> results = _searcher.search(query).getDocs();
//		System.out.println("Result Size:"+results.size());
		if (results.size() == 0)
			return 0; // no result returned

		HashSet<String> relDocs = new HashSet<String>(Arrays.asList(docString.split(" ")));
		int i = 1;
		double avgp = 0.0;
		double numRel = 0;
		for (ResultDoc rdoc : results) {
			if (relDocs.contains(rdoc.title())) {
				numRel ++;
				avgp += numRel/i;
//				System.out.print("  ");
			} else {
//				System.out.print("X ");
			}
//			System.out.println(i + ". " + rdoc.title());
			++i;
		}

		//compute average precision here
		if (numRel==0){
			avgp = 0;
		}else{
			avgp = avgp/relDocs.size();
		}
//		System.out.println("Average Precision: " + avgp);
		return avgp;
	}
	
	//precision at K
	double Prec(String query, String docString, int k) {
		double p_k = 0;
		ArrayList<ResultDoc> results = _searcher.search(query).getDocs();
		if (results.size() == 0)
			return 0; // no result returned

		HashSet<String> relDocs = new HashSet<String>(Arrays.asList(docString.split(" ")));
		double numRel = 0;
		for (int i = 0; i < results.size() && i < k; i++) {
			ResultDoc rdoc = results.get(i);
			if (relDocs.contains(rdoc.title())) {
				numRel++;
			}
		}
		p_k = numRel/k;
//		System.out.println("Precision@k: " + p_k);
		return p_k;
	}

	//Reciprocal Rank
	double RR(String query, String docString) {
		ArrayList<ResultDoc> results = _searcher.search(query).getDocs();
		if (results.size() == 0)
			return 0; // no result returned

		HashSet<String> relDocs = new HashSet<String>(Arrays.asList(docString.split(" ")));
		for (int i = 0; i < results.size(); i++) {
			ResultDoc rdoc = results.get(i);
			if (relDocs.contains(rdoc.title())) {
				return 1.0/(i+1);
			}
		}
		return 0;
	}
	
	//Normalized Discounted Cumulative Gain
	double NDCG(String query, String docString, int k) {
		double dcg = 0;
		double idcg = 0;
		ArrayList<ResultDoc> results = _searcher.search(query).getDocs();
		if (results.size() == 0)
			return 0; // no result returned

		HashSet<String> relDocs = new HashSet<String>(Arrays.asList(docString.split(" ")));
		double numRel = 0;
		for (int i = 0; i < results.size() && i < k; i++) {
			ResultDoc rdoc = results.get(i);
			if (relDocs.contains(rdoc.title())) {
				numRel++;
				dcg += 1.0/(Math.log(i+2)/Math.log(2));
//				System.out.print("  ");
			} else {
//				System.out.print("X ");
			}
//			System.out.println(i+1 + ". " + rdoc.title());
		}

		for (int i = 0; i < k && i< relDocs.size(); i++){
			idcg += 1.0/(Math.log(i+2)/Math.log(2));
		}
		if (idcg == 0){
			return 0;
		}
		return dcg/idcg;
	}
}