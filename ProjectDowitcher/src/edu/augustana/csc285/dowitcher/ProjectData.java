package edu.augustana.csc285.dowitcher;

import java.io.File;
import java.util.List;

public class ProjectData {
	private List<AnimalTrack> track;
	private Video video;
	
	public void exportCSV(File outFile) {
		
	}
	
	public void saveProject(File projectFile) {
		
	}

	public ProjectData(List<AnimalTrack> track, Video video) {
		super();
		this.track = track;
		this.video = video;
	}

	public List<AnimalTrack> getTrack() {
		return track;
	}

	public void setTrack(List<AnimalTrack> track) {
		this.track = track;
	}

	public Video getVideo() {
		return video;
	}

	public void setVideo(Video video) {
		this.video = video;
	}
}
