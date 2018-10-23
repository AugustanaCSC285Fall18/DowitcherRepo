package application;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import utils.Utils;

import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

import datamodel.AnimalTrack;
import datamodel.ProjectData;
import datamodel.TimePoint;

//import application.TimePoint;

import java.util.ArrayList;
import java.util.List;
import org.opencv.core.Mat;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

public class ManualTrackWindowController {

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
	private TextField startFrame;
	@FXML
	private TextField endFrame;
	@FXML
	private TextField numChicks;
	@FXML
	private Button finishManualTrackingBtn;
	@FXML
	private MenuButton chooseChickMenu;
	
	@FXML
	private Button setNameButton;
	@FXML
	private TextField setNameTextField;


	// a timer for acquiring the video stream
	// private ScheduledExecutorService timer;
	//private VideoCapture projectData.getVideo().getVidCap() = new VideoCapture();
	private String fileName = null;
	private int curFrameNum;
	private double numFrame;
	private final int incrementSeconds = 1;

//	private int start;
//	private int end;
//	private int pixelPerCm;

	private ProjectData projectData;
	private ArrayList<TimePoint> listTimePoints = new ArrayList<>();
	private List<AnimalTrack> manualTrackSegments = new ArrayList<AnimalTrack>();
	public Color[] colorList = new Color[] { Color.RED, Color.ORANGE, Color.YELLOW, Color.GREEN, Color.BLUE,
			Color.BLACK, Color.PURPLE };
	public ArrayList<Circle> circleList = new ArrayList<Circle>();
	private List<MenuItem> menuItemOption = new ArrayList<MenuItem>();
	private Circle circle;

	@FXML
	public void initialize() {
		sliderSeekBar.setDisable(true);
		jumpToFrameArea.setDisable(true);
		runSliderSeekBar();

	}

/*	@FXML
	private void handleSubmit() {
		start = Integer.parseInt(startFrame.getText());
		end = Integer.parseInt(endFrame.getText());
		projectData.getChickNum() = Integer.parseInt(numChicks.getText());

	}*/
	
	@FXML
	private void handleFinishManualTracking() {
		trackingComplete(manualTrackSegments);
	}
	
	@FXML
	private void handleSetNameButton() {
		
		String oldName = chooseChickMenu.getText();
		if (setNameTextField.getText() != null && !oldName.equals("Choose Chick To Track:") ) {
			String newName =  setNameTextField.getText();
			for (int i = 0; i < menuItemOption.size(); i++) {
				if (oldName.equals(menuItemOption.get(i).getText())){
					menuItemOption.get(i).setText(newName); 
					chooseChickMenu.setText(newName);
					setNameTextField.clear();
				}
			}	
		}
		
	}


	public void start(String fName, ProjectData projectData) {
		this.projectData = projectData;
		this.fileName = fName;
		startVideo();
		currentFrameArea.appendText("Current time: " +  projectData.getVideo().secondsToString(projectData.getVideo().getStartFrameNum()) + "\n");
		runSliderSeekBar();
		runJumpTo();
	}

