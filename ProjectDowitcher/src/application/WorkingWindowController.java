package application;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;

import org.opencv.core.Mat;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import autotracking.AutoTrackListener;
import autotracking.AutoTracker;
import datamodel.AnimalTrack;
import datamodel.ProjectData;
import datamodel.TimePoint;
import datamodel.Video;
import utils.TimeUtils;
import utils.UtilsForOpenCV;

public class WorkingWindowController implements AutoTrackListener {

	@FXML
	private Button btnSetName;
	@FXML
	private TextField textfieldSetName;
	@FXML
	private TextField textfieldJumpTime;

	@FXML
	private Canvas videoCanvas;
	@FXML
	private Pane paneHoldingVideoCanvas;
	@FXML
	private Slider sliderVideoTime;
	@FXML
	private TextField textFieldCurTime;

	@FXML
	private ComboBox<String> comboBoxChicks;

	@FXML
	private Label labelStartFrame;
	@FXML
	private Label labelEndFrame;
	@FXML
	private Button btnAutotrack;
	@FXML
	private ProgressBar progressAutoTrack;
	@FXML
	private MenuItem menuitemSave;
	@FXML
	private MenuItem menuitemExport;
	@FXML
	private MenuItem menuitemExportAvg;

	public static final Color[] TRACK_COLORS = new Color[] { Color.RED, Color.BLUE, Color.GREEN, Color.CYAN,
			Color.MAGENTA, Color.BLUEVIOLET, Color.ORANGE };

	private final int defaultIncrementSeconds = 1;

	private ProjectData project;
	private Video vid;
	private AutoTracker autotracker;
	private int frameRate;

	/**
	 * Binds the canvas changes to the pane changes for resizing.
	 * 
	 */
	public void initialize() {
		// bind it so when the the pane changes width, the canvas matches it
		videoCanvas.widthProperty().bind(paneHoldingVideoCanvas.widthProperty());
		videoCanvas.heightProperty().bind(paneHoldingVideoCanvas.heightProperty());
		videoCanvas.widthProperty().addListener((obs, oldV, newV) -> repaintCanvas());
		videoCanvas.heightProperty().addListener((obs, oldV, newV) -> repaintCanvas());

		sliderVideoTime.valueProperty().addListener((obs, oldV, newV) -> showFrameAt(newV.intValue()));
	}

	/**
	 * Loads video from filePath and throws an exception if video is not found
	 * 
	 * @param filePath
	 * @param projectData
	 * @throws FileNotFoundException
	 */
	public void loadVideo(ProjectData projectData) throws FileNotFoundException {
		this.project = projectData;
		this.vid = projectData.getVideo();
		this.frameRate = (int) Math.round(vid.getFrameRate());
		System.err.println("Frame Rate: " + frameRate);

		showFrameAt(vid.getStartFrameNum());

		// set up the properties of the video based on the Calibration Window config
		labelStartFrame.setText(vid.convertFramesToString(vid.getStartFrameNum()));
		labelEndFrame.setText(vid.convertFramesToString(vid.getEndFrameNum()));
		sliderVideoTime.setMax((int) vid.getEndFrameNum());
		sliderVideoTime.setMin((int) vid.getStartFrameNum());
		sliderVideoTime.setBlockIncrement(defaultIncrementSeconds * frameRate);
		for (int i = 0; i < project.getChickNum(); i++) {
			String chickName = ("Chick #" + (i + 1));
			project.getTracks().add(new AnimalTrack(chickName));
			comboBoxChicks.getItems().add(chickName);
		}
	}

	/**
	 * repaints the canvas based on the current time
	 */
	public void repaintCanvas() {
		if (project != null) {
			showFrameAt((int) sliderVideoTime.getValue());
		}
	}

