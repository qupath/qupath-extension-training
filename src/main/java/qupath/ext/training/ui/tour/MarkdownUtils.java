package qupath.ext.training.ui.tour;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import org.commonmark.node.BlockQuote;
import org.commonmark.node.Text;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.AttributeProvider;
import org.commonmark.renderer.html.HtmlRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Map;

/**
 * Helper class for creating HTML to display for a tour item.
 */
class MarkdownUtils {

    private static final Logger logger = LoggerFactory.getLogger(MarkdownUtils.class);

    private static final Parser parser = Parser.builder().build();

    private static final HtmlRenderer renderer = HtmlRenderer.builder()
            .attributeProviderFactory(context -> new AdmonitionAttributeProvider())
            .build();

    static String createHtml(String title, String text, Image img) {
        var sb = new StringBuilder();
        if (title != null)
            sb.append("### ")
                    .append(title)
                    .append("\n");

        if (text != null)
            sb.append(text).append("\n");

        if (img != null) {
            var imgTag = createEmbeddedImage(img);
            if (imgTag != null) {
                sb.append("\n\n").append(imgTag);
            }
        }

        var doc = parser.parse(sb.toString());
        var html = renderer.render(doc);
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
