package robowars.robot;

import lejos.nxt.Battery;
import lejos.nxt.LCD;
import lejos.robotics.TachoMotor;
import lejos.robotics.navigation.Pilot;

/*
 * NOTICE: This is based off the TachoPilot class provided by LeJOS
 * There have been modifications to allow geared wheels and to have a 
 * functioning 360 degree steering system.
 * @mwright
 */

/*
 * WARNING: THIS CLASS IS SHARED BETWEEN THE classes AND pccomms PROJECTS.
 * DO NOT EDIT THE VERSION IN pccomms AS IT WILL BE OVERWRITTEN WHEN THE PROJECT IS BUILT.
 */

/**
 * The TachoPilot class is a software abstraction of the Pilot mechanism of a
 * NXT robot. It contains methods to control robot movements: travel forward or
 * backward in a straight line or a circular path or rotate to a new direction.<br>
 * Note: this class will only work with two independently controlled motors to
 * steer differentially, so it can rotate within its own footprint (i.e. turn on
 * one spot).<br>
 * It can be used with robots that have reversed motor design: the robot moves
 * in the direction opposite to the the direction of motor rotation. Uses the
 * Motor class, which regulates motor speed using the NXT motor's built in
 * tachometer.<br>
 * Some methods optionally return immediately so the thread that called the
 * method can monitor sensors and call stop() if necessary.<br>
 * Uses the smoothAcceleration property of Motors to improve motor
 * synchronization when starting a movement. Example:
 * <p>
 * <code><pre>
 * Pilot pilot = new TachoPilot(2.1f, 4.4f, Motor.A, Motor.C, true);  // parameters in inches
 * pilot.setRobotSpeed(10);                                           // inches per second
 * pilot.travel(12);                                                  // inches
 * pilot.rotate(-90);                                                 // degree clockwise
 * pilot.travel(-12,true);
 * while(pilot.isMoving())Thread.yield();
 * pilot.rotate(-90);
 * pilot.rotateTo(270);
 * pilot.steer(-50,180,true);
 * while(pilot.isMoving())Thread.yield();
 * pilot.steer(100);
 * try{Thread.sleep(1000);}
 * catch(InterruptedException e){}
 * pilot.stop();
 * </pre></code>
 * </p>
 * 
 * Note: if you are sure you do not want to use any other part of navigation you
 * can as well use "TachoPilot pilot = new TachoPilot(...)" instead of
 * "Pilot pilot = new TachoPilot(...)"
 **/
public class RoboWarsTachoPilot implements Pilot {

	/**
	 * Gear Ratio for the motor to the wheels
	 */
	protected final float _gearRatio=3.39f;
	/**
	 * Left motor.
	 */
	protected final TachoMotor _left;

	/**
	 * Right motor.
	 */
	protected final TachoMotor _right;

	/**
	 * Left motor degrees per unit of travel.
	 */
	protected final float _leftDegPerDistance;

	/**
	 * Right motor degrees per unit of travel.
	 */
	protected final float _rightDegPerDistance;

	/**
	 * Left motor revolutions for 360 degree rotation of robot (motors running
	 * in opposite directions). Calculated from wheel diameter and track width.
	 * Used by rotate() and steer() methods.
	 **/
	protected final float _leftTurnRatio;

	/**
	 * Right motor revolutions for 360 degree rotation of robot (motors running
	 * in opposite directions). Calculated from wheel diameter and track width.
	 * Used by rotate() and steer() methods.
	 **/
	protected final float _rightTurnRatio;

	/**
	 * Speed of robot for moving in wheel diameter units per seconds. Set by
	 * setSpeed(), setMoveSpeed()
	 */
	protected float _robotMoveSpeed;

	/**
	 * Speed of robot for turning in degree per seconds.
	 */
	protected float _robotTurnSpeed;

	/**
	 * Motor speed degrees per second. Used by forward(),backward() and steer().
	 */
	protected int _motorSpeed;

	/**
	 * Motor rotation forward makes robot move forward if parity == 1.
	 */
	private byte _parity;

	/**
	 * If true, motor speed regulation is turned on. Default = true.
	 */
	private boolean _regulating = true;

	/**
	 * Distance between wheels. Used in steer() and rotate().
	 */
	protected final float _trackWidth;

	/**
	 * Diameter of left wheel.
	 */
	protected final float _leftWheelDiameter;

	/**
	 * Diameter of right wheel.
	 */
	protected final float _rightWheelDiameter;

