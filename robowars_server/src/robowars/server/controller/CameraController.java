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
	 * The X,Y, and Z coordinates of the camera (should use the same units and
	 * axis orientation as the game model, with the Z axis added to represent
	 * height). 
	 */
	private float xPos, yPos, zPos;
	
	/**
	 * The horizontal and vertical orientation of the camera (measured in degrees
	 * of clockwise offset from the positive direction of the x axis).
	 */
	private float horOrientation, verOrientation;
	
	/**
	 * The field of view of the camera (in degrees)
	 */
	private float fov;
	
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
		xPos = 0;
		yPos = 0;
		zPos = 0;
		horOrientation = 0;
		verOrientation = 0;
		fov = 90;
	}
	
	/**
	 * @return	The name of the camera managed by this controller (usually the
	 * 			make of the camera, i.e. "Logitech Quickcam Pro 9000")
	 */
	public String getCameraName() {
		return deviceInfo.getName();
	}
	
	/**
	 * @return	A MediaLocator object specifying where the video source for this
	 * 			camera can be read.
	 */
	public MediaLocator getMediaLocator() {
		return deviceInfo.getLocator();
	}
	
	/**
	 * @return	A string representation of the camera controller.
	 */
	public String toString() {
		return deviceInfo.getName();
	}
	
	/**
	 * Sets the position of the camera (using same axis orientation as the game
	 * model, with the z axis added for height of the camera).
	 * @param xPos	The x coordinate of the camera's position.
	 * @param yPos	The y coordinate of the camera's position.
	 * @param zPos	The z coordinate of the camera's position.
	 */
	public void setPosition(float xPos, float yPos, float zPos) {
		this.xPos = xPos;
		this.yPos = yPos;
		this.zPos = zPos;
	}
	
	/**
	 * Sets the orientation of the camera (measured in degrees
	 * of clockwise offset from the positive direction of the x axis).
	 * @param horOrientation	The horizontal orientation of the camera.
	 * @param verOrientation	The vertical orientation of the camera.
	 */
	public void setOrientation(float horOrientation, float verOrientation) {
		this.horOrientation = horOrientation;
		this.verOrientation = verOrientation;
	}
	
	/**
	 * Sets the field of view of the camera
	 * @param fov	The field of view (set in degrees)
	 */
	public void setFov(float fov) {
		this.fov = fov;
	}

	/**
	 * @return The x position coordinate of the camera
	 */
	public float getxPos() {
		return xPos;
	}

	/**
	 * @return The y position coordinate of the camera
	 */
	public float getyPos() {
		return yPos;
	}

	/**
	 * @return The z position coordinate of the camera
	 */
	public float getzPos() {
		return zPos;
	}

	/**
	 * @return The horizontal orientation of the camera (degrees of offset from
	 * the x axis)
	 */
	public float getHorOrientation() {
		return horOrientation;
	}

	/**
	 * @return The vertical orientation of the camera (degrees of offset from the
	 * x axis)
	 */
	public float getVerOrientation() {
		return verOrientation;
	}

	/**
	 * @return The field of view of the camera
	 */
	public float getFov() {
		return fov;
	}
}
