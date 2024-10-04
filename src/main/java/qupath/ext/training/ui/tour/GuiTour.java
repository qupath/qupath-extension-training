package qupath.ext.training.ui.tour;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Pagination;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.controlsfx.control.action.Action;
import qupath.lib.common.GeneralTools;
import qupath.lib.gui.QuPathGUI;
import qupath.lib.gui.viewer.tools.PathTools;

import java.util.List;

public class GuiTour implements Runnable {

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


    private static ObservableList<TourItem> createInstructions(QuPathGUI qupath) {
        return FXCollections.observableArrayList(
                createInstruction(
                        "Tour of QuPath's user interface",
                        "Click through each page for a quick introduction to the main parts of QuPath's user interface.",
                        qupath.getStage().getScene().getRoot()
                ),
                createInstruction(
                        "Tab pane",
                        "This is the main tab pane on the left.\n\n" +
                        "Here you can select images in a project, view metadata for each image, " +
                                "see a list of annotations, and create a 'Workflow' from commands you have run.\n\n" +
                                "> This used to be called the 'Analysis Pane', but was renamed because " +
                                "it isn't really all about analysis...",
                        qupath.getAnalysisTabPane()
                ),
                createInstruction(
                        "Viewer",
                        "The main viewer for displaying images - and objects created for images.\n\n" +
                                "Right-click on this to access a context menu with additional options - which includes " +
                                "creating a grid of viewers to display several images at the same time.\n\n" +
                                "> **Tip:** Drag & drop project folders, images or scripts onto the viewer to open them quickly - " +
                                "there's no need to use the _File_ menu to open most things in QuPath.",
                        qupath.getViewer().getView() // Consider what to do if the user already has multiple viewers
                ),
                createInstruction(
                        "Toolbar",
                        "This is the main toolbar, where you can access many of the most commonly used actions.\n\n" +
                                "Click on the buttons to perform actions, or right-click to access additional options.\n\n" +
                                "> **Tip:** Hover the cursor over a button for an explanation of what it does, " +
                                "and also to see any shortcut key associated with the button.",
                        qupath.getToolBar()
                ),
                createToolbarInstruction(
                        "Toggle 'Tab pane'",
                        "Show or hide the main tab pane on the left.",
                        qupath.getCommonActions().SHOW_ANALYSIS_PANE),
                createToolbarInstruction(
                        "Move tool",
                        "Activate this, then click and drag in the viewer to pan around the image or to move objects.\n\n" +
                                "You should have the 'Move tool' active most of the time when using QuPath, " +
                                "to avoid accidentally drawing things.",
                        qupath.getToolManager().getToolAction(PathTools.MOVE)),
                createToolbarInstruction(
                        "Drawing tools",
                        "Active one of these and then click in the viewer to draw new annotations on an image.\n\n" +
                                "_(As long as 'Selection mode' isn't enabled - see the next instruction!)_",
                        qupath.getToolManager().getToolAction(PathTools.RECTANGLE),
                        qupath.getToolManager().getToolAction(
                                qupath.getToolManager().getTools()
                                        .stream()
                                        .filter(t -> t.getName().equals("Wand"))
                                        .findFirst()
                                        .orElse(PathTools.BRUSH))),
                createToolbarInstruction(
                        "Selection mode",
                        "Toggle 'Selection mode'.\n\n" +
                                "This switches the behavior of the drawing tools, so that they " +
                                "select objects instead of drawing new ones.\n\n" +
                                "> **Tip:** By default, selected objects are shown in _yellow_. " +
                                "You can change this behavior in the 'Preferences...'",
                        qupath.getToolManager().getSelectionModeAction()),
                createToolbarInstruction(
                        "Brightness/Contrast dialog",
                        "Open the dialog to adjust the brightness and contrast of the image.\n\n" +
                                "You can also use this to switch between different channels, or change " +
                                "the colors used to display channels for some image types.",
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
                        "Zoom-to-fit",
                        "Change the magnification of the current image so that it fits within the current viewer.",
                        qupath.getViewerActions().ZOOM_TO_FIT),
                createToolbarInstruction(
                        "Show annotations",
                        "Toggle the visibility of annotations in the viewer.",
                        qupath.getOverlayActions().SHOW_ANNOTATIONS),
                createToolbarInstruction(
                        "Show detections",
                        "Toggle the visibility of detections (e.g. cells) in the viewer.",
                        qupath.getOverlayActions().SHOW_DETECTIONS),
                createToolbarInstruction(
                        "Show overview",
                        "Toggle the visibility of the overview image in the viewer.",
                        qupath.getViewerActions().SHOW_OVERVIEW)
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
        var page = TourUtils.createPage(item);
//        page.setOnKeyReleased(this::handlePageKeyReleased);
        var nodesToHighlight = item.getNodes();
        if (!nodesToHighlight.isEmpty()) {
            Platform.runLater(() -> {
                highlightNodes(nodesToHighlight);
                stage.requestFocus();
            });
        }
        return page;
    }

    private Stage createStage() {
       var stage = new Stage();
        stage.initOwner(qupath.getStage());
        stage.initModality(Modality.NONE);
        stage.setAlwaysOnTop(true); // It'll also be on top of other applications!
        stage.setTitle("QuPath Tour");
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
     * @param title a short title
     * @param text the main text to display
     * @param actions the actions to highlight; we'll look for these in the toolbar
     * @return
     */
    static TourItem createToolbarInstruction(String title, String text, Action... actions) {
        var items = QuPathGUI.getInstance().getToolBar().getItems()
                .stream()
                .filter(node -> containsActionProperty(node, actions))
                .toList();
        return new TourItem(title, text, items);
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

    private static boolean containsActionProperty(Node node, Action... actions) {
        for (var action : actions) {
            if (node.getProperties().containsValue(action))
                return true;
        }
        return false;
    }



    /**
     * Highlight a node to help the user find it.
     * @param nodes
     */
    void highlightNodes(List<? extends Node> nodes) {
        if (!Platform.isFxApplicationThread()) {
            Platform.runLater(() -> highlightNodes(nodes));
            return;
        }
        highlight.highlightNodes(nodes);
    }


}
