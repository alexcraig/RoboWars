package robowars.robot;

import lejos.robotics.Pose;
import lejos.geom.Point;
import lejos.robotics.*;
import lejos.robotics.navigation.Pilot;
import lejos.robotics.navigation.TachoPilot;

/*
 * WARNING: THIS CLASS IS SHARED BETWEEN THE classes AND pccomms PROJECTS.
 * DO NOT EDIT THE VERSION IN pccomms AS IT WILL BE OVERWRITTEN WHEN THE PROJECT IS BUILT.
 */

/**
 * The SimpleNavigator class uses dead reckoning to  keep track of its robot pose (the location in the plane
 * and its heading -  the direction in which it moves).  While dead reckoning is relatively
 * easy to implement and very quick to caolculate,  its disadvantage
 * is that errors in the estimated pose accumulate.<br>
 * SimpleNavigator can perform  three elementary movements  in a plane:  trvel in  a straignt line,
 * move in the arc of a circle, and a rotate  in place.  The movement commands
 * have an immediate return option which is useful, for example, for a client
 * class that uses s SimpleNavigataor to detect obstacles or monitor incoming messages while moving.
 *  <br>
 * This class uses a private Pilot  object to execute these moves.  The Pilot directly
 * controls the hardware, which must be able to  turn in place,
 * for example using 2 wheel differential steering.  The  Pilot is passed to the Navitator
 * a the parameter of its constructor.  After the Navigator is constructed,
 * the client has no further need for the Pilot, but issues commands to the Navigator.
 * If the client  bypasses the navigator and issues commsnds directly to the Pilot, this  will destroy
 * the accuracy of the Navigataor's pose.<br>
 *<b>A note about coordinates:</b> All angles are in degrees, distances in the units used to specify robot dimensions.
 * Angles related to positions in the plane are relative to the X axis ;  direction of the Y axis is 90 degrees.
 * The x and y coordinate values and the direction angle are all initialized to 0, so if the first move is forward() the robot will run along
 * the x axis.<br>
 *
 */
public class SimpleNavigator {


    /**
     * Allocates a SimpleNavigator with a Pilot that you supply.
     * @param  pilot can be  any class that implements the pilot interface
     */
    public SimpleNavigator(Pilot pilot) {
        this.pilot = pilot;
    }

    /**
     * Allocates a SimpleNavigator object and initializes it with a new TachoPilot <br>
     * If you want to use a different Pilot class, use the single parameter constructor.
     * @param wheelDiameter The diameter of the wheel, usually printed on the Lego tire.
     *  Use any units you wish (e.g. 56 mm = 5.6 cm = 2.36 in)
     * @param trackWidth The distance from the center of the left tire to the center
     * of the right tire, same units as wheel diameter
     * @param rightMotor The motor used to drive the right wheel e.g. Motor.C.
     * @param leftMotor The motor used to drive the left wheel e.g. Motor.A.
     * @param reverse  If motor.forward() dives the robot backwars, set this parameter true.
   * 
   * @deprecated The correct way is to create the Pilot in advance and to use that in construction of the
   *             SimpleNavigator. Otherwise the SimpleNavigator needs to know detail it should not care about!
     */
    public SimpleNavigator(float wheelDiameter, float trackWidth, TachoMotor leftMotor, TachoMotor rightMotor, boolean reverse) {
      // In the signature Motor was not changed to TachoMotor. This method only saves one to write "new TachoPilot" at the
      // cost of maintaining this method and comments, thus it should not be used!
        pilot = new TachoPilot(wheelDiameter, trackWidth, leftMotor, rightMotor, reverse);
    }

