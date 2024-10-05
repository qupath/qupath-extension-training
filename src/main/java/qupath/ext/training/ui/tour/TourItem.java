package qupath.ext.training.ui.tour;

import javafx.scene.Node;
import javafx.scene.image.Image;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class TourItem {

    private String key;
    private Image image;
    private List<Node> nodes;

    private TourItem(String key, Collection<? extends Node> nodes, Image image) {
        this.key = key;
        this.nodes = nodes == null ? Collections.emptyList() : List.copyOf(nodes);
        this.image = image;
    }

    public static TourItem create(String key, Collection<? extends Node> nodes) {
        return new TourItem(key, nodes, null);
    }

    public static TourItem createWithImage(String key, Collection<? extends Node> nodes, Image image) {
        return new TourItem(key, nodes, image);
    }

    /**
     * Get the title to display.
     * @return
     */
    public String getTitle() {
        return TourResources.getTitle(key);
    }

    /**
     * Get the main text to display.
     * This is typically markdown.
     * @return
     */
    public String getText() {
        return TourResources.getText(key);
    }

    /**
     * Get a static image to display, or null if no static image is stored.
     * @return
     */
    public Image getImage() {
        return image;
    }

    /**
     * Get an unmodifiable list of nodes to display.
     * @return
     */
    public List<Node> getNodes() {
        return nodes;
    }

    @Override
    public String toString() {
        return "TourItem[" + getTitle() + "]";
    }

}