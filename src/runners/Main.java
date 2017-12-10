package runners;

import java.io.*;
import java.lang.reflect.Array;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import edu.virginia.cs.evaluator.Evaluate;
import edu.virginia.cs.index.Indexer;
import edu.virginia.cs.index.ResultDoc;
import edu.virginia.cs.index.SearchResult;
import edu.virginia.cs.index.Searcher;
import opennlp.tools.postag.*;

public class Main {	
    //please keep those constants
    final static String _indexPath1 = "def-index1";
    final static String _indexPath2 = "def-index2";
    final static String _indexPath3 = "def-index3";    
    final static String _prefix = "data/";
    final static String _file = "rel_words.txt";

    static BufferedReader in;

    public static void main(String[] args) throws IOException {
    	
        //deleteDir(new File(_prefix + _indexPath1));
        //deleteDir(new File(_prefix + _file));

        ArrayList<String> definitions = new ArrayList<String>();
        ArrayList<String> rel_words = new ArrayList<String>();

        String[] parsed;
        int index = 0, def_num = 0, def_index;
        Scanner keyboard = new Scanner(System.in);
        System.out.print("Enter your Sentence: ");
        String context = keyboard.nextLine().toLowerCase().replace("[^a-z ]", "");
        System.out.print("Enter your search term: ");
        String query = keyboard.nextLine().toLowerCase().replace("[^a-z ]", "");
        String pos = getQueryPOS(context, query, keyboard);        
        
        URL url = new URL("http://www.dictionary.com/browse/" + query.toLowerCase());
        in = new BufferedReader(new InputStreamReader(url.openStream()));

        String inputLine, nextLine, data, def;
        nextLine = in.readLine();

        // Build Basic Collection from Dictionary.com
        while (true) {
            inputLine = nextLine;
            if ((nextLine = in.readLine()) == null)
                break;
            if (inputLine.contains("def-number"))
                def_num = Integer.parseInt(removeTags(inputLine).replace(".", "").trim());
            if (inputLine.contains("def-content") && def_num <= 6) {
                if (nextLine.contains("dbox-ex")) {
                    parsed = removeTags(nextLine).toLowerCase().split(":");
                    def = parsed[0];
                    data = removeTags(nextLine).replace("  ", "");
                } else {
                    def = removeTags(nextLine).toLowerCase();
                    data = def;
                }
                definitions.add(def);
                data = data.trim().replace("[^a-z ]", "");
                rel_words.add(data.trim() + " ");
                // Parse Example Sentence into words
            }
        }
        in.close();
        // initial File
        updateFile(_prefix + _file, rel_words);

        try {
            // Index for Thesaurus
            Indexer.index(_prefix + _indexPath1, _prefix, _file);

            // Parse Thesaurus.com and Match Definition
            url = new URL("http://www.thesaurus.com/browse/" + query.toLowerCase());
            in = new BufferedReader(new InputStreamReader(url.openStream()));
            String check1 = "";
            def_index = 0;
            nextLine = in.readLine();
            while (true) {
                if ((nextLine = in.readLine()) == null)
                    break;
                if (nextLine.contains("class=\"ttl\""))
                    if (!removeTags(nextLine).trim().equals(check1)) {
                        check1 = removeTags(nextLine).trim();
                        continue;
                    } else {
                        if (check1.replaceAll("\\s", "").equals(""))
                            continue;
                        check1 = check1.replaceAll(",", "");
                        ArrayList<ResultDoc> best_match = new Evaluate().search("--jm", _prefix + _indexPath1, check1);
                        if (best_match.size() == 0)
                            continue;
                        def_index = best_match.get(0).id();
                        while (true) {
                            nextLine = in.readLine();
                            if (nextLine.contains("Antonyms"))
                                break;
                            String temp = removeTags(nextLine).trim().toLowerCase();
                            if (!temp.equals(""))
                                rel_words.set(def_index, rel_words.get(def_index) + " " + temp.substring(0, temp.lastIndexOf("star")));
                        }

                    }
            }

            // Update Text file
            updateFile(_prefix + _file, rel_words);
        } catch (Exception e){}

        //deleteDir(new File(_prefix + _indexPath1)); // Delete Previous Index

        // Index for Oxford
        Indexer.index(_prefix + _indexPath2, _prefix, _file);

        // Parse Oxford Dictionary and Match Definition
        url = new URL("https://en.oxforddictionaries.com/definition/" + query.toLowerCase());
        in = new BufferedReader(new InputStreamReader(url.openStream()));
        nextLine = in.readLine();
        String ox_def, ox_data;
        int curr_ind = 0, next_ind = 0;
        while (true) {
            if ((nextLine = in.readLine()) == null)
                break;
            if (nextLine.contains("class=\"ind\"")) {
                next_ind = nextLine.indexOf("class=\"ind\"");
                while (nextLine.substring(next_ind + 1, nextLine.length()).contains("class=\"ind\"")) {
                    curr_ind = next_ind;
                    next_ind = nextLine.indexOf("class=\"ind\"", next_ind + 1);// +12, -6

                    String curr_data = removeTags(nextLine.substring(curr_ind + 12, next_ind - 6));
                    curr_data = curr_data.toLowerCase().replaceAll("[^a-z ]", "");
                    ArrayList<ResultDoc> best_match = new Evaluate().search("--jm", _prefix + _indexPath2, curr_data);
                    if (best_match.size() == 0)
                        continue;
                    def_index = best_match.get(0).id();
                    rel_words.set(def_index, rel_words.get(def_index) + " " + curr_data);
                }
            }
        }

        // Update Text file
        updateFile(_prefix + _file, rel_words);
        //deleteDir(new File(_prefix + _indexPath2)); // Delete Previous Index

        Indexer.index(_prefix + _indexPath3, _prefix, _file);
        ArrayList<ResultDoc> best_match = new Evaluate().search("--jm", _prefix + _indexPath3, context.toLowerCase().replaceAll("[^a-z ]", ""));
        for (int i = 0; i < Math.min(10, best_match.size()); i++) {
            System.out.println((i + 1) + ". " + definitions.get(best_match.get(i).id()).trim());
        }
    }
    
