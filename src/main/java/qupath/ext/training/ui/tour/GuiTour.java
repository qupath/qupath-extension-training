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
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.controlsfx.control.action.Action;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qupath.lib.common.GeneralTools;
import qupath.lib.gui.QuPathGUI;
import qupath.lib.gui.viewer.tools.PathTools;

import java.util.List;

public class GuiTour implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(GuiTour.class);

    private final QuPathGUI qupath;

    private ObservableList<TourItem> items;
    private Pagination pagination;
    private Stage stage;

    private GuiHighlight highlight;

    public GuiTour(QuPathGUI qupath) {
        this.qupath = qupath;
    }

    private void initialize() {
        this.items = createInstructions(qupath);
        this.pagination = createPagination();
        this.stage = createStage();
        this.highlight = new GuiHighlight(qupath.getStage());
    }


    private ObservableList<TourItem> createInstructions(QuPathGUI qupath) {
        return FXCollections.observableArrayList(
                createInstruction(
                        "intro",
                        qupath.getStage().getScene().getRoot()
                ),
                createInstruction(
                        "tab-pane",
                        qupath.getAnalysisTabPane()
                ),
                createInstruction(
                        "viewer",
                        qupath.getViewer().getView() // Consider what to do if the user already has multiple viewers
                ),
                createInstruction(
                        "toolbar",
                        qupath.getToolBar()
                ),
                createToolbarInstruction(
                        "toolbar.tab-pane",
                        qupath.getCommonActions().SHOW_ANALYSIS_PANE),
                createToolbarInstruction(
                        "toolbar.move",
                        qupath.getToolManager().getToolAction(PathTools.MOVE)),
                createToolbarInstruction(
                        "toolbar.drawing",
                        qupath.getToolManager().getToolAction(PathTools.RECTANGLE),
                        qupath.getToolManager().getToolAction(
                                qupath.getToolManager().getTools()
                                        .stream()
                                        .filter(t -> t.getName().equals("Wand"))
                                        .findFirst()
                                        .orElse(PathTools.BRUSH))),

                createToolbarInstruction(
                        "toolbar.points",
                        qupath.getToolManager().getToolAction(PathTools.POINTS)
                ),
                createToolbarInstruction(
                        "toolbar.selection-mode",
                        qupath.getToolManager().getSelectionModeAction()),
                createToolbarInstruction(
                        "toolbar.bc",
                        qupath.getCommonActions().BRIGHTNESS_CONTRAST),
                // Need to make magnification accessible here...
//        createToolbarInstruction(
//                "Magnification",
//                "Check the magnification of the image as it currently appears in the viewer.\n" +
//                        "If you have a microscopy image that stores the magnification value of the " +
//                        "objective lens, this will be used to calculate the magnification here.\n" +
//                        "Otherwise, 1x means viewing the image at 'full resolution'.",
//                qupath.getContextActions().MAGNIFICATION),
                createToolbarInstruction(
                        "toolbar.zoom-to-fit",
                        qupath.getViewerActions().ZOOM_TO_FIT),
                createToolbarInstruction(
                        "toolbar.show-annotations",
                        qupath.getOverlayActions().SHOW_ANNOTATIONS),
                createToolbarInstruction(
                        "toolbar.fill-annotations",
                        qupath.getOverlayActions().FILL_ANNOTATIONS),
                createToolbarInstruction(
                        "toolbar.show-names",
                        qupath.getOverlayActions().SHOW_NAMES),
                createToolbarInstruction(
                        "toolbar.show-tma",
                        qupath.getOverlayActions().SHOW_TMA_GRID),
                createToolbarInstruction(
                        "toolbar.show-detections",
                        qupath.getOverlayActions().SHOW_DETECTIONS),
                createToolbarInstruction(
                        "toolbar.fill-detections",
                        qupath.getOverlayActions().FILL_DETECTIONS),

                createToolbarInstruction(
                        "toolbar.show-connections",
                        qupath.getOverlayActions().SHOW_CONNECTIONS),

                createToolbarInstruction(
                        "toolbar.show-classification",
                        qupath.getOverlayActions().SHOW_PIXEL_CLASSIFICATION),

                // TODO: Access opacity slider in a more robust way
                createInstruction(
                        "toolbar.opacity-slider",
                        qupath.getToolBar().getItems().stream()
                                .filter(n -> n instanceof Slider)
                                .findFirst()
                                .orElse(null)),

                // TODO: Access measurement menu in a more robust way
                createInstruction(
                        "toolbar.measurement-tables",
                        qupath.getToolBar().getItems().stream()
                                .filter(n -> n instanceof MenuButton)
                                .findFirst()
                                .orElse(null)),

                createToolbarInstruction(
                        "toolbar.script-editor",
                        qupath.getAutomateActions().SCRIPT_EDITOR),

                createToolbarInstruction(
                        "toolbar.show-overview",
                        qupath.getViewerActions().SHOW_OVERVIEW),

                createToolbarInstruction(
                        "toolbar.show-location",
                        qupath.getViewerActions().SHOW_LOCATION),

                createToolbarInstruction(
                        "toolbar.scalebar",
                        qupath.getViewerActions().SHOW_SCALEBAR
                ),

                createToolbarInstruction(
                        "toolbar.prefs",
                        qupath.getCommonActions().PREFERENCES
                ),
                createToolbarInstruction(
                        "toolbar.log",
                        qupath.getCommonActions().SHOW_LOG
                ),
                createToolbarInstruction(
                        "toolbar.help",
                        qupath.getCommonActions().HELP_VIEWER
                ),
                createTabPaneInstruction(
                        "tab-pane.project",
                        "Project"
                ),
                createTabPaneInstruction(
                        "tab-pane.image",
                        "Image"
                ),
                createTabPaneInstruction(
                        "tab-pane.annotations",
                        "Annotations"
                ),
                createTabPaneInstruction(
                        "tab-pane.hierarchy",
                        "Hierarchy"
                ),
                createTabPaneInstruction(
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
        var page = TourUtils.createPage(item);
        return page;
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

    private void handlePageKeyReleased(KeyEvent event) {
        if (event.isConsumed() || (event.getCode() != KeyCode.LEFT && event.getCode() != KeyCode.RIGHT))
            return;
        switch (event.getCode()) {
            case KeyCode.RIGHT -> incrementPage(1);
            case KeyCode.LEFT -> incrementPage(-1);
        }
        event.consume();
    }

    private void incrementPage(int increment) {
        if (pagination == null || pagination.getPageCount() == 0)
            return;
        int page = pagination.getCurrentPageIndex() + increment;
        pagination.setCurrentPageIndex(
                GeneralTools.clipValue(page, 0, pagination.getPageCount()-1)
        );
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
    TourItem createToolbarInstruction(String key, Action... actions) {
        var items = qupath.getToolBar().getItems()
                .stream()
                .filter(node -> containsActionProperty(node, actions))
                .toList();
        return new TourItem(key, items, null);
    }

    TourItem createTabPaneInstruction(String key, String tabName) {
        var items = qupath.getAnalysisTabPane()
                .getTabs()
                .stream()
                .filter(tab -> tabName.equals(tab.getText()))
                .map(Tab::getContent)
                .toList();
        return new TourItem(key, items, null);
    }

    /**
     * Create a UI component instruction for specific nodes.
     * @param title a short title
     * @param text the main text to display
     * @param nodes the specific nodes (e.g. buttons) to highlight
     * @return
     */
    static TourItem createInstruction(String title, String text, Node... nodes) {
        return new TourItem(title, text, List.of(nodes));
    }

    static TourItem createInstruction(String key, Node... nodes) {
        return new TourItem(key, List.of(nodes), null);
    }

    static TourItem createInstruction(String key, Image image, Node... nodes) {
        return new TourItem(key, List.of(nodes), image);
    }


    private static boolean containsActionProperty(Node node, Action... actions) {
        for (var action : actions) {
            if (node.getProperties().containsValue(action))
                return true;
        }
        return false;
    }


    /**
     * Highlight one or more nodes to help the user find it.
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
