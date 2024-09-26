package qupath.ext.training.ui;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import org.controlsfx.control.PopOver;

import java.util.concurrent.Callable;

public class GUIQuestion implements Question {

    private final BorderPane pane;
    private final Callable<String> explanationGetter;

    GUIQuestion(String question, Callable<String> explanationGetter) {
        this.explanationGetter = explanationGetter;
        pane = new BorderPane();

        Pane questionPane = new Pane(new Label(question));
        pane.setTop(questionPane);
        var acceptBtn = new Button("Accept");
        acceptBtn.setOnAction(e -> {
            PopOver po = new PopOver();
            po.setContentNode(new Label(isCurrentAnswerRight() ? "Right!" : "Wrong." + "\n" + getExplanation()));
            po.show(acceptBtn);
        });
        pane.setBottom(acceptBtn);
    }

    @Override
    public boolean isCurrentAnswerRight() {
        try {
            return explanationGetter.call().isEmpty();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getExplanation() {
        try {
            return explanationGetter.call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Pane getPane() {
        return pane;
    }
}
