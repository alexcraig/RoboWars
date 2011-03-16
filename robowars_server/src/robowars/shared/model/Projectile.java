package robowars.shared.model;


public class Projectile extends GameEntity{

	/**
	 * 
	 */
	private static final long serialVersionUID = 193578038942405071L;
	private float speed;
	private float distanceTraveled;

	public Projectile(Posture Posture, int speed, int id) {
		super(Posture, null, id);
		Vector shape[] = new Vector[] {new Vector(1,1), new Vector(-1,1), 
				new Vector(-1,-1), new Vector(1,-1)};
		setVertices(shape);
		this.speed=speed;
		// TODO Auto-generated constructor stub
	}
	
	public void updatePosition(int timeElapsed){
		float change=speed*timeElapsed;
		//Vector v = Vector.createUnitVector(getPosture());
		//v.setX(v.getX() * change);
		//v.setY(v.getY() * change);
		Posture newPosture = clonePosture();
		newPosture.moveUpdate(change);
		setPosture(newPosture);
		distanceTraveled += change;
	}
	
	public float getDistanceTraveled(){
		return distanceTraveled;
	}

}
