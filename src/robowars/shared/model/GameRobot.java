package robowars.shared.model;

import java.util.Vector;

public class GameRobot extends GameEntity{

	/**
	 * 
	 */
	private static final long serialVersionUID = -6919578909238135742L;
	private int health;
	private int startingHealth;
	private Vector<Float> initLocation;
	private String robotId;
	public GameRobot(Vector<Float> position, Vector<Float> heading, float length, float width, int id, int health, String robotId) {
		super(position, heading, length, width, id);
		this.startingHealth=health;
		this.health=health;
		this.initLocation=position;
		this.robotId=robotId;
		// TODO Auto-generated constructor stub
	}
	public RobotCommand getResetPath(GameRobot[] hazzards){
		return new RobotCommand();
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
	public void decreaseHealth(int change){health-=change;}
	public String getRobotId(){return robotId;}
}

	