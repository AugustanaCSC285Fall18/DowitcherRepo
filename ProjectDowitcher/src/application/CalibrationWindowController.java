package application;

import java.awt.Point;

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
import application.WorkingWindowController;
import utils.TimeUtils;
import utils.UtilsForOpenCV;
import datamodel.ProjectData;
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

	private String fileName = null;
	private int curFrameNum;
	private int numFrame;

	private double startX;
	private double startY;
	private double endX;
	private double endY;
	private Video vid;
	private Rectangle bound = new Rectangle();

	
	@FXML
	public void initialize() {
		currentFrameWrapper.getChildren().add(bound);
		setOriginMenu.setDisable(true);
	}

	/**
	 * Handles when the submit button is clicked. Makes sure that all information the user needs to input prior to tacking the chick is input here before moving forward. 
	 * Then saves all the user input into various fields in the video class for later usage and loads in the video in the WorkingWindow.
	 * @throws Exception
	 */
	@FXML
	private void handleSubmit() throws Exception {
		if (!startTime.getText().equals("") && !endTime.getText().equals("") && !numChicks.getText().equals("") 
				&& !actualWidthTextField.getText().equals("") && !actualHeightTextField.getText().equals("")  && !(bound.getWidth() == 0)) {
			if (TimeUtils.convertMinutesToSeconds(startTime.getText()) > 0
					&& TimeUtils.convertMinutesToSeconds(endTime.getText()) <= numFrame
					&& Integer.parseInt(numChicks.getText()) > 0) {
				vid.setStartFrameNum(vid.convertSecondsToFrameNums(TimeUtils.convertMinutesToSeconds(startTime.getText())));
				vid.setEndFrameNum(vid.convertSecondsToFrameNums(TimeUtils.convertMinutesToSeconds(endTime.getText())));
				projectData.setChickNum(Integer.parseInt(numChicks.getText()));
				double ratio = vid.calculateRatio(currentFrameImage.getFitWidth(), currentFrameImage.getFitHeight());
				vid.getArenaBounds().setRect(startX * ratio,startY * ratio,endX * ratio - startX * ratio,
						endY * ratio - startY * ratio);				
				vid.setXPixelsPerCm(Integer.parseInt(actualWidthTextField.getText()));
				vid.setYPixelsPerCm(Integer.parseInt(actualHeightTextField.getText()));
				FXMLLoader loader = new FXMLLoader(getClass().getResource("WorkingWindow.fxml"));
				BorderPane root = (BorderPane) loader.load();
				WorkingWindowController workController = loader.getController();
				Scene nextScene = new Scene(root, root.getPrefWidth(), root.getPrefHeight());
				nextScene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
				Stage primary = (Stage) submitBtn.getScene().getWindow();
				primary.setScene(nextScene);
				workController.initializeWithStage(primary);
				workController.loadVideo(fileName, projectData);
			
			}
		}
	}

	/**
	 * This video takes in the file name of a video, calls startVideo and sets the time the video starts on as 0:00. 
	 * @param fName - is the name of the video file.
	 * @throws FileNotFoundException - if the video file is not found.
	 */
	public void start(String fName) throws FileNotFoundException {
		this.fileName = fName;
		projectData = new ProjectData(fileName);
		vid = projectData.getVideo();
		startVideo();
		currentFrameArea.appendText("Current time: (0:00)");
		runSliderSeekBar();

	}

	/**
	 * Gets the total number of frames in the video and displays it in MM:SS format to the user. 
	 * Sets the boundary of the slider part to match the length of the video.
	 */
	protected void startVideo() {
		updateFrameView();
		numFrame = projectData.getVideo().getTotalNumFrames();
		endTimeLabel.setText(vid.convertFramesToString(numFrame));
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

			} catch (Exception e) {
				// log the error
				System.err.println("Exception during the image elaboration: " + e);
			}
		}

		return frame;
	}

	/**
	 * Creates a listener on the slider bar to update the current frame of the video and the current time of the video for the user (MM:SS)
	 */
	private void runSliderSeekBar() {

		sliderSeekBar.valueProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				curFrameNum = (int) Math.round(newValue.doubleValue());
				currentFrameArea.setText("Current time: (" + projectData.getVideo().convertFramesToString(curFrameNum)
						+ ")");
				vid.getVidCap().set(Videoio.CAP_PROP_POS_FRAMES, curFrameNum - 1);
				updateFrameView();
			}

		});
	}

	/**
	 * Sends the X and Y coordinates where the mouse was clicked and where it finished being dragged to to the drawRectangle method.
	 */
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

	/**
	 * Takes in the staring and ending X and Y values for where the mouse was clicked and dragged to and draws the physical rectangle for the user to see.
	 * @param startX - from first click
	 * @param startY - from first click
	 * @param endX - from where it finished dragging
	 * @param endY - from where it finished dragging 
	 */
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