    /**
     * Allocates a SimpleNavigator object and initializes it with a new TachoPilot.<br>
     * If you want to use a different Pilot class, use the single parameter constructor.
     * @param wheelDiameter The diameter of the wheel, usually printed on the Lego tire.
     *  Use any units you wish (e.g. 56 mm = 5.6 cm = 2.36 in)
     * @param trackWidth The distance from the center of the left tire to the center
     * of the right tire, sane units as wheel diameter
     * @param rightMotor The motor used to drive the right wheel e.g. Motor.C.
     * @param leftMotor The motor used to drive the left wheel e.g. Motor.A
   * 
   * @deprecated The correct way is to create the Pilot in advance and to use that in construction of the
   *             SimpleNavigator. Otherwise the SimpleNavigator needs to know detail it should not care about!
     */
    public SimpleNavigator(float wheelDiameter, float trackWidth, TachoMotor leftMotor, TachoMotor rightMotor) {
      // In the signature Motor was not changed to TachoMotor. This method only saves one to write "new TachoPilot" at the
      // cost of maintaining this method and comments, thus it should not be used!
        pilot = new TachoPilot(wheelDiameter, trackWidth, leftMotor, rightMotor);
    }
/**
 *gets the current value of the X coordinate
 * @return current x
 */
  public float getX()
  {
    updatePose();
    _current = !isMoving();
    return _pose.getX();
  }
/**
 * gets the current value of the Y coordinate
 * @return current Y
 */
  public float getY()
  {
    updatePose();
    _current = !isMoving();
    return _pose.getY();
  }
/**
 * gets the current value of the robot heading 
 * @return current heading
 */
  public float getHeading()
  {
    updatePose();
        _current = !isMoving();
    return _pose.getHeading();
  }
  /**
   * gets the current robot pose
   * @return current pose
   */
public float getAngle()
{
  return getHeading();
}
 public void forward()
 {
   updatePose();
   _current = false;
   pilot.forward();
 }
 public void backward()
 {
    updatePose();
   _current = false;
   pilot.backward();
 }
 public void rotateLeft()
 {
   steer(200);
 }
 public void rotateRight()
 {
   steer(-200);
 }
  public Pose getPose()
  {
    updatePose();
        _current = !isMoving();
    return _pose;
  }
  public void updatePosition()
  {
    updatePose();
  }

/**
 * sets the robot pose to the new coordinates and heading
 * @param x coordinate
 * @param y coordinate
 * @param heading direction of robot forward movement
 */
  public void setPose(float x, float y, float heading)
  {
    _pose.setLocation(new Point(x, y));
    _pose.setHeading(heading);
  }
/**
 * sets the robot pose
 * @param pose new pose
 */
  public void setPose(Pose pose)
  {
    _pose = pose;
  }
  public void setPosition(float x, float y, float heading)
  {
    setPose(x,y,heading);
  }
/**
 * sets the robots movement speed  - distance units/second
 * @param speed
 */
  public void setMoveSpeed(float speed)
  {
    pilot.setMoveSpeed(speed);
  }
/**
 * sets the robot turn speed  -degrees/second
 * @param speed
 */
  public void setTurnSpeed(float speed)
  {
    pilot.setTurnSpeed(speed);
  }
/**
 * Stops the robot. Depending on the robot speed, it travels a bit before actually
 * coming to a complete halt.
 */
  public void stop()
  {
    pilot.stop();
    updatePose();
    _current = true;
  }
/**
 * returns true if the robot is moving
 * @return true if it is moving
 */
  public boolean isMoving()
  {
    return pilot.isMoving();
  }

  /**
   * Moves the NXT robot a specific distance. A positive value moves it forwards and
   * a negative value moves it backwards.
   * @param distance The positive or negative distance to move the robot, same units as _wheelDiameter
   */
  public void travel(float distance)
  {
    travel(distance, false);
  }

  /**
   * Moves the NXT robot a specific distance. A positive value moves it forwards and
   * a negative value moves it backwards.
   * @param distance The positive or negative distance to move the robot, same units as _wheelDiameter
   * @param immediateReturn if true, the method returns immediately
   */
  public void travel(float distance, boolean immediateReturn)
  {
    updatePose();
        _current = false;
    pilot.travel(distance, immediateReturn);
  }