	/**
	 * Allocates a TachoPilot object, and sets the physical parameters of the
	 * NXT robot.<br>
	 * Assumes Motor.forward() causes the robot to move forward.
	 * 
	 * @param wheelDiameter
	 *            Diameter of the tire, in any convenient units (diameter in mm
	 *            is usually printed on the tire).
	 * @param trackWidth
	 *            Distance between center of right tire and center of left tire,
	 *            in same units as wheelDiameter.
	 * @param leftMotor
	 *            The left Motor (e.g., Motor.C).
	 * @param rightMotor
	 *            The right Motor (e.g., Motor.A).
	 */
	public RoboWarsTachoPilot(final float wheelDiameter, final float trackWidth,
			final TachoMotor leftMotor, final TachoMotor rightMotor) {
		this(wheelDiameter, trackWidth, leftMotor, rightMotor, false);
	}

	/**
	 * Allocates a TachoPilot object, and sets the physical parameters of the
	 * NXT robot.<br>
	 * 
	 * @param wheelDiameter
	 *            Diameter of the tire, in any convenient units (diameter in mm
	 *            is usually printed on the tire).
	 * @param trackWidth
	 *            Distance between center of right tire and center of left tire,
	 *            in same units as wheelDiameter.
	 * @param leftMotor
	 *            The left Motor (e.g., Motor.C).
	 * @param rightMotor
	 *            The right Motor (e.g., Motor.A).
	 * @param reverse
	 *            If true, the NXT robot moves forward when the motors are
	 *            running backward.
	 */
	public RoboWarsTachoPilot(final float wheelDiameter, final float trackWidth,
			final TachoMotor leftMotor, final TachoMotor rightMotor,
			final boolean reverse) {
		this(wheelDiameter, wheelDiameter, trackWidth, leftMotor, rightMotor,
				reverse);
	}

	/**
	 * Allocates a TachoPilot object, and sets the physical parameters of the
	 * NXT robot.<br>
	 * 
	 * @param leftWheelDiameter
	 *            Diameter of the left wheel, in any convenient units (diameter
	 *            in mm is usually printed on the tire).
	 * @param rightWheelDiameter
	 *            Diameter of the right wheel. You can actually fit
	 *            intentionally wheels with different size to your robot. If you
	 *            fitted wheels with the same size, but your robot is not going
	 *            straight, try swapping the wheels and see if it deviates into
	 *            the other direction. That would indicate a small difference in
	 *            wheel size. Adjust wheel size accordingly. The minimum change
	 *            in wheel size which will actually have an effect is given by
	 *            minChange = A*wheelDiameter*wheelDiameter/(1-(A*wheelDiameter)
	 *            where A = PI/(moveSpeed*360). Thus for a moveSpeed of 25
	 *            cm/second and a wheelDiameter of 5,5 cm the minChange is about
	 *            0,01058 cm. The reason for this is, that different while sizes
	 *            will result in different motor speed. And that is given as an
	 *            integer in degree per second.
	 * @param trackWidth
	 *            Distance between center of right tire and center of left tire,
	 *            in same units as wheelDiameter.
	 * @param leftMotor
	 *            The left Motor (e.g., Motor.C).
	 * @param rightMotor
	 *            The right Motor (e.g., Motor.A).
	 * @param reverse
	 *            If true, the NXT robot moves forward when the motors are
	 *            running backward.
	 */
	public RoboWarsTachoPilot(final float leftWheelDiameter,
			final float rightWheelDiameter, final float trackWidth,
			final TachoMotor leftMotor, final TachoMotor rightMotor,
			final boolean reverse) {
		// left
		_left = leftMotor;
		_leftWheelDiameter = leftWheelDiameter;
		_leftTurnRatio = trackWidth / leftWheelDiameter;
		_leftDegPerDistance = 360 / ((float) Math.PI * leftWheelDiameter);
		// right
		_right = rightMotor;
		_rightWheelDiameter = rightWheelDiameter;
		_rightTurnRatio = trackWidth / rightWheelDiameter;
		_rightDegPerDistance = 360 / ((float) Math.PI * rightWheelDiameter);
		// both
		_trackWidth = trackWidth;
		_parity = (byte) (reverse ? -1 : 1);
		setSpeed(360);
	}

	/**
	 * @return left motor.
	 */
	public TachoMotor getLeft() {
		return _left;
	}

	/**
	 * @return right motor.
	 */
	public TachoMotor getRight() {
		return _right;
	}

