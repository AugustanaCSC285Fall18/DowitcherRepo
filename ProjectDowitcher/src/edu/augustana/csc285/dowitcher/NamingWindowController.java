package edu.augustana.csc285.dowitcher;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

import datamodel.ProjectData;
import datamodel.Video;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import utils.UtilsForOpenCV;

public class NamingWindowController {
	@FXML
	private Button submitBtn;
	@FXML
	private ImageView currentFrameImage;
	private String fileName;
	private int start;
	private int end;
	private VideoCapture capture = new VideoCapture();
	private double numFrame;
	
	@FXML
	public void initialize() {
		
	}

	@FXML
	public void handleSubmit() throws IOException {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("AutoTrackWindow.fxml"));
		BorderPane root = (BorderPane) loader.load();
		AutoTrackWindowController autoController = loader.getController();
		Scene nextScene = new Scene(root, root.getPrefWidth(), root.getPrefHeight());
		nextScene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
		Stage primary = (Stage) submitBtn.getScene().getWindow();
		primary.setScene(nextScene);
		autoController.loadVideo(fileName);

		autoController.setTextFieldStartFrame(start);
		autoController.setTextFieldEndFrame(end);
	}

	protected void startVideo(String fileName, int start, int end) {
		this.fileName = fileName;
		this.start = start;
		this.end = end;
		// start the video capture
		this.capture .open(fileName);
		numFrame = this.capture.get(Videoio.CV_CAP_PROP_FRAME_COUNT);
		updateFrameView();		
	}

	/**
	 * Get a frame from the opened video stream (if any)
	 *
	 * @return the {@link Mat} to show
	 */
	private Mat grabFrame() {
		// init everything
		Mat frame = new Mat();

		// check if the capture is open
		if (this.capture.isOpened()) {
			try {
				// read the current frame
				this.capture.read(frame);

				// if the frame is not empty, process it to black and white color

				// if (!frame.empty()) { Imgproc.cvtColor(frame, frame, Imgproc.COLOR_BGR2GRAY);
				// }

			} catch (Exception e) {
				// log the error
				System.err.println("Exception during the image elaboration: " + e);
			}
		}

		return frame;
	}
	

	private void updateFrameView() {
		Platform.runLater(new Runnable() {
			

			@Override
			public void run() {
				// effectively grab and process a single frame
				Mat frame = grabFrame();
				// convert and show the frame
				Image imageToShow = Utils.mat2Image(frame);
				currentFrameImage.setImage(imageToShow);
			}
		});

	}
}
