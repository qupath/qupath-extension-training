package qupath.fx.controls.tour;

import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Pagination;
import javafx.scene.control.SkinBase;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Window;

import java.util.List;

/**
 * The default skin for the {@link GuiTour} control.
 * <p>
 * This uses a {@link Pagination} control to display the different items in the tour.
 */
public class GuiTourSkin extends SkinBase<GuiTour> {

    private final Pagination pagination;

    private final TourHighlight highlight;

    /**
     * Constructor for all SkinBase instances.
     *
     * @param control The control for which this Skin should attach to.
     */
    protected GuiTourSkin(GuiTour control, TourHighlight highlight) {
        super(control);
        this.highlight = highlight;
        if (highlight instanceof OverlayHighlight stageHighlight) {
            stageHighlight.animateProperty().bind(control.animateProperty());
            stageHighlight.animateProperty().bind(control.animateProperty());
        }
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
            tryToEnsureVisible(nodesToHighlight.getFirst());
        }
        // Need to create page first, because it could create screenshots
        // that would be changed by highlighting
        Node page = item.createPage();
        highlightNodes(nodesToHighlight);
        return page;
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

    /**
     * Try to ensure that a node is visible.
     * <p>
     * Currently, this only handles tab panes;
     * in the future, we might need to worry about windows as well.
     * @param node
     */
    void tryToEnsureVisible(Node node) {
        var tab = searchForTab(node);
        if (tab != null) {
            tab.getTabPane().getSelectionModel().select(tab);
        }
    }


    /**
     * Search for a tab that contains a specified node.
     * <p>
     * This is useful when we want to highlight anything under a TabPane,
     * because the containing tab might not be visible.
     * @param node
     * @return a tab if found, or null
     */
    private static Tab searchForTab(Node node) {
        if (node == null)
            return null;
        var grandparent = node.getParent() == null ? null : node.getParent().getParent();
        if (grandparent instanceof TabPane tabPane) {
            // This is very ugly, but finding the tab is awkward
            // (TabPaneSkin gets in the way)
            return tabPane.getTabs().stream()
                    .filter(tab -> tab.getContent() == node)
                    .findFirst()
                    .orElse(null);
        }
        return searchForTab(node.getParent());
    }

}
