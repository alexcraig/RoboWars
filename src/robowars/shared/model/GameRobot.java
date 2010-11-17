package robowars.shared.model;

import java.util.Vector;
import lejos.robotics.Pose;
import lejos.geom.Point;

public class GameRobot extends GameEntity{

	/**
	*
	*/
	private static final long serialVersionUID = -6919578909238135742L;

	public static int DEFAULT_START_HEALTH = 3;

	private int health;
	private int startingHealth;
	private Point initLocation;
	private Pose lastPose;
	private String robotIdentifier;
	private RobotCommand command;

	public GameRobot(Pose pose, float length, float width, int id, int health, String robotId) {
		super(pose, length, width, id);
		this.startingHealth=health;
		this.health=health;
		this.initLocation=pose.getLocation();
		this.lastPose = pose;
		this.robotIdentifier=robotId;
		command = null;
		// TODO Auto-generated constructor stub
	}

	/**
	* Generates a new robot with a position (0,0) and heading of 0 (looking 
	* along positive x-axis) and it's id set to 0.
	* @param length The length of the robot being represented
	* @param width The width of the robot being represented
	* @param identifier The identifier of the robot (MAC address?);
	*/
	public GameRobot(float length, float width, String identifier) {
		super(new Pose(0,0,0), length, width, 0);

		startingHealth = DEFAULT_START_HEALTH;
		health = startingHealth;
		this.initLocation=pose.getLocation();
		this.lastPose = pose;

		robotIdentifier = identifier;
		command = null;
	}

	public RobotCommand getResetPath(GameRobot[] hazzards){
		return new RobotCommand(CommandType.RETURN_TO_START_POSITION, 9);
	}

	public boolean checkCollision(GameEntity target){
		float closestX=clamp(this.getPose().getX(), target.getPose().getX()-target.getLength(), target.getPose().getX()+target.getLength());
		float closestY=clamp(this.getPose().getY(), target.getPose().getY()-target.getWidth(), target.getPose().getY()+target.getWidth());
		float distanceX=closestX-this.getPose().getX();
		float distanceY=closestY-this.getPose().getY();
		if(((distanceX*distanceX)+(distanceY*distanceY))<=this.getWidth()*this.getWidth())return true;
		return false;
	}

	private float clamp(float value, float min, float max){
		if (value > max) return max;
		if (value < min) return min;
		return value;
	}

	public Pose getLastPose(){
		return lastPose;
	}

	public void setPose(Pose newPose){
		lastPose = super.getPose();
		super.setPose(newPose);
	}
	
	public void setCommand(RobotCommand command){
		command = command;
	}
	
	public RobotCommand getCommand(){
		return command;
	}


	public void decreaseHealth(int change){health-=change;}
	public String getRobotId(){return robotIdentifier;}
}


