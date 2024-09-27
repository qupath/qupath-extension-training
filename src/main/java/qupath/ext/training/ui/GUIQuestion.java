package qupath.ext.training.ui;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.util.ResourceBundle;
import java.util.concurrent.Callable;

public class GUIQuestion implements Question {

    private final VBox pane;
    private final Callable<String> explanationGetter;
    private static final ResourceBundle resources = ResourceBundle.getBundle("qupath.ext.training.ui.strings");
    private final BooleanProperty hasBeenSolved = new SimpleBooleanProperty(false);

    GUIQuestion(String question, Callable<String> explanationGetter) {
        this.explanationGetter = explanationGetter;
        pane = new VBox();
        pane.setPadding(new Insets(5));
        pane.setSpacing(10);
        // pane.setAlignment(Pos.CENTER_LEFT);

        pane.getChildren().add(new Label(question));
        pane.getChildren().add(new Separator());
        var acceptBtn = new Button(resources.getString("quiz.question.accept"));
        acceptBtn.setOnAction(e -> Questions.showPopover(this, acceptBtn));
        pane.getChildren().add(acceptBtn);
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

    @Override
    public BooleanProperty hasBeenSolved() {
        return this.hasBeenSolved;
    }
}
