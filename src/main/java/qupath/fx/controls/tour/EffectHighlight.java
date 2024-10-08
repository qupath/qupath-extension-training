package qupath.fx.controls.tour;

import javafx.scene.Node;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Effect;
import javafx.scene.effect.InnerShadow;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

/**
 * Highlight nodes using CSS.
 */
class EffectHighlight implements TourHighlight {

    private List<Node> currentNodes = new ArrayList<>();

    /**
     * Create a new highlighter.
     */
    public EffectHighlight() {}

    /**
     * Hide the highlight window.
     */
    @Override
    public void show() {
        for (var node : currentNodes) {
            if (node.getEffect() == null) {
                node.setEffect(createEffect(node));
            }
        }
    }

    private Effect createEffect(Node node) {
        var innerShaddow = new InnerShadow(10.0, Color.ORANGE);
        var outerShadow = new DropShadow(10.0, Color.ORANGE);
        outerShadow.setInput(innerShaddow);
        return outerShadow;
    }

    /**
     * Hide the highlight window.
     */
    @Override
    public void hide() {
        for (var node : currentNodes) {
            node.setEffect(null);
        }
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

        // Only add nodes that don't already have an effect
        nodes.stream().filter(n -> n.getEffect() == null).forEach(currentNodes::add);
        show();
    }

}
