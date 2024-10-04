package qupath.ext.training.ui.tour;

import javafx.scene.Node;
import javafx.scene.image.Image;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class TourItem {

    private String key;

    private String title;
    private String text;
    private Image image;
    private List<Node> nodes;

    TourItem(String title, String text, Collection<? extends Node> nodes) {
        this.title = title;
        this.text = text;
        this.nodes = List.copyOf(nodes);
    }

    TourItem(String key, Collection<? extends Node> nodes, Image image) {
        this.key = key;
        this.nodes = nodes == null ? Collections.emptyList() : List.copyOf(nodes);
        this.image = image;
    }

    /**
     * Get the title to display.
     * @return
     */
    String getTitle() {
        if (title != null)
            return title;
        return TourResources.getTitle(key);
    }

    /**
     * Get the main text to display.
     * This is typically markdown.
     * @return
     */
    String getText() {
        if (text != null)
            return text;
        return TourResources.getText(key);
    }

    /**
     * Get a static image to display, or null if no static image is stored.
     * @return
     */
    Image getImage() {
        return image;
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