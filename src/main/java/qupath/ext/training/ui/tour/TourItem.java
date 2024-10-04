package qupath.ext.training.ui.tour;

import javafx.scene.Node;

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

    /**
     * Get the title to display.
     * @return
     */
    String getTitle() {
        return title;
    }

    /**
     * Get the main text to display.
     * This is typically markdown.
     * @return
     */
    String getText() {
        return text;
    }

    /**
     * Get an unmodifiable list of nodes to display.
     * @return
     */
    List<Node> getNodes() {
        return nodes;
    }

    @Override
    public String toString() {
        return text;
    }

}