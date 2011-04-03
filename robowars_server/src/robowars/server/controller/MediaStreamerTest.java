package robowars.server.controller;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.apache.log4j.PropertyConfigurator;
import org.junit.Before;
import org.junit.Test;

import robowars.shared.model.GameType;
import robowars.shared.model.User;
import robowars.test.TestMediaStreamer;
import robowars.test.TestRobotProxy;
import robowars.test.TestUserProxy;

import com.lti.civil.CaptureException;
import com.lti.civil.CaptureObserver;
import com.lti.civil.CaptureStream;
import com.lti.civil.Image;

/**
 * Unit tests for the MediaStreamer, CameraController and CameraPosition 
 * classes.
 * 
 * Note: These tests assume a USB webcam is connected to the system,
 * and will fail if no camera is available.
 * 
 * @author Alexander Craig
 */
public class MediaStreamerTest {
	
	public static int TEST_PORT = 100;
	
	private TestMediaStreamer testMedia;
	private ServerLobby testLobby;
	private TestUserProxy user1;

	@Before
	public void setUp() throws Exception {
		// Use log4j config file "log_config.properties"
		PropertyConfigurator.configure("config/log_config.properties");
		
		testMedia = new TestMediaStreamer(TEST_PORT);
		testLobby = new ServerLobby("TestServerLobby", 10, 10);
		user1 = new TestUserProxy(testLobby, "TestUser1");
		
	}

	@Test
	public void testGetPort() {
		assertEquals(TEST_PORT, testMedia.getPort());
	}

	@Test
	public void testUpdateDeviceList() {
		testMedia.updateDeviceList();
		
		// Assert that a valid camera was found and selected as the active camera
		assertEquals(true, testMedia.getActiveCamera() != null);
		
		// Test of camera controller getter / setters
		CameraController activeCam = testMedia.getActiveCamera();
		
		activeCam.setFov(100);
		activeCam.setPosition(100, 200, 300);
		activeCam.setOrientation(20, 60);
		
		assertEquals(100, activeCam.getFov(), 0.05);
		assertEquals(100, activeCam.getxPos(), 0.05);
		assertEquals(200, activeCam.getyPos(), 0.05);
		assertEquals(300, activeCam.getzPos(), 0.05);
		assertEquals(20, activeCam.getHorOrientation(), 0.05);
		assertEquals(60, activeCam.getVerOrientation(), 0.05);
		assertEquals(true, activeCam.getCameraName() != null);
		assertEquals(true, activeCam.getDeviceId() != null);
		
		// Test that a redetection of devices preserves the active camera
		testMedia.updateDeviceList();
		assertEquals(activeCam, testMedia.getActiveCamera());
	}

	@Test
	public void testGetActiveCamera() {
		assertEquals(null, testMedia.getActiveCamera());
		testMedia.updateDeviceList();
		assertEquals(true, testMedia.getActiveCamera() != null);
	}

	@Test
	public void testVideoStream() {
		testMedia.updateDeviceList();
		assertEquals(true, testMedia.getActiveCamera() != null);
		
		// Camera has been found, start streaming and verify
		testMedia.playStream();
		assertEquals(true, testMedia.isStreaming());
		
		// Allow some time for frame to be generated, and ensure the proper
		// callbacks occurred
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		assertEquals(true, testMedia.getFrameCount() > 0);
		
		// Ensure stream continues if a device redetection is attempted
		// while a stream is playing
		testMedia.updateDeviceList();
		testMedia.clearFrameCount();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		assertEquals(true, testMedia.getFrameCount() > 0);
		
		testMedia.stopStream();
		assertEquals(false, testMedia.isStreaming());
	}

	@Test
	public void testObserverVideoStream() {
		testMedia.updateDeviceList();
		assertEquals(true, testMedia.getActiveCamera() != null);
		
		// Test that feeding frames to an external observer works
		TestCaptureObserver testObserver = new TestCaptureObserver();
		testMedia.setObserver(testObserver);
		testMedia.playStream();
		assertEquals(true, testMedia.isStreaming());
		
		// Allow some time for frame to be generated, and ensure the proper
		// callbacks occurred
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		assertEquals(true, testObserver.getFrameCount() > 0);
		testMedia.stopStream();
		assertEquals(false, testMedia.isStreaming());
	}
	
	@Test
	public void testGetAvailableCameras() {
		testMedia.updateDeviceList();
		CameraController activeCam = testMedia.getActiveCamera();
		
		List<CameraController> camList = testMedia.getAvailableCameras();
		assertEquals(true, camList.contains(activeCam));
	}

	@Test
	public void testAddRemoveUser() {
		testLobby.addLobbyStateListener(testMedia);
		User testUser = user1.getUser();
	
		testLobby.addUserProxy(user1);
		assertEquals(true, testMedia.isServingUser(testUser));
		assertEquals(1, testMedia.getNumUsers());
		
		// Ensure duplicate users aren't added
		testMedia.addUser(testUser);
		assertEquals(1, testMedia.getNumUsers());
		
		// Ensure null users aren't added
		testMedia.addUser(null);
		assertEquals(1, testMedia.getNumUsers());
		
		testLobby.removeUserProxy(user1);
		assertEquals(false, testMedia.isServingUser(testUser));
	}


	@Test
	public void testLobbyGameStateChanged() {
		testMedia.updateDeviceList();
		assertEquals(true, testMedia.getActiveCamera() != null);
		
		testLobby.addLobbyStateListener(testMedia);
		testLobby.setGameType(GameType.FREETEST);
		TestRobotProxy robot1 = new TestRobotProxy(testLobby, "TestRobot1");
		testLobby.addUserProxy(user1);
		user1.processChangedReadyState(true);
		
		// Ensure that a game launch triggers the media stream
		user1.processGameLaunch();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		assertEquals(true, testLobby.gameInProgress());
		assertEquals(true, testMedia.isStreaming());
		
		// Ensure that game termination closes the video stream
		testLobby.endCurrentGame();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		assertEquals(false, testMedia.isStreaming());
		
		testLobby.removeLobbyStateListener(testMedia);
	}

	private class TestCaptureObserver implements CaptureObserver {
		/** The number of video frames read from a camera */
		int frameCount;

		public TestCaptureObserver() {
			frameCount = 0;
		}
		
		/**
		 * @return	The number of frames read from a camera by this MediaStreamer
		 * 			since the last counter reset.
		 */
		public int getFrameCount() {
			return frameCount;
		}
		
		/**
		 * Resets the frame counter to 0.
		 */
		public void clearFrameCount() {
			frameCount = 0;
		}

		@Override
		public void onNewImage(CaptureStream stream, Image image) {
			frameCount++;
		}
		
		@Override
		public void onError(CaptureStream arg0, CaptureException arg1) {}
	}
}
