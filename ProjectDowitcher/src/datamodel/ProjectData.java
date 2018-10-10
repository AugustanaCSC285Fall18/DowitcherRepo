
package datamodel;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import java.io.File;
import datamodel.AnimalTrack;
import datamodel.Video;

public class ProjectData {
	private Video video;
	private List<AnimalTrack> tracks;
	private List<AnimalTrack> unassignedSegments;
	private int chickNum;
	//private List<AnimalTrack> manualTrackSegments;
	
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

	public void exportCSV(File outFile) {
		
	}
	
	
	public void saveProject(File projectFile) {
		
	}
	
	public void setChickNum(int numberOfChick ) {
		this.chickNum = numberOfChick;
	}
	
	public int getChickNum() {
		return this.chickNum;
	}
}

