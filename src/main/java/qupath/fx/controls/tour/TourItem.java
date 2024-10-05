package qupath.fx.controls.tour;

import javafx.scene.Node;

import java.util.List;

/**
 * A single item in a {@link GuiTour}.
 * <p>
 * This should provide a title, a list of nodes to highlight, and a page to display that explains
 * the purpose of the nodes.
 * <p>
 * It is permitted to return an empty list of nodes to highlight, in which case the page should
 * provide a more general explanation.
 */
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
     * Create a content page to display for this item.
     * @return
     */
    Node createPage();

}