package qupath.ext.training.ui.quiz;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import qupath.lib.gui.scripting.QPEx;
import qupath.lib.gui.scripting.languages.ScriptLanguageProvider;
import qupath.lib.scripting.ScriptParameters;
import qupath.lib.scripting.languages.ExecutableLanguage;

import javax.script.ScriptException;
import java.util.Collections;
import java.util.ResourceBundle;
import java.util.concurrent.Callable;

public class ScriptQuestion implements Question {
    private final VBox pane;
    private final Callable<String> explanationGetter;
    private final TextField tfScript;
    private static final ResourceBundle resources = ResourceBundle.getBundle("qupath.ext.training.ui.strings");
    private final BooleanProperty hasBeenSolved = new SimpleBooleanProperty(false);

    public ScriptQuestion(String question, Callable<String> explanationGetter) {
        this.explanationGetter = explanationGetter;
        pane = new VBox();
        pane.setPadding(new Insets(5));
        // pane.setAlignment(Pos.CENTER_LEFT);
        pane.setSpacing(10);

        pane.getChildren().add(new Label(question));
        pane.getChildren().add(new Separator());
        // todo: block the GUI somehow? otherwise people can point and click their way to the answer
        tfScript = new TextField();
        pane.getChildren().add(tfScript);
        pane.getChildren().add(new Separator());
        var acceptBtn = new Button(resources.getString("quiz.question.accept"));
        acceptBtn.setOnAction(e -> Questions.checkCurrentSolution(this, acceptBtn));
        pane.getChildren().add(acceptBtn);
    }

    @Override
    public boolean isCurrentAnswerRight() {
        var builder = ScriptParameters.builder()
                .setDefaultImports(QPEx.getCoreClasses())
                .setDefaultStaticImports(Collections.singletonList(QPEx.class))
                .setScript(tfScript.getText());
        var params = builder.build();
        var language = ScriptLanguageProvider.fromString("groovy");
        try {
            ((ExecutableLanguage)language).execute(params);
        } catch (ScriptException e) {
            throw new RuntimeException(e);
        }
        try {
            return explanationGetter.call().isBlank();
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public String getExplanation() {
        try {
            return explanationGetter.call();
        } catch (Exception e) {
            return String.format(resources.getString("quiz.question.error"), e.getMessage());
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
