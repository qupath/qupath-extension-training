package qupath.ext.training.ui.tour;

import javafx.application.Platform;
import javafx.geometry.BoundingBox;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import qupath.fx.utils.FXUtils;

import java.util.Collection;
import java.util.List;

public class TourItem {

    private String title;
    private String text;
    private List<Node> nodes;

    TourItem(String title, String text, Collection<? extends Node> nodes) {
        this.title = title;
        this.text = text;
        this.nodes = List.copyOf(nodes);
    }

    @Override
    public String toString() {
        return text;
    }

    List<Node> getNodes() {
        return nodes;
    }

    Node createPage() {
        var ta = new TextArea();
        ta.setWrapText(true);
        ta.setText(text);
        ta.setEditable(false);
        ta.setPrefWidth(250);
        var pane = new BorderPane(ta);
        if (title != null) {
            var label = new Label(title);
            label.setStyle("-fx-font-weight: bold; -fx-font-size: 1.2em;");
            label.setMaxWidth(Double.MAX_VALUE);
            label.setAlignment(Pos.CENTER);
            label.setPadding(new Insets(5.0));
            pane.setTop(label);
        }
        return pane;
    }

}