package robowars.shared.model;

import java.util.Vector;

public class GameRobot extends GameEntity{

	/**
	 * 
	 */
	private static final long serialVersionUID = -6919578909238135742L;
	
	public static int DEFAULT_START_HEALTH = 3;
	
	private int health;
	private int startingHealth;
	private Vector<Float> initLocation;
	private Vector<Float> lastLocation;
	private Vector<Float> lastHeading;
	private String robotIdentifier;
	
	public GameRobot(Vector<Float> position, Vector<Float> heading, float length, float width, int id, int health, String robotId) {
		super(position, heading, length, width, id);
		this.startingHealth=health;
		this.health=health;
		this.initLocation=position;
		this.lastLocation=position;
		this.lastHeading=heading;
		this.robotIdentifier=robotId;
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * Generates a new robot with a position and heading of <0,0> and it's
	 * id set to 0.
	 * @param length	The length of the robot being represented
	 * @param width		The width of the robot being represented
	 * @param identifier	The identifier of the robot (MAC address?);
	 */
	public GameRobot(float length, float width, String identifier) {
		super(null, null, length, width, 0);
		
		Vector<Float> position = new Vector<Float>();
		position.addElement(Float.valueOf(0));
		position.addElement(Float.valueOf(0));
		setPosition(position);
		
		Vector<Float> heading = new Vector<Float>();
		heading.addElement(Float.valueOf(0));
		heading.addElement(Float.valueOf(0));
		setHeading(heading);
		
		startingHealth = DEFAULT_START_HEALTH;
		health = startingHealth;
		initLocation = position;
		lastLocation = position;
		lastHeading = heading;
		
		robotIdentifier = identifier;
	}
	
	public RobotCommand getResetPath(GameRobot[] hazzards){
		return new RobotCommand(CommandType.RETURN_TO_START_POSITION);
	}
	
	public boolean checkCollision(GameEntity target){
		float closestX=clamp(this.getX(), target.getX()-target.getLength(), target.getX()+target.getLength());
		float closestY=clamp(this.getY(), target.getY()-target.getWidth(), target.getY()+target.getWidth());
		float distanceX=closestX-this.getX();
		float distanceY=closestY-this.getY();
		if(((distanceX*distanceX)+(distanceY*distanceY))<=this.getWidth()*this.getWidth())return true;
		return false;
	}
	
	private float clamp(float value, float min, float max){
		if (value > max) return max;
		if (value < min) return min;
		return value;
	}
	
	public Vector<Float> getLastLocation(){
		return lastLocation;
	}
	
	public Vector<Float> getLastHeading(){
		return lastHeading;
	}
	
	public void setPosition(Vector<Float> newPos){
		lastLocation = super.getPosition();
		super.setPosition(newPos);
	}
	
	public void setHeading(Vector<Float> newHeading){
		lastHeading = super.getHeading();
		super.setPosition(newHeading);
	}
	
	public void decreaseHealth(int change){health-=change;}
	public String getRobotId(){return robotIdentifier;}
}

	