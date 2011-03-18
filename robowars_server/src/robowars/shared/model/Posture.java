package robowars.shared.model;

import lejos.robotics.Pose;
import java.io.Serializable;

public class Posture implements Serializable{
	
	protected  Vector _location;
	protected  float _heading;

	/**
	 * Represents the location and heading(direction angle) of a robot.<br>
	 * This class includes  methods for updating the Pose to track common robot movements
	 * 
	 * This class was adapted from class Pose from the Lejos classes library for
	 * compatability with the Robowars Project.
	 * 
	 * @author Roger Glassey, Alex Dinardo
	 */
	  /**
	   * allocate a new Posture at the origin, heading  = 0:the direction  the positive X axis
	   */
	public Posture()
	{
	  _location = new Vector(0,0);
	  _heading = 0;
	}
	
	/**
	 * Create a new Posture object from a given Pose object.
	 * @param pose the pose
	 */
	public Posture(Pose pose){
		_location = new Vector(pose.getX(), pose.getY());
		_heading = pose.getHeading();
	}
	/**
	 * Allocate a new posture at location (x,y) with specified heading in degrees.
	 * 
	 * @param x the X coordinate
	 * @param y the Y coordinate
	 * @param heading the heading
	 */
	public Posture(float x, float y, float heading)
	{
	  _location = new Vector(x,y);
	  _heading = heading;
	}
	
	public Pose convertToPose(){
		return new Pose(_location.getX(), _location.getY(), _heading);
	}
	
	/**
	 * Rotate the heading through the specified angle
	 * 
	 * @param angle
	 */
	public void rotateUpdate(float angle)
	{
	  _heading += angle;
	  while(_heading < 180)_heading += 360;
	  while(_heading > 180)_heading -= 360;
	}

	/**
	 * Move the specified distance in the direction of current heading.
	 * 
	 * @param distance to move
	 */
	public void moveUpdate(float distance)
	{
	  float x = distance * (float)Math.cos(Math.toRadians(_heading));
	  float y = distance * (float)Math.sin(Math.toRadians(_heading));
	  translate(x,y);
	}
	/**
	 * Change the x and y coordinates of the posture by adding dx and dy.
	 * 
	 * @param dx  change in x coordinate
	 * @param dy  change in y coordinate
	 */
	public void translate( float dx, float dy)
	{
	    _location.setX(_location.getX()+dx);
	    _location.setY(_location.getY()+dy);
	}
	/**
	 * Sets the posture locatin and heading to the currect values resulting from travel
	 * in a circular arc.  The radius is calculated from the distance and turn angle
	 * 
	 * @param distance the dtistance traveled
	 * @param turnAngle the angle turned
	 */
	public void arcUpdate(float distance, float turnAngle)
	{
	  float dx = 0;
	    float  dy = 0;
	    double heading = (Math.toRadians(_heading));
	    if (Math.abs(turnAngle) > .5)
	    {
	      float turn = (float)Math.toRadians(turnAngle);
	     float radius = distance / turn;
	      dy = radius * (float) (Math.cos(heading) - Math.cos(heading + turn));
	      dx = radius * (float)(Math.sin(heading + turn) - Math.sin(heading));
	    } else if (Math.abs(distance) > .01)
	    {
	      dx = distance * (float) Math.cos(heading);
	      dy = distance * (float) Math.sin(heading);
	    }
	    translate((float) dx, (float) dy);
	    rotateUpdate(turnAngle);
	}
	/**
	 * 
	 * Calculates the absolute angle to destination from the current location of the pose
	 * 
	 * @param destination
	 * @return angle in degrees
	 */
	public float angleTo(Vector destination)
	{
	  Vector d = delta(destination);
	  return (float)Math.toDegrees(Math.atan2(d.getY(),d.getX()));
	}
	/**
	 * Get the distance to the destination
	 * 
	 * @param destination
	 * @return  the distance
	 */
	public float distanceTo(Vector destination)
	{
	   Vector d = delta(destination);
	  return (float) Math.sqrt( d.getX()*d.getX() + d.getY()*d.getY());
	}
	private Vector delta(Vector d)
	{
	  return new Vector((float)(d.getX() - _location.getX()),
	          (float) (d.getY() - _location.getY()));
	}
	/**
	 * returns the heading (direction angle) of the Posture
	 * 
	 * @return the heading
	 */
	public float getHeading() { return _heading ; }
	/**
	 * Get the X coordinate
	 * 
	 * @return the X coordinate
	 */
	public float getX(){ return (float) _location.getX();}
	/**
	 * Get the Y coordinate
	 * 
	 * @return the Y coordinate
	 */
	public float getY() {return (float)_location.getY();}
	/**
	 * Get the location as a Point
	 * 
	 * @return the location as a point
	 */
	public Vector getLocation() { return _location;}

	/**
	 * Set the location of the posture
	 * 
	 * @param p the new location
	 */
	public void setLocation(Vector p)
	{
	  _location = p;
	}
	public void setHeading(float heading )
	{
	  _heading = heading;
	}

}

