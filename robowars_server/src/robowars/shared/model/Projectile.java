package robowars.shared.model;

import java.io.Serializable;

public class Projectile extends GameEntity implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 193578038942405071L;
	private float speed;
	private float distanceTraveled;
	private int updateDelay;

	public Projectile(Posture Posture, int speed, int id) {
		super(Posture, null, id);
		Vector shape[] = new Vector[] {new Vector(1,1), new Vector(-1,1), 
				new Vector(-1,-1), new Vector(1,-1)};
		setVertices(shape);
		this.speed=speed;
		this.updateDelay = 0;
		// TODO Auto-generated constructor stub
	}
	
	public void updatePosition(int timeElapsed){
		if(timeElapsed == 0)
			timeElapsed = 1;
		updateDelay += timeElapsed;
		if(updateDelay >= 50){
			updateDelay -= 50;
			float change=speed;
			//Vector v = Vector.createUnitVector(getPosture());
			//v.setX(v.getX() * change);
			//v.setY(v.getY() * change);
			Posture newPosture = clonePosture();
			newPosture.moveUpdate(change);
			setPosture(newPosture);
			distanceTraveled += change;
		}
	}
	
	public float getDistanceTraveled(){
		return distanceTraveled;
	}

}
