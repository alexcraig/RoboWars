package robowars.shared.model;

import java.io.Serializable;
import java.util.Vector;
import lejos.robotics.Pose;
import lejos.geom.Point;

/**
*
* NOTE : width is width/2 and length is length/2 center is a point in the middle
* @author mwright
*
*/
public abstract class GameEntity implements Serializable{


	private static final long serialVersionUID = 5016873544195577415L;
	protected Pose pose;
	protected float length;
	protected float width;
	protected int id;

	public GameEntity(Pose pose, float length, float width, int id){
		this.pose = pose;
		this.setLength(length);
		this.width=width;
		this.id=id;
	}
	public boolean checkCollision(GameEntity target){
		if((pose.getX())-width<=target.getPose().getX()&&(pose.getX())+width>=target.getPose().getX()&&
			(pose.getY())-getLength()<=target.getPose().getY()&&(pose.getY())+getLength()>=target.getPose().getY())return true;
		else return false;
	}
	public void setPose(Pose pose){
		this.pose = pose;
	}
	public void setLength(float length) {
		this.length = length;
	}
	public float getLength() {
		return length;
	}
	public Pose getPose(){
		return pose;
	}
	public int getId(){return id;}
	public float getWidth(){return width;}

}
