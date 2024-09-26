package qupath.ext.training.ui;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import org.controlsfx.control.PopOver;
import qupath.lib.gui.scripting.DefaultScriptEditor;
import qupath.lib.gui.scripting.QPEx;
import qupath.lib.gui.scripting.languages.ScriptLanguageProvider;
import qupath.lib.scripting.ScriptParameters;
import qupath.lib.scripting.languages.ExecutableLanguage;
import qupath.lib.scripting.languages.ScriptLanguage;

import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.util.Collections;
import java.util.concurrent.Callable;

public class ScriptQuestion implements Question {
    private final BorderPane pane;
    private final Callable<String> explanationGetter;
    private final TextField tfScript;

    ScriptQuestion(String question, Callable<String> explanationGetter) {
        this.explanationGetter = explanationGetter;
        pane = new BorderPane();

        Pane questionPane = new Pane(new Label(question));
        pane.setTop(questionPane);
        tfScript = new TextField();
        pane.setCenter(tfScript);
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
