package robowars.shared.model;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Vector;
import robowars.robot.*;

public class RobotMap {
	private Vector<MapPoint> points;
	public RobotMap(){
		points=new Vector<MapPoint>();
	}
	public RobotMap(Vector<MapPoint> points){
		this.points=points;
	}
	public RobotMap(String config){
		LejosInputStream stream;
		RobotMap temp = null;
		try {
			stream = new  LejosInputStream(new FileInputStream(config));
			temp=(RobotMap)stream.readObject();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for(int i=0; i<temp.getPoints().size(); i++){
			addPoint(temp.getPoints().get(i));
		}
		
	}
	public Vector<MapPoint> getPoints(){
		return points;
	}
	public MapPoint getPoint(int r, int g, int b){
		for(int i=0; i<points.size(); i++){
			MapPoint point=points.get(i);
			if(point.getR()==r&&point.getG()==g&&point.getB()==b)return point;
		}
		return new MapPoint();
	}
	public MapPoint getPoint(int x, int y){
		for(int i=0; i<points.size(); i++){
			MapPoint point=points.get(i);
			if(point.getX()==x&&point.getY()==y)return point;
		}
		return new MapPoint();
	}
	public void addPoint(MapPoint p){
		points.add(p);
	}
	public String toString(){
		String s="[";
		for(int i=0; i<points.size(); i++){
			s+=points.get(i).toString()+",";
		}
		s=s.substring(0, s.lastIndexOf(','));
		s+="]";
		return s;
	}
}