    public static String getQueryPOS(String context, String query, Scanner keyboard) throws IOException {
    	String pos = "";
    	try(InputStream modelIn = new FileInputStream("lib/en-pos-maxent.bin")) {
    		POSModel model = new POSModel(modelIn);
    		POSTaggerME tagger = new POSTaggerME(model);
    		String split_context[] = context.split(" ");
    		String tags[] = tagger.tag(split_context);
    		double probs[] = tagger.probs();
    		ArrayList<Integer> query_indices = new ArrayList<Integer>();
    		for(int i = 0; i < split_context.length; i++) {
    			if(split_context[i].equals(query)) {
    				query_indices.add(i);
    			}
    		}
    		if(query_indices.size() == 0) {
    			System.out.println("The search term was not found in the sentence.");
    			System.exit(0);
    		} else if(query_indices.size() == 1) {
    			if(probs[query_indices.get(0)] > 0.75) {
    				pos = tags[query_indices.get(0)];
    			}
    		} else {
    			System.out.println("Search term occurred multiple times in context, please specify which: ");
    			int count = 1;
    			for(int i = 0; i < split_context.length; i++) {
    				if(split_context[i].equals(query)) {
    					System.out.print(count + ".");
    					count++;
    				}
    				System.out.print(split_context[i] + " ");
    			}
    			System.out.println();
    			int queryIndex = Integer.parseInt(keyboard.nextLine()) - 1;
    			if(probs[query_indices.get(queryIndex)] > 0.75) {
    				pos = tags[query_indices.get(queryIndex)];
    			}
    		}
     	} 
    	return pos;
    }

    public static void updateFile(String filepath, ArrayList<String> rel_words) throws IOException {
        PrintWriter writer = new PrintWriter(filepath, "UTF-8");
        for (String i : rel_words)
            writer.println(i);
        writer.close();
    }

    static String removeTags(String htmlString) {
        return htmlString.replaceAll("\\<.*?>", "");
    }

    public static boolean deleteDir(File dir) throws IOException {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        return Files.deleteIfExists(dir.toPath()); // The directory is empty now and can be deleted.
    }
}

