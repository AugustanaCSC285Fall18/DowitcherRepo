package datamodel;

import java.awt.Point;
import java.awt.Rectangle;
import java.io.FileNotFoundException;

import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

public class Video {
	
	private String filePath;
	private VideoCapture vidCap;
	private int emptyFrameNum;
	private int startFrameNum;
	private int endFrameNum;
	
	private int frameWidth;
	private int frameHeight;
	private double xPixelsPerCm;
	private double yPixelsPerCm;
	private Rectangle arenaBounds; 
	private Point origin;
	
		
	public Video(String filePath) throws FileNotFoundException {
		this.filePath = filePath;
		this.vidCap = new VideoCapture(filePath);
		if (!vidCap.isOpened()) {
			throw new FileNotFoundException("Unable to open video file: " + filePath);
		}
		//fill in some reasonable default/starting values for several fields
		this.emptyFrameNum = 0;
		this.startFrameNum = 0;
		this.endFrameNum = this.getTotalNumFrames()-1;
		
		frameWidth = (int)vidCap.get(Videoio.CAP_PROP_FRAME_WIDTH);
		frameHeight = (int)vidCap.get(Videoio.CAP_PROP_FRAME_HEIGHT);
		System.err.println(frameWidth + " "  + frameHeight);

		this.arenaBounds = new Rectangle(0,0,0,0); //used to be 0,0,frameWidth, frameHeight
		this.origin = new Point(0,0);

	}
	
	
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
	
	public Mat readFrame() {
		Mat frame = new Mat();
		vidCap.read(frame);
		return frame;
	}
	
	public String getFilePath() {
		return this.filePath;
	}
	/** 
	 * @return frames per second
	 */
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

	public void setXPixelsPerCm(double xCm) {

		xPixelsPerCm = arenaBounds.getWidth()/ xCm;
		System.err.println("arena width: " + arenaBounds.getWidth());
		System.err.println("arena width times ratio: " + arenaBounds.getWidth());
		System.err.println("height in cm: " + xCm);
		System.err.println("x pixels per cm: " + xPixelsPerCm);
	}

	public double getYPixelsPerCm() {
		return yPixelsPerCm;
	}

	public void setYPixelsPerCm(double yCm) {

		yPixelsPerCm = arenaBounds.getHeight()/ yCm;
		System.err.println("arena height: " + arenaBounds.getHeight());
		System.err.println("arena height times ratio: " + arenaBounds.getHeight());
		System.err.println("height in cm: " + yCm);
		System.err.println("y pixels per cm: " + yPixelsPerCm);

	}

	public double getAvgPixelsPerCm() {
		return (xPixelsPerCm + yPixelsPerCm)/2;
	}

	public Rectangle getArenaBounds() {
		return arenaBounds;
	}

	public void setArenaBounds(Rectangle arenaBounds) {
		this.arenaBounds = arenaBounds;
	}
	
	public void setOrigin(Point origin) {
		Point ratioOrigin = new Point((int)(origin.getX()), (int)(origin.getY()));
		this.origin = ratioOrigin;
		System.err.println("Origin: "+origin);
	}
	
	public Point getOrigin() {
		return origin;
	}
	
	public double convertFrameNumsToSeconds(int numFrames) {
		return numFrames / getFrameRate();
	}

	public int convertSecondsToFrameNums(double numSecs) {
		return (int) Math.round(numSecs * getFrameRate());
	}
	
	public VideoCapture getVidCap() {
		return vidCap;
	}
	
	//I don't understand this method because look like it's converting from frameNum to String not from seconds
	public String convertSecondsToString(int numFrames){
		int sec = (int) Math.round(numFrames / getFrameRate());
	    return String.format("%02d:%02d", sec / 60, sec % 60);
	}
	
	public int stringToSeconds(String time) {
		String[] timeStr = time.split(":");
		int minute=Integer.parseInt(timeStr[0]);
		int second=Integer.parseInt(timeStr[1]);
		return second + (60 * minute);
	}
	

	public double calculateRatio(double imgViewWidth, double imgViewHeight) {
		System.err.println(imgViewWidth + " " + imgViewHeight);
		double ratio = Math.max(frameHeight/imgViewHeight, frameWidth/imgViewWidth);
		System.err.println("ratio " + ratio);
		return ratio;
	}


}

