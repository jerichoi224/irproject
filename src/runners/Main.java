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

        Indexer.index(_prefix + _indexPath, _prefix, _file);
        int best_match = new Evaluate().search("--jm", _prefix + _indexPath, context);
        System.out.println(collection.get(best_match));
    }

    static String removeTags(String htmlString) {
        return htmlString.replaceAll("\\<.*?>", "");
    }
}
