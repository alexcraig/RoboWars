package robowars.shared.model;

import java.io.Serializable;

/**
 * Entity class which stores information on the position, heading,
 * and FOV of a camera using the same coordinate system as the rest of
 * the model.
 */
public class CameraPosition implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8040566344670841049L;

	/** The default field of view to use for a new CameraPosition */
	public static float FOV_DEFAULT = 90;
	
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
	 * Generates a new CameraPosition with all fields set to 0
	 * (except FOV, which defaults to FOV_DEFAULT)
	 */
	public CameraPosition() {
		xPos = 0;
		yPos = 0;
		zPos = 0;
		horOrientation = 0;
		verOrientation = 0;
		fov = FOV_DEFAULT;
	}

	/**
	 * @return the xPos
	 */
	public float getxPos() {
		return xPos;
	}

	/**
	 * @param xPos the xPos to set
	 */
	public void setxPos(float xPos) {
		this.xPos = xPos;
	}

	/**
	 * @return the yPos
	 */
	public float getyPos() {
		return yPos;
	}

	/**
	 * @param yPos the yPos to set
	 */
	public void setyPos(float yPos) {
		this.yPos = yPos;
	}

	/**
	 * @return the zPos
	 */
	public float getzPos() {
		return zPos;
	}

	/**
	 * @param zPos the zPos to set
	 */
	public void setzPos(float zPos) {
		this.zPos = zPos;
	}

	/**
	 * @return the horOrientation
	 */
	public float getHorOrientation() {
		return horOrientation;
	}

	/**
	 * @param horOrientation the horOrientation to set
	 */
	public void setHorOrientation(float horOrientation) {
		this.horOrientation = horOrientation;
	}

	/**
	 * @return the verOrientation
	 */
	public float getVerOrientation() {
		return verOrientation;
	}

	/**
	 * @param verOrientation the verOrientation to set
	 */
	public void setVerOrientation(float verOrientation) {
		this.verOrientation = verOrientation;
	}

	/**
	 * @return the field of view (in degrees)
	 */
	public float getFov() {
		return fov;
	}

	/**
	 * @param fov the field of view to set (in degrees)
	 */
	public void setFov(float fov) {
		this.fov = fov;
	}

}
