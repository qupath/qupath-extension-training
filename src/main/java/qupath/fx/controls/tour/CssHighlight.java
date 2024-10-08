package qupath.fx.controls.tour;

import javafx.scene.Node;
import javafx.stage.Window;
import qupath.ext.training.ui.tour.GuiTourCommand;
import qupath.fx.utils.FXUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Highlight nodes using CSS.
 */
class CssHighlight implements TourHighlight {

    private static final String HIGHLIGHT_CLASS = "tour-highlight-node";
    private static String stylesheet = GuiTourCommand.class.getClassLoader().getResource("css/tour.css").toExternalForm();

    private List<Node> currentNodes = new ArrayList<>();

    /**
     * Create a new highlighter.
     */
    public CssHighlight() {}

    /**
     * Hide the highlight window.
     */
    @Override
    public void show() {
        for (var node : currentNodes) {
            if (!node.getStyleClass().contains(HIGHLIGHT_CLASS)) {
                ensureStylesheet(FXUtils.getWindow(node));
                node.getStyleClass().add(HIGHLIGHT_CLASS);
            }
        }
    }

    /**
     * Hide the highlight window.
     */
    @Override
    public void hide() {
        for (var node : currentNodes) {
            node.getStyleClass().remove(HIGHLIGHT_CLASS);
        }
    }

    private boolean ensureStylesheet(Window owner) {
        if (owner == null)
            return false;
        if (!owner.getScene().getStylesheets().contains(stylesheet)) {
            owner.getScene().getStylesheets().add(stylesheet);
        }
        return true;
    }

    @Override
    public void highlightNodes(List<? extends Node> nodes) {
        if (currentNodes.equals(nodes))
            return;

        hide();
        currentNodes.clear();

        if (nodes.isEmpty()) {
            return;
        }

        currentNodes.addAll(nodes);
        show();
    }

}
