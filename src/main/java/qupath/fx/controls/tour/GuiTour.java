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
     * @return
     */
    public BooleanProperty animateProperty() {
        return animate;
    }

    /**
     * Set whether the tour should animate highlights when moving between items.
     * @param doAnimate
     */
    public void setAnimate(boolean doAnimate) {
        animate.set(doAnimate);
    }

    /**
     * Get whether the tour should animate highlights when moving between items.
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
        return new GuiTourSkin(this);
    }

}