  /**
   * Rotates the NXT robot through a specific number of degrees in a direction (+ or -).
   * @param angle Angle to rotate in degrees. A positive value rotates left, a negative value right.
   **/
  public void rotate(float angle)
  {
    rotate(angle, false);
  }

  /**
   * Rotates the NXT robot through a specific number of degrees in a direction (+ or -).
   *  If immediateReturn is true, method returns immidiately.
   * @param angle Angle to rotate in degrees. A positive value rotates left, a negative value right.
   * @param immediateReturn if true, the method returns immediately
   */
  public void rotate(float angle, boolean immediateReturn)
  {
    int turnAngle = Math.round(angle);
    updatePose();
      _current = false;
    pilot.rotate(turnAngle, immediateReturn);
  }

  /**
   * Rotates the NXT robot to point in a specific direction, using the smallest
   * rotation necessary
   * @param angle The angle to rotate to, in degrees.
   */
  public void rotateTo(float angle)
  {
    rotateTo(angle, false);
  }

  /**
   * Rotates the NXT robot to point in a specific direction relative to the x axis.  It make the smallest
   * rotation  necessary .
   * If immediateReturn is true, method returns immidiately
   * @param angle The angle to rotate to, in degrees.
   * @param immediateReturn if true,  method returns immediately
   */
  public void rotateTo(float angle, boolean immediateReturn)
  {
    float turnAngle = angle - _pose.getHeading();
    while (turnAngle < -180)turnAngle += 360;
    while (turnAngle > 180) turnAngle -= 360;
    rotate(turnAngle, immediateReturn);
  }
/**
 * Robot moves to grid coordinates x,y.  First it rotates to the proper direction
 * then travels in a straight line to that point
 * @param x destination X coordinate
 * @param y destination Y coordinate
 */
  public void goTo(float x, float y)
  {
    goTo(x, y, false);
  }
/**
 * Robot moves to grid coordinates x,y.  First it rotates to the proper direction
 * then travels in a straight line to that point
 * @param x destination X coordinate
 * @param y destination Y coordinate
 */
  public void goTo(float x, float y, boolean immediateReturn)
  {
    rotateTo(angleTo(x, y));
    travel(distanceTo(x, y), immediateReturn);
  }
/**
 * Returns the distance from robot current location to the point with coordinates x,y
 * @param x coordinate of destination
 * @param y coordinate of destination
 * @return the distance
 */
  public float distanceTo(float x, float y)
  {
    updatePose();
    _current = !isMoving();
    return _pose.distanceTo(new Point(x, y));
  }
/**
 * Returns the  angle from robot current location to the point with coordinates x,y
 * @param x coordinate of destination
 * @param y coordinate of destination
 * @return angle
 */
  public float angleTo(float x, float y)
  {
    updatePose();
    _current = !isMoving();
    return _pose.angleTo(new Point(x, y));
  }

    public void updatePose()
  {
      if(_current) return;
    float distance = pilot.getTravelDistance() - _distance0;
    float turnAngle = pilot.getAngle() - _angle0;
    double dx = 0;
    double dy = 0;
    double headingRad = (Math.toRadians(_pose.getHeading()));
    if (Math.abs(turnAngle) > .5)
    {
      double turnRad = Math.toRadians(turnAngle);
      double radius = distance / turnRad;
      dy = radius * (Math.cos(headingRad) - Math.cos(headingRad + turnRad));
      dx = radius * (Math.sin(headingRad + turnRad) - Math.sin(headingRad));
    } else if (Math.abs(distance) > .01)
    {
      dx = distance * (float) Math.cos(headingRad);
      dy = distance * (float) Math.sin(headingRad);
    }
    _pose.translate((float) dx, (float) dy);
    _pose.rotateUpdate(turnAngle);
    _angle0 = pilot.getAngle();
    _distance0 = pilot.getTravelDistance();
  }

    /**
     * Starts  the NXT robot moving in a circular path with a specified radius; <br>
     * The center of the turning circle is on the left side of the robot if parameter radius is positive
     * and on the right if negative.  <br>
     * @param radius - the radius of the circular path. If positive, the left wheel is on the inside of the turn.
     * If negative, the left wheel is on the outside.
     */
    public void arc(float radius) {
        pilot.arc(radius);
    }

