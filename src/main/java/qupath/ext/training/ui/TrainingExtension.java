package qupath.ext.training.ui;

import javafx.stage.Stage;
import org.controlsfx.control.action.Action;
import org.controlsfx.glyphfont.FontAwesome;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qupath.ext.training.ui.tour.GuiTourCommand;
import qupath.lib.common.Version;
import qupath.lib.gui.QuPathGUI;
import qupath.lib.gui.actions.ActionTools;
import qupath.lib.gui.extensions.GitHubProject;
import qupath.lib.gui.extensions.QuPathExtension;
import qupath.lib.gui.tools.IconFactory;

import java.util.ResourceBundle;


public class TrainingExtension implements QuPathExtension, GitHubProject {
	
	private static final Logger logger = LoggerFactory.getLogger(TrainingExtension.class);

	private static final ResourceBundle resources = ResourceBundle.getBundle("qupath.ext.training.ui.strings");

	/**
	 * Display name for your extension
	 */
	private static final String EXTENSION_NAME = resources.getString("ext.title");

	/**
	 * Short description, used under 'Extensions > Installed extensions'
	 */
	private static final String EXTENSION_DESCRIPTION = resources.getString("ext.description");

	/**
	 * QuPath version that the extension is designed to work with.
	 * This allows QuPath to inform the user if it seems to be incompatible.
	 */
	private static final Version EXTENSION_QUPATH_VERSION = Version.parse("v0.6.0");

	/**
	 * GitHub repo that your extension can be found at.
	 * This makes it easier for users to find updates to your extension.
	 * If you don't want to support this feature, you can remove
	 * references to GitHubRepo and GitHubProject from your extension.
	 */
	private static final GitHubRepo EXTENSION_REPOSITORY = GitHubRepo.create(
			EXTENSION_NAME, "qupath", "qupath-extension-training");

	/**
	 * Flag whether the extension is already installed (might not be needed... but we'll do it anyway)
	 */
	private boolean isInstalled = false;

	/**
	 * Create a stage for the extension to display
	 */
	private Stage stage;

	@Override
	public void installExtension(QuPathGUI qupath) {
		if (isInstalled) {
			logger.debug("{} is already installed", getName());
			return;
		}
		isInstalled = true;

		var tour = new GuiTourCommand(qupath);
		var action = new Action("User interface tour", e -> tour.run());
		action.setGraphic(IconFactory.createNode(FontAwesome.Glyph.MAP_MARKER, 16));
		var item = ActionTools.createMenuItem(action);
		qupath.getMenu("Help", true).getItems().addFirst(item);
		qupath.getToolBar().getItems().add(ActionTools.createButtonWithGraphicOnly(action));
	}

	@Override
	public String getName() {
		return EXTENSION_NAME;
	}

	@Override
	public String getDescription() {
		return EXTENSION_DESCRIPTION;
	}
	
	@Override
	public Version getQuPathVersion() {
		return EXTENSION_QUPATH_VERSION;
	}

	@Override
	public GitHubRepo getRepository() {
		return EXTENSION_REPOSITORY;
	}
}
