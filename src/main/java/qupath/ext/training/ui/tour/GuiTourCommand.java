package qupath.ext.training.ui.tour;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.MenuButton;
import javafx.scene.control.Pagination;
import javafx.scene.control.Slider;
import javafx.scene.control.Tab;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.controlsfx.control.action.Action;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qupath.lib.gui.QuPathGUI;
import qupath.lib.gui.viewer.tools.PathTools;

import java.util.List;

/**
 * A command to run a tour of the QuPath user interface.
 * <p>
 * This is intended to help new users understand what is going on quickly.
 */
public class GuiTourCommand implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(GuiTourCommand.class);

    private final QuPathGUI qupath;

    private ObservableList<TourItem> items;
    private Pagination pagination;
    private Stage stage;

    private GuiHighlight highlight;

    public GuiTourCommand(QuPathGUI qupath) {
        this.qupath = qupath;
    }

    private void initialize() {
        this.items = createItems(qupath);
        this.pagination = createPagination();
        this.stage = createStage();
        this.highlight = new GuiHighlight(qupath.getStage());
    }


    private ObservableList<TourItem> createItems(QuPathGUI qupath) {
        return FXCollections.observableArrayList(
                createItem(
                        "intro",
                        qupath.getStage().getScene().getRoot()
                ),
                createItem(
                        "tab-pane",
                        qupath.getAnalysisTabPane()
                ),
                createItem(
                        "viewer",
                        qupath.getViewer().getView() // Consider what to do if the user already has multiple viewers
                ),
                createItem(
                        "toolbar",
                        qupath.getToolBar()
                ),
                createToolbarItem(
                        "toolbar.tab-pane",
                        qupath.getCommonActions().SHOW_ANALYSIS_PANE),
                createToolbarItem(
                        "toolbar.move",
                        qupath.getToolManager().getToolAction(PathTools.MOVE)),
                createToolbarItem(
                        "toolbar.drawing",
                        qupath.getToolManager().getToolAction(PathTools.RECTANGLE),
                        qupath.getToolManager().getToolAction(
                                qupath.getToolManager().getTools()
                                        .stream()
                                        .filter(t -> t.getName().equals("Wand"))
                                        .findFirst()
                                        .orElse(PathTools.BRUSH))),

                createToolbarItem(
                        "toolbar.points",
                        qupath.getToolManager().getToolAction(PathTools.POINTS)
                ),
                createToolbarItem(
                        "toolbar.selection-mode",
                        qupath.getToolManager().getSelectionModeAction()),
                createToolbarItem(
                        "toolbar.bc",
                        qupath.getCommonActions().BRIGHTNESS_CONTRAST),
                createToolbarItem(
                        "toolbar.zoom-to-fit",
                        qupath.getViewerActions().ZOOM_TO_FIT),
                createToolbarItem(
                        "toolbar.show-annotations",
                        qupath.getOverlayActions().SHOW_ANNOTATIONS),
                createToolbarItem(
                        "toolbar.fill-annotations",
                        qupath.getOverlayActions().FILL_ANNOTATIONS),
                createToolbarItem(
                        "toolbar.show-names",
                        qupath.getOverlayActions().SHOW_NAMES),
                createToolbarItem(
                        "toolbar.show-tma",
                        qupath.getOverlayActions().SHOW_TMA_GRID),
                createToolbarItem(
                        "toolbar.show-detections",
                        qupath.getOverlayActions().SHOW_DETECTIONS),
                createToolbarItem(
                        "toolbar.fill-detections",
                        qupath.getOverlayActions().FILL_DETECTIONS),

                createToolbarItem(
                        "toolbar.show-connections",
                        qupath.getOverlayActions().SHOW_CONNECTIONS),

                createToolbarItem(
                        "toolbar.show-classification",
                        qupath.getOverlayActions().SHOW_PIXEL_CLASSIFICATION),

                // TODO: Access opacity slider in a more robust way
                createItem(
                        "toolbar.opacity-slider",
                        qupath.getToolBar().getItems().stream()
                                .filter(n -> n instanceof Slider)
                                .findFirst()
                                .orElse(null)),

                // TODO: Access measurement menu in a more robust way
                createItem(
                        "toolbar.measurement-tables",
                        qupath.getToolBar().getItems().stream()
                                .filter(n -> n instanceof MenuButton)
                                .findFirst()
                                .orElse(null)),

                createToolbarItem(
                        "toolbar.script-editor",
                        qupath.getAutomateActions().SCRIPT_EDITOR),

                createToolbarItem(
                        "toolbar.show-overview",
                        qupath.getViewerActions().SHOW_OVERVIEW),

                createToolbarItem(
                        "toolbar.show-location",
                        qupath.getViewerActions().SHOW_LOCATION),

                createToolbarItem(
                        "toolbar.scalebar",
                        qupath.getViewerActions().SHOW_SCALEBAR
                ),

                createToolbarItem(
                        "toolbar.prefs",
                        qupath.getCommonActions().PREFERENCES
                ),
                createToolbarItem(
                        "toolbar.log",
                        qupath.getCommonActions().SHOW_LOG
                ),
                createToolbarItem(
                        "toolbar.help",
                        qupath.getCommonActions().HELP_VIEWER
                ),
                createTabPaneItem(
                        "tab-pane.project",
                        "Project"
                ),
                createTabPaneItem(
                        "tab-pane.image",
                        "Image"
                ),
                createTabPaneItem(
                        "tab-pane.annotations",
                        "Annotations"
                ),
                createTabPaneItem(
                        "tab-pane.hierarchy",
                        "Hierarchy"
                ),
                createTabPaneItem(
                        "tab-pane.workflow",
                        "Workflow"
                )
                );
    }

    private Pagination createPagination() {
        var pagination = new Pagination();
        pagination.pageCountProperty().bind(Bindings.size(items));
        pagination.setPageFactory(this::createPage);
        return pagination;
    }

    private Node createPage(int pageIndex) {
        var item = items.get(pageIndex);
        // It's important to highlight first, otherwise nodes might not
        // be visible, and dynamic screenshots don't work
        var nodesToHighlight = item.getNodes();
        if (!nodesToHighlight.isEmpty()) {
            highlightNodes(nodesToHighlight);
            Platform.runLater(() -> {
                stage.requestFocus();
            });
        }
        return TourUtils.createPage(item);
    }

    private Stage createStage() {
       var stage = new Stage();
        stage.initOwner(qupath.getStage());
        stage.initModality(Modality.NONE);
        stage.setAlwaysOnTop(true); // It'll also be on top of other applications!
        stage.setTitle(TourResources.getString("title"));
        var scene = new Scene(pagination);
        stage.setScene(scene);
        stage.setOnCloseRequest(e -> {
            if (highlight != null)
                highlight.hide();
        });
        return stage;
    }

    @Override
    public void run() {
        if (!Platform.isFxApplicationThread()) {
            Platform.runLater(this);
            return;
        }
        if (stage == null)
            initialize();
        stage.show();
    }


    /**
     * Create a UI component instruction for actions that are displayed as buttons in the toolbar.
     * @param key a resource bundle key
     * @param actions the actions to highlight; we'll look for these in the toolbar
     * @return
     */
    TourItem createToolbarItem(String key, Action... actions) {
        var items = qupath.getToolBar().getItems()
                .stream()
                .filter(node -> containsActionProperty(node, actions))
                .toList();
        return TourItem.create(key, items);
    }

    /**
     * Create a UI component instruction for a specific tab in the tab pane.
     * TODO: This is a bit fragile, as it relies on the tab name.
     * @param key
     * @param tabName
     * @return
     */
    TourItem createTabPaneItem(String key, String tabName) {
        var items = qupath.getAnalysisTabPane()
                .getTabs()
                .stream()
                .filter(tab -> tabName.equals(tab.getText()))
                .map(Tab::getContent)
                .toList();
        return TourItem.create(key, items);
    }

    /**
     * Create a UI component instruction for specific nodes.
     * @param key the resource bundle key
     * @param nodes the specific nodes (e.g. buttons) to highlight
     * @return
     */
    private static TourItem createItem(String key, Node... nodes) {
        return TourItem.create(key, List.of(nodes));
    }

    /**
     * Check if a node contains at least one of several specified action properties.
     * @param node the note to check
     * @param actions
     * @return
     */
    private static boolean containsActionProperty(Node node, Action... actions) {
        for (var action : actions) {
            if (node.getProperties().containsValue(action))
                return true;
        }
        return false;
    }

    /**
     * Highlight one or more nodes.
     * @param nodes
     */
    private void highlightNodes(List<? extends Node> nodes) {
        if (!Platform.isFxApplicationThread()) {
            Platform.runLater(() -> highlightNodes(nodes));
            return;
        }
        highlight.highlightNodes(nodes);
    }

}
