package robowars.shared.model;

import java.io.Serializable;
import java.util.Vector;

	/**
	 * 
	 * NOTE : width is width/2 and length is length/2 center is a point in the middle
	 * @author mwright
	 *
	 */
public abstract class GameEntity implements Serializable{


	private static final long serialVersionUID = 5016873544195577415L;
	private Vector<Float> position;
	private Vector<Float> heading;
	private float length;
	private float width;
	private boolean exists;
	private int id;

	public GameEntity(Vector<Float> position, Vector<Float> heading, float length, float width, int id){
		this.position=position;
		this.heading=heading;
		this.setLength(length);
		this.width=width;
		this.exists=true;
		this.id=id;
	}
	public boolean checkCollision(GameEntity target){
		if(((Float)position.get(0))-width<=target.getX()&&((Float)position.get(0))+width>=target.getX()&&
		   ((Float)position.get(1))-getLength()<=target.getY()&&((Float)position.get(1))+getLength()>=target.getY())return true;
		else return false;
	}
	public float getX() {
		return position.get(0);
	}
	public float getY() {
		return position.get(1);
	}
	public void setPosition(Vector<Float> newPos){
		this.position=newPos;
	}
	public void setHeading(Vector<Float> newHeading){
		this.heading=newHeading;
	}
	public void setLength(float length) {
		this.length = length;
	}
	public float getLength() {
		return length;
	}
	public Vector<Float> getHeading(){return heading;}
	public Vector<Float> getPosition(){return position;}
	public int getId(){return id;}
	public float getWidth(){return width;}

}
