package core;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javafx.application.Application;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.XYChart;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.Tooltip;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

public class Main extends Application {

	// Matching paths, to validate file extensions
	private PathMatcher matcher = FileSystems.getDefault()
			.getPathMatcher("glob:**.{java,class,txt,log,css}");

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage stage) throws Exception {

		TextArea area = new TextArea();
		area.setWrapText(true);
		area.setTooltip(
				new Tooltip("Drag files here to open them or shift+click the text area to open a file chooser"));

		// The mouse is generating a drag event, but leaves the area.
		area.setOnDragExited(event -> {
			area.setStyle("text-area-background: #FFFFFF");
		});

		// The mouse is generating a drag event, and is over the target area.
		area.setOnDragOver(event -> {
			Dragboard board = event.getDragboard();
			// Here follows a couple of examples of what can be done with the
			// Dragboard
			if (board.hasFiles()) {
				boolean supportedFiles = board.getFiles()
						.stream()
						.map(File::toPath)
						.allMatch(matcher::matches);
				if (supportedFiles) {
					event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
					area.setStyle("text-area-background: #AAFFAA");
				} else {
					area.setStyle("text-area-background: #FFAAAA");
				}
			} else if (event.getGestureSource() != area) {
				event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
				area.setStyle("text-area-background: #AAFFAA");
			}
		});

		// If the setOnDragOver method approves of what the mouse is trying to
		// drop, this method gets called
		area.setOnDragDropped(event -> {
			Dragboard board = event.getDragboard();
			// Depending on the content we act in different ways.
			if (board.hasFiles()) {
				String content = readFiles(board.getFiles());
				area.setText(content);
			} else if (board.hasUrl()) {
				String url = board.getUrl();
				open(url);
			} else if (board.hasString()) {
				area.setText(board.getString());
			}
		});

		// Simple example of a mouse event, similar to what has been done
		// before.
		area.setOnMouseClicked(event -> {
			if (!event.isShiftDown()) {
				return;
			}
			FileChooser fileChooser = new FileChooser();
			fileChooser.setTitle("Open files");
			fileChooser.getExtensionFilters()
					.addAll(new ExtensionFilter("All files", "*.*"), new ExtensionFilter("Text files", "*.txt"));
			List<File> files = fileChooser.showOpenMultipleDialog(stage);
			if (files != null) {
				String content = readFiles(files);
				area.setText(content);
			}
		});

		// Mostly for fun. Demonstrates that JavaFX is a quite capable UI
		// library with a lot built in.
		XYChart<String, Number> exampleChart = ExampleChart.createChart();

		// A splitpane splits a number of nodes with a divider between them.
		// Allows for easy resizing!
		SplitPane splitPane = new SplitPane(area, exampleChart);
		splitPane.setOrientation(Orientation.VERTICAL);

		// A VBox is a layoutmanager which stacks its children vertically. In
		// this case only one child is added.
		VBox box = new VBox();
		box.getChildren()
				.add(splitPane);

		box.setFillWidth(true);
		for (Node n : box.getChildren()) {
			VBox.setVgrow(n, Priority.ALWAYS);
		}

		// Create a new scene with a set size. Also, add the style sheet which
		// later can be used by other components.
		Scene scene = new Scene(box, 1200, 900);
		scene.getStylesheets()
				.add("text_area.css");

		// Finally, create and show the window!
		stage.setTitle("Window");
		stage.setScene(scene);
		stage.show();
	}

	/****************************************************/
	// Convenience methods, nothing related to JavaFX here
	private String readFiles(Collection<File> files) {
		return files.stream()
				.map(this::readFile)
				.collect(Collectors.joining("\n"));
	}

	private String readFile(File file) {
		return readFile(file.toPath());
	}

	private String readFile(Path path) {
		try {
			return String.join("\n", Files.readAllLines(path));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "";
	}

	private boolean open(String url) {
		try {
			return open(new URL(url).toURI());
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return false;
	}

	private boolean open(URI uri) {
		Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
		if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
			try {
				desktop.browse(uri);
				return true;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return false;
	}
}
