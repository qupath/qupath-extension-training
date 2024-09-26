package qupath.ext.training.ui;

import ij.gui.GUI;
import javafx.scene.Scene;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qupath.lib.common.Version;
import qupath.lib.gui.QuPathGUI;
import qupath.lib.gui.extensions.GitHubProject;
import qupath.lib.gui.extensions.QuPathExtension;
import qupath.lib.gui.scripting.QPEx;
import qupath.lib.objects.PathObject;

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
		addMenuItem(qupath);
	}
	private void addMenuItem(QuPathGUI qupath) {
		var menu = qupath.getMenu("Extensions>" + EXTENSION_NAME, true);
		MenuItem menuItem = new MenuItem("My menu item");
		menuItem.setOnAction(e -> createMCQ());
		menu.getItems().add(menuItem);
	}

	private void createMCQ() {
		var mcq = new MultipleChoiceQuestion(
				"If you guess on this question, what is the probability you will get it right?",
				"",
				new MultipleChoiceQuestion.MCQOption("25%", "There is no correct answer!"),
				new MultipleChoiceQuestion.MCQOption("50%", "There is no correct answer!"),
				new MultipleChoiceQuestion.MCQOption("0%", "There is no correct answer!"),
				new MultipleChoiceQuestion.MCQOption("25%", "There is no correct answer!"));
		Stage s = new Stage();
		GridPane gp = new GridPane();
		gp.addRow(0, mcq.getPane());
		var gq = new GUIQuestion("Select all annotations", () -> {
			var selected = QPEx.getSelectedObjects();
			if (selected == null) {
				return "No objects selected!";
			}
			if (!selected.stream().allMatch(PathObject::isAnnotation)) {
				return "Some non-annotation objects selected.";
			}
			if (selected.containsAll(QPEx.getAnnotationObjects())) {
				return "";
			}
			return "Not all annotations are selected";
		});
		gp.addRow(1, gq.getPane());

		var sq = new ScriptQuestion("Select all detections", () -> {
			var selected = QPEx.getSelectedObjects();
			if (selected == null) {
				return "No objects selected!";
			}
			if (!selected.stream().allMatch(PathObject::isDetection)) {
				return "Some non-detection objects selected.";
			}
			if (selected.containsAll(QPEx.getDetectionObjects())) {
				return "";
			}
			return "Not all detections are selected";
		});
		gp.addRow(2, sq.getPane());

		Scene ss = new Scene(gp);

		s.setScene(ss);
		s.show();
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
