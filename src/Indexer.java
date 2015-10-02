import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Paths;


public class Indexer {

    public static void main(String [] args)
    {
        try {
            Indexer indexer = new Indexer("./index");
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }
    public Indexer(String indexDir) throws IOException {

        Analyzer analyzer = new StandardAnalyzer();
        Directory index = FSDirectory.open(Paths.get(indexDir));

        IndexWriterConfig config = new IndexWriterConfig(analyzer);

        IndexWriter w = new IndexWriter(index, config);

        indexWebPageDirectory(w, new File("./DATA/"));
        w.close();

        System.out.println("Indexing Complete!");

    }


    // Recursively index a directory
    static void indexWebPageDirectory(IndexWriter w, File directory) {
        try {
            File[] files = directory.listFiles();

            for (File file : files) {
                if (file.isDirectory()) {
                    // Recursively index the sub-directory
                    indexWebPageDirectory(w, new File(file.getCanonicalPath()));
                }
                // Index the file
                else {

                    Document doc = new Document();
                    String path = file.getCanonicalPath();
                    //doc.add(new Field(FIELD_PATH, path, Field.Store.YES, Field.Index.UN_TOKENIZED));
                    doc.add(new Field("path", path, Field.Store.YES, Field.Index.ANALYZED));
                    Reader reader = new FileReader(file);
                    //document.add(new Field(FIELD_CONTENTS, reader));
                    doc.add(new Field("contents", reader));

                    w.addDocument(doc);
                }
            }
        }
        catch (Exception e ){
            e.printStackTrace();
        }


    }


}
