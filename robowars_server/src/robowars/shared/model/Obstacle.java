package robowars.shared.model;

import lejos.robotics.Pose;

public class Obstacle extends GameEntity{

	private static final long serialVersionUID = 2354988184568266965L;
	private int strength;
	private long lifeTime;
	private boolean destroyOnContact;
	private boolean destroyByProjectile;

	public Obstacle(Pose pose, Vector shape[], int strength, boolean destroyOnContact, boolean destroyByProjectile, int id) {
		super(pose, shape, id);
		this.strength=strength;
		this.destroyByProjectile=destroyByProjectile;
		this.destroyOnContact=destroyOnContact;
		lifeTime = 0;
	}

	public Obstacle(Pose pose, int i, float wallWidth, int j, boolean b, boolean c, int k) {
		// TODO: Made-up constructor so project would compile. This constructor is useless.
		super(pose, new Vector[] {new Vector(0,1), new Vector(0,-1)}, 1);
	}

	public void shrink(int amount){
		//if(amount>=1)this.setLength(this.getLength() - amount);
	}
	public void grow(int amount){
		//if(amount>=1)this.setLength(this.getLength() + amount);
	}
	public void passTime(long time){
		lifeTime += time;
	}
	public long getTime() {return lifeTime;}
	public boolean isHittable(){return destroyOnContact;}
	public boolean isShootable(){return destroyByProjectile;}
	public int getStrength(){return strength;}

	public int getLength() {
		// TODO Auto-generated method stub
		return 0;
	}
}