    /**
     * Moves the NXT robot in a circular arc through the specified angle;  <br>
     * The center of the turning circle is on the left side of the robot if parameter radius is positive
     * and on the right if negative.
     * Robot will stop when total rotation equals angle. If angle is negative, robot will move travel backwards.
     * <br>See also {@link #travelArc(float radius, float distance)}
   * @param radius - the radius of the circular path. If positive, the left wheel is on the inside of the turn.
     * If negative, the left wheel is on the outside.
     */
    public void arc(float radius, int angle) {
        arc(radius, angle, false);
    }

    /**
     * Moves the NXT robot in a circular arc through a specific angle; <br>
     * The center of the turning circle is on the left side of the robot if parameter radius is positive
     * and on the right if negative.
     * Robot will stop when total rotation equals angle. If angle is negative, robot will travel backwards.
     * <br>See also {@link #travelArc(float radius, float distance, boolean immedisteReturn)}
      * @param radius - the radius of the circular path. If positive, the left wheel is on the inside of the turn.
     * If negative, the left wheel is on the outside.
     * @param angle The sign of the angle determines the direction of robot motion
     * @param immediateReturn if true, the method returns immediately 
     */
    public void arc(float radius, int angle, boolean immediateReturn) {
        updatePose();
        _current = false;
        pilot.arc(radius, angle, immediateReturn);
    }
    
     /**
     * Moves the NXT robot in a circular arc through a specific distance; <br>
     * The center of the turning circle is on the left side of the robot if parameter radius is positive
     * and on the right if negative.
     * Robot will stop when distance traveled equals distance. If distance is negative, robot will travel backwards.
     * <br>See also {@link #arc(float radius, int angle)}
   
     * @param radius  of the turning circle; the sign determines if the center if the turn is left or right of the robot.
     * @param distance The sign of the distance determines the direction of robot motion
     * updatePosition() before the robot moves again.
     */
    public void travelArc(float radius, float distance){
      travelArc(radius,distance,false);
    }
    
    /**
     * Moves the NXT robot in a circular arc through a specific distance; <br>
     * The center of the turning circle is on the left side of the robot if parameter radius is positive
     * and on the right if negative.
     * Robot will stop when distance traveled equals distance. If distance is negative, robot will travel backwards.
     * <br>See also {@link #arc(float radius, int angle, boolean immediateReturn)}
     * @param radius  of the turning circle; the sign determines if the center if the turn is left or right of the robot.
     * @param distance The sign of the distance determines the direction of robot motion
     * @param immediateReturn if true, the method returns immediately.
     */
    public void travelArc(float radius, float distance, boolean immediateReturn)
    {
      updatePose();
        pilot.travelArc(radius, distance, immediateReturn);
    }

      /**
   * Starts the robot moving along a curved path. This method is similar to the
   * {@link #arc(float radius)} method except it uses a ratio of motor
   * speeds to determine the curvature of the path and therefore has the ability to drive straight. This makes
   * it useful for line following applications.
   * <p>
   * The <code>turnRate</code> specifies the sharpness of the turn, between -200 and +200.<br>
   * The <code>turnRate</code> is used to calculate the  ratio of inner wheel speed to outer wheel speed <b>as a percent</b>.<br>
   * <I>Formula:</I> <code>ratio = 100 - abs(turnRate)</code>.<br>
   * When the ratio is negative, the outer and inner wheels rotate in
   * opposite directions.
   * <p>
   * If <code>turnRate</code> is positive, the center of the turning circle is on the left side of the robot.<br>
   * If <code>turnRate</code> is negative, the center of the turning circle is on the right side of the robot.<br>
   * If <code>turnRate</code> is zero, the robot travels in a straight line
   * <p>
   * Examples of how the formula works:
   * <UL>
   * <LI><code>steer(0)</code> -> inner and outer wheels turn at the same speed, travel  straight
   * <LI><code>steer(25)</code> -> the inner wheel turns at 75% of the speed of the outer wheel, turn left
   * <LI><code>steer(100)</code> -> the inner wheel stops and the outer wheel is at 100 percent, turn left
   * <LI><code>steer(200)</code> -> the inner wheel turns at the same speed as the outer wheel - a zero radius turn.
   * </UL>
   * <p>
   * Note: If you have specified a drift correction in the constructor it will not be applied in this method.
   *
   * @param turnRate If positive, the left side of the robot is on the inside of the turn. If negative,
   * the left side is on the outside.
   */
  public void steer(int turnRate)
  {
    updatePose();
    _current = false;
    pilot.steer(turnRate);
  }