	protected void startVideo() {

		// start the video capture
		this.projectData.getVideo().getVidCap().open(fileName);
		// = this.capture.get(Videoio.CV_CAP_PROP_FRAME_COUNT);
		projectData.getVideo().getVidCap().set(Videoio.CAP_PROP_POS_FRAMES, projectData.getVideo().getStartFrameNum());
		totalFrameArea.appendText("Total time: " + projectData.getVideo().secondsToString(((int) projectData.getVideo().getEndFrameNum() - (int) projectData.getVideo().getStartFrameNum())) + "\n");
		sliderSeekBar.setDisable(false);
		jumpToFrameArea.setDisable(false);
		updateFrameView();
		sliderSeekBar.setMax((int) projectData.getVideo().getEndFrameNum());
		sliderSeekBar.setMin((int) projectData.getVideo().getStartFrameNum());
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
		if (this.projectData.getVideo().getVidCap().isOpened()) {
			try {
				// read the current frame
				this.projectData.getVideo().getVidCap().read(frame);

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
				currentFrameArea.appendText("Current time: " + projectData.getVideo().secondsToString((int) Math.round(newValue.doubleValue())) + "\n");
				curFrameNum = (int) Math.round(newValue.doubleValue());
				projectData.getVideo().getVidCap().set(Videoio.CAP_PROP_POS_FRAMES, curFrameNum - 1);
				updateFrameView();
				currentFrameWrapper.getChildren().removeAll(circleList);
				for (int i = 0; i < listTimePoints.size(); i++) {
					if (curFrameNum == listTimePoints.get(i).getFrameNum()) {
						drawingDot(listTimePoints.get(i).getX(), listTimePoints.get(i).getY(), circleList.get(i).getFill());
					}
				}
				manualTrack();
				chooseChickMenu.setText("Choose Chick To Track:");
				chooseChickMenu.setTextFill(Color.BLACK);
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
						realValue = (int) numFrame - 1;
					}
					currentFrameArea.appendText("Current time: " +projectData.getVideo().secondsToString((int) numFrame) + "\n");
					sliderSeekBar.setValue(realValue);
					curFrameNum = realValue;
					projectData.getVideo().getVidCap().set(Videoio.CAP_PROP_POS_FRAMES, curFrameNum - 1);
					updateFrameView();
					currentFrameWrapper.getChildren().removeAll(circleList);
					for (int i = 0; i < listTimePoints.size(); i++) {
						if (realValue == listTimePoints.get(i).getFrameNum()) {
							drawingDot(listTimePoints.get(i).getX(), listTimePoints.get(i).getY(), circleList.get(i).getFill());
						}
					}
					manualTrack();
					chooseChickMenu.setText("Choose Chick To Track:");
					chooseChickMenu.setTextFill(Color.BLACK);
				} catch (NumberFormatException ex) {
					// ignore it for now
				}
			}

		});

	}

	private void increment() {
		curFrameNum += (int) Math.round(incrementSeconds * projectData.getVideo().getFrameRate());
		currentFrameArea.appendText("Current time: " + projectData.getVideo().secondsToString(curFrameNum) + "\n");
		sliderSeekBar.setValue(curFrameNum);
		projectData.getVideo().getVidCap().set(Videoio.CAP_PROP_POS_FRAMES, curFrameNum - 1);
		updateFrameView();
/*		//currentFrameWrapper.getChildren().removeAll(circleList);
		for (int i = 0; i < listTimePoints.size(); i++) {
			if (curFrameNum == listTimePoints.get(i).getFrameNum()) {
				drawingDot(listTimePoints.get(i).getX(), listTimePoints.get(i).getY(), circleList.get(i).getFill());
			}
		}
		manualTrack();*/
		chooseChickMenu.setText("Choose Chick To Track:");
		chooseChickMenu.setTextFill(Color.BLACK);
	}
	
	
	private String[] createIds(int numberOfChick) {
		String[] listOfIds = new String[numberOfChick];
		for (int i = 0; i < numberOfChick; i++) {
			listOfIds[i] = ("Chick " + (i + 1));
		}
		return listOfIds;
	}

	private void setupChooseChickMenu() {
		String[] names = createIds(projectData.getChickNum());
		for (int i = 0; i < projectData.getChickNum(); i++) {
			MenuItem chick = new MenuItem(names[i]);
			menuItemOption.add(chick);
			menuItemOption.get(i).setId(names[i]);
			MenuItem chickItem = menuItemOption.get(i);
			chooseChickMenu.getItems().add(chickItem);
			AnimalTrack animalTrack = new AnimalTrack(names[i]);
			manualTrackSegments.add(animalTrack);
			//currently, each animalTrack ID in our list of AnimalTracks are 1, 2, 3 ...
			//We'll change this to reflect the names we assign
		}

	}

	private void runChooseChick() {
		for (int i = 0; i < menuItemOption.size(); i++) {
			int temp = i;
			menuItemOption.get(i).setOnAction(e -> {
				chooseChickMenu.setText(menuItemOption.get(temp).getText());
				chooseChickMenu.setTextFill(colorList[temp]);
			});
		}

	}

	private void manualTrack() {
		// the following line allows detection of clicks on transparent
		// parts of the image:
		currentFrameImage.setPickOnBounds(true);
		currentFrameImage.setOnMouseClicked(e -> {
			drawingDot((int) e.getX(), (int) e.getY(), chooseChickMenu.getTextFill());
			//System.out.println(circle.getFill().toString()); Only for testing
			TimePoint positionInfo = new TimePoint(e.getX(), e.getY(), curFrameNum);
			System.out.println(curFrameNum);
			listTimePoints.add(positionInfo); //this needs to be stored into an AnimalTrack or we can directly add to the AnimalTrack
			//System.out.println(circleList.size() + " cirles"); Only for testing
			for (int i = 0; i <= projectData.getChickNum(); i++) {
				if (circle.getFill().equals(colorList[i])) {
					manualTrackSegments.get(i).add(positionInfo);	
					//UpdateTracks(manualTrackSegments.get(i).getFinalTimePoint()); 
					// currently throws error. We need to test this method more.
					System.err.println(manualTrackSegments.get(i).toString());
				}
			}
			
			new java.util.Timer().schedule( //adds in a delay before the time increments to the user can see the circle placement
			        new java.util.TimerTask() {
			            @Override
			            public void run() {
			            	Platform.runLater(() -> increment());
			            }
			        }, 
			        500 //delay time in milliseconds
			);			
			
		});
	}

	private void drawingDot(double xPos, double yPos, Paint paint) {
		circle = new Circle(10);
		circle.setFill(paint);
		circle.setTranslateX(xPos + currentFrameImage.getLayoutX());
		circle.setTranslateY(yPos + currentFrameImage.getLayoutY());
		currentFrameWrapper.getChildren().add(circle);

		String[] names = createIds(projectData.getChickNum());
		for (int i = 0; i <= projectData.getChickNum(); i++) {
			if (paint.equals(colorList[i])) {
				circle.setId(names[i]);
			}
		}
		circleList.add(circle);
		
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
	
	/**
	 * adds TimePoint to end of selected chick's final AnimalTrack and combines that AnimalTrack with an unassigned autotrack segment if one is nearby by examining the first
	 * TimePoint of each unassigned segment 
	 * @param tp newest TimePoint created by user's last circle-placement
	 */
	public void UpdateTracks(TimePoint tp) {
		AnimalTrack selectedChick = projectData.getTracks().get(0);//The 0 will have to be replaced with a way to get a specific chick based on menu item or animalID. Currently it just saves it to chick 1
		selectedChick.add(tp);
		if(projectData.compareManualPointToUnassigned(tp)!=-1) {//test if the TimePoint is nearby an unassigned segment
			int indexOfUnassigned=projectData.compareManualPointToUnassigned(tp);//index of unassigned segment from the list of unassigned segments in ProjectData
			AnimalTrack toBeAssigned =projectData.getUnassignedSegments().get(indexOfUnassigned);
			List<TimePoint> positions = toBeAssigned.getPositions();
			for(int i=0; i<=positions.size(); i++) {
				selectedChick.add(positions.get(i));
			}
			//play video until numFrame of toBeAssigned before manualTracking resumes 
		}
	}
	
	public void trackingComplete(List<AnimalTrack> trackedSegments) {
	//	project.getUnassignedSegments().clear();
	//	project.getUnassignedSegments().addAll(trackedSegments);

		System.out.println("Printing new manual track segments");
		for (AnimalTrack track: trackedSegments) {
			System.out.println(track);
			System.out.println("  " + track.getPositions());
		}
//		Platform.runLater(() -> { 
//			progressAutoTrack.setProgress(1.0);
//			btnAutotrack.setText("Start auto-tracking");
//		});	
		//I copied the Platform.runLater part from Stonedahl's AutoTrackWindow- Anthony
		
	}
}
