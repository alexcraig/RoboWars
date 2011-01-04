package robowars.test;

import org.apache.log4j.Logger;

import lejos.robotics.TachoMotor;

/**
 * Simulates a tachometer enabled motor with no limitation on the degree
 * of rotation. This class should be used in conjunction with a SimpleNavigator
 * in order to simulate a remote robot without actual hardware.
 * 
 * Note: Assumes that methods which do not specify an immediateReturn parameter
 * should return immediately (not specified in documentation)
 * 
 * WARNING: NOT THREAD SAFE
 */
public class TestTachoMotor implements TachoMotor {
	/** The logger used by this class */
	private static Logger log = Logger.getLogger(TestTachoMotor.class);
	
	/** The name of the motor (for logging purposes) */
	private String name;
	
	/** Constants for motor rotation direction */
	public static final int MOVE_FORWARD = 0;
	public static final int MOVE_BACKWARD = 1;
	
	/** Specifies the direction the motor should rotate (use public constants) */
	private int moveDirection;
	
	/** 
	 * The speed of the motor (number of degrees the tachometer should increment
	 * every time updateTachoValue() is called)
	 */
	private int speed;
	
	/** The current degree count of the motor's tachometer */
	private int tachoDegrees;
	
	/** Flag to determine if the motor should be activated */
	private boolean isMoving;
	
	/** Flag to determine if the motor rotation is targeting a specific degree value */
	private boolean rotateToTarget;
	
	/** The degree value to rotate to (if rotateToTarget is set) */
	private long rotateTarget;
	
	/** Generates a new TestTachoMotor */
	public TestTachoMotor(String name) {
		this.name = name;
		speed = 0;
		tachoDegrees = 0;
		rotateTarget = 0;
		rotateToTarget = false;
		isMoving = false;
		moveDirection = MOVE_FORWARD;
	}
	
	/**
	 * Updates the tachometer value of the simulated motor. This function should
	 * be periodically called by a dedicated thread (preferably the same thread
	 * for both motors used with a single pilot). The simulated speed of the motor
	 * will depend on the frequency at which this function is called.
	 */
	public synchronized void updateTachoValue() {
		// log.info(name + " updating: " + moveDirection + " | " + tachoDegrees + " | " + speed
		// 		+ " | " + isMoving + " | " + rotateToTarget + " | " + rotateTarget);
		
		if(isMoving) {
			switch(moveDirection) {
			case MOVE_FORWARD:
				tachoDegrees += speed;
				break;
			case MOVE_BACKWARD:
				tachoDegrees -= speed;
				break;
			default:
				break;
			}
			
			if(rotateToTarget) {
				if(moveDirection == MOVE_FORWARD
						&& tachoDegrees >= rotateTarget) {
					tachoDegrees = (int)rotateTarget;
					isMoving = false;
					rotateToTarget = false;
				} else if(moveDirection == MOVE_BACKWARD
						&& tachoDegrees <= rotateTarget) {
					tachoDegrees = (int)rotateTarget;
					isMoving = false;
					rotateToTarget = false;
				}
			}
		}
		
		// log.info(name + " updated: " + moveDirection + " | " + tachoDegrees + " | " + speed
		// 		+ " | " + isMoving + " | " + rotateToTarget + " | " + rotateTarget);
	}
	
	/** See documentation for lejos.robotics TachoMotor for usage of below functions */
	
	@Override
	public int getSpeed() {
		return speed;
	}

	@Override
	public void regulateSpeed(boolean arg0) {
	}

	@Override
	public synchronized void rotate(int degrees) {
		rotate(degrees, true);
	}

	@Override
	public  synchronized void rotate(int degrees, boolean immediateReturn) {
		// log.info(name + " rotate(" + degrees + "," + immediateReturn);
		
		if(degrees == 0) return;
		
		if(degrees > 0) {
			moveDirection = MOVE_FORWARD;
		} else if (degrees < 0) {
			moveDirection = MOVE_BACKWARD;
		}
		
		isMoving = true;
		rotateToTarget = true;
		rotateTarget = (long)tachoDegrees + (long)degrees;
		
		if(!immediateReturn) {
			while(rotateToTarget);
		}
	}

	@Override
	public synchronized void rotateTo(int degrees) {
		rotateTo(degrees, true);
	}

	@Override
	public synchronized void rotateTo(int degrees, boolean immediateReturn) {
		rotate(degrees - tachoDegrees, immediateReturn);
	}

	@Override
	public synchronized void setSpeed(int speed) {
		// log.info("Speed set to: " + Math.abs(speed) + " (param: " + speed + ")");
		this.speed = Math.abs(speed);
	}

	@Override
	public void smoothAcceleration(boolean arg0) {
	}

	@Override
	public int getRotationSpeed() {
		return speed;
	}

	@Override
	public synchronized int getTachoCount() {
		// log.info(name + " - Tach: " + tachoDegrees);
		return tachoDegrees;
	}

	@Override
	public void resetTachoCount() {
		tachoDegrees = 0;
	}

	@Override
	public synchronized void backward() {
		// log.info("Backward");
		isMoving = true;
		rotateToTarget = false;
		moveDirection = MOVE_BACKWARD;
	}

	@Override
	public synchronized void flt() {
		isMoving = false;
		rotateToTarget = false;
	}

	@Override
	public synchronized void forward() {
		// log.info("Forward");
		isMoving = true;
		rotateToTarget = false;
		moveDirection = MOVE_FORWARD;
	}

	@Override
	public synchronized boolean isMoving() {
		return isMoving;
	}

	@Override
	public synchronized void stop() {
		isMoving = false;
		rotateToTarget = false;
	}
}
