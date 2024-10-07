package qupath.fx.controls.tour;

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
import qupath.ext.training.ui.tour.GuiTourCommand;
import qupath.fx.utils.FXUtils;
import qupath.lib.common.GeneralTools;

import java.util.List;

/**
 * Manage a window that can act as an overlay to highlight GUI elements.
 * <p>
 * Currently, this works by creating a transparent stage with a rectangle that can be moved and resized to highlight.
 * In the future, this implementation might be changed (e.g. to apply CSS to the highlighted nodes directly).
 */
class GuiHighlight {

    private Stage stage;
    private Rectangle rectangle;
    private BooleanProperty animateProperty = new SimpleBooleanProperty(true);
    private RelativeWindowMover mover;

    /**
     * Create a new highlighter.
     */
    public GuiHighlight() {}

    /**
     * Hide the highlight window.
     */
    public void hide() {
        if (stage != null) {
            stage.hide();
        }
    }

    /**
     * Show the highlight window, if available.
     */
    public void show() {
        if (stage != null) {
            stage.show();
        }
    }

    /**
     * Get property to control whether highlights should animate when moving.
     */
    public BooleanProperty animateProperty() {
        return animateProperty;
    }

    private boolean initialize(Window owner) {
        if (owner == null)
            return false;
        var stage = new Stage();
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.initOwner(owner);
        mover = new RelativeWindowMover(stage);
        // We get relative movement for free on Mac, but not on Windows or Linux
        if (!GeneralTools.isMac()) {
            mover.attach(owner);
        }

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

        scene.getStylesheets().add(GuiTourCommand.class.getClassLoader().getResource("css/tour.css").toExternalForm());

        this.rectangle = rect;
        this.stage = stage;
        return true;
    }

    private void handleMouseClick(MouseEvent event) {
        if (stage != null)
            stage.hide();
    }

    /**
     * Ensure that we have a highlight stage that shares the same owner as the provided node,
     * or default owner if no owner could be found.
     *
     * @param node
     * @return true if the stage is initialized and has the correct owner, or false if no owner is found
     */
    private boolean ensureInitializedForOwner(Node node) {
        var owner = node == null ? null : FXUtils.getWindow(node);
        if (stage != null) {
            if (stage.getOwner() != owner) {
                stage.hide();
                stage = null;
                mover.detach();
                mover = null;
            }
        }
        if (stage == null)
            return initialize(owner);
        else
            return owner != null;
    }

    /**
     * Highlight a single node.
     * @param node
     * @see #highlightNodes(List)
     */
    public void highlightNode(Node node) {
        highlightNodes(List.of(node));
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
        var lastFocusedWindow = findFocusedWindow();

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

        // Ensure we have a stage with the required owner window,
        // and return if there is no owner to be found
        if (!ensureInitializedForOwner(firstNode)) {
            hide();
            return;
        }

        var bounds = TourUtils.computeScreenBounds(nodes);

        double pad = 4;
        // Target x,y for the stage - allow padding + 1 extra pixel for the stage itself
        // (this seems to give better centering of the highlights, at least on macOS)
        double targetX = bounds.getMinX() - pad - 1;
        double targetY = bounds.getMinY() - pad - 1;
        if (!animateProperty.get() || !stage.isShowing() || rectangle.getWidth() == 0 || rectangle.getHeight() == 0) {
            stage.hide();
            rectangle.setWidth(bounds.getWidth() + pad * 2);
            rectangle.setHeight(bounds.getHeight() + pad * 2);
            mover.moveTo(targetX, targetY);
        } else {
            // I wasn't able to get animation working for both stage x,y location and rectangle width,height -
            // there seemed to be a bug whereby the simultaneous changing of the width,height resulted in the
            // x,y coordinates being displaced.
            rectangle.setWidth(bounds.getWidth() + 2 * pad);
            rectangle.setHeight(bounds.getHeight() + 2 * pad);
            stage.sizeToScene();
            var animation = new HighlightTransition(mover, Duration.millis(300), targetX, targetY);
            animation.playFromStart();
        }
        stage.show();

        // We don't want to steal focus from the user
        if (lastFocusedWindow != null)
            lastFocusedWindow.requestFocus();
    }

    private static Window findFocusedWindow() {
        return Window.getWindows()
                .stream()
                .filter(Window::isFocused)
                .findFirst()
                .orElse(null);
    }



    /**
     * Try to ensure that a node is visible.
     * <p>
     * Currently, this only handles tab panes;
     * in the future, we might need to worry about windows as well.
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
     * <p>
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
     * Transition to animate moving a stage to a target X and Y.
     * Note that Stage.setX() and Stage.setY() ominously report that they may be ignored on some platforms.
     */
    private static class HighlightTransition extends Transition {

        private RelativeWindowMover mover;
        private final double startX, startY, targetX, targetY;

        private HighlightTransition(RelativeWindowMover mover, Duration cycleDuration, double targetX, double targetY) {
            this.mover = mover;
            this.startX = mover.getWindow().getX();
            this.startY = mover.getWindow().getY();
            this.targetX = targetX;
            this.targetY = targetY;
            setCycleDuration(cycleDuration);
        }

        @Override
        protected void interpolate(double frac) {
            double newX = startX + frac * (targetX - startX);
            double newY = startY + frac * (targetY - startY);
            mover.moveTo(newX, newY);
        }

    }

}
