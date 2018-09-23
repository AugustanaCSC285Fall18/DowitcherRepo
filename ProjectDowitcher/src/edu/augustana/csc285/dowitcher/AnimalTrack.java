package edu.augustana.csc285.dowitcher;

import java.util.List;

public class AnimalTrack {
	private String animalID;
	private List<TimePoint> positions;
	
	
	public AnimalTrack(String animalID, List<TimePoint> positions) {
		super();
		this.animalID = animalID;
		this.positions = positions;
	}


	public String getAnimalID() {
		return animalID;
	}


	public void setAnimalID(String animalID) {
		this.animalID = animalID;
	}


	public List<TimePoint> getPositions() {
		return positions;
	}


	public void setPositions(List<TimePoint> positions) {
		this.positions = positions;
	}
	
	
}
