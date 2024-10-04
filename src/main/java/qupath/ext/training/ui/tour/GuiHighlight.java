package qupath.ext.training.ui.tour;

import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import qupath.fx.utils.FXUtils;

import java.util.List;

/**
 * Manage a window that can act as an overlay to highlight GUI elements.
 */
class GuiHighlight {

    private final Window defaultOwner;

    private Stage stage;
    private Rectangle rectangle;

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
        rect.getStyleClass().addAll("tour", "highlight");

        var pane = new BorderPane(rect);
        pane.setStyle("-fx-background-color: rgba(0, 0, 0, 0);");
        var scene = new Scene(pane, Color.TRANSPARENT);
        stage.getProperties().put("_INSTRUCTION_HIGHLIGHT", true);
        stage.setScene(scene);

        scene.setOnMouseClicked(event -> {
            if (event.getClickCount() > 1) {
                stage.close();
                event.consume();
            }
        });

        scene.getStylesheets().add(GuiTour.class.getClassLoader().getResource("css/styles.css").toExternalForm());

        this.rectangle = rect;
        this.stage = stage;
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

        // Hide any existing highlights
        hideAllHighlightWindows();

        // Try to ensure any tab is visible
        // (We assume that, if we have multiple nodes, all are in the same tab)
        var firstNode = nodes.getFirst();
        tryToEnsureVisible(firstNode);

        var bounds = computeBoundsForAll(nodes);

        // Ensure we have a stage with the required owner window
        ensureInitializedForOwner(firstNode);

        double pad = 4;
        // TODO: Consider animation
        rectangle.setWidth(bounds.getWidth() + pad * 2);
        rectangle.setHeight(bounds.getHeight() + pad * 2);
        stage.setX(bounds.getMinX() - pad);
        stage.setY(bounds.getMinY() - pad);

        rectangle.setMouseTransparent(true);

        //        // Create a short animation to highlight the button
//        var transparent = new KeyValue(rect.opacityProperty(), 0.5);
//        var opaque = new KeyValue(rect.opacityProperty(), 1.0);
//        var tl = new Timeline();
//        tl.getKeyFrames().addAll(
//                new KeyFrame(Duration.ZERO, transparent),
//                new KeyFrame(Duration.millis(500), opaque),
//                new KeyFrame(Duration.millis(1000), transparent)
//        );
//        tl.setCycleCount(5)
//        tl.setOnFinished(e -> stage.close());
//        tl.playFromStart();

        stage.show();
    }

    private void hideAllHighlightWindows() {
        List.copyOf(Window.getWindows())
                .stream()
                .filter(stage -> Boolean.TRUE.equals(stage.getProperties().getOrDefault("_INSTRUCTION_HIGHLIGHT", Boolean.FALSE)))
                .forEach(Window::hide);
    }

    private static Bounds computeBoundsForAll(List<? extends Node> nodes) {
        var firstNode = nodes.getFirst();
        var bounds = firstNode.localToScreen(firstNode.getBoundsInLocal());
        if (nodes.size() > 1) {
            double minX = bounds.getMinX();
            double minY = bounds.getMinY();
            double maxX = bounds.getMaxX();
            double maxY = bounds.getMaxY();
            for (int i = 1; i < nodes.size(); i++) {
                var tempNode = nodes.get(i);
                var tempBounds = tempNode.localToScreen(tempNode.getBoundsInLocal());
                minX = Math.min(minX, tempBounds.getMinX());
                minY = Math.min(minY, tempBounds.getMinY());
                maxX = Math.max(maxX, tempBounds.getMaxX());
                maxY = Math.max(maxY, tempBounds.getMaxY());
            }
            bounds = new BoundingBox(minX, minY, maxX - minX, maxY - minY);
        }
        return bounds;
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

}
