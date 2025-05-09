package qupath.ext.training.ui.tour;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.controlsfx.control.action.Action;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qupath.fx.controls.tour.GuiTour;
import qupath.fx.controls.tour.TourItem;
import qupath.lib.gui.QuPathGUI;
import qupath.lib.gui.viewer.tools.PathTools;

import java.util.List;
import java.util.ResourceBundle;

/**
 * A command to run a tour of the QuPath user interface.
 * <p>
 * This is intended to help new users understand what is going on quickly.
 */
public class GuiTourCommand implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(GuiTourCommand.class);

    private static final ResourceBundle resources = ResourceBundle.getBundle("qupath.ext.training.ui.tour");

    private final QuPathGUI qupath;

    private GuiTour tour;
    private Stage stage;

    private String tourStyle = GuiTour.STYLE_HIGHLIGHT_CSS;

    public GuiTourCommand(QuPathGUI qupath) {
        this.qupath = qupath;
    }

    private void initialize() {
        this.tour = new GuiTour();
        this.tour.getStyleClass().add(tourStyle);
        var items = createItems(qupath);
        this.tour.getItems().setAll(items);
        this.stage = createStage();
    }

    /**
     * Create all the items for the main tour of the QuPath GUI.
     * We have to do a bit of work to find the UI components, since they weren't written with this in mind...
     * @param qupath
     * @return
     */
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
                        qupath.getToolManager().getTools()
                                .stream()
                                .filter(t -> t != PathTools.POINTS && t != PathTools.MOVE)
                                .map(p -> qupath.getToolManager().getToolAction(p))
                                .toArray(Action[]::new)),
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

                createItem(
                        "toolbar.opacity-slider",
                        qupath.getToolBar().lookup("#opacitySlider")),

                createItem(
                        "toolbar.measurement-tables",
                        qupath.getToolBar().lookup("#measurementTablesMenuButton")),

                createToolbarItem(
                        "toolbar.script-editor",
                        qupath.getAutomateActions().SCRIPT_EDITOR),

                createItem(
                        "toolbar.viewer-menubutton",
                        qupath.getToolBar().lookup("#viewerMenuButton")),

                createToolbarItem(
                        "toolbar.help",
                        qupath.getCommonActions().HELP_VIEWER
                ),
                createToolbarItem(
                        "toolbar.log",
                        qupath.getCommonActions().SHOW_LOG
                ),
                createToolbarItem(
                        "toolbar.prefs",
                        qupath.getCommonActions().PREFERENCES
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


    private Stage createStage() {
       var stage = new Stage();
        stage.initOwner(qupath.getStage());
        stage.initModality(Modality.NONE);
        stage.setAlwaysOnTop(GuiTour.STYLE_HIGHLIGHT_OVERLAY.equals(tourStyle)); // If true, it'll also be on top of other applications!
        stage.setTitle(getTitle());
        var scene = new Scene(tour);
        stage.setScene(scene);
        return stage;
    }

    /**
     * Get the title to display for the QuPath tour.
     * @return
     */
    public String getTitle() {
        return resources.getString("title");
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
        return MarkdownTourItem.create(resources, key, items);
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
        return MarkdownTourItem.create(resources, key, items);
    }

    /**
     * Create a UI component instruction for specific nodes.
     * @param key the resource bundle key
     * @param nodes the specific nodes (e.g. buttons) to highlight
     * @return
     */
    private static TourItem createItem(String key, Node... nodes) {
        return MarkdownTourItem.create(resources, key, List.of(nodes));
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

}
