package qupath.ext.training.ui.tour;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;

public class GuiTour extends Control {

    private final ObservableList<TourItem> items;
    private final BooleanProperty animate = new SimpleBooleanProperty(true);
    private final BooleanProperty showHighlight = new SimpleBooleanProperty(true);

    public GuiTour() {
        this.items = FXCollections.observableArrayList();
    }

    public ObservableList<TourItem> getItems() {
        return items;
    }

    public BooleanProperty animateProperty() {
        return animate;
    }

    public BooleanProperty showHighlightProperty() {
        return showHighlight;
    }

    @Override
    protected Skin<GuiTour> createDefaultSkin() {
        return new GuiTourSkin(this);
    }

}
