package datamodel;

import java.util.ArrayList;
import java.util.List;


public class AnimalTrack {
	private String animalID;
	
	private List<TimePoint> positions;
	
	public AnimalTrack(String id) {
		this.animalID = id;
		positions = new ArrayList<TimePoint>();
	}
	
	public void add(TimePoint pt) {
		positions.add(pt);
	}
	
	public TimePoint getTimePointAtIndex(int index) {
		return positions.get(index);
	}

	/**
	 * Returns the TimePoint at the specified time, or null
	 * @param frameNum
	 * @return
	 */
	
	public TimePoint getTimePointAtTime(int frameNum) {
		//TODO: This method's implementation is inefficient [linear search is O(N)]
		//      Replace this with binary search (O(log n)] or use a Map for fast access
		for (TimePoint pt : positions) {
			if (pt.getFrameNum() == frameNum) {
				return pt;
			}
		}
		return null;
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
		return positions.get(positions.size()-1);
	}
	
	public String toString() {
		int startFrame = positions.get(0).getFrameNum();
		int endFrame = getFinalTimePoint().getFrameNum();
		return "AnimalTrack[id="+ animalID + ",numPts=" + positions.size()+" start=" + startFrame + " end=" + endFrame +"]"; 
	}
	
	public String getPositionsString() {
		return positions.toString();
	}
	
	public List<TimePoint> getPositions(){
		return positions;
	}
	
	public String getID() {
		return animalID;
	}
}
