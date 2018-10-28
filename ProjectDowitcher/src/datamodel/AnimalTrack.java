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
		// TODO: Eventually, we should consider a more efficient way to keep
		// timepoints in sorted order (such as using a TreeMap data structure)
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
		// TODO: This method's implementation is inefficient [linear search is O(N)]
		// Replace this with binary search (O(log n)] or use a Map for fast access
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
	
	public Double calculateTotalDistance() {
		double distance = 0;
		for(int i = 0; i < this.getNumPoints() - 1; i++) {
			distance += this.getPositions().get(i).getDistanceTo(this.getPositions().get(i + 1));
		}
		return distance;
	}
	
	public int getNearestIndex(int second) {
		int diff = Integer.MAX_VALUE;
		int nearestIndex = second;
		for (int index = 0; index < this.getNumPoints(); index++) {
			if (Math.abs(this.getTimePointAtIndex(index).getFrameNum() - second) <= diff) {
				diff = Math.abs(this.getTimePointAtIndex(index).getFrameNum() - second);
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
