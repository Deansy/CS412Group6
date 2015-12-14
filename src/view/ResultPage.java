package view;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.control.TextField;
import javafx.scene.input.TouchPoint;
import javafx.scene.layout.Region;
import javafx.scene.web.WebView;
import java.io.File;
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

    public void loadPage(File f, TextField pageID) {
        try {
            browser.getEngine().load(f.toURI().toURL().toString());
            this.pageID = pageID;

        }
        catch (MalformedURLException e) {
            e.printStackTrace();
        }
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