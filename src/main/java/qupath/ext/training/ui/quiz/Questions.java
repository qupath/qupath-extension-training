package qupath.ext.training.ui.quiz;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.controlsfx.control.PopOver;

import java.util.ResourceBundle;

public class Questions {
    private static final ResourceBundle resources = ResourceBundle.getBundle("qupath.ext.training.ui.strings");

    static void checkCurrentSolution(Question question, Button button) {
        PopOver po = new PopOver();
        var vb = new VBox();
        boolean isCorrect = question.isCurrentAnswerRight();
        if (isCorrect) {
            question.hasBeenSolved().set(true);
        }
        vb.getChildren().add(new Label(isCorrect ? resources.getString("quiz.question.correct") : resources.getString("quiz.question.incorrect")
                        + "\n" + question.getExplanation()));
        vb.setPadding(new Insets(5));
        po.setContentNode(vb);
        po.show(button);
    }

}
