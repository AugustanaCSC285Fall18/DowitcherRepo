package edu.augustana.csc285.dowitcher;

import java.util.List;

import org.opencv.core.Mat;

public interface AutoTrackListener {

	public void handleTrackedFrame(Mat frame, int frameNumber, double percentTrackingComplete);
	public void trackingComplete(List<AnimalTrack> trackedSegments);
}
