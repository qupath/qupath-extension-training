package qupath.ext.training.ui.tour;

import javafx.scene.Node;

import java.util.List;

public interface TourItem {

    /**
     * Get the title for this item.
     * @return
     */
    String getTitle();

    /**
     * Get a list of nodes to highlight.
     * @return
     */
    List<Node> getHighlightNodes();

    /**
     * Create a page to display for this item.
     * @return
     */
    Node createPage();

}