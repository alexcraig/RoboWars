package robowars.test;

import com.lti.civil.CaptureStream;
import com.lti.civil.Image;

import robowars.server.controller.MediaStreamer;

/**
 * A testing version of the MediaStreamer which does not perform
 * JPEG encoding of video frames, but rather keeps a count of
 * video frames received for testing purposes.
 * 
 * @author Alexander Craig
 */
public class TestMediaStreamer extends MediaStreamer {
	/** The number of video frames read from a camera */
	int frameCount;

	public TestMediaStreamer(int port) {
		super(port);
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
	/** Called whenever a new frame is read from the active capture stream */
	public void onNewImage(CaptureStream stream, Image image) {
		frameCount++;
	}
}
