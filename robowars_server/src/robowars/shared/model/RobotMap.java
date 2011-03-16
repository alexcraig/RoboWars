package robowars.shared.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Vector;

import robowars.robot.LejosInputStream;

import lejos.nxt.LCD;
import lejos.robotics.Pose;

public class RobotMap {
	private Vector gPoints;
	private Vector bPoints;
	private Vector rPoints;
	private Vector yPoints;
	private Vector points;
	public RobotMap(){
		gPoints=new Vector();
		bPoints=new Vector();
		rPoints=new Vector();
		yPoints=new Vector();
		points=new Vector();
		points.addElement(gPoints);
		points.addElement(bPoints);
		points.addElement(yPoints);
		points.addElement(rPoints);
	}
	public RobotMap(Vector points){
		this.points=points;
	}
	public RobotMap(File config){
		LejosInputStream stream = null;
		RobotMap temp = null;
		gPoints=new Vector();
		bPoints=new Vector();
		rPoints=new Vector();
		yPoints=new Vector();
		points=new Vector();
		points.addElement(gPoints);
		points.addElement(bPoints);
		points.addElement(yPoints);
		points.addElement(rPoints);
		try {
			stream = new  LejosInputStream(new FileInputStream(config));
			temp=(RobotMap)stream.readObject();
		} catch (FileNotFoundException e) {
			System.out.println("File Not Found");
		} catch (IOException e) {
			System.out.println("IOEXCEPTION");
		}
		for(int i=0; i<temp.getPoints().size(); i++){
			for(int x=0; x<((Vector) temp.getPoints().elementAt(i)).size(); x++){
				addPoint((MapPoint)((Vector) temp.getPoints().elementAt(i)).elementAt(x));
			}
		}
		try {
			stream.close();
		} catch (IOException e) {
		}
		
	}
	public Vector getPoints(){
		return points;
	}
	public MapPoint getPoint(float x, float y, float h, int color){
		Vector l=null;
		if(color==lejos.robotics.Colors.RED)l=rPoints;
		else if(color==lejos.robotics.Colors.BLUE)l=bPoints;
		else if(color==lejos.robotics.Colors.YELLOW)l=yPoints;
		else if(color==lejos.robotics.Colors.GREEN)l=gPoints;
		return findClosest(x, y, l);
	}
	private MapPoint findClosest(float x, float y, Vector list) {
		float difference=(float) 10000.0;
		int index=-1;
		for(int i=0; i<list.size(); i++){
			float dx=x-((MapPoint) list.elementAt(i)).getX();
			float dy=y-((MapPoint) list.elementAt(i)).getY();
			float newDiff=(float) Math.sqrt(((dx*dx) + (dy*dy)));
			if(newDiff<difference){
				index=i;
				difference=newDiff;
				if(difference<2)return (MapPoint) list.elementAt(i);
			}
		}
		return (MapPoint) list.elementAt(index);
	}
	public void addPoint(MapPoint p){
		if(p!=null){
			//LCD.drawInt(p.getColor(), 0, 1);
			if(p.getColor()==lejos.robotics.Colors.RED)rPoints.addElement(p);
			else if(p.getColor()==lejos.robotics.Colors.BLUE)bPoints.addElement(p);
			else if(p.getColor()==lejos.robotics.Colors.YELLOW)yPoints.addElement(p);
			else if(p.getColor()==lejos.robotics.Colors.GREEN)gPoints.addElement(p);
		}
	}
	public String toString(){
		String s="[";
		for(int i=0; i<points.size(); i++){
			for(int x=0; x<((Vector)points.elementAt(i)).size(); x++){
				s+=((MapPoint)(((Vector)points.elementAt(i)).elementAt(x))).toOutputString();
			}
		}
		s+="]";
		return s;
	}
	public Pose getStartPoint(int index){
		if(index==0){
			return new Pose((float)(findBiggestX()*.5),(float) (findBiggestY()*.25), (float)90);
		}
		if(index==1){
			return new Pose((float)(findBiggestX()*.5),(float) (findBiggestY()*.75), (float)270);
		}
		
		return null;
	}
	public float findBiggestX(){
		float biggest=-1;
		for( int i=0; i<points.size(); i++){
			for(int x=0; x<((Vector)points.elementAt(i)).size(); x++){
				if(((MapPoint)((Vector)points.elementAt(i)).elementAt(x)).getX()>biggest){
					biggest=((MapPoint)((Vector)points.elementAt(i)).elementAt(x)).getX();
				}
			}
		}
		return biggest;
	}
	public float findBiggestY(){
		float smallest=999999;
		for( int i=0; i<points.size(); i++){
			for(int x=0; x<((Vector)points.elementAt(i)).size(); x++){
				if(((MapPoint)((Vector)points.elementAt(i)).elementAt(x)).getX()<smallest){
					smallest=((MapPoint)((Vector)points.elementAt(i)).elementAt(x)).getX();
				}
			}
		}
		return smallest;
	}
}
