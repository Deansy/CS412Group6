package lucene;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.tartarus.snowball.ext.PorterStemmer;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by camsh on 02/10/2015.
 */
public class Searcher {

    public static void main(String[] args) {
        try {
            Searcher searcher = new Searcher("./index");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    Searcher(String indexDir) {
    }

    public static List<Document> search(String query, int maxHits, String filter) {

        String indexDir = "./index";

        try {
            Analyzer analyzer = new StandardAnalyzer();
            Directory index = FSDirectory.open(Paths.get(indexDir));

            // Remove stop words
            String newQuery = stopAndStem(query);
            System.out.println("New Query: " + newQuery);

            //String newQuery = query;

            // Don't allow an empty search
            if (newQuery.length() >= 1) {

                Query q = null;

                try {

                    switch (filter) {

                        case "Contents":
                            q = new QueryParser("contents", analyzer).parse(newQuery);
                            break;
                        case "Headers":
                            q = new QueryParser("header", analyzer).parse(newQuery);
                            break;
                        case "Chapters":
                            q = new QueryParser("chapter", analyzer).parse(newQuery);
                            break;
                        default:
                            // Default to searching contents
                            q = new QueryParser("contents", analyzer).parse(newQuery);
                            break;
                    }


                    IndexReader reader = DirectoryReader.open(index);
                    IndexSearcher searcher = new IndexSearcher(reader);

                    TopScoreDocCollector collector = TopScoreDocCollector.create(maxHits);
                    searcher.search(q, collector);
                    ScoreDoc[] hits = collector.topDocs().scoreDocs;

                    List<Document> results = new ArrayList<>();

                    System.out.println("Found " + hits.length + " hits.");
                    for (int i = 0; i < hits.length; ++i) {
                        int docId = hits[i].doc;
                        Document d = searcher.doc(docId);

                        results.add(d);

                    }
                    return results;
                } catch (ParseException e) {
                    System.out.println("Don't have enough search parameters");

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new ArrayList<>();
    }

    private static String stopAndStem(String q) {
        Tokenizer tokenizer = new StandardTokenizer();
        StringBuilder sb = new StringBuilder();
        try {
            tokenizer.setReader(new StringReader(q));
            StandardFilter standardFilter = new StandardFilter(tokenizer);

            List customStopWords = Arrays.asList(new String[]{"a", "an", "are", "as", "at", "be", "but", "by", "for", "in", "into", "is", "it", "no", "not", "of", "on", "such", "that", "the", "their", "then", "there", "these", "they", "this", "to", "was", "will", "with"});
            CharArraySet stopSet = new CharArraySet(customStopWords, false);

            StopFilter stopFilter = new StopFilter(standardFilter, stopSet);
            CharTermAttribute charTermAttribute = tokenizer.addAttribute(CharTermAttribute.class);

            PorterStemmer stemmer = new PorterStemmer();

            stopFilter.reset();

            // Stem every non- stopped word
            while (stopFilter.incrementToken()) {
                String token = charTermAttribute.toString().toString();
                stemmer.setCurrent(token);
                stemmer.stem();
                String stemmed = stemmer.getCurrent();

                if (sb.length() > 0) {
                    sb.append(" ");
                }
                sb.append(stemmed);
            }

        } catch (IOException e) {
            System.err.println("Error removing stop words");
        }

        return sb.toString();
    }
}
