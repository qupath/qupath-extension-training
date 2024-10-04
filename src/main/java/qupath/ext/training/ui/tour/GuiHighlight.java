package qupath.ext.training.ui.tour;

import javafx.animation.Transition;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javafx.util.Duration;
import qupath.fx.utils.FXUtils;

import java.util.List;

/**
 * Manage a window that can act as an overlay to highlight GUI elements.
 */
class GuiHighlight {

    private final Window defaultOwner;

    private Stage stage;
    private Rectangle rectangle;
    private BooleanProperty doAnimate = new SimpleBooleanProperty(true);

    public GuiHighlight(Window defaultOwner) {
        this.defaultOwner = defaultOwner;
    }

    public void hide() {
        if (stage != null) {
            stage.hide();
        }
    }

    private void initialize(Window owner) {
        var stage = new Stage();
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.initOwner(owner);

        var rect = new Rectangle();
        rect.getStyleClass().addAll("tour-highlight-rect");

        var pane = new BorderPane(rect);
        pane.getStyleClass().setAll("tour-highlight-pane");

        // TODO: Consider Setting transparent because I'd like (I think) clicks to still pass through whatever is highlighted.
        // I can only confirm this doesn't work on macOS though... possibly because of
        // https://bugs.openjdk.org/browse/JDK-8088104
//        rect.setMouseTransparent(true);
//        pane.setMouseTransparent(true);
        rect.setOnMouseClicked(this::handleMouseClick);

        var scene = new Scene(pane, Color.TRANSPARENT);

        // This was previously used to find highlight windows to close, but may no longer be needed
        stage.getProperties().put("_INSTRUCTION_HIGHLIGHT", true);
        stage.setScene(scene);

        scene.getStylesheets().add(GuiTour.class.getClassLoader().getResource("css/styles.css").toExternalForm());

        this.rectangle = rect;
        this.stage = stage;
    }

    private void handleMouseClick(MouseEvent event) {
        if (stage != null)
            stage.hide();
    }

    /**
     * Ensure that we have a stage that shares the same owner as the provided node,
     * or default owner if no owner could be found.
     *
     * @param node
     */
    private void ensureInitializedForOwner(Node node) {
        var owner = node == null ? null : FXUtils.getWindow(node);
        if (owner == null)
            owner = defaultOwner;
        if (stage != null) {
            if (stage.getOwner() != owner) {
                stage.hide();
                stage = null;
            }
        }
        if (stage == null)
            initialize(owner);
    }

    /**
     * Highlight the specified nodes.
     * @param nodes
     * @see #highlightNodes(List)
     */
    public void highlightNodes(Node... nodes) {
        highlightNodes(List.of(nodes));
    }

    /**
     * Highlight a collection of nodes.
     * <ul>
     *     <li>If a single node is provided, then the highlight window is shown around it.</li>
     *     <li>If multiple nodes are provided, then the highlight is the bounding box of all nodes.</li>
     *     <li>If no nodes are provided, any existing highlight window is hidden.</li>
     * </ul>
     * Note that all non-visible nodes are ignored, but if a node is within a tab pane then this class will attempt
     * to ensure that the parent tab is shown.
     * @param nodes
     */
    public void highlightNodes(List<? extends Node> nodes) {
        nodes = nodes.stream()
                .filter(Node::isVisible)
                .toList();

        if (nodes.isEmpty()) {
            hide();
            return;
        }

        // Try to ensure any tab is visible
        // (We assume that, if we have multiple nodes, all are in the same tab)
        var firstNode = nodes.getFirst();
        tryToEnsureVisible(firstNode);

        // This can occur whenever we're part of a toolbar overflow
        if (!firstNode.isVisible() || (firstNode.getParent() != null && !firstNode.getParent().isVisible())) {
            hide();
            return;
        }

        var bounds = TourUtils.computeBoundsForAll(nodes);

        // Ensure we have a stage with the required owner window
        ensureInitializedForOwner(firstNode);
        double pad = 4;
        // Target x,y for the stage - allow padding + 1 extra pixel for the stage itself
        // (this seems to give better centering of the highlights, at least on macOS)
        double targetX = bounds.getMinX() - pad - 1;
        double targetY = bounds.getMinY() - pad - 1;
        if (!doAnimate.get() || !stage.isShowing() || rectangle.getWidth() == 0 || rectangle.getHeight() == 0) {
            stage.hide();
            rectangle.setWidth(bounds.getWidth() + pad * 2);
            rectangle.setHeight(bounds.getHeight() + pad * 2);
            stage.setX(targetX);
            stage.setY(targetY);
        } else {
            // I wasn't able to get animation working for both stage x,y location and rectangle width,height -
            // there seemed to be a bug whereby the simultaneous changing of the width,height resulted in the
            // x,y coordinates being displaced.
            rectangle.setWidth(bounds.getWidth() + 2 * pad);
            rectangle.setHeight(bounds.getHeight() + 2 * pad);
            stage.sizeToScene();
            var animation = new HighlightTransition(stage, Duration.millis(300), targetX, targetY);
            animation.playFromStart();
        }
        stage.show();
    }


    /**
     * Try to ensure that a node is visible.
     * Currently, this only handles tabpanes; in the future, we might need to worry about windows as well.
     *
     * @param node
     */
    void tryToEnsureVisible(Node node) {
        var tab = searchForTab(node);
        if (tab != null) {
            tab.getTabPane().getSelectionModel().select(tab);
        }
    }


    /**
     * Search for a tab that contains a specified node.
     * This is useful when we want to highlight anything under a TabPane,
     * because the containing tab might not be visible.
     * @param node
     * @return a tab if found, or null
     */
    private static Tab searchForTab(Node node) {
        if (node == null)
            return null;
        var grandparent = node.getParent() == null ? null : node.getParent().getParent();
        if (grandparent instanceof TabPane tabPane) {
            // This is very ugly, but finding the tab is awkward
            // (TabPaneSkin gets in the way)
            return tabPane.getTabs().stream()
                    .filter(tab -> tab.getContent() == node)
                    .findFirst()
                    .orElse(null);
        }
        return searchForTab(node.getParent());
    }

    /**
     * Move a stage to a target X and Y.
     * Note that Stage.setX() and Stage.setY() ominously report that they may be ignored on some platforms.
     */
    private static class HighlightTransition extends Transition {

        private final Stage stage;
        private final double startX, startY, targetX, targetY;

        private HighlightTransition(Stage stage, Duration cycleDuration, double targetX, double targetY) {
            this.stage = stage;
            this.startX = stage.getX();
            this.startY = stage.getY();
            this.targetX = targetX;
            this.targetY = targetY;
            setCycleDuration(cycleDuration);
        }

        @Override
        protected void interpolate(double frac) {
            stage.setX(startX + frac * (targetX - startX));
            stage.setY(startY + frac * (targetY - startY));
        }

    }

}
