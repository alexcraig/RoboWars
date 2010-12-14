package robowars.shared.model;

import java.util.*;
public class RobotMap {
	private Vector<MapPoint> points;
	public RobotMap(){
		points=new Vector<MapPoint>();
	}
	public RobotMap(Vector<MapPoint> points){
		this.points=points;
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
