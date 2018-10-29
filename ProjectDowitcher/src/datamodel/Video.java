package datamodel;

import java.awt.Point;
import java.awt.Rectangle;
import java.io.FileNotFoundException;

import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

public class Video {

	private String filePath;
	private transient VideoCapture vidCap;
	private int emptyFrameNum;
	private int startFrameNum;
	private int endFrameNum;

	private int frameWidth;
	private int frameHeight;
	private double xPixelsPerCm;
	private double yPixelsPerCm;
	private Rectangle arenaBounds;
	private Point origin;

	/**
	 * constructs object of type Video
	 * @param filePath String representing file path of video being analyzed
	 * @throws FileNotFoundException if file path is invalid
	 */
	public Video(String filePath) throws FileNotFoundException {
		this.filePath = filePath;
		this.vidCap = new VideoCapture(filePath);
		if (!vidCap.isOpened()) {
			throw new FileNotFoundException("Unable to open video file: " + filePath);
		}
		// fill in some reasonable default/starting values for several fields
		this.emptyFrameNum = 0;
		this.startFrameNum = 0;
		this.endFrameNum = this.getTotalNumFrames() - 1;

		frameWidth = (int) vidCap.get(Videoio.CAP_PROP_FRAME_WIDTH);
		frameHeight = (int) vidCap.get(Videoio.CAP_PROP_FRAME_HEIGHT);

		this.arenaBounds = new Rectangle(0, 0, 0, 0); // used to be 0,0,frameWidth, frameHeight
		this.origin = new Point(0, 0);

	}

	/**
	 * connects VideoCapture object to the file
	 * @throws FileNotFoundException if file path is invalid
	 */
	synchronized void connectVideoCapture() throws FileNotFoundException {
		this.vidCap = new VideoCapture(filePath);
		if (!vidCap.isOpened()) {
			throw new FileNotFoundException("Unable to open video file: " + filePath);
		}
	}

	public void setCurrentFrameNum(int seekFrame) {
		vidCap.set(Videoio.CV_CAP_PROP_POS_FRAMES, (double) seekFrame);
	}

	public int getCurrentFrameNum() {
		return (int) vidCap.get(Videoio.CV_CAP_PROP_POS_FRAMES);
	}

	public synchronized int getFrameWidth() {
		return (int) vidCap.get(Videoio.CAP_PROP_FRAME_WIDTH);
	}

	public synchronized int getFrameHeight() {
		return (int) vidCap.get(Videoio.CAP_PROP_FRAME_HEIGHT);
	}

	/**
	 * 
	 * @return Mat object based on current frame of video
	 */
	public Mat readFrame() {
		Mat frame = new Mat();
		vidCap.read(frame);
		return frame;
	}

	public String getFilePath() {
		return this.filePath;
	}

	public double getFrameRate() {
		return vidCap.get(Videoio.CAP_PROP_FPS);
	}

	public int getTotalNumFrames() {
		return (int) vidCap.get(Videoio.CAP_PROP_FRAME_COUNT);
	}

	public int getEmptyFrameNum() {
		return emptyFrameNum;
	}

	public void setEmptyFrameNum(int emptyFrameNum) {
		this.emptyFrameNum = emptyFrameNum;
	}

	public int getStartFrameNum() {
		return startFrameNum;
	}

	public void setStartFrameNum(int startFrameNum) {
		this.startFrameNum = startFrameNum;
	}

	public int getEndFrameNum() {
		return endFrameNum;
	}

	public void setEndFrameNum(int endFrameNum) {
		this.endFrameNum = endFrameNum;
	}

	public double getXPixelsPerCm() {
		return xPixelsPerCm;
	}

	/**
	 * Sets the number of horizontal pixels per a given number of centimeters
	 * 
	 * @param xCm number of centimeters given by the user
	 */
	public void setXPixelsPerCm(double xCm) {
		xPixelsPerCm = arenaBounds.getWidth() / xCm;
	}

	public double getYPixelsPerCm() {
		return yPixelsPerCm;
	}

	/**
	 * Sets the number of horizontal pixels per a given number of centimeters
	 * 
	 * @param yCm number of centimeters given by the user
	 */
	public void setYPixelsPerCm(double yCm) {
		yPixelsPerCm = arenaBounds.getHeight() / yCm;
	}

	public double getAvgPixelsPerCm() {
		return (xPixelsPerCm + yPixelsPerCm) / 2;
	}

	public Rectangle getArenaBounds() {
		return arenaBounds;
	}

	public void setArenaBounds(Rectangle arenaBounds) {
		this.arenaBounds = arenaBounds;
	}

	/**
	 * Sets the origin of the x/y graph used in exporting
	 * 
	 * @param origin the point specified as the origin
	 */
	public void setOrigin(Point origin) {
		Point ratioOrigin = new Point((int) (origin.getX()), (int) (origin.getY()));
		this.origin = ratioOrigin;
	}

	public Point getOrigin() {
		return origin;
	}

	public int convertSecondsToFrameNums(double numSecs) {
		return (int) Math.round(numSecs * getFrameRate());
	}

	public VideoCapture getVidCap() {
		return vidCap;
	}

	/**
	 * used whenever current time is being displayed in a text field
	 * @param numFrames current frame number
	 * @return string representing current time in seconds
	 */
	public String convertFramesToString(int numFrames) {
		int sec = (int) Math.round(numFrames / getFrameRate());
		return String.format("%02d:%02d", sec / 60, sec % 60);
	}

	public double calculateRatio(double imgViewWidth, double imgViewHeight) {
		double ratio = Math.max(frameHeight / imgViewHeight, frameWidth / imgViewWidth);
		return ratio;
	}

}
