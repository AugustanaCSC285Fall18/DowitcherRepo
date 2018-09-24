package edu.augustana.csc285.dowitcher;

import java.io.FileNotFoundException;

import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

import javafx.scene.shape.Rectangle;


public class Video {
	private String filePath;
	private VideoCapture vidCap;
	private int startFrameNum;
	private int endFrameNum;
	
	private int totalNumberFrames; 
    private double frameRate;
     
    private double xPixelsPerCm;
    private double yPixelsPerCm;
    private Rectangle arenaBounds;
     
     
     public Video(String filePath)throws FileNotFoundException {
		this.filePath = filePath;
		this.vidCap= new VideoCapture(filePath);
		if(!vidCap.isOpened()) {
			throw new FileNotFoundException("Unable to open video file: " + filePath);
		}
	}

     


	public double getFrameRate() {
		return vidCap.get(Videoio.CAP_PROP_FPS);
	}
	
	public int getTotalNumberFrames() {
		return (int) vidCap.get(Videoio.CAP_PROP_FRAME_COUNT);
	}
	
	
	
	public double getxPixelsPerCm() {
		return xPixelsPerCm;
	}
	public double getyPixelsPerCm() {
		return yPixelsPerCm;
	}
	public Rectangle getArenaBounds() {
		return arenaBounds;
	}
	public void setArenaBounds(Rectangle arenaBounds) {
		this.arenaBounds = arenaBounds;
	}

	
	
	public String getFilePath() {
		return filePath;
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






	public double getDurationInSeconds() {
    	 return totalNumberFrames/frameRate;
     }
}
