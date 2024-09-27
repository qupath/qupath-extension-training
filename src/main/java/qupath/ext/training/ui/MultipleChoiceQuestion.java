package qupath.ext.training.ui;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class MultipleChoiceQuestion extends VBox implements Question {
    private final String correctAnswer;
    private final List<MCQOption> options;
    private static final MCQOption NO_ANSWER_SELECTED = new MCQOption("No answer selected", "No answer selected");
    private static final ResourceBundle resources = ResourceBundle.getBundle("qupath.ext.training.ui.strings");

    @FXML
    private Button acceptBtn;
    @FXML
    private Label questionText;
    @FXML
    private VBox optionsBox;

    private final BooleanProperty hasBeenSolved = new SimpleBooleanProperty(false);

    public static MultipleChoiceQuestion createMCQ(String question, String correctAnswer, MCQOption... options) throws IOException {
        return new MultipleChoiceQuestion(question, correctAnswer, options);
    }

    private MultipleChoiceQuestion(String question, String correctAnswer, MCQOption... options) throws IOException {
        this.correctAnswer = correctAnswer;
        this.options = List.of(options);

        var url = MultipleChoiceQuestion.class.getResource("multiple_choice_question.fxml");
        FXMLLoader loader = new FXMLLoader(url, resources);
        loader.setRoot(this);
        loader.setController(this);
        loader.load();

        questionText.setText(question);
        var tg = new ToggleGroup();
        for (MCQOption option : options) {
            var rb = new RadioButton(option.text);
            rb.setToggleGroup(tg);
            optionsBox.getChildren().add(rb);
        }
    }

    @FXML
    void showPopover() {
        Questions.checkCurrentSolution(this, acceptBtn);
    }

    public record MCQOption(String text, String explanation) {
    }

    @Override
    public Pane getPane() {
        return this;
    }

    @Override
    public BooleanProperty hasBeenSolved() {
        return this.hasBeenSolved;
    }


    @Override
    public boolean isCurrentAnswerRight() {
        return getCurrentAnswer().orElse(NO_ANSWER_SELECTED).text.equals(correctAnswer);
    }

    @Override
    public String getExplanation() {
        return getCurrentAnswer().orElse(NO_ANSWER_SELECTED).explanation;
    }

    private Optional<MCQOption> getCurrentAnswer() {
        for (int i = 0; i < optionsBox.getChildren().size(); i++) {
            if (optionsBox.getChildren().get(i) instanceof RadioButton radioButtonbtn && radioButtonbtn.isSelected()) {
                return Optional.of(options.get(i));
            }
        }
        return Optional.empty();
    }
}
