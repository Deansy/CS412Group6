package view;

import com.sun.org.apache.xpath.internal.SourceTree;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;
import lucene.Indexer;
import lucene.Searcher;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * Created by Simon on 02/03/2015.
 */

public class MainApplication extends Application {

    private ObservableList<String> filters = FXCollections.observableArrayList("Contents",
            "Headers", "Chapter", "All");
    private String filter = filters.get(0);
    private ObservableList<Document> results = FXCollections.observableArrayList();
    private ListView resultsPanel = new ListView();
    private ResultPage resultPage;
    private TextField searchBar;
    private List<Document> searchResults;

    private TreeView<File> treeView;
    private TreeItem<File> root;

    private HashMap<String, String> pathHeaders;


    TextField pageId;

    public static void main(String[] args) {
        String dataDir = "./DATA";
        String indexDir = "./index";

        File data = new File(dataDir);
        File index = new File(indexDir);

        if (data.exists()) {
            if (!index.exists()) {
                Indexer.main(args);
            }

            launch(args);
        } else {
            System.out.println("Need a DATA folder to index...");
        }
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        primaryStage.setTitle("Java Docs - CS412 Group 6");


        searchBar = new TextField();
        searchBar.setPromptText("Search");

        ComboBox filterBar = new ComboBox(filters);
        filterBar.setMinWidth(250);
        filterBar.setMinHeight(30);
        filterBar.valueProperty().setValue(filters.get(0));
        filterBar.valueProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue ov, String t, String t1) {
                filter = t1;
            }
        });

        // Listen to the search bar
        setChangeListener(searchBar);
        setCellFactory();
        setSelectedItemListener();


        // Set up results panel
        resultsPanel.setMinHeight(620);

        resultsPanel.setItems( results );


        pageId = new TextField();
        pageId.setMinWidth(1195);
        pageId.setEditable(false);


        // :MAIN: Main Windows
        BorderPane mainLayout = new BorderPane();
        mainLayout.setPadding(new Insets(5, 10, 10, 10));

        // :LEFT: Search | Browse
        TabPane tabPane = new TabPane();
        Tab searchTab = new Tab();
        searchTab.setText("Search");
        searchTab.setClosable(false);

        Tab browseTab = new Tab();
        browseTab.setText("Browse");
        browseTab.setClosable(false);
        tabPane.getTabs().addAll(searchTab, browseTab);
        // Browser
        VBox browsePane = new VBox();



        // Set up browser to show java doc contents
        root = createNode(new File(System.getProperty("user.dir") + "/DATA/java/"));
        treeView = new TreeView<File>(root);
        browsePane.getChildren().add(treeView);

        initializeBrowser();


        // Search and Results
        VBox searchPane = new VBox();
        searchPane.setPadding(new Insets(5, 0, 0, 0));
        searchPane.setSpacing(2);
        searchPane.getChildren().addAll(filterBar, searchBar, resultsPanel);
        searchTab.setContent(searchPane);
        browseTab.setContent(browsePane);

        // :RIGHT: Result Page
        Pane selectedResult = new Pane();
        resultPage = new ResultPage();
        selectedResult.getChildren().add(resultPage);
        selectedResult.setPadding(new Insets(10, 10, 10, 10));

        // :TOP: Navigation Bar
        HBox navBar = new HBox();
        navBar.setPadding(new Insets(0, 0, 10, 0));
        navBar.setSpacing(10);

        // EVENT HANDLING...
        Button backBtn = new Button("\u21e6");
        backBtn.setOnAction(e -> resultPage.goBack());
        Button forwardBtn = new Button("\u21e8");
        forwardBtn.setOnAction(e -> resultPage.goForward());


        navBar.getChildren().addAll(backBtn, forwardBtn, pageId);

        mainLayout.setTop(navBar);
        mainLayout.setLeft(tabPane);
        mainLayout.setCenter(selectedResult);

        Scene scene = new Scene(mainLayout, 1280, 768);


        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                onClose(new File("./DATA/"));
            }
        });

    }


    private void initializeBrowser() {
        treeView.setCellFactory(tv ->  {
            final Tooltip tooltip = new Tooltip();
            TreeCell<File> cell = new TreeCell<File>() {
                @Override
                public void updateItem(File item, boolean empty) {

                    super.updateItem(item, empty);
                    if (empty) {
                        setText(null);
                        setTooltip(null);
                    } else if (getTreeItem() == root) {
                        setText("Java Reference Library");
                        setTooltip(null);
                    } else {
                        String header = getHeaderForPath(item.getAbsolutePath().toString());
                        if (header != null) {
                            setText(header);
                        } else {
                            String name = item.getName();

                            switch(name) {
                                case "awt" :
                                    setText("Java AWT Reference");
                                    break;
                                case "exp" :
                                    setText("Exploring Java");
                                    break;
                                case "fclass" :
                                    setText("Java Fundamental Classes Reference");
                                    break;
                                case "index" :
                                    setText("Combined Index");
                                    break;
                                case "javanut" :
                                    setText("Java in a Nutshell");
                                    break;
                                case "langref" :
                                    setText("Java Language Reference");
                                    break;
                                case "gifs" :
                                    setText("Images");
                                    break;
                                default :
                                    setText(item.getName());
                                    break;
                            }
                        }
                        tooltip.setText(item.getAbsolutePath().toString());

                        setTooltip(tooltip);
                    }
                }
            };
            cell.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2 && ! cell.isEmpty()) {
                    File file = cell.getItem();
                    // Stops an exception when changing search types
                    if (file != null) {

                            resultPage.loadPage(file, pageId, searchBar.getCharacters().toString());

                            pageId.setText(file.getAbsolutePath());

                    }
                    else {
                        // Do nothing
                        System.out.println("XX");
                    }
                }
            });
            return cell ;
        });
    }

    private String getHeaderForPath(String path) {

        // Stuff for trying to do TreeView using the indexed files
        String indexDir = "./index";

        try {
            Analyzer analyzer = new StandardAnalyzer();
            Directory index = FSDirectory.open(Paths.get(indexDir));
            IndexReader reader = DirectoryReader.open(index);
            IndexSearcher searcher = new IndexSearcher(reader);
            TopScoreDocCollector collector = TopScoreDocCollector.create(1);
            QueryParser qp = new QueryParser("path", analyzer);
            path = qp.escape(path);

            Query q = qp.parse(path);

            searcher.search(q, collector);

            ScoreDoc[] hits = collector.topDocs().scoreDocs;

            for (int i = 0; i < hits.length; i++) {
                Document d = searcher.doc(hits[i].doc);
                String header = d.get("header");
                if (header != null && !header.equals(""))
                    return d.get("header");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void setSelectedItemListener() {
        // Clicked on a new item in search results
        resultsPanel.getSelectionModel().selectedItemProperty()
                .addListener(new ChangeListener() {
                    @Override
                    public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                        // Stops an exception when changing search types
                        if (newValue != null) {
                            try {
                                Document d = (Document) newValue;

                                resultPage.loadPage(new File(d.get("path")), pageId, searchBar.getCharacters().toString());

                                pageId.setText(d.get("path"));
                            } catch (NullPointerException e) {
                                e.printStackTrace();
                            }
                        }
                        else {
                            // Do nothing
                            System.out.println("XX");
                        }

                    }
                });
    }

    private void setCellFactory() {
        // Set the style of the cells
        resultsPanel
                .setCellFactory(new Callback<ListView<Document>, ListCell<Document>>() {

                    public ListCell<Document> call(ListView<Document> param) {
                        final Label leadLbl = new Label();
                        final Tooltip tooltip = new Tooltip();
                        final ListCell<Document> cell = new ListCell<Document>() {
                            @Override
                            public void updateItem(Document item, boolean empty) {
                                super.updateItem(item, empty);
                                if (item != null) {
                                    if (filter.equals("Chapter")) {
                                        leadLbl.setText("[" + item.get("chapter") + "]" + item.get("header"));
                                        setText("(" + item.get("chapter") + ")" + item.get("header"));
                                    }
                                    else if (filter.equals("Headers")) {
                                        leadLbl.setText(item.get("header"));
                                        setText(item.get("header"));
                                    }
                                    else {
                                        leadLbl.setText(item.get("title"));
                                        setText(item.get("title"));
                                    }
                                    tooltip.setText(item.get("path"));
                                    setTooltip(tooltip);
                                }
                            }
                        }; // ListCell
                        return cell;
                    }
                }); // setCellFactory
    }

    private void setChangeListener(TextField searchBar) {
        searchBar.textProperty().addListener(
                new ChangeListener() {
                    public void changed(ObservableValue observable, Object oldVal, Object newVal) {

//                        if ( oldVal != null && (((String) newVal).length() < ((String) oldVal).length()) ) {
//                            System.out.println("Deleting - Just do previous search");
//                        } else {

//                            System.out.println("Searching for: " + (String)newVal);

                          // What's the purpose of this line??
//                        resultPage.loadPage(new File((String) newVal), pageId);

                        // Call the search
                        searchResults = Searcher.search((String) newVal, 1000, filter);

                        // Clear old results
                        results.clear();

                        // Hold a list of paths currently in the results
                        // This is to stop duplicates
                        List<String> paths = new ArrayList<String>();

                        // Fill results
                        for (int i = 1; i < searchResults.size(); i++) {
                            try {

                                IndexableField pathField = searchResults.get(i).getField("path");
                                IndexableField headerField = searchResults.get(i).getField("header");

//                                System.out.println(pathField.stringValue());



                                // Don't add duplicates
                                if (!paths.contains(pathField.stringValue())) {
                                    paths.add(pathField.stringValue());

                                    if (headerField == null) {
                                        // Don't add if the header is null
                                        // This stops empty entrys in the list
                                    } else {
                                        results.add(searchResults.get(i));
                                    }
                                }
                            }
                            catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        resultsPanel.setItems(results);

                    }
//                    }
                });
    }

    private void onClose(File directory) {

            File[] files = directory.listFiles();

            for (File file : files) {
                if (file.isDirectory()) {
                    // Recursively index the sub-directory
                    try {
                        onClose(new File(file.getCanonicalPath()));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                // Index the file
                else {
                    if (file.getName().startsWith("tmp")) {
                        System.out.println("Deleted - " + file.getName());
                        file.delete();

                    }
                }
            }
    }

    //Oracle treeView example code
    private TreeItem<File> createNode(final File f) {
        return new TreeItem<File>(f) {
            protected boolean isLeaf;
            private boolean isFirstTimeChildren = true;
            private boolean isFirstTimeLeaf = true;

            @Override
            public ObservableList<TreeItem<File>> getChildren() {
                if (isFirstTimeChildren) {
                    isFirstTimeChildren = false;
                    super.getChildren().setAll(buildChildren(this));
                }
                return super.getChildren();
            }

            @Override
            public boolean isLeaf() {
                if (isFirstTimeLeaf) {
                    isFirstTimeLeaf = false;
                    File f = (File) getValue();
                    isLeaf = f.isFile();
                }
                return isLeaf;
            }

            private ObservableList<TreeItem<File>> buildChildren(
                    TreeItem<File> TreeItem) {
                File f = TreeItem.getValue();
                if (f == null) {
                    return FXCollections.emptyObservableList();
                }
                if (f.isFile()) {
                    return FXCollections.emptyObservableList();
                }
                File[] files = f.listFiles();
                if (files != null) {
                    ObservableList<TreeItem<File>> children = FXCollections
                            .observableArrayList();
                    for (File childFile : files) {
                        children.add(createNode(childFile));
                    }
                    return children;
                }
                return FXCollections.emptyObservableList();
            }
        };
    }

}