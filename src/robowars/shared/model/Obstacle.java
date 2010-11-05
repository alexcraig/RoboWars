package robowars.shared.model;

import java.util.Vector;

public class Obstacle extends GameEntity{

	private static final long serialVersionUID = 2354988184568266965L;
	private int strength;
	private int lifeTime;
	private boolean destroyOnContact;
	private boolean destroyByProjectile;

	public Obstacle(Vector<Float> position, Vector<Float> heading, 
			float length, float width, int strength, boolean destroyOnContact,
			boolean destroyByProjectile, int id) {
		super(position, heading, length, width, id);
		this.strength=strength;
		this.destroyByProjectile=destroyByProjectile;
		this.destroyOnContact=destroyOnContact;
		lifeTime = 0;
	}
	public void shrink(int amount){
		if(amount>=1)this.setLength(this.getLength() - amount);
	}
	public void grow(int amount){
		if(amount>=1)this.setLength(this.getLength() + amount);
	}
	public void passTime(int time){
		lifeTime += time;
	}
	public int getTime() {return lifeTime;}
	public boolean isHittable(){return destroyOnContact;}
	public boolean isShootable(){return destroyByProjectile;}
	public int getStrength(){return strength;}
}