	/**
	 * @return tachoCount of left motor. Positive value means motor has moved
	 *         the robot forward.
	 */
	public int getLeftCount() {
		return _parity * _left.getTachoCount();
	}

	/**
	 * @return tachoCount of the right motor. Positive value means motor has
	 *         moved the robot forward.
	 */
	public int getRightCount() {
		return _parity * _right.getTachoCount();
	}

	/**
	 * @return actual speed of left motor in degrees per second. A negative
	 *         value if motor is rotating backwards. Updated every 100 ms.
	 **/
	public int getLeftActualSpeed() {
		return _left.getRotationSpeed();
	}

	/**
	 * @return actual speed of right motor in degrees per second. A negative
	 *         value if motor is rotating backwards. Updated every 100 ms.
	 **/
	public int getRightActualSpeed() {
		return _right.getRotationSpeed();
	}

	/**
	 * @return ratio of motor revolutions per 360 degree rotation of the robot.
	 *         If your robot has wheels with different size, it is the average.
	 */
	public float getTurnRatio() {
		return (_leftTurnRatio + _rightTurnRatio) / 2.0f;
	}

	/**
	 * Sets speed of both motors, as well as moveSpeed and turnSpeed. Only use
	 * if your wheels have the same size.
	 * 
	 * @param speed
	 *            The wanted speed in degrees per second.
	 */
	public void setSpeed(final int speed) {
		_motorSpeed = speed;
		_robotMoveSpeed = speed
				/ Math.max(_leftDegPerDistance, _rightDegPerDistance);
		_robotTurnSpeed = speed / Math.max(_leftTurnRatio, _rightTurnRatio);
		setSpeed(speed, speed);
	}

	private void setSpeed(final int leftSpeed, final int rightSpeed) {
		_left.regulateSpeed(_regulating);
		_left.smoothAcceleration(!isMoving());
		_right.regulateSpeed(_regulating);
		_right.smoothAcceleration(!isMoving());
		_left.setSpeed(leftSpeed);
		_right.setSpeed(rightSpeed);
	}

	/**
	 * also sets _motorSpeed
	 * 
	 * @see lejos.robotics.navigation.Pilot#setMoveSpeed(float)
	 */
	public void setMoveSpeed(float speed) {
		_robotMoveSpeed = speed;
		_motorSpeed = Math.round(0.5f * speed
				* (_leftDegPerDistance + _rightDegPerDistance));
		setSpeed(Math.round(speed * _leftDegPerDistance), Math.round(speed
				* _rightDegPerDistance));
	}

	/**
	 * @see lejos.robotics.navigation.Pilot#getMoveSpeed()
	 */
	public float getMoveSpeed() {
		return _robotMoveSpeed;
	}

	/**
	 * @see lejos.robotics.navigation.Pilot#getMoveMaxSpeed()
	 */
	public float getMoveMaxSpeed() {
		// it is generally assumed, that the maximum accurate speed of Motor is
		// 100 degree/second * Voltage
		return Battery.getVoltage() * 100.0f
				/ Math.max(_leftDegPerDistance, _rightDegPerDistance);
		// max degree/second divided by degree/unit = unit/second
	}

	/**
	 * @see lejos.robotics.navigation.Pilot#setTurnSpeed(float)
	 */
	public void setTurnSpeed(float speed) {
		_robotTurnSpeed = speed;
		setSpeed(Math.round(speed * _leftTurnRatio), Math.round(speed
				* _rightTurnRatio));
	}

	/**
	 * @see lejos.robotics.navigation.Pilot#getTurnSpeed()
	 */
	public float getTurnSpeed() {
		return _robotTurnSpeed;
	}

	/**
	 * @see lejos.robotics.navigation.Pilot#getTurnMaxSpeed()
	 */
	public float getTurnMaxSpeed() {
		// it is generally assumed, that the maximum accurate speed of Motor is
		// 100 degree/second * Voltage
		return Battery.getVoltage() * 100.0f
				/ Math.max(_leftTurnRatio, _rightTurnRatio);
		// max degree/second divided by degree/unit = unit/second
	}

	/**
	 * Moves the NXT robot forward until stop() is called.
	 */
	public void forward() {
		setSpeed(Math.round(_robotMoveSpeed * _leftDegPerDistance), Math
				.round(_robotMoveSpeed * _rightDegPerDistance));
		if (_parity == 1) {
			fwd();
		} else {
			bak();
		}
	}

