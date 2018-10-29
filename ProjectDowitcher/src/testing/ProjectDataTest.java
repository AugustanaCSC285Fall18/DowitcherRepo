package testing;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.FileNotFoundException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.opencv.core.Core;

import datamodel.AnimalTrack;
import datamodel.ProjectData;
import datamodel.TimePoint;

class ProjectDataTest {

	@BeforeAll
	static void initialize() {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}

	ProjectData makeFakeData() throws FileNotFoundException {
		ProjectData project = new ProjectData("testVideos/CircleTest1_no_overlap.mp4"); //this must be updated to where a usable file path is for testing
		AnimalTrack track1 = new AnimalTrack("chicken1");
		AnimalTrack track2 = new AnimalTrack("chicken2");
		project.getTracks().add(track1);
		project.getTracks().add(track2);

		track1.add(new TimePoint(100, 200, 0));
		track1.add(new TimePoint(105, 225, 30));
		track2.add(new TimePoint(300, 400, 90));

		AnimalTrack segment1 = new AnimalTrack("<Auto-1>");
		AnimalTrack segment2 = new AnimalTrack("<Auto-2>");
		AnimalTrack segment3 = new AnimalTrack("<Auto-3>");
		project.getUnassignedSegments().add(segment1);
		project.getUnassignedSegments().add(segment2);
		project.getUnassignedSegments().add(segment3);

		segment1.add(new TimePoint(100, 200, 0));
		segment1.add(new TimePoint(110, 200, 10));
		segment1.add(new TimePoint(120, 200, 20));

		segment2.add(new TimePoint(300, 400, 0));
		segment2.add(new TimePoint(300, 410, 30));
		segment2.add(new TimePoint(300, 420, 60));

		segment3.add(new TimePoint(150, 200, 50));
		segment3.add(new TimePoint(160, 200, 60));
		segment3.add(new TimePoint(170, 200, 70));

		return project;
	}

	@Test
	void testGetNearestUnassignedSegment() throws FileNotFoundException {
		ProjectData project = makeFakeData();

		// <Auto-1> has the closest point: 100,200@T=0
		AnimalTrack seg1 = project.getNearestUnassignedSegment(99, 201, 0, 30);
		assertEquals(seg1.getID(), "<Auto-1>");

		// <Auto-1> has the closest point: 110,200@T=10
		seg1 = project.getNearestUnassignedSegment(112, 201, 0, 30);
		assertEquals(seg1.getID(), "<Auto-1>");

		// <Auto-2> has the closest point: 300,400@T=30
		AnimalTrack seg2 = project.getNearestUnassignedSegment(305, 413, 30, 35);
		assertEquals(seg2.getID(), "<Auto-2>");

		// <Auto-3> has the closest point: 160,200@T=60
		AnimalTrack seg3 = project.getNearestUnassignedSegment(160, 200, 0, 90);
		assertEquals(seg3.getID(), "<Auto-3>");

		// no unassigned segments have points in the time interval 5 <= frameNum <= 8
		AnimalTrack segNone = project.getNearestUnassignedSegment(100, 200, 5, 8);
		assertNull(segNone);
	}

	@Test
	void testJSONSerializationDeserialization() throws FileNotFoundException {
		ProjectData fake = makeFakeData();
		String json = fake.toJSON();

		ProjectData reconstructedFake = ProjectData.fromJSON(json);

		assertEquals(fake.getVideo().getFilePath(), reconstructedFake.getVideo().getFilePath());
		assertEquals(fake.getTracks().get(0).getTimePointAtIndex(0),
				reconstructedFake.getTracks().get(0).getTimePointAtIndex(0));

	}

	@Test
	void testFileSaving() throws FileNotFoundException {
		ProjectData fake = makeFakeData();
		File fSave = new File("fake_test.project");
		fake.saveToFile(fSave);
		assertTrue(fSave.exists());
	}
	
	@Test
	void testGetAvgDistanceAtTime() throws FileNotFoundException {
		ProjectData fake = makeFakeData();
		double distance = fake.getAvgDistanceAtTime(0);
		assertEquals(distance, Math.sqrt(200*200*2));
	}

}
