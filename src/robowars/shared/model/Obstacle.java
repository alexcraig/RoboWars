<<<<<<< HEAD
package robowars.shared.model;

import java.util.Vector;

public class Obstacle extends GameEntity{

	private static final long serialVersionUID = 2354988184568266965L;
	private int strength;
	private int lifeTime;
	private boolean destroyOnContact;
	private boolean destroyByProjectile;

	public Obstacle(Vector<Float> position, Vector<Float> heading, float length, float width, int strength, boolean destroyOnContact, boolean destroyByProjectile, int id) {
		super(position, heading, length, width, id);
		this.strength=strength;
		this.destroyByProjectile=destroyByProjectile;
		this.destroyOnContact=destroyOnContact;
		lifeTime = 0;
	}
	public void shrink(int factor){
		if(factor>=1)this.setLength(this.getLength() / factor);
	}
	public void grow(int factor){
		if(factor>=1)this.setLength(this.getLength() * factor);
	}
	public void passTime(int time){
		lifeTime += time;
	}
	public int getTime() {return lifeTime;}
	public boolean isHittable(){return destroyOnContact;}
	public boolean isShootable(){return destroyByProjectile;}
	public int getStrength(){return strength;}
}
=======
package robowars.shared.model;

import java.util.Vector;

public class Obstacle extends GameEntity{

	private static final long serialVersionUID = 2354988184568266965L;
	private int strength;
	private long lifeTime;
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
	public void passTime(long time){
		lifeTime += time;
	}
	public long getTime() {return lifeTime;}
	public boolean isHittable(){return destroyOnContact;}
	public boolean isShootable(){return destroyByProjectile;}
	public int getStrength(){return strength;}
}
>>>>>>> 9a5d31a390e7d072a22f5916cd27c8da2d306806
