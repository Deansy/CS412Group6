package view;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Callback;
import lucene.Searcher;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexableField;

import java.io.File;
import java.util.List;


/**
 * Created by Simon on 02/03/2015.
 */

public class MainApplication extends Application {

    private ObservableList<String> filters = FXCollections.observableArrayList("Contents",
            "Headers", "Chapter");
    private String filter = "";
    private ObservableList<Document> results = FXCollections.observableArrayList();
    private ListView resultsPanel = new ListView();
    private TreeView<String> treeView;
    private ResultPage resultPage;

    private List<Document> searchResults;

    TextField pageId;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Java Docs - CS412 Group 6");


        TextField searchBar = new TextField();
        searchBar.setPromptText("Search");

        ComboBox filterBar = new ComboBox(filters);
        filterBar.setMinWidth(250);
        filterBar.setMinHeight(30);
        filterBar.valueProperty().setValue("Contents");
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
        /* MISSING CODE */
        treeView = new TreeView<>();
        browsePane.getChildren().add(treeView);
        VBox.setVgrow(treeView, Priority.ALWAYS);


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
                                resultPage.loadPage(new File(d.get("path")), pageId);

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
                                    else {
                                        leadLbl.setText(item.get("header"));
                                        setText(item.get("header"));
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
                        resultPage.loadPage(new File((String) newVal), pageId);

                        searchResults = Searcher.search((String) newVal, 50, filter);


                        results.clear();

                        // Fill results with test data
                        for (int i = 1; i < searchResults.size(); i++) {
                            try {
                                IndexableField headerField = searchResults.get(i).getField("header");
                                if (headerField == null) {
                                    // Don't add if the header is null
                                    // This stops empty entrys in the list
                                } else {
                                    results.add(searchResults.get(i));
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

//    **    THESE COULD BE HELPFUL      **
//    public void handleSearchByKey(String oldVal, String newVal) {
//        // If the number of characters in the text box is less than last time
//        // it must be because the user pressed delete
//        if ( oldVal != null && (newVal.length() < oldVal.length()) ) {
//            // Restore the lists original set of entries
//            // and start from the beginning
//            resultsPanel.setItems( results );
//        }
//
//        // Change to upper case so that case is not an issue
//        newVal = newVal.toUpperCase();
//
//        // Filter out the entries that don't contain the entered text
//        ObservableList<String> subentries = FXCollections.observableArrayList();
//        for ( Object entry: resultsPanel.getItems() ) {
//            String entryText = (String)entry;
//            if ( entryText.toUpperCase().contains(newVal) ) {
//                subentries.add(entryText);
//            }
//        }
//        resultsPanel.setItems(subentries);
//    }
//
//    public void handleSearchByKey2(String oldVal, String newVal) {
//
//
//        // Break out all of the parts of the search text
//        // by splitting on white space
//        String[] parts = newVal.toUpperCase().split(" ");
//
//        // Filter out the entries that don't contain the entered text
//        ObservableList<String> subentries = FXCollections.observableArrayList();
//        for ( Object entry: resultsPanel.getItems() ) {
//            boolean match = true;
//            String entryText = (String)entry;
//            for ( String part: parts ) {
//                // The entry needs to contain all portions of the
//                // search string *but* in any order
//                if ( ! entryText.toUpperCase().contains(part) ) {
//                    match = false;
//                    break;
//                }
//            }
//
//            if ( match ) {
//                subentries.add(entryText);
//            }
//        }
//        resultsPanel.setItems(subentries);
//    }
}