  /**
   * Moves the robot along a curved path through a specified turn angle. This method is similar to the
   * {@link #arc(float radius , int angle)} method except it uses a ratio of motor
   * speeds to determine the curvature of the  path and therefore has the ability to drive straight. This makes
   * it useful for line following applications. This method does not return until the robot has
   * completed moving <code>angle</code> degrees along the arc.<br>
   * The <code>turnRate</code> specifies the sharpness of the turn, between -200 and +200.<br>
   * For details about how this paramet works. see {@link #steer(int turnRate) }
   * <p>
   * The robot will stop when the degrees it has moved along the arc equals <code>angle</code>.<br>
   * If <code>angle</code> is positive, the robot will move travel forwards.<br>
   * If <code>angle</code> is negative, the robot will move travel backwards.
   * If <code>angle</code> is zero, the robot will not move and the method returns immediately.
   * <p>
   * Note: If you have specified a drift correction in the constructor it will not be applied in this method.
   *
   * @param turnRate If positive, the left side of the robot is on the inside of the turn. If negative,
   * the left side is on the outside.
   * @param angle The angle through which the robot will rotate. If negative, robot traces the turning circle backwards.
   */
  public void steer(int turnRate, int angle)
  {
    pilot.steer(turnRate, angle, false);
  }
/**
   * Moves the robot along a curved path for a specified angle of rotation. This method is similar to the
   * {@link #arc(float radius, int angle, boolean immediateReturn)} method except it uses a ratio of motor
   * speeds to speeds to determine the curvature of the path and therefore has the ability to drive straight.
   * This makes it useful for line following applications. This method has the ability to return immediately
   * by using the <code>immediateReturn</code> parameter set to <b>true</b>.
 
   * <p>
   * The <code>turnRate</code> specifies the sharpness of the turn, between -200 and +200.<br>
   * For details about how this paramet works, see {@link #steer(int turnRate) }
   * <p>
   * The robot will stop when the degrees it has moved along the arc equals <code>angle</code>.<br>
   * If <code>angle</code> is positive, the robot will move travel forwards.<br>
   * If <code>angle</code> is negative, the robot will move travel backwards.
   * If <code>angle</code> is zero, the robot will not move and the method returns immediately.
   * <p>
   * Note: If you have specified a drift correction in the constructor it will not be applied in this method.
   *
   * @param turnRate If positive, the left side of the robot is on the inside of the turn. If negative,
   * the left side is on the outside.
   * @param angle The angle through which the robot will rotate. If negative, robot traces the turning circle backwards.
   * @param immediateReturn If immediateReturn is true then the method returns immediately.
   */
  public void steer(int turnRate, int angle, boolean immediateReturn)
  {
    updatePose();
    _current = false;
    pilot.steer(turnRate, angle, immediateReturn);
  }
  public RoboWarsTachoPilot getPilot(){
	  return (RoboWarsTachoPilot) pilot;
  }
  public void setCurrent(boolean curr){
	  _current=curr;
  }
        // orientation and co-ordinate data
    private Pose _pose = new Pose();
    private float _distance0 = 0;
    private float _angle0 = 0;
    private boolean _current = false; //prevent unnecessary pose updates
    private Pilot pilot;
}

