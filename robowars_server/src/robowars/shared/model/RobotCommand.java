package robowars.shared.model;

import java.io.Serializable;
import java.util.Vector;

import lejos.robotics.Pose;
import java.io.Serializable;
import java.util.Vector;

import lejos.robotics.Pose;

/**
 * Represents a command that should be passed to a remote robot. Several
 * command types are available with different parameters.
 */
public class RobotCommand implements Serializable{
	public static final int DEFAULT_PRIORITY = 1;

	/** The type of the command */
	private CommandType type;
	
	/** The moveSpeed setting for the command */
	private float moveSpeed;
	
	/** 
	 * The turning parameter for the command. This will be either a number
	 * of degrees or a turn rate depending on the type of command.
	 */
	private float turnParam;
	
	/** Special flags to include (used for sounds / lights) */
	private String specialFlags;
	
	/** 
	 * A position and heading (used for position override commands and move to
	 * position commands) 
	 * */
	private Pose newPos;
	
	/** The priority of this command. The higher the value, the higher the priority. */
	private int priority;

	/** The maximum value that should be sent in the moveSpeed parameter */
	public static final float MAX_SPEED = 75; // This will need to be tweaked for the real hardware

	/**
	 * Generates a new RobotCommand
	 * @param type	The type of the command
	 * @param moveSpeed	The moveSpeed setting for the command
	 * @param turnParam	The turning parameter for the command.
	 * @param specialFlags	Special flags to include (used for sounds / lights)
	 * @param newPos	A position and heading
	 * @param priority	The priority of this command. The higher the value, the higher the priority.
	 */
	private RobotCommand(CommandType type, float moveSpeed, float turnParam,
			String specialFlags, Pose newPos, int priority) {
		this.type = type;
		
		if(moveSpeed > MAX_SPEED)
			moveSpeed = MAX_SPEED;
		this.moveSpeed = moveSpeed;
		
		this.turnParam = turnParam;
		this.newPos = newPos;
		this.specialFlags = specialFlags;
		this.priority = priority;
	}
	
	/**
	 * Generates a MOVE_CONTINUOUS robot command
	 * @param moveSpeed	The moveSpeed for the command
	 * @return	A MOVE_CONTINUOUS robot command
	 */
	public static RobotCommand moveContinuous(float moveSpeed) {
		return new RobotCommand(CommandType.MOVE_CONTINUOUS, moveSpeed, 
				0, null, null, DEFAULT_PRIORITY);
	}
	
	/**
	 * Generates a SET_POSITION robot command
	 * @param newPos	The new position for the robot (overrides any existing data)
	 * @return A SET_POSITION robot command
	 */
	public static RobotCommand setPosition(Pose newPos) {
		return new RobotCommand(CommandType.SET_POSITION, 0, 
				0, null, newPos, DEFAULT_PRIORITY);
	}
	
	/**
	 * Generates a STOP robot command
	 * @return A STOP robot command
	 */
	public static RobotCommand stop() {
		return new RobotCommand(CommandType.STOP, 0, 
				0, null, null, DEFAULT_PRIORITY);
	}
	
	/**
	 * Generates a TURN_ANGLE_LEFT command
	 * @param degrees	The number of degrees to turn left (stopped, on the spot rotation)
	 * @return A TURN_ANGLE_LEFT command
	 */
	public static RobotCommand turnAngleLeft(int degrees) {
		return new RobotCommand(CommandType.TURN_ANGLE_LEFT, 0, 
				degrees, null, null, DEFAULT_PRIORITY);
	}
	
	/**
	 * Generates a TURN_ANGLE_RIGHT command
	 * @param degrees	The number of degrees to turn right (stopped, on the spot rotation)
	 * @return A TURN_ANGLE_RIGHT command
	 */
	public static RobotCommand turnAngleRight(int degrees) {
		return new RobotCommand(CommandType.TURN_ANGLE_RIGHT, 0, 
				degrees, null, null, DEFAULT_PRIORITY);
	}
	
	/**
	 * Generates a ROLLING_TURN command
	 * @param moveSpeed	The forward movement speed
	 * @param turnRate	The turn rate (from -200 to 200, positive values for left turns)
	 * @return A ROLLING_TURN command
	 */
	public static RobotCommand rollingTurn(float moveSpeed, int turnRate) {
		if(turnRate < -200) turnRate = -200;
		if(turnRate > 200) turnRate = 200;
		
		return new RobotCommand(CommandType.ROLLING_TURN, moveSpeed, 
				turnRate, null, null, DEFAULT_PRIORITY);
	}
	
	/**
	 * Generates a RETURN_TO_START_POSITION robot command
	 * @return A RETURN_TO_START_POSITION robot command
	 */
	public static RobotCommand returnToStart() {
		return new RobotCommand(CommandType.RETURN_TO_START_POSITION, 0, 
				0, null, null, DEFAULT_PRIORITY);
	}
	/**
	 * Generates a EXIT robot command
	 * @return A EXIT robot command
	 */
	public static RobotCommand exit() {
		return new RobotCommand(CommandType.EXIT, 0, 
				0, null, null, DEFAULT_PRIORITY);
	}
	public void setPriority(int priority) {
		this.priority = priority;
	}

	public Pose getPos(){
		return newPos;
	}
	public float getThrottle() {
		return moveSpeed;
	}

	public float getTurnBearing() {
		return turnParam;
	}

	public CommandType getType() {
		return type;
	}
	public int getPriority() {
		return priority;
	}
	
	public String toString() {
		String returnStr = "[" + type.toString() + "|";
		returnStr += "speed:" + moveSpeed + "|turn:" + turnParam;
		if(specialFlags != null) {
			returnStr += "|flags:" + specialFlags;
		}
		
		if(newPos != null) {
			returnStr += "|x:" + newPos.getX() + " y:" + newPos.getY()
				+ " h:" + newPos.getHeading();
		} 
		returnStr += "]";
		return returnStr;
	}
	public String toOutputString(){
		String returnStr="[" + type.ordinal() + "|";
		returnStr += moveSpeed + "|" + turnParam;
		if(specialFlags != null) {
			returnStr += "|" + specialFlags;
		}
		
		if(newPos != null) {
			returnStr += "|" + newPos.getX() + "|" + newPos.getY()
				+ "|" + newPos.getHeading();
		} 
		returnStr += "]";
		
		return returnStr;
	}

}

