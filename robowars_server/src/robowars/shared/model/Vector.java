package robowars.shared.model;

import java.math.*;
import lejos.robotics.Pose;

public class Vector {
	private float x;
	private float y;
	
	public Vector (float x, float y){
		this.x = x;
		this.y = y;
	}
	
	public float dotProduct(Vector p){
		return x*p.x + y*p.y;
	}
	
	public void unitVector(){
		float magnitude = magnitude();
		x /= magnitude;
		y /= magnitude;
	}
	
	public float magnitude(){
		return (float) Math.sqrt(x*x + y*y);
	}
	
	public static Vector createUnitVector(Pose pose){
		Vector v = new Vector(1,0);
		v.rotate(pose.getHeading());
		return v;
	}
	
	public void rotate(float angleDifference){
		Vector.setPolarCoord(this);
		setTheta(getTheta() + angleDifference);
		System.out.println("In Polar: " + getX() + "," + getY());
		Vector.setCartesianCoord(this);
		System.out.println("In Cart: " + getX() + "," + getY());
	}

	public static void setPolarCoord(Vector p){
		double X = (double) p.getX();
		double Y = (double) p.getY();
		p.setR(p.magnitude());
		p.setTheta((float) (Math.toDegrees(Math.atan2(Y, X))));
	}
	
	public static void setCartesianCoord(Vector p){
		double r = (double) p.getR();
		double theta = (double) p.getTheta();
		
		if(theta == 90 || theta == -90)
			p.setX(0);
		else
			p.setX((float) (Math.cos(Math.toRadians(theta)) * r));
		
		if(theta == 180)
			p.setY(0);
		else
			p.setY((float) (Math.sin(Math.toRadians(theta)) * r));
	}
	
	public float getX(){return x;}
	public float getY(){return y;}
	public float getR(){return x;}
	public float getTheta(){return y;}
	
	public void setX(float x){this.x = x;}
	public void setY(float y){this.y = y;}
	public void setR(float r){this.x = r;}
	public void setTheta(float theta){this.y = theta;}
}
