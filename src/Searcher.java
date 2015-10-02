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

        try {
            Analyzer analyzer = new StandardAnalyzer();
            Directory index = FSDirectory.open(Paths.get(indexDir));
            String querystr = "Safety of Implementation";

            Query q = new QueryParser("contents", analyzer).parse(querystr);
            int hitsPerPage = 10;
            IndexReader reader = DirectoryReader.open(index);
            IndexSearcher searcher = new IndexSearcher(reader);

            TopScoreDocCollector collector = TopScoreDocCollector.create(hitsPerPage);
            searcher.search(q, collector);
            ScoreDoc[] hits = collector.topDocs().scoreDocs;


            System.out.println("Found " + hits.length + " hits.");
            for(int i=0;i<hits.length;++i) {
                int docId = hits[i].doc;
                Document d = searcher.doc(docId);
                System.out.println((i + 1 + ". " + d.get("path")));
            }
        }
        catch (Exception e) {

        }

    }
}
