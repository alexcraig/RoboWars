package robowars.shared.model;

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
	
	/** The last RobotCommand that was successfully transmitted to the robot */
	private RobotCommand lastCommand;
	
	/** Lock object to ensure mutual exclusion when accessing the last valid command */
	private final Object lastCommandLock = new Object();

	public GameRobot(Pose pose, Vector shape[], int id, int health, String robotId) {
		super(pose, shape, id);
		this.startingHealth=health;
		this.health=health;
		this.initLocation=pose.getLocation();
		this.lastPose = pose;
		this.robotIdentifier=robotId;
		command = null;
		setLastCommand(null);
	}

	/**
	* Generates a new robot with a given position and heading, shaped 
	* as a 30x20 arrow, and it's id set to 0.
	* @param identifier The identifier of the robot (MAC address?);
	*/
	public GameRobot(String identifier, Pose pose, int id) {
		super(pose, null, id);
		Vector shape[] = new Vector[4];
		shape[0] = new Vector(10,8);
		shape[1] = new Vector(-10,8);
		shape[2] = new Vector(-10,-8);
		shape[3] = new Vector(10,-8);
		//shape[3] = new Vector(10,-10);
		//shape[5] = new Vector(20,0);
		
		super.setVertices(shape);

		startingHealth = DEFAULT_START_HEALTH;
		health = startingHealth;
		this.initLocation=pose.getLocation();
		this.lastPose = pose;

		robotIdentifier = identifier;
		command = null;
	}
	
	
	/**
	* Generates a new robot with a position (10,10) and heading of 0 (looking 
	* along positive x-axis), shaped as a 30x20 arrow, and it's id set to 0.
	* @param identifier The identifier of the robot (MAC address?);
	*/
	public GameRobot(String identifier) {
		super(new Pose(10,10,0), null, 0);
		Vector shape[] = new Vector[4];
		shape[0] = new Vector(10,8);
		shape[1] = new Vector(-10,8);
		shape[2] = new Vector(-10,-8);
		shape[3] = new Vector(10,-8);
		//shape[3] = new Vector(10,-10);
		//shape[5] = new Vector(20,0);	
		
		super.setVertices(shape);

		startingHealth = DEFAULT_START_HEALTH;
		health = startingHealth;
		this.initLocation=pose.getLocation();
		this.lastPose = pose;

		robotIdentifier = identifier;
		command = null;
	}

	public RobotCommand getResetPath(GameRobot[] hazzards){
		// TODO: This should probably generate a serious of "move to coordinate"
		// commands (to ensure robots do not hit each other)
		return RobotCommand.returnToStart();
	}

	public Pose getLastPose(){
		return lastPose;
	}

	public void setPose(Pose newPose){
		lastPose = super.getPose();
		super.setPose(newPose);
	}
	
	public void setCommand(RobotCommand command){
		this.command = command;
	}
	
	public RobotCommand getCommand(){
		return command;
	}


	public void decreaseHealth(int change){health-=change;}
	
	public int getHealth(){return health;}
	
	public String getRobotId(){return robotIdentifier;}

	/**
	 * Sets the last command sent to the robot. This should only be called after
	 * it has been verified that the command was written to an output stream
	 * successfully.
	 * 
	 * @param lastCommand	The last command successfully sent to the robot.
	 */
	public void setLastCommand(RobotCommand lastCommand) {
		synchronized(lastCommandLock) {
			this.lastCommand = lastCommand;
		}
	}

	/**
	 * @return	The last command that was successfully sent to the robot (or null
	 * 			if no commands have been sent)
	 */
	public RobotCommand getLastCommand() {
		synchronized(lastCommandLock) {
			return lastCommand;
		}
	}
}


