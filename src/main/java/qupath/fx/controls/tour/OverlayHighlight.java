package qupath.fx.controls.tour;

import javafx.animation.Transition;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.Scene;
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
 * <p>
 * Currently, this works by creating a transparent stage with a rectangle that can be moved and resized to highlight.
 * In the future, this implementation might be changed (e.g. to apply CSS to the highlighted nodes directly).
 */
class OverlayHighlight implements TourHighlight {

    private Stage stage;
    private Rectangle rectangle;
    private final BooleanProperty animateProperty = new SimpleBooleanProperty(true);

    private final ChangeListener<Number> windowMoveListener = this::handleStageMoved;
    private final ChangeListener<Number> windowResizeListener = this::handleStageResized;

    /**
     * Create a new highlighter.
     */
    public OverlayHighlight() {}

    private void handleStageMoved(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
        if (stage != null && stage.isShowing())
            hide();
    }

    private void handleStageResized(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
        if (stage != null && stage.isShowing())
            hide();
    }

    private void attachWindowListener(Window stage) {
        stage.xProperty().addListener(windowMoveListener);
        stage.yProperty().addListener(windowMoveListener);
        stage.widthProperty().addListener(windowResizeListener);
        stage.heightProperty().addListener(windowResizeListener);
    }

    private void detachWindowListener(Window stage) {
        stage.xProperty().removeListener(windowMoveListener);
        stage.yProperty().removeListener(windowMoveListener);
        stage.widthProperty().removeListener(windowResizeListener);
        stage.heightProperty().removeListener(windowResizeListener);
    }

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
        attachWindowListener(owner);

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

        scene.getStylesheets().add(OverlayHighlight.class.getClassLoader().getResource("css/tour.css").toExternalForm());

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
                if (stage.getOwner() != null)
                    detachWindowListener(stage.getOwner());
                stage = null;
            }
        }
        if (stage == null)
            return initialize(owner);
        else
            return owner != null;
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
    @Override
    public void highlightNodes(List<? extends Node> nodes) {
        highlightNodes(nodes, animateProperty.get());
    }


    private void highlightNodes(List<? extends Node> nodes, boolean doAnimate) {
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
        if (!doAnimate || !stage.isShowing() || rectangle.getWidth() == 0 || rectangle.getHeight() == 0) {
            double newWidth = bounds.getWidth() + pad * 2;
            double newHeight = bounds.getHeight() + pad * 2;
            if (rectangle.getWidth() != newWidth || rectangle.getHeight() != newHeight) {
                stage.hide();
                rectangle.setWidth(bounds.getWidth() + pad * 2);
                rectangle.setHeight(bounds.getHeight() + pad * 2);
            }
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
        if (!stage.isShowing()) {
            stage.show();

            // We don't want to steal focus from the user
            if (lastFocusedWindow != null)
                lastFocusedWindow.requestFocus();
        }
    }

    private static Window findFocusedWindow() {
        return Window.getWindows()
                .stream()
                .filter(Window::isFocused)
                .findFirst()
                .orElse(null);
    }

    /**
     * Transition to animate moving a stage to a target X and Y.
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
            double newX = startX + frac * (targetX - startX);
            double newY = startY + frac * (targetY - startY);
            stage.setX(newX);
            stage.setY(newY);
        }

    }

}
