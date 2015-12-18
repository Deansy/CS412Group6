package view;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.control.TextField;
import javafx.scene.layout.Region;
import javafx.scene.web.WebView;
import lucene.Searcher;

import java.io.*;
import java.net.MalformedURLException;

class ResultPage extends Region {

    private final WebView browser = new WebView();
    TextField pageID;

    public ResultPage() {
        // Default Page
        File f = new File("DATA/java/index.htm");

        try {
            browser.getEngine().load(f.toURI().toURL().toString());


            // On Page Change
            browser.getEngine().getLoadWorker().stateProperty().addListener(
                    new ChangeListener<Worker.State>() {
                        @Override
                        public void changed(ObservableValue<? extends Worker.State> observable, Worker.State oldValue, Worker.State newValue) {
                            try {
                                if (pageID != null) {
                                    pageID.setText(browser.getEngine().getTitle());
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
            );

        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        getChildren().add(browser);
    }

    public void loadPage(File f, TextField pageID, String query) {
        try {

            String nq;
            if (query.startsWith("\"") && query.endsWith("\"")) {
                // Don't stop and stem a quote
                nq = query;
            }
            else {
                nq = Searcher.stopAndStem(query);
            }

            String[] arr = nq.split(" ");
            nq = "";

            // Recreate the query string without uppercase words
            // To stop booleans being highlighted
            for (String ss : arr) {
                char start = ss.charAt(0);
                char end = ss.charAt(ss.length()-1);
                if(!Character.isUpperCase(start) && !Character.isUpperCase(end)) {
                    if (!nq.equals("")) {
                        nq += " ";
                    }
                    nq += ss;
                }
            }

            FileInputStream fin=new FileInputStream(f);
            BufferedReader br=new BufferedReader(new InputStreamReader(fin));
            String oldHtml = "";
            String n;
            while((n=br.readLine())!=null)
            {
                oldHtml += n;
            }

            HighlighterUtil hl = new HighlighterUtil(nq, oldHtml);
            String newhtmlString = hl.getHighlightedHtml();

            try{

                String absolutePath = f.getAbsolutePath();
                String filePath = absolutePath.
                        substring(0,absolutePath.lastIndexOf(File.separator));

                //create a temp file
                File temp = new File(filePath, "tmp" + f.getName());

                //write it
                BufferedWriter bw = new BufferedWriter(new FileWriter(temp));
                bw.write(newhtmlString);
                bw.close();

                browser.getEngine().load(temp.toURI().toURL().toString());

                System.out.println(temp.getAbsolutePath());

            }catch(IOException e){

                e.printStackTrace();

            }

            this.pageID = pageID;

        }
        catch (MalformedURLException e) {
            e.printStackTrace();
        }
        catch (FileNotFoundException e1) {
        e1.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadPageString(String pageString, TextField pageID) {
        browser.getEngine().loadContent(pageString, "text/html");
        this.pageID = pageID;
    }

    public void goBack() {
        try {
            browser.getEngine().getHistory().go(-1);
        } catch (IndexOutOfBoundsException e) {
            System.out.println("Can't go back");
        }
    }

    public void goForward() {
        try {
            browser.getEngine().getHistory().go(+1);
        } catch (IndexOutOfBoundsException e) {
            System.out.println("Can't go forward");
        }
    }

    @Override protected void layoutChildren() {
        double w = getWidth();
        double h = getHeight();
        layoutInArea(browser,0,0,w,h,0, HPos.CENTER, VPos.CENTER);
    }

    @Override protected double computePrefWidth(double height) {
        return 1020;
    }

    @Override protected double computePrefHeight(double width) {
        return 713;
    }
}