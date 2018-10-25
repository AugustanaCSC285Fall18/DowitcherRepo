package application;

import java.awt.Point;
import java.awt.Shape.*;
import java.io.File;
import java.io.FileInputStream;

import java.io.FileNotFoundException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
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
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import utils.TimeUtils;
import utils.UtilsForOpenCV;
import datamodel.ProjectData;
import datamodel.TimePoint;
import datamodel.Video;

import org.opencv.core.Mat;
import org.opencv.videoio.Videoio;

import javafx.scene.shape.Rectangle;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

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
	private TextField actualHeightTextField;
	@FXML
	private TextField actualWidthTextField;
	@FXML
	private Button submitBtn;
	@FXML
	private Label endTimeLabel;
	@FXML
	private MenuButton setOriginMenu;
	@FXML
	private MenuItem topLeft;
	@FXML
	private MenuItem bottomLeft;
	@FXML
	private MenuItem center;

	private ProjectData projectData;

	// a timer for acquiring the video stream
	// private ScheduledExecutorService timer;
	// private VideoCapture projectData.getVideo().getVidCap();
	private String fileName = null;
	private int curFrameNum;
	private int numFrame;

	/*
	 * private static int start; private static int end; private static int
	 * numChick; private int pixelPerCm;
	 */

	private double startX;
	private double startY;
	private double endX;
	private double endY;
	private double ratio;
	private Video vid;
	private Rectangle bound = new Rectangle();

	@FXML
	public void initialize() {
		currentFrameWrapper.getChildren().add(bound);
		setOriginMenu.setDisable(true);
	}

	@FXML
	private void handleSubmit() throws Exception {
		if (!startTime.getText().equals("") && !endTime.getText().equals("") && !numChicks.getText().equals("") 
				&& !actualWidthTextField.getText().equals("") && !actualHeightTextField.getText().equals("")  && !(bound.getWidth() == 0)) {
			if (TimeUtils.convertMinutesToSeconds(startTime.getText()) > 0
					&& TimeUtils.convertMinutesToSeconds(endTime.getText()) <= numFrame
					&& Integer.parseInt(numChicks.getText()) > 0) {
				vid.setStartFrameNum(vid.convertSecondsToFrameNums(TimeUtils.convertMinutesToSeconds(startTime.getText())));
				vid.setEndFrameNum(projectData.getVideo().convertSecondsToFrameNums(TimeUtils.convertMinutesToSeconds(endTime.getText())));
				projectData.setChickNum(Integer.parseInt(numChicks.getText()));
				double ratio = vid.calculateRatio(currentFrameImage.getFitWidth(), currentFrameImage.getFitHeight());
				vid.getArenaBounds().setRect(startX * ratio,startY * ratio,endX * ratio - startX * ratio,
						endY * ratio - startY * ratio);				
				vid.setXPixelsPerCm(Integer.parseInt(actualWidthTextField.getText()));
				vid.setYPixelsPerCm(Integer.parseInt(actualHeightTextField.getText()));
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
		vid = projectData.getVideo();
		startVideo();
		currentFrameArea.appendText("Current time: (0:00)\t Current frame: 0\n");
		runSliderSeekBar();

	}

	protected void startVideo() {
		updateFrameView();
		numFrame = projectData.getVideo().getTotalNumFrames();
		endTimeLabel.setText(vid.secondsToString(numFrame));
		sliderSeekBar.setDisable(false);
		sliderSeekBar.setMax(vid.getEndFrameNum() - 1);
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
		if (vid.getVidCap().isOpened()) {
			try {
				// read the current frame
				this.vid.getVidCap().read(frame);

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
				currentFrameArea.appendText("Current time: (" + projectData.getVideo().secondsToString(curFrameNum)
						+ ")\tCurent frame: " + curFrameNum + "\n");
				vid.getVidCap().set(Videoio.CAP_PROP_POS_FRAMES, curFrameNum - 1);
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
			if(e.getX() > currentFrameImage.getFitWidth()) {
				endX = currentFrameImage.getFitWidth();
			}else if( e.getY() > currentFrameImage.getFitHeight()) {
				endY = currentFrameImage.getFitHeight();
			}else {
				endX = e.getX();
				endY = e.getY();
			}
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
		setOriginMenu.setDisable(false);
	}
	
	@FXML
	private void handleOriginTopLeft() {
		double ratio = vid.calculateRatio(currentFrameImage.getFitWidth(), currentFrameImage.getFitHeight());
		vid.setOrigin(new Point((int) (startX * ratio), (int) (startY * ratio)));
	}
	
	@FXML
	private void handleOriginBottomLeft() {
		double ratio = vid.calculateRatio(currentFrameImage.getFitWidth(), currentFrameImage.getFitHeight());
		vid.setOrigin(new Point((int) (startX * ratio), (int) ((startY + bound.getHeight()) * ratio)));
	}
	
	@FXML
	private void handleOriginCenter() {
		double ratio = vid.calculateRatio(currentFrameImage.getFitWidth(), currentFrameImage.getFitHeight());
		vid.setOrigin(new Point((int) ((startX + (bound.getWidth() / 2)) * ratio), (int) ((startY + (bound.getHeight() / 2)) * ratio)));

	}

	private void updateFrameView() {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				// effectively grab and process a single frame
				Mat frame = grabFrame();
				// convert and show the frame
				Image imageToShow = UtilsForOpenCV.matToJavaFXImage(frame);
				currentFrameImage.setImage(imageToShow);
			}
		});

	}
}