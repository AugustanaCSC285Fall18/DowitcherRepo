package testing;

import static org.junit.Assert.assertEquals;


import org.junit.jupiter.api.Test;

import datamodel.AnimalTrack;
import datamodel.TimePoint;


class AnimalTrackTesting {
	
	AnimalTrack animal = new AnimalTrack("Bob");
	TimePoint point = new TimePoint (100,73,79);
	TimePoint point2 = new TimePoint (200,150,83);
	
	@Test
	void testAnimalTrackID() {
		assertEquals("Bob", animal.getID());
	}
	
	@Test
	void testGetTimePointAtIndex() {
		animal.add(point);
		assertEquals(point, animal.getTimePointAtIndex(0));
	}
	
	@Test
	void testGetTimePointAtTime() {
		animal.add(point);
		assertEquals(point, animal.getTimePointAtTime(79));
	}
	
	@Test
	void testGetFinalTimePoint() {
		animal.add(point);
		animal.add(point2);
		assertEquals(point2, animal.getFinalTimePoint());
	}
	
	@Test
	void testgetPositions() {
		animal.add(point);
		assertEquals("[(100.0,73.0@T=79)]", animal.getPositions());
	}
	
	@Test
	void testToString() {
		animal.add(point);
		assertEquals("AnimalTrack[id=Bob,numPts=1 start=79 end=79]", animal.toString());
	}
	
}
