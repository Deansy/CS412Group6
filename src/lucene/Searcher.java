package lucene;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by camsh on 02/10/2015.
 */
public class Searcher {

    public static void main(String [] args)
    {
        try {
            Searcher searcher = new Searcher("./index");
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

    Searcher(String indexDir) {
    }

    public static List<Document> search(String query, int maxHits) {

        String indexDir = "./index";

        try {
            Analyzer analyzer = new StandardAnalyzer();
            Directory index = FSDirectory.open(Paths.get(indexDir));


            Query q = new QueryParser("header", analyzer).parse(query);

            IndexReader reader = DirectoryReader.open(index);
            IndexSearcher searcher = new IndexSearcher(reader);

            TopScoreDocCollector collector = TopScoreDocCollector.create(maxHits);
            searcher.search(q, collector);
            ScoreDoc[] hits = collector.topDocs().scoreDocs;


            List<Document> results = new ArrayList<>();

            System.out.println("Found " + hits.length + " hits.");
            for(int i=0;i<hits.length;++i) {
                int docId = hits[i].doc;
                Document d = searcher.doc(docId);

                results.add(d);

            }

            return results;
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return new ArrayList<>();
    }
}
