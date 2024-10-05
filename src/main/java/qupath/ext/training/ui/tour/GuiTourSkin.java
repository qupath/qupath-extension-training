package qupath.ext.training.ui.tour;

import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Pagination;
import javafx.scene.control.SkinBase;
import javafx.stage.Window;

import java.util.List;

public class GuiTourSkin extends SkinBase<GuiTour> {

    private Pagination pagination;

    private GuiHighlight highlight;

    /**
     * Constructor for all SkinBase instances.
     *
     * @param control The control for which this Skin should attach to.
     */
    protected GuiTourSkin(GuiTour control) {
        super(control);
        this.highlight = new GuiHighlight();
        this.highlight.animateProperty().bind(control.animateProperty());
        this.highlight.animateProperty().bind(control.animateProperty());
        this.pagination = createPagination();
        control.showHighlightProperty().addListener(this::handleShowHighlightChange);
        // Show/hide the highlight when the window is shown/hidden
        control.sceneProperty()
                .flatMap(Scene::windowProperty)
                .flatMap(Window::showingProperty)
                .addListener(this::handleShowHighlightChange);
        getChildren().add(pagination);
    }

    private void handleShowHighlightChange(ObservableValue<? extends Boolean> value, Boolean oldValue, Boolean newValue) {
        if (newValue) {
            highlight.show();
        } else {
            highlight.hide();
        }
    }

    private Pagination createPagination() {
        var pagination = new Pagination();
        var items = getItems();
        pagination.pageCountProperty().bind(Bindings.size(items));
        pagination.setPageFactory(this::createPage);
        return pagination;
    }

    private ObservableList<TourItem> getItems() {
        return getSkinnable().getItems();
    }

    private Node createPage(int pageIndex) {
        var items = getItems();
        var item = items.get(pageIndex);
        // It's important to highlight first, otherwise nodes might not
        // be visible, and dynamic screenshots don't work
        var nodesToHighlight = item.getHighlightNodes();
        if (!nodesToHighlight.isEmpty()) {
            highlightNodes(nodesToHighlight);
        }
        return item.createPage();
    }

    /**
     * Highlight one or more nodes.
     * @param nodes
     */
    private void highlightNodes(List<? extends Node> nodes) {
        // We want to update the highlight even if it's not shown... but then we have to hide it quickly
        highlight.highlightNodes(nodes);
        if (!getSkinnable().showHighlightProperty().get())
            highlight.hide();
    }

}
