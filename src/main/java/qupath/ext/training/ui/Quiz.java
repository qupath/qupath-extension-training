package qupath.ext.training.ui;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Pagination;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;

import java.util.List;

public class Quiz {

    private final List<Question> questions;
    private final BorderPane pane;
    private final String title;
    private final ProgressBar progressBar;

    Quiz(String title, List<Question> questions) {
        this.questions = questions;
        this.pane = new BorderPane();
        this.title = title;
        this.progressBar = new ProgressBar(0);
        DoubleProperty fracCompleted = new SimpleDoubleProperty(0);
        for (Question q: questions) {
            q.hasBeenSolved().addListener((v, o, n) -> {
                if (n) {
                    fracCompleted.set(fracCompleted.get() + ((double) 1 / questions.size()));
                }
            });
        }
        fracCompleted.addListener((v, o, n) -> progressBar.setProgress(n.doubleValue()));
        var pagination = new Pagination();
        pagination.setPageCount(questions.size());
        pagination.setPageFactory(pageIndex -> questions.get(pageIndex).getPane());
        pane.setCenter(pagination);
        AnchorPane ap = new AnchorPane();
        ap.getChildren().add(progressBar);
        AnchorPane.setRightAnchor(progressBar, 0.);
        AnchorPane.setLeftAnchor(progressBar, 0.);
        ap.setPadding(new Insets(5, 50, 5, 50));
        pane.setBottom(ap);
    }

    Quiz(String title, Question... questions) {
        this(title, List.of(questions));
    }

    Pane getPane() {
        return this.pane;
    }

    String getTitle() {
        return this.title;
    }
}
