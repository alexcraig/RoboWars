package robowars.shared.model;

import java.util.Vector;

public class Projectile extends GameEntity{

	/**
	 * 
	 */
	private static final long serialVersionUID = 193578038942405071L;
	private float speed;

	public Projectile(Vector<Float> position, Vector<Float> heading, float length, float width, int speed, int id) {
		super(position, heading, length, width, id);
		this.speed=speed;
		// TODO Auto-generated constructor stub
	}
	public void updatePosition(int timeElapsed){
		float change=speed*timeElapsed;
		float newX=change*this.getHeading().get(0);
		float newY=change*this.getHeading().get(1);
		Vector<Float> newPosition=new Vector<Float>();
		newPosition.add(getX()+newX);
		newPosition.add(getY()+newY);
		setPosition(newPosition);
	}

}
