package datamodel;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class ProjectData {
	private Video video;
	private List<AnimalTrack> tracks;
	private List<AnimalTrack> unassignedSegments;
	private int chickNum;

	public ProjectData(String videoFilePath) throws FileNotFoundException {
		video = new Video(videoFilePath);
		tracks = new ArrayList<>();
		unassignedSegments = new ArrayList<>();
	}

	public Video getVideo() {
		return video;
	}

	public List<AnimalTrack> getTracks() {
		return tracks;
	}

	public List<AnimalTrack> getUnassignedSegments() {
		return unassignedSegments;
	}

	/**
	 * This method returns the unassigned segment that contains a TimePoint (between
	 * startFrame and endFrame) that is closest to the given x,y location
	 * 
	 * @param x          - x coordinate to search near
	 * @param y          - y coordinate to search near
	 * @param startFrame - (inclusive)
	 * @param endFrame   - (inclusive)
	 * @return the unassigned segment (AnimalTrack) that contained the nearest point
	 *         within the given time interval, or *null* if there is NO unassigned
	 *         segment that contains any TimePoints within the given range.
	 *         Citation- this code was give by Dr. Stonedahl on the Augustana Q&A
	 *         page.
	 */
	public AnimalTrack getNearestUnassignedSegment(double x, double y, int startFrame, int endFrame) {
		double minDistance = 6 * this.getVideo().getAvgPixelsPerCm();
		AnimalTrack nearest = null;
		for (AnimalTrack segment : unassignedSegments) {
			List<TimePoint> ptsInInterval = segment.getTimePointsWithinInterval(startFrame, endFrame);
			for (TimePoint pt : ptsInInterval) {
				double dist = pt.getDistanceTo(x, y);
				if (dist < minDistance) {
					minDistance = dist;
					nearest = segment;
				}
			}
		}
		return nearest;
	}

	/**
	 * This method returns a list of all of the unassigned segments that contain a
	 * TimePoint (within the interval from startFrame to endFrame) that is
	 * sufficiently close (within a specified distance) to the given x,y location.
	 * 
	 * @param x             - x coordinate to search near
	 * @param y             - y coordinate to search near
	 * @param startFrame    - (inclusive)
	 * @param endFrame      - (inclusive)
	 * @param distanceRange - the farthest away that the segment can be and still
	 *                      count.
	 * @return the list of unassigned segments that had TimePoints in the right time
	 *         interval AND within *distanceRange* of the specified (x,y) point.
	 *         Citation- this code was give by Dr. Stonedahl on the Augustana Q&A
	 *         page.
	 */
	public List<AnimalTrack> getUnassignedSegmentsInRange(double x, double y, int startFrame, int endFrame,
			double distanceRange) {

		List<AnimalTrack> closeEnough = new ArrayList<AnimalTrack>();
		for (AnimalTrack segment : unassignedSegments) {
			List<TimePoint> ptsInInterval = segment.getTimePointsWithinInterval(startFrame, endFrame);
			for (TimePoint pt : ptsInInterval) {
				double dist = pt.getDistanceTo(x, y);
				if (dist <= distanceRange) {
					closeEnough.add(segment);
					break;
				}
			}
		}
		return closeEnough;
	}


	/**
	 * Gets the average distance between each pair of chicks at a given frame number
	 * 
	 * @param frameNum the time in frames
	 * @return the sum of the distances between each pair of AnimalTracks divided by
	 *         the number of pairs
	 */
	public double getAvgDistanceAtTime(int frameNum) {
		if (this.getTracks().size() > 1) {
			double totalDistance = 0;
			double numPairs = 0;
			int numTracks = 0;
			for(AnimalTrack chick :this.getTracks()) {
				if(chick.getNumPoints() != 0) {
					numTracks++;
				}
			}
			for (int track = 0; track < numTracks - 1; track++) {
				for (int i = track + 1; i < numTracks; i++) {
					TimePoint pt1 = this.getTracks().get(track)
							.getTimePointAtIndex(this.getTracks().get(track).getNearestIndex(frameNum));
					TimePoint pt2 = this.getTracks().get(i)
							.getTimePointAtIndex(this.getTracks().get(i).getNearestIndex(frameNum));
					totalDistance += pt1.getDistanceTo(pt2);
					numPairs++;
				}
			}
			return totalDistance / numPairs;
		} else {
			return 0;
		}
	}

	/**
	 * writes an instance of ProjectData to a JSON String
	 * 
	 * @param saveFile the file to save to
	 * @throws FileNotFoundException
	 */
	public void saveToFile(File saveFile) throws FileNotFoundException {
		String json = toJSON();
		PrintWriter out = new PrintWriter(saveFile);
		out.print(json);
		out.close();
	}

	public String toJSON() {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		return gson.toJson(this);
	}

	/**
	 * Reads a JSON String and turns it back into a ProjectData
	 * 
	 * @param loadFile the file to read from
	 * @return an instance of ProjectData
	 * @throws FileNotFoundException
	 */
	public static ProjectData loadFromFile(File loadFile) throws FileNotFoundException {
		String json = new Scanner(loadFile).useDelimiter("\\Z").next();
		return fromJSON(json);
	}

	public static ProjectData fromJSON(String jsonText) throws FileNotFoundException {
		Gson gson = new Gson();
		ProjectData data = gson.fromJson(jsonText, ProjectData.class);
		data.getVideo().connectVideoCapture();
		return data;
	}
	
	public void setChickNum(int numberOfChick) {
		this.chickNum = numberOfChick;
	}

	public int getChickNum() {
		return this.chickNum;
	}

}
