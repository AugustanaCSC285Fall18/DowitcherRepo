package testing;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import datamodel.AnimalTrack;
import datamodel.TimePoint;

class AnimalTrackTest {

	@Test
	void testAddingAndGettingPoints() {
		AnimalTrack testTrack = new AnimalTrack("ChickenLittle");
		assertEquals("ChickenLittle", testTrack.getID());

		testTrack.add(new TimePoint(100, 100, 0));
		testTrack.add(new TimePoint(110, 110, 1));
		testTrack.add(new TimePoint(150, 200, 5));
		assertEquals(3, testTrack.getNumPoints());

		TimePoint ptAt0 = testTrack.getTimePointAtTime(0);
		assertEquals(new TimePoint(100, 100, 0), ptAt0);
		TimePoint ptAt2 = testTrack.getTimePointAtTime(2);
		assertNull(ptAt2);
		TimePoint lastPt = testTrack.getFinalTimePoint();
		assertEquals(5, lastPt.getFrameNum());
	}

}
