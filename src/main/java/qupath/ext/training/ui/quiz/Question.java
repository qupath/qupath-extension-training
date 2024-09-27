package qupath.ext.training.ui.quiz;

import javafx.beans.property.BooleanProperty;
import javafx.scene.layout.Pane;

public interface Question {
    /**
     * Check if the current answer is right or wrong. This could be combined with getExplanation in some sort of Answer class.
     * @return true if the answer is right
     */
    boolean isCurrentAnswerRight();

    /**
     * Get an explanation for why the current answer is right or wrong.
     * @return Hopefully a helpful explanation of the underlying concepts, but maybe nothing?
     */
    String getExplanation();

    /**
     * Get the GUI pane used to display the question
     * @return Some sort of pane.
     */
    Pane getPane();

    /**
     * Records whether the user has come up with an answer
     * @return true if the user has gotten it right yet
     */
    BooleanProperty hasBeenSolved();
}
