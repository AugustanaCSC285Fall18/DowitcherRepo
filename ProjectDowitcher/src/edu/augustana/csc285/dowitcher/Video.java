package edu.augustana.csc285.dowitcher;

import javafx.scene.shape.Rectangle;

public class Video {
     private double frameRate;
     private double xPixelsPerCm;
     private double yPixelsPerCm;
     private int totalNumberFrames;
     private String filePath;
     private int startFrameNum;
     private int endFrameNum;
     private Rectangle arenaBounds;
     
     
     
     
     public Video(double xPixelsPerCm, double yPixelsPerCm, int totalNumberFrames, String filePath,
			int startFrameNum, int endFrameNum, Rectangle arenaBounds) {
		super();
		this.frameRate = 29.97; //ask Stonedahl if this is the correct number
		this.xPixelsPerCm = xPixelsPerCm;
		this.yPixelsPerCm = yPixelsPerCm;
		this.totalNumberFrames = totalNumberFrames; //vidCap.get(Videoio.CV_CAP_PROP_FRAME_COUNT); where vidCap is an opencv VideoCapture object
		this.filePath = filePath;
		this.startFrameNum = startFrameNum;
		this.endFrameNum = endFrameNum;
		this.arenaBounds = arenaBounds;
	}




	public double getFrameRate() {
		return frameRate;
	}




	public void setFrameRate(double frameRate) {
		this.frameRate = frameRate;
	}




	public double getxPixelsPerCm() {
		return xPixelsPerCm;
	}




	public void setxPixelsPerCm(double xPixelsPerCm) {
		this.xPixelsPerCm = xPixelsPerCm;
	}




	public double getyPixelsPerCm() {
		return yPixelsPerCm;
	}




	public void setyPixelsPerCm(double yPixelsPerCm) {
		this.yPixelsPerCm = yPixelsPerCm;
	}




	public int getTotalNumberFrames() {
		return totalNumberFrames;
	}




	public void setTotalNumberFrames(int totalNumberFrames) {
		this.totalNumberFrames = totalNumberFrames;
	}




	public String getFilePath() {
		return filePath;
	}




	public void setFilePath(String filePath) {
		this.filePath = filePath;
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




	public Rectangle getArenaBounds() {
		return arenaBounds;
	}




	public void setArenaBounds(Rectangle arenaBounds) {
		this.arenaBounds = arenaBounds;
	}




	public double getDurationInSeconds() {
    	 return totalNumberFrames * frameRate;
     }
}
