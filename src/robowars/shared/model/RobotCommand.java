package robowars.shared.model;

import java.io.Serializable;
import java.util.Vector;

public class RobotCommand implements Serializable{
	
	private CommandType type;
	private float throttle;
	private float turnBearing;
	private String specialFlags;
	private Vector newPos;
	
	public static final float MAX_SPEED = 100;
	
	public RobotCommand(CommandType type)
	{
		this.type = type;
		
		switch(type){
			case MOVE_CONTINUOUS:
				throttle = MAX_SPEED;
				turnBearing = 0; break;
				
			case TURN_RIGHT_ANGLE_LEFT:
				throttle = 0;
				turnBearing = 90; break;
				
			case TURN_RIGHT_ANGLE_RIGHT:
				throttle = 0;
				turnBearing = -90; break;
				
			case STOP:
				throttle = 0;
				turnBearing = 0; break;
				
			case RETURN_TO_START_POSITION: break;
				
			case MOVE_COORDINATE: break;
			
			case MOVE_DEGREE_TURN: break;
			
			case FIRE_PROJECTILE: specialFlags = "Fire"; break;
		}
	}
	
	public RobotCommand(float throttle, float turnBearing, String specialFlags)
	{
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
