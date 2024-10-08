package qupath.fx.controls.tour;

import javafx.scene.Node;

import java.util.List;

/**
 * Helper class to highlight specific nodes during a GUI tour.
 */
public interface TourHighlight {

    /**
     * Highlight multiple nodes in the GUI.
     * @param nodes
     */
    void highlightNodes(List<? extends Node> nodes);

    /**
     * Ensure the highlight is showing.
     */
    void show();

    /**
     * Stop displaying the highlight.
     */
    void hide();

}