	/**
	 * Displays the current frame of the video to the user provided that the
	 * autotraker is not running. Also shows what is drawn on the canvas at that
	 * specific frame.
	 * 
	 * @param frameNum
	 */
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
			g.setStroke(Color.RED);
			g.strokeRect(vid.getArenaBounds().getX() * getImageScalingRatio(),
					vid.getArenaBounds().getY() * getImageScalingRatio(),
					vid.getArenaBounds().getWidth() * getImageScalingRatio(),
					vid.getArenaBounds().getHeight() * getImageScalingRatio());
		}
		textFieldCurTime.setText(vid.convertFramesToString(frameNum));
	}

	//draws circles representing a chick's recent positions 
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

	//draws a grey rectangle for when an unassigned auto track segment is available
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

	/**
	 * Adds an unassigned segment from the auto track to a specific chick if a point
	 * clicked in the manual track is close enough to a point in the unassigned
	 * segment based on location of the click and the frame number.
	 * 
	 * @param tp
	 * @param chickNum
	 */
	public void addUnassignedSegments(TimePoint tp, int chickNum) {
		AnimalTrack selectedChick = project.getTracks().get(chickNum);
		selectedChick.add(tp);
		if (project.getNearestUnassignedSegment(tp.getX(), tp.getY(), (int) (tp.getFrameNum() - vid.getFrameRate()),
				(int) (tp.getFrameNum() + vid.getFrameRate())) != null) {// test if the TimePoint is nearby an
																			// unassigned segment
			AnimalTrack toBeAssigned = project.getNearestUnassignedSegment(tp.getX(), tp.getY(),
					(int) (tp.getFrameNum() - vid.getFrameRate()), (int) (tp.getFrameNum() + vid.getFrameRate()));
			for (int i = 0; i < toBeAssigned.getNumPoints(); i++) {
				selectedChick.add(toBeAssigned.getTimePointAtIndex(i));
			}
		}
	}

	//returns minimum scaling ratio between height and width ratios for the canvas's size relative to the video's
	private double getImageScalingRatio() {
		double widthRatio = videoCanvas.getWidth() / project.getVideo().getFrameWidth();
		double heightRatio = videoCanvas.getHeight() / project.getVideo().getFrameHeight();
		return Math.min(widthRatio, heightRatio);
	}

	//updates selected chick's AnimalTrack and updates the displayed image by the increment amount
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
			addUnassignedSegments(selectedTrack.getTimePointAtTime(curFrameNum), selectedChickIndex);
			jumpTime(defaultIncrementSeconds);

		} else {
			new Alert(AlertType.WARNING, "You must CHOOSE a chick first!").showAndWait();
		}
	}

	@FXML
	private void handleSetNameButton() {
		int selectedChickIndex = comboBoxChicks.getSelectionModel().getSelectedIndex();
		String newName = textfieldSetName.getText();
		if (selectedChickIndex >= 0) {
			if (!(newName.equals(""))) {
				comboBoxChicks.getItems().set(selectedChickIndex, newName);
				project.getTracks().get(selectedChickIndex).setID(newName);
			} else {
				new Alert(AlertType.WARNING, "You must TYPE IN a name first!").showAndWait();
			}
		} else {
			new Alert(AlertType.WARNING, "You must CHOOSE a chick first!").showAndWait();
		}
		textfieldSetName.clear();
	}

	@FXML
	private void handleBackward() {
		if (!(textfieldJumpTime.getText().equals(""))) {
			int num = Integer.parseInt(textfieldJumpTime.getText());
			jumpTime(-num);
		} else {
			new Alert(AlertType.WARNING, "You must TYPE IN how many seconds you want to go first!").showAndWait();
		}

	}

	@FXML
	private void handleForward() {
		if (!(textfieldJumpTime.getText().equals(""))) {
			int num = Integer.parseInt(textfieldJumpTime.getText());
			jumpTime(num);
		} else {
			new Alert(AlertType.WARNING, "You must TYPE IN how many seconds you want to go first!").showAndWait();
		}
	}

	/**
	 * changes image displayed to the video's image a set amount of frames away from the current displayed frame
	 * @param numberOfFrames
	 */
	private void jumpTime(int numberOfFrames) {
		double oldValue = sliderVideoTime.getValue();
		sliderVideoTime.setValue(sliderVideoTime.getValue() + numberOfFrames * frameRate);
		// if slider didn't change (e.g. tried to move past slider bounds)
		// we still want to update the canvas drawing
		if (sliderVideoTime.getValue() == oldValue) {
			repaintCanvas();
		}
	}

	/**
	 * Handles when the autotrack button is clicked to start the auto track from the
	 * user set start from until the user set end frame. Throws exception if
	 * interrupted.
	 * 
	 * @throws InterruptedException
	 */
	@FXML
	public void handleStartAutotracking() throws InterruptedException {
		if (autotracker == null || !autotracker.isRunning()) {
			// Video video = project.getVideo();
			vid.setStartFrameNum(
					(int) Math.round(TimeUtils.convertMinutesToSeconds(labelStartFrame.getText()) * frameRate));
			vid.setEndFrameNum(
					(int) Math.round(TimeUtils.convertMinutesToSeconds(labelEndFrame.getText()) * frameRate));
			autotracker = new AutoTracker();
			// Use Observer Pattern to give autotracker a reference to this object,
			// and call back to methods in this class to update progress.
			autotracker.addAutoTrackListener(this);

			// this method will start a new thread to run AutoTracker in the background
			// so that we don't freeze up the main JavaFX UI thread.
			autotracker.startAnalysis(vid);
			btnAutotrack.setText("CANCEL auto-tracking");
		} else {
			autotracker.cancelAnalysis();
			btnAutotrack.setText("Start auto-tracking");
		}

	}

	/**
	 * Handles when the save menu item is selected. Allows the user to save the
	 * current progress of the project to be reloaded in later.
	 * 
	 * @throws FileNotFoundException
	 */
	@FXML
	public void handleSave() throws FileNotFoundException {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Choose the File You Wish to Save To");
		ContextMenu menuBar = menuitemSave.getParentPopup();
		Window mainWindow = menuBar.getScene().getWindow();
		FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter("Project", "*.project");
		fileChooser.getExtensionFilters().add(filter);
		File chosenFile = fileChooser.showSaveDialog(mainWindow);
		if (chosenFile == null) {
			return;
		}
		project.saveToFile(chosenFile);
	}

	/**
	 * handles when the user clicks the export menu item. Allows the user to save
	 * all the animal track data into a CSV file in order to be be opened elsewhere.
	 * Also saves all data as a StringBuilder so that it can be opened in the CSV in
	 * a nice looking format.
	 */
	@FXML
	public void handleExport() {
		PrintWriter pw = null;
		try {
			FileChooser fileChooser = new FileChooser();
			fileChooser.setTitle("Choose the File You Wish to Save To");
			ContextMenu menuBar = menuitemExport.getParentPopup();
			Window mainWindow = menuBar.getScene().getWindow();
			FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter("CSV", "*.csv");
			fileChooser.getExtensionFilters().add(filter);
			File chosenFile = fileChooser.showSaveDialog(mainWindow);
			if (chosenFile == null) {
				return;
			}
			pw = new PrintWriter(chosenFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		StringBuilder builder = new StringBuilder();
		String ColumnNamesList = "Id" + "," + "Time" + "," + "X (cm)" + "," + "Y (cm)";
		// No need give the headers Like: id, Name on builder.append
		builder.append(ColumnNamesList + "\n");
		for (AnimalTrack track : project.getTracks()) {
			if (track.getNumPoints() != 0) {
				NumberFormat formatter = new DecimalFormat("#0.000");
				for (int second = vid.getStartFrameNum(); second <= vid.getEndFrameNum(); second += frameRate) {
					int nearestIndex = track.getNearestIndex(second);
					TimePoint tPt = track.getTimePointAtIndex(nearestIndex);
					String time = vid.convertFramesToString(tPt.getFrameNum());
					String xPos = formatter.format((tPt.getX() - vid.getOrigin().getX()) / vid.getAvgPixelsPerCm());
					String yPos = formatter.format((vid.getOrigin().getY() - tPt.getY()) / vid.getAvgPixelsPerCm());
					builder.append(track.getID() + "," + time + "," + xPos + "," + yPos + "," + "\n");
				}
				builder.append("Total Distance (cm)" + "," + formatter.format(
								track.calculateTotalDistance(frameRate, vid.getStartFrameNum(), vid.getEndFrameNum())
								/ vid.getAvgPixelsPerCm())+ "\n");
			}
		}
		pw.write(builder.toString());
		pw.close();
	}

	@FXML
	public void handleExportAvgDistance() {
		PrintWriter pw = null;
		try {
			FileChooser fileChooser = new FileChooser();
			fileChooser.setTitle("Choose the File You Wish to Save To");
			ContextMenu menuBar = menuitemExportAvg.getParentPopup();
			Window mainWindow = menuBar.getScene().getWindow();
			FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter("CSV", "*.csv");
			fileChooser.getExtensionFilters().add(filter);
			File chosenFile = fileChooser.showSaveDialog(mainWindow);
			if (chosenFile == null) {
				return;
			}
			pw = new PrintWriter(chosenFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		StringBuilder builder = new StringBuilder();
		String ColumnNamesList = "Time" + "," + "Avg Distance (cm)";
		// No need give the headers Like: id, Name on builder.append
		builder.append(ColumnNamesList + "\n");
		AnimalTrack track = project.getTracks().get(0);
		if (track.getNumPoints() != 0) {
			NumberFormat formatter = new DecimalFormat("#0.000");
			for (int second = vid.getStartFrameNum(); second <= vid.getEndFrameNum(); second += frameRate) {
				int nearestIndex = track.getNearestIndex(second);
				TimePoint tPt = track.getTimePointAtIndex(nearestIndex);
				String time = vid.convertFramesToString(tPt.getFrameNum());
				String avgDistance = formatter.format(project.getAvgDistanceAtTime(second) / vid.getAvgPixelsPerCm());
				builder.append(time + "," + avgDistance + "\n");
			}
		}
		pw.write(builder.toString());
		pw.close();
	}

	@FXML
	private void handleAbout() {
		new Alert(AlertType.INFORMATION,
				"Chick Tracking Project by Augustana College CSC 285 students: \nAdam Donovan, Anthony Santangelo, \nNGUYEN TRUONG, Wesley Pulver \nSupervisor: Dr. Forrest Stonedahl")
						.showAndWait();
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

		Platform.runLater(() -> {
			progressAutoTrack.setProgress(1.0);
			btnAutotrack.setText("Start auto-tracking");
		});

	}

}
