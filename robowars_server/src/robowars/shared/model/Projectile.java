package robowars.shared.model;

import lejos.robotics.Pose;

public class Projectile extends GameEntity{

	/**
	 * 
	 */
	private static final long serialVersionUID = 193578038942405071L;
	private float speed;

	public Projectile(Pose pose, int speed, int id) {
		super(pose, null, id);
		Vector shape[] = new Vector[0];
		setVertices(shape);
		this.speed=speed;
		// TODO Auto-generated constructor stub
	}
	public void updatePosition(int timeElapsed){
		// TODO Update this method to conform to new Pose position system.
		/*
		float change=speed*timeElapsed;
		float newX=change*this.getPose().getHeading();
		float newY=change*this.getHeading().get(1);
		Vector<Float> newPosition=new Vector<Float>();
		newPosition.add(getX()+newX);
		newPosition.add(getY()+newY);
		setPose(newPose);
		*/
	}

}
