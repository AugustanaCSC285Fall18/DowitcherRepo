package edu.augustana.csc285.dowitcher;

import java.awt.Point;

public class TimePoint {
	private Point pt;
	private int frameNum;
	
	
	public TimePoint(Point pt, int frameNum) {
		super();
		this.pt = pt;
		this.frameNum = frameNum;
	}


	public Point getPt() {
		return pt;
	}


	public void setPt(Point pt) {
		this.pt = pt;
	}


	public int getFrameNum() {
		return frameNum;
	}


	public void setFrameNum(int frameNum) {
		this.frameNum = frameNum;
	}
	
	
	
	
}
