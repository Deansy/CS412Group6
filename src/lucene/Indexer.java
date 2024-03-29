package lucene;



import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexOptions;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;


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
                    Elements headerElements = parsedContent.select("h1");
                    headerElements.addAll(parsedContent.select("a.title"));

                    doc.add(new Field("title", parsedContent.title(), Field.Store.YES, Field.Index.ANALYZED));

                    Pattern p = Pattern.compile("\\[([^]]+)\\]");
                    Matcher m = p.matcher(parsedContent.title());

                    String chapter = " ";

                    while(m.find()) {
                        chapter = m.group(1);
                    }

                    if (!chapter.equals(" ")) {
                        doc.add(new Field("chapter", chapter, Field.Store.YES, Field.Index.ANALYZED));
                    }


                    if (!headerElements.isEmpty()) {
                        for (Element e : headerElements) {

                            // Don't want empty elements
                            if (!e.text().isEmpty()) {
                                // Removes the synopsis/description problem
//                                if (e.text().equals("Synopsis") || e.text().equals("Description") || e.text().equals("Class Summary")){
//
//                                } else {
                                    // Index each header into the document

                                    Field headerField = new Field("header", e.text(), Field.Store.YES, Field.Index.ANALYZED);
                                    headerField.setBoost(0.5f);

                                    doc.add(headerField);
//                                }
                            }
                        }


                    }
                    // Index the file contents into the document
                    //Without Term Vector
                    doc.add(new TextField("ncontents", parsedContent.text(), Field.Store.YES));
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
