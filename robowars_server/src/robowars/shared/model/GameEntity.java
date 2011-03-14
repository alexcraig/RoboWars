package robowars.shared.model;

import java.io.Serializable;
import java.math.*;
import lejos.robotics.Pose;

/**
*
* @author Alex Dinardo
*
*/
public abstract class GameEntity implements Serializable{


	private static final long serialVersionUID = 5016873544195577415L;
	protected Pose pose;
	protected Vector vertices[];
	protected Vector edges[];
	protected int id;
	

	public GameEntity(Pose pose, Vector vertices[], int id){
		this.id=id;
		this.pose = pose;
		if(vertices != null)
			setVertices(vertices);
	}
	
	//TODO: This is general polygon collision detection with support for
	//rotating structures, but an easy way to represent circles is still needed.
	//Perhaps a shape with one vertex to represent radius?
	public boolean checkCollision(GameEntity target){
		//System.out.print("Checking collide... ");
		Vector currentEdge;
		for (int i = 0; i < edges.length + target.getEdges().length; i++){
			if (i < edges.length) {
	            currentEdge = edges[i];
	        } else {
	            currentEdge = target.getEdges()[i - edges.length];
	        }
			Vector perpAxis = new Vector(-currentEdge.getY(), currentEdge.getX());
	        perpAxis.unitVector();
	        
	        Vector intervalA = new Vector(0,0);
	        Vector intervalB = new Vector(0,0);
	        projectToAxis(perpAxis, this.vertices,  intervalA);
	        projectToAxis(perpAxis, target.getVertices(),  intervalB);
	        if (intervalDistance(intervalA, intervalB) > 0){
	        	//System.out.println("None.");
	            return false;
	        }

		}
		//System.out.println("Collision between " + this.getId() + " and " + target.getId());
		return true;
	}
	
	public void setPose(Pose pose){
		float headingDiff = pose.getHeading() - this.pose.getHeading();
		float xDiff = pose.getX() - this.pose.getX();
		float yDiff = pose.getY() - this.pose.getY();
		System.out.println("Entity movement diffs: (" + xDiff + "," + yDiff + "," + headingDiff + ")");
		this.pose = pose;
		for (Vector v : vertices){
			v.setX(v.getX() + xDiff);
			v.setY(v.getY() + yDiff);
		}
		compensateForRotation(vertices, headingDiff);
		generateEdges();
	}
	
	public void setVertices(Vector vertices[]){
		this.vertices = vertices;
		for (Vector v : this.vertices){
			v.setX(v.getX() + pose.getX());
			v.setY(v.getY() + pose.getY());
		}
		compensateForRotation(this.vertices, pose.getHeading());
		generateEdges();
	}
	
	public void setVertex(int index, Vector vector){
		vertices[index] = vector;
	}
	
	public void getCoordArrays(int x[], int y[]){
		for(int i = 0; i < vertices.length; i++){
			x[i] = (int) vertices[i].getX();
			y[i] = (int) vertices[i].getY();
		}
	}
	
	public Pose clonePose(){
		return new Pose(pose.getX(), pose.getY(), pose.getHeading());
	}
	
	public Pose getPose(){
		return pose;
	}
	
	public Vector[] getVertices(){
		return vertices;
	}
	
	public Vector getVertex(int index){
		return vertices[index];
	}
	
	public int getId(){return id;}
	
	public Vector[] getEdges(){return edges;}
	
	private Vector[] generateEdges(){
		Vector p1;
		Vector p2;
		edges = new Vector[vertices.length];
		for (int i = 0; i < vertices.length; i++) {
			p1 = vertices[i];
			if (i + 1 >= vertices.length) {
				p2 = vertices[0];
			} else {
				p2 = vertices[i + 1];
			}
			edges[i] = new Vector(p2.getX() - p1.getX(), p2.getY() - p1.getY());
		}
		return edges;
	}
	
	private void projectToAxis(Vector perpAxis, Vector[] vertices, Vector interval) {

		float dotProduct = perpAxis.dotProduct(vertices[0]);
		interval.setX(dotProduct);
		interval.setY(dotProduct);
		for (int i = 0; i < vertices.length; i++) {
			dotProduct = vertices[i].dotProduct(perpAxis);
			if (dotProduct < interval.getX()) {
				interval.setX(dotProduct);
			} else {
				if (dotProduct > interval.getY()) {
					interval.setY(dotProduct);
				}
			}
		}
	}
	
	private float intervalDistance(Vector intervalA, Vector intervalB) {
	    if (intervalA.getX() < intervalB.getX()) {
	        return intervalB.getX() - intervalA.getY();
	    } else {
	        return intervalA.getX() - intervalB.getY();
	    }
	}
	
	protected void compensateForRotation(Vector points[], float angleDifference){
		for (Vector p : points){
			p.setX(p.getX() - pose.getX());
			p.setY(p.getY() - pose.getY());
			Vector.setPolarCoord(p);
			p.setTheta(p.getTheta() + angleDifference);
			Vector.setCartesianCoord(p);
			p.setX(p.getX() + pose.getX());
			p.setY(p.getY() + pose.getY());
		}
	}
}
