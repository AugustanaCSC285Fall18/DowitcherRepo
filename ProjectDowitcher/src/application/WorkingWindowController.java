package application;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Optional;

import org.opencv.core.Mat;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import autotracking.AutoTrackListener;
import autotracking.AutoTracker;
import datamodel.AnimalTrack;
import datamodel.ProjectData;
import datamodel.TimePoint;
import datamodel.Video;
import utils.UtilsForOpenCV;

public class WorkingWindowController implements AutoTrackListener {

	@FXML
	private Button btnBrowse;
	@FXML
	private Canvas videoCanvas;
	@FXML
	private Pane paneHoldingVideoCanvas;
	@FXML
	private Slider sliderVideoTime;
	@FXML
	private TextField textFieldCurFrameNum;

	@FXML
	private ComboBox<String> comboBoxChicks;

	@FXML
	private TextField textfieldStartFrame;
	@FXML
	private TextField textfieldEndFrame;
	@FXML
	private Button btnAutotrack;
	@FXML
	private ProgressBar progressAutoTrack;

	public static final Color[] TRACK_COLORS = new Color[] { Color.RED, Color.BLUE, Color.GREEN, Color.CYAN,
			Color.MAGENTA, Color.BLUEVIOLET, Color.ORANGE };

	private ProjectData project;
	private Stage stage;
	private AutoTracker autotracker;
	private String fileName = null;

	public void initializeWithStage(Stage stage) {
		this.stage = stage;

		// bind it so when the the pane changes width, the canvas matches it
		videoCanvas.widthProperty().bind(paneHoldingVideoCanvas.widthProperty());
		videoCanvas.heightProperty().bind(paneHoldingVideoCanvas.heightProperty());
		videoCanvas.widthProperty().addListener((obs, oldV, newV) -> repaintCanvas());
		videoCanvas.heightProperty().addListener((obs, oldV, newV) -> repaintCanvas());

		sliderVideoTime.valueProperty().addListener((obs, oldV, newV) -> showFrameAt(newV.intValue()));

		// load test video and some settings for quicker testing/debugging
//		Platform.runLater(() -> {
//			loadVideo("testVideos/CircleTest1_no_overlap.mp4");
//			project.getVideo().setXPixelsPerCm(6);
//			project.getVideo().setYPixelsPerCm(6);
//			sliderVideoTime.setValue(30);
//		});
	}

//	@FXML
//	public void handleBrowse() {
//		FileChooser fileChooser = new FileChooser();
//		fileChooser.setTitle("Open Video File");
//		File chosenFile = fileChooser.showOpenDialog(stage);
//		if (chosenFile != null) {
//			loadVideo(chosenFile.getPath());
//		}
//	}

	public void loadVideo(String filePath, ProjectData projectData) throws FileNotFoundException {
		this.project = projectData;
		fileName = filePath;

		project = new ProjectData(filePath);
		Video video = project.getVideo();
		sliderVideoTime.setMin(video.getStartFrameNum());
		sliderVideoTime.setMax(video.getEndFrameNum());
		showFrameAt(projectData.getVideo().getStartFrameNum());
		textfieldStartFrame.setText("" + video.secondsToString(projectData.getVideo().getStartFrameNum()));
		textfieldEndFrame.setText("" + video.secondsToString(projectData.getVideo().getEndFrameNum()));

	}

	public void repaintCanvas() {
		if (project != null) {
			showFrameAt((int) sliderVideoTime.getValue());
		}
	}

	
	public void showFrameAt(int frameNum) {
		if (autotracker == null || !autotracker.isRunning()) {

			project.getVideo().setCurrentFrameNum(frameNum);
			Image curFrame = UtilsForOpenCV.matToJavaFXImage(project.getVideo().readFrame());
			GraphicsContext g = videoCanvas.getGraphicsContext2D();

			g.clearRect(0, 0, videoCanvas.getWidth(), videoCanvas.getHeight());
			double scalingRatio = getImageScalingRatio();
			g.drawImage(curFrame, 0, 0, curFrame.getWidth() * scalingRatio, curFrame.getHeight() * scalingRatio);

			drawAssignedAnimalTracks(g, scalingRatio, frameNum);
			drawUnassignedSegments(g, scalingRatio, frameNum);
		}
		textFieldCurFrameNum.setText(String.format("%05d", frameNum));
	}

	private void drawAssignedAnimalTracks(GraphicsContext g, double scalingRatio, int frameNum) {
		for (int i = 0; i < project.getTracks().size(); i++) {
			AnimalTrack track = project.getTracks().get(i);
			Color trackColor = TRACK_COLORS[i % TRACK_COLORS.length];
			Color trackPrevColor = trackColor.deriveColor(0, 0.5, 1.5, 1.0); // subtler variant

			g.setFill(trackPrevColor);
			// draw chick's recent trail from the last few seconds
			for (TimePoint prevPt : track.getTimePointsWithinInterval(frameNum - 90, frameNum)) {
				g.fillOval(prevPt.getX() * scalingRatio - 3, prevPt.getY() * scalingRatio - 3, 7, 7);
			}
			// draw the current point (if any) as a larger dot
			TimePoint currPt = track.getTimePointAtTime(frameNum);
			if (currPt != null) {
				g.setFill(trackColor);
				g.fillOval(currPt.getX() * scalingRatio - 7, currPt.getY() * scalingRatio - 7, 15, 15);
			}
		}
	}

