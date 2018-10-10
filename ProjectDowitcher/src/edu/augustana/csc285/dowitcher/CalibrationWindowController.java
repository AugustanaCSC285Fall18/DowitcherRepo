package edu.augustana.csc285.dowitcher;

import edu.augustana.csc285.dowitcher.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.ScheduledExecutorService;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;

import datamodel.AnimalTrack;
import datamodel.ProjectData;
import datamodel.TimePoint;

//import application.TimePoint;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.util.Duration;

public class CalibrationWindowController {

	@FXML
	private AnchorPane appArea;

	@FXML
	private AnchorPane currentFrameWrapper;

	@FXML
	private ImageView currentFrameImage;
	@FXML
	private Slider sliderSeekBar;
	@FXML
	private TextArea currentFrameArea;

	@FXML
	private TextField startTime;
	@FXML
	private TextField endTime;
	@FXML
	private TextField numChicks;
	@FXML
	private Button submitBtn;
	@FXML
	private Label endTimeLabel;
	
	private ProjectData projectData;
	

	// a timer for acquiring the video stream
	// private ScheduledExecutorService timer;
	//private VideoCapture projectData.getVideo().getVidCap();
	private String fileName = null;
	private int curFrameNum;
	private int numFrame;

/*	private static int start;
	private static int end;
	private static int numChick;
	private int pixelPerCm;*/
	
	private double startX;
	private double startY;
	private double endX;
	private double endY;
	private Rectangle bound = new Rectangle();

	

	@FXML
	public void initialize() {		
		currentFrameWrapper.getChildren().add(bound);
	}

	@FXML
	private void handleSubmit() throws Exception {
		if (!startTime.getText().equals("") && !endTime.getText().equals("")&& !numChicks.getText().equals("")) {
			if(Integer.parseInt(startTime.getText()) > 0 && Integer.parseInt(endTime.getText()) <= numFrame && Integer.parseInt(numChicks.getText()) > 0) {
				projectData.getVideo().setStartFrameNum(projectData.getVideo().convertSecondsToFrameNums(Integer.parseInt(startTime.getText())));
				projectData.getVideo().setEndFrameNum(projectData.getVideo().convertSecondsToFrameNums(Integer.parseInt(endTime.getText())));
				projectData.setChickNum(Integer.parseInt(numChicks.getText()));
				FXMLLoader loader = new FXMLLoader(getClass().getResource("AutoTrackWindow.fxml"));
				BorderPane root = (BorderPane) loader.load();
				AutoTrackWindowController autoController = loader.getController();
				Scene nextScene = new Scene(root, root.getPrefWidth(), root.getPrefHeight());
				nextScene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
				Stage primary = (Stage) submitBtn.getScene().getWindow();
				primary.setScene(nextScene);
				autoController.loadVideo(fileName, projectData);
			}
		}
	}

	public void start(String fName) throws FileNotFoundException {
		this.fileName = fName;
		projectData = new ProjectData(fileName);
		startVideo();
		currentFrameArea.appendText("Current time: (0:00)\t Current frame: 0\n");
		runSliderSeekBar();
		
		
	}
	

	protected void startVideo() {
		updateFrameView();
		numFrame = projectData.getVideo().getTotalNumFrames();
		endTimeLabel.setText(projectData.getVideo().secondsToString(numFrame));
		sliderSeekBar.setDisable(false);
		sliderSeekBar.setMax(projectData.getVideo().getEndFrameNum()-1);
		drawVideoBound();
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
		if (projectData.getVideo().getVidCap().isOpened()) {
			try {
				// read the current frame
				this.projectData.getVideo().getVidCap().read(frame);

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

	private void runSliderSeekBar() {

		sliderSeekBar.valueProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				curFrameNum = (int) Math.round(newValue.doubleValue());
				currentFrameArea.appendText("Current time: (" + projectData.getVideo().secondsToString(curFrameNum)+ ")\tCurent frame: " +curFrameNum+"\n");
				projectData.getVideo().getVidCap().set(Videoio.CAP_PROP_POS_FRAMES, curFrameNum - 1);
				updateFrameView();
			}

		});
	}
	
	private void drawVideoBound() {
		currentFrameImage.setPickOnBounds(true);
		currentFrameImage.setOnMousePressed(e -> {
			startX = e.getX();
			startY = e.getY();
		});
		
		currentFrameImage.setOnMouseDragged(e -> {
			endX = e.getX();
			endY = e.getY();
			drawRectangle(startX, startY, endX, endY);
		});
	
	}
	
	private void drawRectangle(double startX, double startY, double endX, double endY) {
		bound.setFill(null);
		bound.setStroke(Color.RED);
		bound.setTranslateX(startX + currentFrameImage.getLayoutX());
		bound.setTranslateY(startY + currentFrameImage.getLayoutY());
		bound.setWidth(endX - startX);
		bound.setHeight(endY - startY);
		projectData.getVideo().getArenaBounds().setRect(startX, startY, endX - startX, endY - startY);
		
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
