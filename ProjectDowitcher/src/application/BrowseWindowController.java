
package application;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import datamodel.ProjectData;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;

public class BrowseWindowController {
	@FXML
	private Button browseBtn;
	@FXML
	private Button btnLoad;
	
	private String fileName;

	

	@FXML
	public void handleBrowse() throws IOException {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Open Image File");
		Window mainWindow = browseBtn.getScene().getWindow(); 
		File chosenFile = fileChooser.showOpenDialog(mainWindow);
		if (chosenFile == null) {
			return;
		}
		fileName = chosenFile.toURI().toString();
		FXMLLoader loader = new FXMLLoader(getClass().getResource("CalibrationWindow.fxml"));
		AnchorPane root = (AnchorPane) loader.load();
		CalibrationWindowController calController = loader.getController();
		Scene nextScene = new Scene(root, root.getPrefWidth(), root.getPrefHeight());
		nextScene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
		Stage primary = (Stage) browseBtn.getScene().getWindow();
		primary.setScene(nextScene);
		calController.start(fileName);
	}
	
	@FXML
	public void handleLoad() throws Exception {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Open Project File");
		Window mainWindow = btnLoad.getScene().getWindow(); 
		File chosenFile = fileChooser.showOpenDialog(mainWindow);
		if (chosenFile == null) {
			return;
		}
		ProjectData project = ProjectData.loadFromFile(chosenFile);
		FXMLLoader loader = new FXMLLoader(getClass().getResource("WorkingWindow.fxml"));
		BorderPane root = (BorderPane) loader.load();
		WorkingWindowController workController = loader.getController();
		Scene nextScene = new Scene(root, root.getPrefWidth(), root.getPrefHeight());
		nextScene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
		Stage primary = (Stage) btnLoad.getScene().getWindow();
		primary.setScene(nextScene);
		workController.initializeWithStage(primary);
		workController.loadVideo(project.getVideo().getFilePath(), project);
	}
}