	private void drawUnassignedSegments(GraphicsContext g, double scalingRatio, int frameNum) {
		for (AnimalTrack segment : project.getUnassignedSegments()) {

			g.setFill(Color.DARKGRAY);
			// draw this segments recent past & near future locations
			for (TimePoint prevPt : segment.getTimePointsWithinInterval(frameNum - 30, frameNum + 30)) {
				g.fillRect(prevPt.getX() * scalingRatio - 1, prevPt.getY() * scalingRatio - 1, 2, 2);
			}
			// draw the current point (if any) as a larger square
			TimePoint currPt = segment.getTimePointAtTime(frameNum);
			if (currPt != null) {
				g.fillRect(currPt.getX() * scalingRatio - 5, currPt.getY() * scalingRatio - 5, 11, 11); 
			}
		}
	}

	private double getImageScalingRatio() {
		double widthRatio = videoCanvas.getWidth() / project.getVideo().getFrameWidth();
		double heightRatio = videoCanvas.getHeight() / project.getVideo().getFrameHeight();
		return Math.min(widthRatio, heightRatio);
	}

	@FXML
	private void handleCanvasClick(MouseEvent event) {

		int selectedChickIndex = comboBoxChicks.getSelectionModel().getSelectedIndex();
		if (selectedChickIndex >= 0) {
			AnimalTrack selectedTrack = project.getTracks().get(selectedChickIndex);			
			int curFrameNum = (int) sliderVideoTime.getValue();

			double scalingRatio = getImageScalingRatio();
			double unscaledX = event.getX() / scalingRatio;
			double unscaledY = event.getY() / scalingRatio;
			selectedTrack.setTimePointAtTime(unscaledX, unscaledY, curFrameNum);
			jumpTimeForward(); //parameter?
		} else {
			new Alert(AlertType.WARNING, "You must ADD a chick first!").showAndWait();
		}
	}

	@FXML
	private void handleAddChickButton() {
		String suggestedInput = "Chick #" + (comboBoxChicks.getItems().size() + 1);
		TextInputDialog dialog = new TextInputDialog(suggestedInput);
		dialog.setTitle("Add Chick:");
		dialog.setHeaderText(null);
		dialog.setContentText("Enter Chick Name:");

		Optional<String> result = dialog.showAndWait();
		if (result.isPresent()) {
			String chickName = result.get();
			project.getTracks().add(new AnimalTrack(chickName));
			comboBoxChicks.getItems().add(chickName);
			comboBoxChicks.getSelectionModel().select(chickName);
		}
	}
	
	@FXML 
	private void handleRemoveChickButton() {
		String selectedChick = comboBoxChicks.getSelectionModel().getSelectedItem();
		int selectedChickIndex = comboBoxChicks.getSelectionModel().getSelectedIndex();
		System.err.println("Before: "+ project.getTracks().size());
		comboBoxChicks.getItems().remove(comboBoxChicks.getSelectionModel().getSelectedItem());
		

		AnimalTrack selectedRemove = project.getTracks().get(selectedChickIndex);
		project.getTracks().remove(selectedRemove);
		
		System.err.println("Just remove: " + selectedChick);
		System.err.println("After: "+ project.getTracks().size());
	}

	@FXML
	private void jumpTimeBackward() {
		jumpTimeForward(-30);
	}

	@FXML
	private void jumpTimeForward() {
		jumpTimeForward(30);
	}

	private void jumpTimeForward(int numberOfFrames) {
		double oldValue = sliderVideoTime.getValue();
		sliderVideoTime.setValue(sliderVideoTime.getValue() + numberOfFrames);
		// if slider didn't change (e.g. tried to move past slider bounds)
		// we still want to update the canvas drawing
		if (sliderVideoTime.getValue() == oldValue) {
			repaintCanvas();
		}
	}

	@FXML
	public void handleStartAutotracking() throws InterruptedException {
		if (autotracker == null || !autotracker.isRunning()) {
			Video video = project.getVideo();
			video.setStartFrameNum(Integer.parseInt(textfieldStartFrame.getText()));
			video.setEndFrameNum(Integer.parseInt(textfieldEndFrame.getText()));
			autotracker = new AutoTracker();
			// Use Observer Pattern to give autotracker a reference to this object,
			// and call back to methods in this class to update progress.
			autotracker.addAutoTrackListener(this);

			// this method will start a new thread to run AutoTracker in the background
			// so that we don't freeze up the main JavaFX UI thread.
			autotracker.startAnalysis(video);
			btnAutotrack.setText("CANCEL auto-tracking");
		} else {
			autotracker.cancelAnalysis();
			btnAutotrack.setText("Start auto-tracking");
		}

	}

	// this method will get called repeatedly by the Autotracker after it analyzes
	// each frame
	@Override
	public void handleTrackedFrame(Mat frame, int frameNumber, double fractionComplete) {
		Image imgFrame = UtilsForOpenCV.matToJavaFXImage(frame);
		// this method is being run by the AutoTracker's thread, so we must
		// ask the JavaFX UI thread to update some visual properties
		Platform.runLater(() -> {
			GraphicsContext g = videoCanvas.getGraphicsContext2D();
			g.clearRect(0, 0, videoCanvas.getWidth(), videoCanvas.getHeight());
			double scalingRatio = getImageScalingRatio();
			g.drawImage(imgFrame, 0, 0, imgFrame.getWidth() * scalingRatio, imgFrame.getHeight() * scalingRatio);

			progressAutoTrack.setProgress(fractionComplete);
			sliderVideoTime.setValue(frameNumber);
		});
	}

	@Override
	public void trackingComplete(List<AnimalTrack> trackedSegments) {
		project.getUnassignedSegments().clear();
		project.getUnassignedSegments().addAll(trackedSegments);

		for (AnimalTrack track : trackedSegments) {
			System.out.println(track);
//			System.out.println("  " + track.getPositions());
		}
		Platform.runLater(() -> {
			progressAutoTrack.setProgress(1.0);
			btnAutotrack.setText("Start auto-tracking");
		});

	}

}
