package qupath.ext.training.ui.tour;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.Image;
import javafx.scene.transform.Scale;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qupath.lib.gui.tools.WebViews;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

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

        if (!nodes.isEmpty()) {
            var node = nodes.getFirst();
            var img = createScaledSnapshot(node, 2.0);
            String maxDim;
            if (img.getWidth() > img.getHeight()*2)
                maxDim = "max-width: 90%;";
            else
                maxDim = "max-height: " + Math.min(256, img.getHeight()/2) + ";";
            try {
                var base64 = base64Encode(img);
                sb.append("\n\n<img src=\"data:image/png;base64,")
                        .append(base64)
                        .append("\" style=\"display: block; margin: auto; ")
                        .append(maxDim)
                        .append("\"")
                        .append(" />");
            } catch (IOException e) {
                logger.error("Exception creating snapshot image: {}", e.getMessage(), e);
            }
        }

        var doc = Parser.builder().build().parse(sb.toString());
        var html = HtmlRenderer.builder().build().render(doc);

        return "<div style=\"text-align: center;\">" + html + "</div>";
    }

    private static Image createScaledSnapshot(Node node, double scale) {
        var params = new SnapshotParameters();
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

}
