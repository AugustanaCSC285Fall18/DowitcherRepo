package edu.augustana.csc285.dowitcher;

import edu.augustana.csc285.dowitcher.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.concurrent.ScheduledExecutorService;

import javafx.fxml.FXML;
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
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

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

public class MainWindowController {
	
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
	private TextArea totalFrameArea;
	@FXML
	private TextField jumpToFrameArea;
	@FXML
	private MenuButton chooseChickMenu;
	

	

	// a timer for acquiring the video stream
	// private ScheduledExecutorService timer;
	private VideoCapture capture = new VideoCapture();
	private String fileName = null;
	private int curFrameNum;
	private double numFrame;
	//private Circle circle = new Circle(10);
	private ArrayList<edu.augustana.csc285.dowitcher.TimePoint> list = new ArrayList<TimePoint>();
	private ArrayList<edu.augustana.csc285.dowitcher.AnimalTrack> animalTrackList = new ArrayList<AnimalTrack>();
	public Color[] colorList = new Color[] {Color.RED, Color.ORANGE, Color.YELLOW, Color.GREEN, Color.BLUE, Color.BLACK, Color.PURPLE};
	public ArrayList<Circle> circleList = new ArrayList<Circle>(); 
	private List<MenuItem> menuItemOption = new ArrayList<MenuItem>();
	private Circle circle;
	

	@FXML
	public void initialize() {
		sliderSeekBar.setDisable(true);
		jumpToFrameArea.setDisable(true);

	}

	@FXML
	public void start(String fName) {
		this.fileName = fName;
		startVideo();
		currentFrameArea.appendText("Current frame: 0\n");
		runSliderSeekBar();
		runJumpTo();
	}

	protected void startVideo() {

		// start the video capture
		this.capture.open(fileName);
		numFrame = this.capture.get(Videoio.CV_CAP_PROP_FRAME_COUNT);
		totalFrameArea.appendText("Total frames: " + (int) numFrame + "\n");
		sliderSeekBar.setDisable(false);
		jumpToFrameArea.setDisable(false);
		updateFrameView();
		sliderSeekBar.setMax((int) numFrame);
		setupChooseChickMenu();
		runChooseChick();

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
				/*
				 * if (!frame.empty()) { Imgproc.cvtColor(frame, frame, Imgproc.COLOR_BGR2GRAY);
				 * }
				 */

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
				currentFrameArea.appendText("Current frame: " + ((int) Math.round(newValue.doubleValue())) + "\n");
				curFrameNum = (int) Math.round(newValue.doubleValue());
				capture.set(Videoio.CAP_PROP_POS_FRAMES, curFrameNum - 1);
				updateFrameView();
				currentFrameWrapper.getChildren().removeAll(circleList);
				for (int i = 0; i<list.size(); i++) {
					if (curFrameNum == list.get(i).getFrameNum()) {
						drawingDot(list.get(i).getX(), list.get(i).getY(), circleList.get(i).getFill());
					} 
				} 
				manualTrack();
				
			}

		});
	}

	private void runJumpTo() {

		jumpToFrameArea.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				try {
					int realValue = Integer.parseInt(newValue);
					if (realValue <= 0) {
						realValue = 0;
					}
					if (realValue >= numFrame) {
						realValue = (int) numFrame;
					}
					currentFrameArea.appendText("Current frame: " + (realValue) + "\n");
					sliderSeekBar.setValue(realValue);
					curFrameNum = realValue;
					capture.set(Videoio.CAP_PROP_POS_FRAMES, curFrameNum - 1);
					updateFrameView();
					currentFrameWrapper.getChildren().removeAll(circleList);
					for (int i = 0; i<list.size(); i++) {
						if (realValue == list.get(i).getFrameNum()) {
							drawingDot(list.get(i).getX(), list.get(i).getY(), circleList.get(i).getFill());
						}
					} 
					manualTrack();
					
					
				} catch (NumberFormatException ex) {
					// ignore it for now
				}

			}

		});

	}
	
	private void setupChooseChickMenu() {
		for (int i=0; i< 5; i++) {
			MenuItem chick = new MenuItem("Chick "+(i+1));
			menuItemOption.add(chick);
			menuItemOption.get(i).setId("Chick "+(i+1));
			MenuItem chickItem = menuItemOption.get(i);
			chooseChickMenu.getItems().add(chickItem);
		}
		MenuItem unknownChick = new MenuItem("Chick Unknown");
		menuItemOption.add(unknownChick);
		menuItemOption.get(5).setId("ChickUnknown");
		chooseChickMenu.getItems().add(menuItemOption.get(menuItemOption.size()-1));
		
	}
	
	private void runChooseChick() {
		for (int i=0; i< menuItemOption.size(); i++) {
			int numChick=i;
			menuItemOption.get(i).setOnAction(e -> {
				chooseChickMenu.setText(menuItemOption.get(numChick).getText());
				chooseChickMenu.setTextFill(colorList[numChick]);
			});
		}
		

	}
	
	private void manualTrack() {
        // the following line allows detection of clicks on transparent
        // parts of the image:
		
        currentFrameImage.setPickOnBounds(true);
        currentFrameImage.setOnMouseClicked(e -> {
        	drawingDot((int) e.getX(), (int) e.getY(), chooseChickMenu.getTextFill());
            TimePoint positionInfo = new TimePoint((int) e.getX(), (int) e.getY(), curFrameNum);            
            list.add(positionInfo);
        //    System.out.println(list.toString());
        //    System.out.println(list.size());
            
        });
	}
	
	private void drawingDot(int xPos, int yPos, Paint paint) {
		circle = new Circle(10);
		circle.setFill(paint);
        circle.setTranslateX(xPos+currentFrameImage.getLayoutX());
        circle.setTranslateY(yPos+currentFrameImage.getLayoutY());
        currentFrameWrapper.getChildren().add(circle);
        circleList.add(circle);
        System.out.println(circleList.size() + " cirles");
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