package datamodel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class AnimalTrack implements Iterable<TimePoint> {
	private String animalID;

	private List<TimePoint> positions;

	public AnimalTrack(String id) {
		this.animalID = id;
		positions = new ArrayList<TimePoint>();
	}

	public String getID() {
		return animalID;
	}

	public List<TimePoint> getPositions() {
		return positions;
	}

	public void setID(String newID) {
		this.animalID = newID;
	}

	public void add(TimePoint pt) {
		positions.add(pt);
		Collections.sort(positions);
	}

	public int getNumPoints() {
		return positions.size();
	}

	public TimePoint getTimePointAtIndex(int index) {
		return positions.get(index);
	}

	/**
	 * Returns the TimePoint at the specified time, or null
	 * 
	 * @param frameNum
	 * @return
	 */
	public TimePoint getTimePointAtTime(int frameNum) {
		for (TimePoint pt : positions) {
			if (pt.getFrameNum() == frameNum) {
				return pt;
			}
		}
		return null;
	}

	/**
	 * Create (or modify, if existing) a timepoint for the specified time & place.
	 */
	public void setTimePointAtTime(double x, double y, int frameNum) {
		TimePoint oldPt = getTimePointAtTime(frameNum);
		if (oldPt != null) {
			oldPt.setX(x);
			oldPt.setY(y);
		} else {
			add(new TimePoint(x, y, frameNum));
		}
	}

	/**
	 * 
	 * @param startFrameNum - the starting time (inclusive)
	 * @param endFrameNum   - the ending time (inclusive)
	 * @return all time points in that time interval
	 */
	public List<TimePoint> getTimePointsWithinInterval(int startFrameNum, int endFrameNum) {
		List<TimePoint> pointsInInterval = new ArrayList<>();
		for (TimePoint pt : positions) {
			if (pt.getFrameNum() >= startFrameNum && pt.getFrameNum() <= endFrameNum) {
				pointsInInterval.add(pt);
			}
		}
		return pointsInInterval;
	}

	public TimePoint getFinalTimePoint() {
		return positions.get(positions.size() - 1);
	}
	

	/**
	 * Calculates the total distance moved by a chick 
	 * @param frameRate
	 * @param startFrame 
	 * @param endFrame
	 * @return the distance in pixels
	 */
	public Double calculateTotalDistance(int frameRate, int startFrame, int endFrame) {
		double distance = 0;
		for (int second = startFrame; second <= endFrame - frameRate; second += frameRate) {
			int nearestIndex = this.getNearestIndex(second);
			TimePoint pt1 = this.getTimePointAtIndex(nearestIndex);
			int nearestIndex2 = this.getNearestIndex(second + frameRate);
			TimePoint pt2 = this.getTimePointAtIndex(nearestIndex2);
			distance += pt1.getDistanceTo(pt2);
		}
		return distance;
	}

	/**
	 * Gets the time point with the nearest frame number
	 * 
	 * @param frameNum
	 * @return the index of the time point
	 */
	public int getNearestIndex(int frameNum) {
		int diff = Integer.MAX_VALUE;
		int nearestIndex = frameNum;
		for (int index = 0; index < this.getNumPoints(); index++) {
			if (Math.abs(this.getTimePointAtIndex(index).getFrameNum() - frameNum) <= diff) {
				diff = Math.abs(this.getTimePointAtIndex(index).getFrameNum() - frameNum);
				nearestIndex = index;
			}
		}
		return nearestIndex;
	}

	public String toString() {
		int startFrame = positions.get(0).getFrameNum();
		int endFrame = getFinalTimePoint().getFrameNum();
		return "AnimalTrack[id=" + animalID + ",numPts=" + positions.size() + " start=" + startFrame + " end="
				+ endFrame + "]";
	}

	@Override
	public Iterator<TimePoint> iterator() {
		return positions.iterator();
	}
}
