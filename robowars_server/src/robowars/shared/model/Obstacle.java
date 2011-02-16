package robowars.shared.model;

import lejos.robotics.Pose;

public class Obstacle extends GameEntity{

	private static final long serialVersionUID = 2354988184568266965L;
	private int strength;
	private long lifeTime;
	private boolean destroyOnContact;
	private boolean destroyByProjectile;

	public Obstacle(Pose pose, Vector shape[], int strength, 
			boolean destroyOnContact, boolean destroyByProjectile, int id) {
		super(pose, shape, id);
		this.strength=strength;
		this.destroyByProjectile=destroyByProjectile;
		this.destroyOnContact=destroyOnContact;
		lifeTime = 0;
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
}

