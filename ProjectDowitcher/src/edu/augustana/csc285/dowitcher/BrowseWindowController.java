package edu.augustana.csc285.dowitcher;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class BrowseWindowController {
	@FXML private Button btnNext;
	
	public void handleBrowse() throws IOException  {
	FXMLLoader loader = new FXMLLoader(getClass().getResource("MainWindow.fxml"));
	AnchorPane root = (AnchorPane)loader.load();
	MainWindowController mainController = loader.getController();
	mainController.handleBrowse();
	Scene nextScene = new Scene(root,root.getPrefWidth(),root.getPrefHeight());
	nextScene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
	
	Stage primary = (Stage) btnNext.getScene().getWindow();
	primary.setScene(nextScene);
	}
}
