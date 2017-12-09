package runners;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import edu.virginia.cs.evaluator.Evaluate;
import edu.virginia.cs.index.Indexer;
import edu.virginia.cs.index.ResultDoc;
import edu.virginia.cs.index.SearchResult;
import edu.virginia.cs.index.Searcher;

public class Main {
    //please keep those constants
    final static String _dataset = "npl";
    final static String _indexPath = "lucene-npl-index";
    final static String _prefix = "data/";
    final static String _file = "npl.txt";
    final static String _judgment = "npl-judgements.txt";

    /*
    private static void interactiveSearch(String method) throws IOException {
        Searcher searcher = new Searcher(_indexPath);
        Evaluate.setSimilarity(searcher, method);
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        System.out.println("Type text to search, blank to quit.");
        System.out.print("> ");
        String input;
        while ((input = br.readLine()) != null && !input.equals("")) {
            SearchResult result = searcher.search(input);
            ArrayList<ResultDoc> results = result.getDocs();
            int rank = 1;
            if (results.size() == 0)
                System.out.println("No results found!");
            for (ResultDoc rdoc : results) {
                System.out.println("\n------------------------------------------------------");
                System.out.println(rank + ". " + rdoc.title());
                System.out.println("------------------------------------------------------");
                System.out.println(result.getSnippet(rdoc)
                        .replaceAll("\n", " "));
                ++rank;
            }
            System.out.print("> ");
        }
    }
*/
    ////This makes it easier for you to run the evaluation
    static BufferedReader in;

    public static void main(String[] args) throws IOException {
        ArrayList<String> collection_index = new ArrayList<String>();
        Map<String, ArrayList<Integer>> collection = new HashMap<String, ArrayList<Integer>>();
        String[] parsed;
        int index = 0;
        Scanner keyboard = new Scanner(System.in);
        System.out.print("Enter your Sentence: ");
        String context = keyboard.nextLine();
        System.out.print("Enter your search term: (enter \"q\" to quit): ");
        String query = keyboard.nextLine();
        URL url = new URL("http://www.dictionary.com/browse/" + query.toLowerCase());
        in = new BufferedReader(new InputStreamReader(url.openStream()));

        String inputLine, nextLine;
        nextLine = in.readLine();

        // Build Collection
        while (true) {
            inputLine = nextLine;
            if ((nextLine = in.readLine()) == null)
                break;
            if (inputLine.contains("def-content") && nextLine.contains("example")) {
                parsed = removeTags(nextLine).split(":");

                if (parsed.length == 2) {
                    collection_index.add(parsed[0]);

                    // Parse Example Sentence into words
                    for (String a : parsed[1].replace(".", "").toLowerCase().split(" ")) {
                        if (collection.containsKey(a)) {
                            ArrayList<Integer> docs = collection.get(a);
                            if (!docs.contains(index)) {
                                docs.add(index);
                                collection.put(a, docs);
                            }
                        } else {
                            ArrayList<Integer> docs = new ArrayList<Integer>();
                            docs.add(index);
                            collection.put(a, docs);
                        }
                    }
                    index++;
                }
            }
        }

        // Rank the documents
        int best_index = -1, best_count = 0;
        Map<Integer, Integer> ranking = new HashMap<Integer, Integer>();
        for (String word : context.replace(".", "").toLowerCase().split(" ")) {
            if (collection.get(word) == null)
                continue;
            for (int a : collection.get(word)) {
                if (ranking.get(a) == null) {
                    ranking.put(a, 1);
                    if (best_count == 0) {
                        best_count = 1;
                        best_index = a;
                    }
                } else {
                    ranking.put(a, ranking.get(a) + 1);
                    if (best_count < ranking.get(a)) {
                        best_count = ranking.get(a);
                        best_index = a;
                    }
                }
            }
        }

        System.out.println(collection_index.get(best_index));
    }

    static String removeTags(String htmlString) {
        return htmlString.replaceAll("\\<.*?>", "");

    }
    //To create the index
    //NOTE: you need to create the index once, and you cannot call this function twice without removing the existing index files
    //Indexer.index(_prefix + _indexPath, _prefix, _file);

    //Interactive searching function with your selected ranker
    //NOTE: you have to create the index before searching!
    //new Evaluate().evaluate("--ok", _prefix + _indexPath, _prefix + _judgment);;


}
