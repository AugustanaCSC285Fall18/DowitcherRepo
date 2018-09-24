package edu.augustana.csc285.dowitcher;

import java.io.File;
import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;

public class BrowseWindowController {
	@FXML
	private Button browseBtn;

	@FXML
	public void handleBrowse() throws IOException {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Open Image File");
		Window mainWindow = browseBtn.getScene().getWindow();
		File chosenFile = fileChooser.showOpenDialog(mainWindow);

		if (chosenFile == null) {
			return;
		}

		String fileName = chosenFile.toURI().toString();

		FXMLLoader loader = new FXMLLoader(getClass().getResource("MainWindow.fxml"));
		AnchorPane root = (AnchorPane) loader.load();
		MainWindowController mainController = loader.getController();
		Scene nextScene = new Scene(root, root.getPrefWidth(), root.getPrefHeight());
		nextScene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());

		Stage primary = (Stage) browseBtn.getScene().getWindow();
		primary.setScene(nextScene);

		mainController.start(fileName);
		

	}
}
