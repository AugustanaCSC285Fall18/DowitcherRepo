package edu.augustana.csc285.dowitcher;

import java.awt.Point;

public class TimePoint {
	private Point pt;     // location
	private int frameNum; // time (measured in frames)
	
	public TimePoint(int x, int y, int frameNum) {
		pt = new Point(x,y);
		this.frameNum = frameNum;
	}
	
	public int getX() {
		return pt.x;
	}
	
	public int getY() {
		return pt.y;
	}

	public int getFrameNum() {
		return frameNum;
	}

	public String toString() {
		return "("+pt.x+","+pt.y+"@T="+frameNum +")";
	}

	public double getDistanceTo(TimePoint other) {
		return pt.distance(other.pt);
	}
	
}
