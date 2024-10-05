package qupath.ext.training.ui.tour;

import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.image.Image;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qupath.fx.controls.tour.TourItem;
import qupath.fx.controls.tour.TourUtils;
import qupath.fx.utils.FXUtils;
import qupath.lib.gui.tools.WebViews;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * An implementation of {@link TourItem} that uses resource bundles, markdown and WebViews to display content.
 * <p>
 * This implementation takes a resource bundle key, and uses it to look up the title and text to display.
 * The text is assumed to be markdown, and is rendered as HTML.
 * <p>
 * The item can also provide an image, which can optionally be generated on demand.
 */
public class MarkdownTourItem implements TourItem {

    private static final Logger logger = LoggerFactory.getLogger(MarkdownTourItem.class);

    private final ResourceBundle bundle;
    private String key;
    private Supplier<Image> imageSupplier;
    private List<Node> nodes;

    private MarkdownTourItem(ResourceBundle bundle, String key, Collection<? extends Node> nodes, Supplier<Image> imageSupplier) {
        this.bundle = bundle;
        this.key = key;
        this.nodes = nodes == null ? Collections.emptyList() : List.copyOf(nodes);
        this.imageSupplier = imageSupplier;
    }

    private MarkdownTourItem(ResourceBundle bundle, String key, Collection<? extends Node> nodes) {
        this(bundle, key, nodes, null);
        this.imageSupplier = this::createScaledSnapshot;
    }

    /**
     * Create a new tour item.
     * If nodes are provided, this will lazily generate a snapshot image of the nodes for display.
     * @param bundle the resource bundle to use
     * @param key the resource bundle key for the item
     * @param nodes the nodes to highlight; may be null, if no nodes should be highlighted
     * @return the new tour item
     */
    public static TourItem create(ResourceBundle bundle, String key, Collection<? extends Node> nodes) {
        return new MarkdownTourItem(bundle, key, nodes);
    }

    /**
     * Create a new tour item with a specific (static) image.
     * @param bundle the resource bundle to use
     * @param key the resource bundle key for the item
     * @param nodes the nodes to highlight; may be null, if no nodes should be highlighted
     * @param image the image to display; may be null, if no image should be used
     * @return the new tour item
     */
    public static TourItem createWithImage(ResourceBundle bundle, String key, Collection<? extends Node> nodes, Image image) {
        return createWithImage(bundle, key, nodes, () -> image);
    }

    /**
     * Create a new tour item with a lazily-generated image.
     * @param bundle the resource bundle to use
     * @param key the resource bundle key for the item
     * @param nodes the nodes to highlight; may be null, if no nodes should be highlighted
     * @param imageSupplier the supplier that generates the image to display; may be null, if no image should be used
     * @return the new tour item
     */
    public static TourItem createWithImage(ResourceBundle bundle, String key, Collection<? extends Node> nodes, Supplier<Image> imageSupplier) {
        return new MarkdownTourItem(bundle, key, nodes, imageSupplier);
    }

    /**
     * Get the title to display.
     * @return
     */
    @Override
    public String getTitle() {
        var titleKey = key + ".title";
        return bundle.getString(titleKey);
    }

    /**
     * Get the main text to display, formatted as markdown.
     * @return
     */
    public String getText() {
        var textKey = key + ".text";
        // We treat all resources with keys starting key.text as distinct paragraphs,
        // sorted by length.
        // We also check for keys starting with key.text.tip, key.text.info, key.text.caution,
        // and format them as blockquotes.
        return bundle.keySet()
                .stream()
                .filter(k -> k.startsWith(textKey))
                .sorted(Comparator.comparingInt(String::length))
                .map(this::getUpdatedString)
                .collect(Collectors.joining("\n\n"));
    }


    private String getUpdatedString(String key) {
        var s = bundle.getString(key);
        if (key.contains(".text.tip"))
            return "> **Tip:** " + s.replaceAll("\n", "\n> ");
        if (key.contains(".text.info"))
            return "> **Info:** " + s.replaceAll("\n", "\n> ");
        if (key.contains(".text.caution"))
            return "> **Caution:** " + s.replaceAll("\n", "\n> ");
        return s;
    }


    /**
     * Get a static image to display, or null if no static image is stored.
     * @return
     */
    public Image getImage() {
        return imageSupplier == null ? null : imageSupplier.get();
    }

    /**
     * Get an unmodifiable list of nodes to display.
     * @return
     */
    @Override
    public List<Node> getHighlightNodes() {
        return nodes;
    }

    @Override
    public Node createPage() {
        var webview = WebViews.create(true);
        Platform.runLater(() -> {
            var html = MarkdownUtils.createHtml(getTitle(), getText(), getImage());
            webview.getEngine().loadContent(html);
        });
        return webview;
    }

    @Override
    public String toString() {
        return "TourItem[" + getTitle() + "]";
    }

    private Image createScaledSnapshot() {
        return createScaledSnapshot(getHighlightNodes());
    }

    /**
     * Create a snapshot of one or more nodes.
     * This may be rescaled, so that a higher resolution image is returned for smaller nodes.
     * @param nodes
     * @return the snapshot image, or null if no nodes are provided
     */
    private static Image createScaledSnapshot(List<? extends Node> nodes) {
        if (nodes.isEmpty())
            return null;
        var firstNode = nodes.getFirst();
        if (nodes.size() == 1) {
            double scale = computeScaleFromBounds(firstNode.getLayoutBounds());
            return TourUtils.createScaledSnapshot(firstNode, scale);
        }
        var window = FXUtils.getWindow(firstNode);
        if (window != null) {
            var bounds = TourUtils.computeScreenBounds(nodes);
            double scale = computeScaleFromBounds(bounds);
            double pad = 1;
            var rect = new Rectangle2D(
                    bounds.getMinX()-pad,
                    bounds.getMinY()-pad,
                    bounds.getWidth()+pad*2,
                    bounds.getHeight()+pad*2);
            return TourUtils.createScaledSnapshot(window, rect, scale);
        } else {
            return null;
        }
    }

    /**
     * Compute scale from a bounds object; this is used to have smaller items
     * (e.g. buttons) at a higher resolution.
     * @param bounds
     * @return
     */
    private static double computeScaleFromBounds(Bounds bounds) {
        if (bounds == null)
            return 1.0;
        double minDim = Math.min(bounds.getWidth(), bounds.getHeight());
        if (minDim < 128)
            return 2.0;
        return 1.0;
    }

}
