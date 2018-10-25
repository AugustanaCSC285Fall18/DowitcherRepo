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
	 */
	public AnimalTrack getNearestUnassignedSegment(double x, double y, int startFrame, int endFrame) {
		// FIXME: find and return the correct segment (see Javadoc comment above)
//		List<AnimalTrack> unassignedSegmentAtTime = new List<AnimalTrack>;
//		
//		
//		for (int i = 0; i < unassignedSegments.size(); i++) {
//			unassignedSegments.get(i).getTimePointsWithinInterval(startFrame, endFrame)
//		}
//		
		return null;
	}

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
	
	public void exportCSV(File outFile) {

	}

	public void saveProject(File projectFile) {

	}

	public void setChickNum(int numberOfChick) {
		this.chickNum = numberOfChick;
	}

	public int getChickNum() {
		return this.chickNum;
	}

}