	/**
	 * Moves the NXT robot backward until stop() is called.
	 */
	public void backward() {
		setSpeed(Math.round(_robotMoveSpeed * _leftDegPerDistance), Math
				.round(_robotMoveSpeed * _rightDegPerDistance));

		if (_parity == 1) {
			bak();
		} else {
			fwd();
		}
	}

	/**
	 * Rotates the NXT robot through a specific angle. Returns when angle is
	 * reached. Wheels turn in opposite directions producing a zero radius turn.<br>
	 * Note: Requires correct values for wheel diameter and track width.
	 * 
	 * @param angle
	 *            The wanted angle of rotation in degrees. Positive angle rotate
	 *            left (clockwise), negative right.
	 */
	public void rotate(final float angle) {
		rotate(angle, false);
	}

	/**
	 * Rotates the NXT robot through a specific angle. Returns when angle is
	 * reached. Wheels turn in opposite directions producing a zero radius turn.<br>
	 * Note: Requires correct values for wheel diameter and track width.
	 * 
	 * @param angle
	 *            The wanted angle of rotation in degrees. Positive angle rotate
	 *            left (clockwise), negative right.
	 * @param immediateReturn
	 *            If true this method returns immediately.
	 */
	public void rotate(final float angle, final boolean immediateReturn) {
		setSpeed(Math.round(_robotTurnSpeed * _leftTurnRatio), Math
				.round(_robotTurnSpeed * _rightTurnRatio));
		int rotateAngleLeft = _parity * (int) (angle * _leftTurnRatio);
		int rotateAngleRight = _parity * (int) (angle * _rightTurnRatio);
		_left.rotate(-rotateAngleLeft, true);
		_right.rotate(rotateAngleRight, immediateReturn);
		if (!immediateReturn) {
			while (_left.isMoving() || _right.isMoving())
				// changed isRotating() to isMoving() as this covers what we
				// need and alows us to keep the interface small
				Thread.yield();
		}
	}

	/**
	 * @return the angle of rotation of the robot since last call to reset of
	 *         tacho count;
	 */
	public float getAngle() {
		return _parity * _gearRatio
				* ((_right.getTachoCount() / _rightTurnRatio) - (_left
						.getTachoCount() / _leftTurnRatio)) / 2.0f;
	}

	/**
	 * Stops the NXT robot.
	 */
	public void stop() {
		_left.stop();
		_right.stop();
	}

	/**
	 * @return true if the NXT robot is moving.
	 **/
	public boolean isMoving() {
		return _left.isMoving() || _right.isMoving();
	}

	/**
	 * Resets tacho count for both motors.
	 **/
	public void reset() {
		_left.resetTachoCount();
		_right.resetTachoCount();
	}

	/**
	 * @return distance traveled since last reset of tacho count.
	 **/
	public float getTravelDistance() {
		float left = _left.getTachoCount() / _leftDegPerDistance;
		float right = _right.getTachoCount() / _rightDegPerDistance;
		return _parity * _gearRatio *(left + right) / 2.0f;
	}

	/**
	 * Moves the NXT robot a specific distance in an (hopefully) straight line.<br>
	 * A positive distance causes forward motion, a negative distance moves
	 * backward. If a drift correction has been specified in the constructor it
	 * will be applied to the left motor.
	 * 
	 * @param distance
	 *            The distance to move. Unit of measure for distance must be
	 *            same as wheelDiameter and trackWidth.
	 **/
	public void travel(final float distance) {
		travel(distance, false);
	}

	/**
	 * Moves the NXT robot a specific distance in an (hopefully) straight line.<br>
	 * A positive distance causes forward motion, a negative distance moves
	 * backward. If a drift correction has been specified in the constructor it
	 * will be applied to the left motor.
	 * 
	 * @param distance
	 *            The distance to move. Unit of measure for distance must be
	 *            same as wheelDiameter and trackWidth.
	 * @param immediateReturn
	 *            If true this method returns immediately.
	 */
	public void travel(final float distance, final boolean immediateReturn) {
		setSpeed(Math.round(_robotMoveSpeed * _leftDegPerDistance), Math
				.round(_robotMoveSpeed * _rightDegPerDistance));
		_left.rotate((int) (_parity * distance * _leftDegPerDistance), true);
		_right.rotate((int) (_parity * distance * _rightDegPerDistance),
				immediateReturn);
		if (!immediateReturn) {
			while (_left.isMoving() || _right.isMoving())
				// changed isRotating() to isMoving() as this covers what we
				// need and alows us to keep the interface small
				Thread.yield();
		}
	}

