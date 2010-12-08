package robowars.server.controller;

import javax.media.CaptureDeviceInfo;
import javax.media.MediaLocator;

/**
 * Handles data transfer with a single connected USB camera. This class also
 * stores information on the location and field of view of the connected
 * camera (which is required for rendering calculations).
 */
public class CameraController {	
	/**
	 * Stores details on the camera controlled by this controller, such as its
	 * name, supported formats, and location for the media source.
	 */
	private CaptureDeviceInfo deviceInfo;
	
	/**
	 * Generates a new CameraController
	 * @param info	The CaptureDeviceInfo object storing details on the camera
	 * 				this controller should manage.
	 */
	public CameraController(CaptureDeviceInfo info) {
		deviceInfo = info;
	}
	
	/**
	 * @return	The name of the camera managed by this controller (usually the
	 * 			make of the camera, i.e. "Logitech Quickcam Pro 9000")
	 */
	public String getCameraName() {
		return deviceInfo.getName();
	}
}
