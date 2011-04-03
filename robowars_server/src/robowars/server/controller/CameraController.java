package robowars.server.controller;

import org.apache.log4j.Logger;

import robowars.shared.model.CameraPosition;

import com.lti.civil.CaptureDeviceInfo;
import com.lti.civil.CaptureException;
import com.lti.civil.CaptureStream;
import com.lti.civil.CaptureSystem;
import com.lti.civil.impl.jni.NativeCaptureSystemFactory;

/**
 * Handles data transfer with a single connected USB camera. This class also
 * handles access to information on the location and field of view of the 
 * connected camera (which is required for rendering calculations).
 * 
 * @author Alexander Craig
 */
public class CameraController {
	/** The logger used by this class */
	private static Logger log = Logger.getLogger(CameraController.class);
	
	/**
	 * Stores details on the camera controlled by this controller, such descriptive
	 * name and identifier.
	 */
	private transient CaptureDeviceInfo deviceInfo;
	
	/** 
	 * A text description of the device (usually the name of the camera, ex.
	 * "Logitech Quickcam Pro 9000")
	 */
	private String deviceDescription;
	
	/** Stores all information on the position, orientation, and FOV of the device */
	private CameraPosition position;
	
	/**
	 * Generates a new CameraController.
	 * Newly generated cameras default to a position of 0,0,0 facing straight
	 * along the x axis, with a field of view of 90 degrees.
	 * 
	 * @param info	The CaptureDeviceInfo object storing details on the camera
	 * 				this controller should manage.
	 */
	public CameraController(CaptureDeviceInfo info) {
		deviceInfo = info;
		deviceDescription =  deviceInfo.getDescription();
		position = new CameraPosition();
	}
	
	/**
	 * @return	The device identifier of the camera (a unique string used by
	 * 			LTI-Civil to identify the camera).
	 */
	public String getDeviceId() {
		if(deviceInfo == null) return null;
		
		return deviceInfo.getDeviceID();
	}
	
	/**
	 * @return	The name of the camera managed by this controller (usually the
	 * 			make of the camera, i.e. "Logitech Quickcam Pro 9000")
	 */
	public String getCameraName() {
		return toString();
	}
	
	/**
	 * Opens a CaptureStream for this camera.
	 * @return	An instance of CaptureStream which can be used to read frames
	 * 			from the camera.
	 */
	public CaptureStream getCaptureStream() {
		if(deviceInfo == null) return null;
		
		NativeCaptureSystemFactory captureFactory = new NativeCaptureSystemFactory();
		CaptureSystem captureSystem;
		try {
			captureSystem = captureFactory.createCaptureSystem();
			captureSystem.init();
			return  captureSystem.openCaptureDeviceStream(deviceInfo.getDeviceID());
		} catch (CaptureException e) {
			log.error("Error opening capture stream for device: " + deviceInfo.getDescription());
			return null;
		}
	}
	
	/**
	 * @return	A string representation of the camera controller.
	 */
	public String toString() {
		return deviceDescription;
	}
	
	/**
	 * Sets the position of the camera (using same axis orientation as the game
	 * model, with the z axis added for height of the camera).
	 * @param xPos	The x coordinate of the camera's position.
	 * @param yPos	The y coordinate of the camera's position.
	 * @param zPos	The z coordinate of the camera's position.
	 */
	public void setPosition(float xPos, float yPos, float zPos) {
		position.setxPos(xPos);
		position.setyPos(yPos);
		position.setzPos(zPos);
	}
	
	/**
	 * Sets the orientation of the camera (measured in degrees
	 * of clockwise offset from the positive direction of the x axis).
	 * @param horOrientation	The horizontal orientation of the camera.
	 * @param verOrientation	The vertical orientation of the camera.
	 */
	public void setOrientation(float horOrientation, float verOrientation) {
		position.setHorOrientation(horOrientation);
		position.setVerOrientation(verOrientation);
	}
	
	/**
	 * Sets the field of view of the camera
	 * @param fov	The field of view (set in degrees)
	 */
	public void setFov(float fov) {
		position.setFov(fov);
	}

	/**
	 * @return The x position coordinate of the camera
	 */
	public float getxPos() {
		return position.getxPos();
	}

	/**
	 * @return The y position coordinate of the camera
	 */
	public float getyPos() {
		return position.getyPos();
	}

	/**
	 * @return The z position coordinate of the camera
	 */
	public float getzPos() {
		return position.getzPos();
	}

	/**
	 * @return The horizontal orientation of the camera (degrees of offset from
	 * the x axis)
	 */
	public float getHorOrientation() {
		return position.getHorOrientation();
	}

	/**
	 * @return The vertical orientation of the camera (degrees of offset from the
	 * x axis)
	 */
	public float getVerOrientation() {
		return position.getVerOrientation();
	}

	/**
	 * @return The field of view of the camera
	 */
	public float getFov() {
		return position.getFov();
	}
	
	/**
	 * @return	The serializable CameraPosition object that stores all information
	 * 			on this device's position, orientation, and FOV
	 */
	public CameraPosition getPosition() {
		return position;
	}
}
