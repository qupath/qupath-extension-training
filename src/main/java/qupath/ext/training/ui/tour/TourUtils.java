package qupath.ext.training.ui.tour;

import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.transform.Scale;
import org.commonmark.node.BlockQuote;
import org.commonmark.node.Text;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.AttributeProvider;
import org.commonmark.renderer.html.HtmlRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qupath.fx.utils.FXUtils;
import qupath.lib.gui.tools.WebViews;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Map;

public class TourUtils {

    private static final Logger logger = LoggerFactory.getLogger(TourUtils.class);

    /**
     * Create a node that can be used to display a {@link TourItem} in a pagination.
     * @param item
     * @return
     */
    static Node createPage(TourItem item) {
        var html = createHtml(item);
        var webview = WebViews.create(true);
        webview.getEngine().loadContent(html);
        return webview;
    }

    /**
     * Compute the bounding box for all specified nodes.
     * @param nodes
     * @return
     */
    static Bounds computeBoundsForAll(List<? extends Node> nodes) {
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

    private static String createHtml(TourItem item) {
        var title = item.getTitle();
        var text = item.getText();
        var nodes = item.getNodes();

        var sb = new StringBuilder();
        if (title != null)
            sb.append("### ")
                    .append(title)
                    .append("\n");

        if (text != null)
            sb.append(text).append("\n");

        Image img = createScaledSnapshot(nodes, 2.0);
        if (img != null) {
            var imgTag = createEmbeddedImage(img);
            if (imgTag != null) {
                sb.append("\n\n").append(imgTag);
            }
        }

        var doc = Parser.builder()
                .build()
                .parse(sb.toString());

        var html = HtmlRenderer.builder()
                .attributeProviderFactory(context -> new AdmonitionAttributeProvider())
                .build()
                .render(doc);

        return "<div style=\"text-align: center;\">" + html + "</div>";
    }

    private static String createEmbeddedImage(Image img) {
        String maxDim;
        // This logic may need revised... it attempts to do sth sensible with large nodes
        // and buttons (trying to keep 'normal-sized' buttons the same height)
        if (img.getWidth() > img.getHeight()*2 && img.getHeight() > 64)
            maxDim = "max-width: 90%;";
        else
            maxDim = "max-height: " + Math.min(256, img.getHeight()/2) + ";";
        try {
            var sb = new StringBuilder();
            var base64 = base64Encode(img);
            sb.append("<img src=\"data:image/png;base64,")
                    .append(base64)
                    .append("\" style=\"display: block; margin: auto; ")
                    .append(maxDim)
                    .append("\"")
                    .append(" />");
            return sb.toString();
        } catch (IOException e) {
            logger.error("Exception creating snapshot image: {}", e.getMessage(), e);
        }
        return null;
    }

    private static Image createScaledSnapshot(List<? extends Node> nodes, double scale) {
        if (nodes.isEmpty())
            return null;
        var firstNode = nodes.getFirst();
        if (nodes.size() == 1) {
            return createScaledSnapshot(firstNode, scale);
        }
        var window = FXUtils.getWindow(firstNode);
        if (window != null) {
            var params = new SnapshotParameters();
            params.setFill(Color.TRANSPARENT);
            var bounds = TourUtils.computeBoundsForAll(nodes);
            var root = window.getScene().getRoot();
            bounds = root.screenToLocal(bounds);
            bounds = new BoundingBox(
                    bounds.getMinX()*scale,
                    bounds.getMinY()*scale,
                    bounds.getWidth()*scale,
                    bounds.getHeight()*scale
            );

            double pad = 2;
            params.setViewport(
                    new Rectangle2D(
                            bounds.getMinX()-pad,
                            bounds.getMinY()-pad,
                            bounds.getWidth()+pad*2,
                            bounds.getHeight()+pad*2));
            params.setTransform(new Scale(scale, scale));
            return root.snapshot(params, null);
        } else {
            return null;
        }
    }

    private static Image createScaledSnapshot(Node node, double scale) {
        var params = new SnapshotParameters();
        params.setFill(Color.TRANSPARENT);
        params.setTransform(new Scale(scale, scale));
        return node.snapshot(params, null);
    }

    private static String base64Encode(Image img) throws IOException {
        return base64Encode(SwingFXUtils.fromFXImage(img, null));
    }

    private static String base64Encode(BufferedImage img) throws IOException {
        try (var stream = new ByteArrayOutputStream()) {
            ImageIO.write(img, "PNG", stream);
            return Base64.getEncoder().encodeToString(stream.toByteArray());
        }
    }


    /**
     * Add classes to blockquotes starting with **Tip:**, **Note:** etc.
     * This is a very rough, interim approach.
     * A proper extension would be preferably, e.g. see https://github.com/commonmark/commonmark-java/issues/327
     */
    private static class AdmonitionAttributeProvider implements AttributeProvider {

        @Override
        public void setAttributes(org.commonmark.node.Node node, String tagName, Map<String, String> attributes) {
            if (node instanceof BlockQuote quote) {
                var text = findFirstText(quote);
                if (text != null) {
                    text = text.toLowerCase().strip();
                    if (text.startsWith("tip:"))
                        attributes.put("class", "tip");
                    else if (text.startsWith("warning:"))
                        attributes.put("class", "warn");
                    else if (text.startsWith("caution:"))
                        attributes.put("class", "caution");
                    else if (text.startsWith("info:") || text.startsWith("sidenote:") || text.startsWith("note:"))
                        attributes.put("class", "info");
                }
            }
        }

        /**
         * This is used to find the first text inside a blockquote
         * (which may be nested inside a paragraph, strong formatting etc.)
         * @param node
         * @return
         */
        private static String findFirstText(org.commonmark.node.Node node) {
            if (node instanceof Text text) {
                return text.getLiteral();
            }
            var firstChild = node.getFirstChild();
            if (firstChild == null)
                return null;
            else
                return findFirstText(firstChild);
        }

    }

}
