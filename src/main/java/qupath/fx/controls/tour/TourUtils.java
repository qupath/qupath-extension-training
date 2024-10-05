package qupath.fx.controls.tour;

import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.transform.Scale;
import javafx.stage.Window;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Utility methods for working with tours.
 *
 * TODO: Since these methods are not tour-specific, it is intended to move them to FXUtils.
 */
public class TourUtils {

    private static final Logger logger = LoggerFactory.getLogger(TourUtils.class);

    /**
     * Compute the bounding box for all specified nodes.
     * @param nodes
     * @return
     */
    public static Bounds computeScreenBounds(List<? extends Node> nodes) {
        var firstNode = nodes.getFirst();
        var bounds = firstNode.localToScreen(firstNode.getBoundsInLocal());
        if (nodes.size() > 1) {
            double minX = bounds.getMinX();
            double minY = bounds.getMinY();
            double maxX = bounds.getMaxX();
            double maxY = bounds.getMaxY();
            double minZ = bounds.getMinZ();
            double maxZ = bounds.getMaxZ();
            for (int i = 1; i < nodes.size(); i++) {
                var tempNode = nodes.get(i);
                var tempBounds = tempNode.localToScreen(tempNode.getBoundsInLocal());
                minX = Math.min(minX, tempBounds.getMinX());
                minY = Math.min(minY, tempBounds.getMinY());
                maxX = Math.max(maxX, tempBounds.getMaxX());
                maxY = Math.max(maxY, tempBounds.getMaxY());
                minZ = Math.min(minZ, tempBounds.getMinZ());
                maxZ = Math.max(maxZ, tempBounds.getMaxZ());
            }
            bounds = new BoundingBox(minX, minY, minZ,
                    maxX - minX, maxY - minY, maxZ - minZ);
        }
        return bounds;
    }


    /**
     * Create a snapshot image of a node with a transparent background,
     * scaled by the specified factor.
     * <p>
     * This can be used as a convenient alternative to {@code node.snapshot(params, image)}
     * when we want a higher or lower-resolution image.
     * @param node the node to snapshot
     * @param scale the scale factor; use 1.0 for the original size
     * @return
     */
    public static Image createScaledSnapshot(Node node, double scale) {
        return createScaledSnapshot(node, null, scale);
    }

    /**
     * Create a snapshot image of a node with a transparent background,
     * scaled by the specified factor and optionally cropping to the specified bounds.
     * <p>
     * This can be used as a convenient alternative to {@code node.snapshot(params, image)}
     * when we want a higher or lower-resolution image.
     * @param node the node to snapshot
     * @param bounds the bounds to snapshot; use null for the entire node
     * @param scale the scale factor; use 1.0 for the original size
     * @return the snapshot image
     */
    public static Image createScaledSnapshot(Node node, Rectangle2D bounds, double scale) {
        var params = new SnapshotParameters();
        params.setFill(Color.TRANSPARENT);
        if (scale != 1.0) {
            params.setTransform(new Scale(scale, scale));
        }
        if (bounds != null) {
            params.setViewport(bounds);
        }
        return node.snapshot(params, null);
    }

    /**
     * Create a snapshot image of a portion of a window, using screen coordinates and a transparent background.
     * <p>
     * Note that this will snapshot the content of the window only; it does not include window decorations.
     *
     * @param window the window to snapshot
     * @param screenBounds the screen coordinates to snapshot; if null, the entire content of the window will be used
     * @param scale the scale factor; use 1.0 for the original size
     * @return the snapshot image
     */
    public static Image createScaledSnapshot(Window window, Rectangle2D screenBounds, double scale) {
        var root = window.getScene().getRoot();
        Rectangle2D rect = null;
        if (screenBounds != null) {
            var bounds = root.screenToLocal(rectToBounds(screenBounds));
            rect = new Rectangle2D(
                    bounds.getMinX() * scale,
                    bounds.getMinY() * scale,
                    bounds.getWidth() * scale,
                    bounds.getHeight() * scale
            );
        }
        return createScaledSnapshot(root, rect, scale);
    }

    /**
     * Convert a rectangle to a bounds object.
     * @param rect
     * @return
     */
    public static Bounds rectToBounds(Rectangle2D rect) {
        return new BoundingBox(rect.getMinX(), rect.getMinY(), rect.getWidth(), rect.getHeight());
    }

    /**
     * Convert the 2D component of a bounds object to a rectangle.
     * @param bounds
     * @return
     */
    public static Rectangle2D boundsToRect(Bounds bounds) {
        return new Rectangle2D(bounds.getMinX(), bounds.getMinY(), bounds.getWidth(), bounds.getHeight());
    }


}