	public void steer(final float turnRate) {
		steer(turnRate, Float.POSITIVE_INFINITY, true);
	}

	public void steer(final float turnRate,float angle) {
		steer(turnRate, angle, false);
	}

	public void steer(final float turnRate, final float  angle,
			final boolean immediateReturn) {
		// TODO: make this work with wheels of different size
		TachoMotor inside;
		TachoMotor outside;
		float rate = turnRate;
		if (rate < -200) {
			rate = -200;
		}
		if (rate > 200) {
			rate = 200;
		}
		if (rate == 0) {
			if (angle < 0) {
				backward();
			} else {
				forward();
			}
			return;
		}
		if (turnRate < 0) {
			inside = _right;
			outside = _left;
			rate = -rate;
		} else {
			inside = _left;
			outside = _right;
		}
		outside.setSpeed(_motorSpeed);
		float steerRatio = 1 - rate / 100.0f;
		inside.setSpeed((int) (_motorSpeed * steerRatio));
		if (angle == Float.POSITIVE_INFINITY) // no limit angle for turn
		{
			if (_parity == 1) {
				outside.backward();
			} else {
				outside.backward();
			}
			if (_parity * steerRatio > 0) {
				inside.backward();
			} else {
				inside.backward();
			}
			return;
		}
		float rotAngle = angle * _trackWidth * 2
				/ (_leftWheelDiameter * (1 - steerRatio));
		inside.rotate(_parity * (int) (rotAngle * steerRatio),immediateReturn);
		outside.rotate(_parity * (int) rotAngle, immediateReturn);
		if (immediateReturn) {
			return;
		}
		while (inside.isMoving() || outside.isMoving())
			// changed isRotating() to isMoving() as this covers what we need
			// and alows us to keep the interface small

			Thread.yield();
		inside.setSpeed(outside.getSpeed());
	}

	/*
	 * @return true if either motor actual speed is zero.
	 */
	public boolean stalled() {
		return (0 == _left.getRotationSpeed()) || (0 == _right.getRotationSpeed());
	}

	/**
	 * Motors backward. This is called by forward() and backward().
	 */
	private void bak() {
		_left.backward();
		_right.backward();
	}

	/*
	 * Sets motor speed regulation (default is true).<br> Allows steer() method
	 * to be called by (for example) a line tracker or compass navigator so
	 * direction control is from sensor inputs.
	 * 
	 * @param yes Set motor speed regulation on = true or off = false.
	 */
	public void regulateSpeed(final boolean yes) {
		_regulating = yes;
		_left.regulateSpeed(yes);
		_right.regulateSpeed(yes);
	}

	/**
	 * Motors forward. This is called by forward() and backward().
	 */
	private void fwd() {
		_left.forward();
		_right.forward();
	}

	public void arc(final float radius) {
		steer(turnRate(radius));
	}

	public void arc(final float radius, final float angle) {
		steer(turnRate(radius), angle);
	}

	public void arc(final float radius, final float angle,
			final boolean immediateReturn) {
		steer(turnRate(radius), angle, immediateReturn);
	}

	/**
	 * Calculates the turn rate corresponding to the turn radius; <br>
	 * use as the parameter for steer() negative argument means center of turn
	 * is on right, so angle of turn is negative
	 * 
	 * @param radius
	 * @return steer()
	 */
	private float turnRate(final float radius) {
		int direction;
		float radiusToUse;
		if (radius < 0) {
			direction = -1;
			radiusToUse = -radius;
		} else {
			direction = 1;
			radiusToUse = radius;
		}
		float ratio = (2 * radiusToUse - _trackWidth)
				/ (2 * radiusToUse + _trackWidth);
		return (direction * 100 * (1 - ratio));
	}

	public void travelArc(float radius, float distance) {
		travelArc(radius, distance, false);
	}

	public void travelArc(float radius, float distance, boolean immediateReturn) {
		double angle = (distance * 180) / (Math.PI * radius);
		arc(radius, (int) angle, immediateReturn); // TODO If Pilot.arc() method
													// changes to float for
													// angle, get rid of (int)
	}
	public int getParity(){
		return _parity;
	}
	/* used to toggle parity and send the command to steer*/
	public void robowarsSteer(float turnBearing, float throttle) {
		if(throttle<0)_parity=1;
		else _parity=-1;
		steer(turnBearing);
	}
	/*toggles parity*/
	public void toggleParity() {
		if(_parity==1)_parity=-1;
		else _parity=1;	
	}
}
