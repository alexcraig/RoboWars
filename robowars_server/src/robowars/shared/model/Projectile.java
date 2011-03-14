package robowars.shared.model;

import lejos.robotics.Pose;

public class Projectile extends GameEntity{

	/**
	 * 
	 */
	private static final long serialVersionUID = 193578038942405071L;
	private float speed;
	private float distanceTraveled;

	public Projectile(Pose pose, int speed, int id) {
		super(pose, null, id);
		Vector shape[] = new Vector[] {new Vector(1,1), new Vector(-1,1), 
				new Vector(-1,-1), new Vector(1,-1)};
		setVertices(shape);
		this.speed=speed;
		// TODO Auto-generated constructor stub
	}
	
	public void updatePosition(int timeElapsed){
		float change=speed*timeElapsed;
		//Vector v = Vector.createUnitVector(getPose());
		//v.setX(v.getX() * change);
		//v.setY(v.getY() * change);
		Pose newPose = clonePose();
		newPose.moveUpdate(change);
		setPose(newPose);
		distanceTraveled += change;
	}
	
	public float getDistanceTraveled(){
		return distanceTraveled;
	}

}
