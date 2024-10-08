package qupath.fx.controls.tour;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;

/**
 * A control to manage a tour of GUI elements.
 * <p>
 * As users progress through the tour, different GUI elements can be highlighted and an explanation provided.
 */
public class GuiTour extends Control {

    /**
     * Style class using CSS to highlight nodes.
     */
    public static final String STYLE_HIGHLIGHT_CSS = "HIGHLIGHT_CSS";

    /**
     * Style class that temporary attaches an Effect to nodes to highlight them.
     */
    public static final String STYLE_HIGHLIGHT_EFFECT = "HIGHLIGHT_EFFECT";

    /**
     * Style class using a transparent window overlay to highlight nodes.
     */
    public static final String STYLE_HIGHLIGHT_OVERLAY = "HIGHLIGHT_OVERLAY";

    private final ObservableList<TourItem> items;
    private final BooleanProperty animate = new SimpleBooleanProperty(true);
    private final BooleanProperty showHighlight = new SimpleBooleanProperty(true);

    /**
     * Create a new tour.
     */
    public GuiTour() {
        this.items = FXCollections.observableArrayList();
    }

    /**
     * Get the list of items in the tour.
     * @return
     */
    public ObservableList<TourItem> getItems() {
        return items;
    }

    /**
     * Property to control whether the tour should animate highlights when moving between items.
     * This only has an effect if using an overlay for highlighting, not CSS.
     * @return
     */
    public BooleanProperty animateProperty() {
        return animate;
    }

    /**
     * Set whether the tour should animate highlights when moving between items.
     * This only has an effect if using an overlay for highlighting, not CSS.
     * @param doAnimate
     */
    public void setAnimate(boolean doAnimate) {
        animate.set(doAnimate);
    }

    /**
     * Get whether the tour should animate highlights when moving between items.
     * This only has an effect if using an overlay for highlighting, not CSS.
     * @return
     */
    public boolean doAnimate() {
        return animate.get();
    }

    /**
     * Property to control whether the tour should show highlights.
     * @return
     */
    public BooleanProperty showHighlightProperty() {
        return showHighlight;
    }

    /**
     * Set whether the tour should show highlights.
     * @param doShow
     */
    public void setShowHighlight(boolean doShow) {
        showHighlight.set(doShow);
    }

    /**
     * Get whether the tour should show highlights.
     * @return
     */
    public boolean doShowHighlight() {
        return showHighlight.get();
    }

    @Override
    protected Skin<GuiTour> createDefaultSkin() {
        if (getStyleClass().contains(STYLE_HIGHLIGHT_OVERLAY)) {
            return new GuiTourSkin(this, new OverlayHighlight());
        } else if (getStyleClass().contains(STYLE_HIGHLIGHT_EFFECT)) {
            return new GuiTourSkin(this, new EffectHighlight());
        } else {
            return new GuiTourSkin(this, new CssHighlight());
        }
    }

}
