package robowars.shared.model;

import java.math.*;

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
		float magnitude = (float) Math.sqrt(x*x + y*y);
	}

	public static void setPolarCoord(Vector p){
		double X = (double) p.getX();
		double Y = (double) p.getY();
		p.setR((float) Math.sqrt(Math.pow(X, 2) + Math.pow(Y, 2)));
		p.setTheta((float) (Math.toDegrees(Math.atan2(Y, X))));
	}
	
	public static void setCartesianCoord(Vector p){
		double r = (double) p.getR();
		double theta = (double) p.getTheta();
		p.setX((float) (Math.cos(Math.toRadians(theta)) * r));
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
