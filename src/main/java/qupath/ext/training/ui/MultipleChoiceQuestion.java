package qupath.ext.training.ui;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import org.controlsfx.control.PopOver;

import java.util.ArrayList;
import java.util.List;

public class MultipleChoiceQuestion implements Question {
    private final BorderPane pane;
    private final String correctAnswer;
    private final List<RadioButton> buttons;
    private final List<MCQOption> options;

    MultipleChoiceQuestion(String question, String correctAnswer, MCQOption... options) {
        this.correctAnswer = correctAnswer;
        this.options = List.of(options);
        pane = new BorderPane();

        Pane questionPane = new Pane(new Label(question));
        pane.setTop(questionPane);
        GridPane answerPane = new GridPane();
        var tg = new ToggleGroup();
        buttons = new ArrayList<>();
        for (int i = 0; i < options.length; i++) {
            var rb = new RadioButton(options[i].text);
            rb.setToggleGroup(tg);
            buttons.add(rb);
            answerPane.addRow(i, rb);
        }
        pane.setCenter(answerPane);
        var acceptBtn = new Button("Accept");
        acceptBtn.setOnAction(e -> {
            PopOver po = new PopOver();
            po.setContentNode(new Label(isCurrentAnswerRight() ? "Right!" : "Wrong." + "\n" + getExplanation()));
            po.show(acceptBtn);
        });
        pane.setBottom(acceptBtn);
    }

    record MCQOption(String text, String explanation) {
    }

    @Override
    public Pane getPane() {
        return pane;
    }

    @Override
    public boolean isCurrentAnswerRight() {
        return correctAnswer.equals(getCurrentAnswer().text);
    }

    @Override
    public String getExplanation() {
        return getCurrentAnswer().explanation;
    }

    private MCQOption getCurrentAnswer() {
        for (int i = 0; i < buttons.size(); i++) {
            if (buttons.get(i).isSelected()) {
                return options.get(i);
            }
        }
        return null;
    }
}
