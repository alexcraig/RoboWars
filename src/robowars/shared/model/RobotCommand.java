package robowars.shared.model;

import java.io.Serializable;
import java.util.Vector;

public class RobotCommand implements Serializable{
	
	private CommandType type;
	private GameRobot robot;
	private float throttle;
	private float turnBearing;
	private String specialFlags;
	private Vector newPos;
	
	public static final float MAX_SPEED = 100;
	
	public RobotCommand(GameRobot robot, CommandType type)
	{
		this.type = type;
		this.robot = robot;
		
		if (type == CommandType.MOVE_CONTINUOUS){
			throttle = MAX_SPEED;
			turnBearing = 0;
		}
		
		if (type == CommandType.TURN_RIGHT_ANGLE_LEFT){
			throttle = 0;
			turnBearing = 90;
		}
		
		if (type == CommandType.TURN_RIGHT_ANGLE_RIGHT){
			throttle = 0;
			turnBearing = -90;
		}
		
		if (type == CommandType.RETURN_TO_START_POSITION){

		}
		
		if (type == CommandType.MOVE_COORDINATE){
			
		}
		
		if (type == CommandType.MOVE_DEGREE_TURN){
			
		}
		
		if (type == CommandType.FIRE_PROJECTILE){
			specialFlags = "Fire";
		}
	}
	
	public RobotCommand(GameRobot robot, float throttle, float turnBearing, String specialFlags)
	{
		this.robot = robot;
		this.throttle = throttle;
		this.turnBearing = turnBearing;
		this.specialFlags = specialFlags;
	}
	
	
	public float getThrottle() {
		return throttle;
	}
	
	public float getTurnBearing() {
		return turnBearing;
	}
	
	public CommandType getType() {
		return type;
	}
	
}
