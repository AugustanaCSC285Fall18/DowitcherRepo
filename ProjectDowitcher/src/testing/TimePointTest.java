package testing;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import datamodel.TimePoint;

class TimePointTest {

	TimePoint pt1 = new TimePoint(0, 0, 0);
	TimePoint pt2 = new TimePoint(10, 10, 0);
	@Test
	void testGetDistanceTo() {
		assertEquals(pt1.getDistanceTo(pt2), Math.sqrt(200));
	}

}
