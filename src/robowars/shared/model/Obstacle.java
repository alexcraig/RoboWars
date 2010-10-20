package robowars.shared.model;

import java.util.Vector;

public class Obstacle extends GameEntity{

	private static final long serialVersionUID = 2354988184568266965L;
	private int strength;
	private boolean destroyOnContact;
	private boolean destroyByProjectile;

	public Obstacle(Vector<Float> position, Vector<Float> heading, float length, float width, int strength, boolean destroyOnContact, boolean destroyByProjectile, int id) {
		super(position, heading, length, width, id);
		this.strength=strength;
		this.destroyByProjectile=destroyByProjectile;
		this.destroyOnContact=destroyOnContact;
	}
	public void shrink(int factor){
		if(factor>=1)this.setLength(this.getLength() / factor);
	}
	public void grow(int factor){
		if(factor>=1)this.setLength(this.getLength() * factor);
	}
	public boolean isHittable(){return destroyOnContact;}
	public boolean isShootable(){return destroyByProjectile;}
	public int getStrength(){return strength;}
}
