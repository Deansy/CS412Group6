package lucene;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
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

                    // Index the path of the file to the document
                    doc.add(new Field("path", path, Field.Store.YES, Field.Index.ANALYZED));


                    String rawString = fileToString(file);

                    // Parse the HTML
                    org.jsoup.nodes.Document parsedContent = Jsoup.parse(rawString);

                    // Get all elements that are of type a and have class "title"
                    // This covers 99% of the headers
                    Elements headerElements = parsedContent.select("a.title");

//                    Elements headerElements = new Elements();
//
//
//                    if (parsedContent.select("h1").isEmpty()) {
//                          if (parsedContent.select("h2").isEmpty()) {
//                              headerElements = parsedContent.select("a.title");
//                          }
//                        else {
//                              headerElements.addAll(parsedContent.select("h2"));
//                          }
//                    }
//                    else {
//                        headerElements.addAll(parsedContent.select("h1"));
//                    }






                    // TODO: Determine the chapter and index it

                    if (!headerElements.isEmpty()) {
                        for (Element e : headerElements) {

                            // Don't want empty elements
                            if (!e.text().isEmpty()) {
                                // Removes the synopsis/description problem
                                if (e.text().equals("Synopsis") || e.text().equals("Description")) {

                                }
                                else {
                                    // Index each header into the document
                                    doc.add(new Field("header", e.text(), Field.Store.YES, Field.Index.ANALYZED));
                                }
                            }
                        }


                    }
                    // Index the file contents into the document
                    doc.add(new Field("contents", parsedContent.text(), Field.Store.YES, Field.Index.ANALYZED));

                    w.addDocument(doc);
                }
            }
        }
        catch (Exception e ){
            e.printStackTrace();
        }
    }


    // Take a file and return the content in a string
    static String fileToString(File f) {
        try {
            FileReader fileReader = new FileReader(f);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            StringBuffer stringBuffer = new StringBuffer();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuffer.append(line);
                stringBuffer.append("\n");
            }
            fileReader.close();

            return stringBuffer.toString();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


}
