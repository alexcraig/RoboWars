package robowars.robot;

import java.io.*;
import java.util.Vector;

import lejos.robotics.Pose;

import robowars.shared.model.CommandType;
import robowars.shared.model.MapPoint;
import robowars.shared.model.RobotCommand;
import robowars.shared.model.RobotMap;

public class LejosInputStream {
	private InputStream in;

	public LejosInputStream(InputStream in){
		this.in=in;
	}
	public Object readObject() throws IOException{
		int input=in.read();
		//robotCommand
		if((char)input=='1'){
			String s="";
			while((input=in.read())!=-1&&(char)input!=']'){
				s+=(char)input;
			}
			s=s.substring(1,s.length());
			String type=s.substring(0,s.indexOf('|'));
			if(type.equals("MOVE_CONTINUOUS")){
				return new RobotCommand(CommandType.MOVE_CONTINUOUS,99);
			}
			else if(type.equals("TURN_RIGHT_ANGLE_RIGHT")){
				return new RobotCommand(CommandType.TURN_RIGHT_ANGLE_RIGHT,99);
			}
			else if(type.equals("TURN_RIGHT_ANGLE_LEFT")){
				return new RobotCommand(CommandType.TURN_RIGHT_ANGLE_LEFT,99);
			}
			else if(type.equals("STOP")){
				return new RobotCommand(CommandType.STOP,99);
			}
			else if(type.equals("EXIT")){
				return new RobotCommand(CommandType.EXIT,99);
			}
		}
		//Pose
		else if((char)input=='2'){
			String s="";
			while((input=in.read())!=-1&&input!=0){
				s+=(char)input;
			}
			s=s.substring(1,s.length()-1);
			String x=s.substring(0,s.indexOf("|"));
			s=s.substring(s.indexOf("|")+1);
			String y=s.substring(0,s.indexOf("|"));
			s=s.substring(s.indexOf("|")+1);
			String h=s;
			x=x.substring(2);
			y=y.substring(2);
			h=h.substring(2);
			return new Pose(Float.parseFloat(x),Float.parseFloat(y),Float.parseFloat(h));
		}
		else if((char) input=='3'){
			String s="";
			while((input=in.read())!=-1&&input!=0){
				s+=(char)input;
			}
			s=s.substring(1);
			s=s.substring(0, s.length()-1);
			Vector<MapPoint> points=getPoints(s,',');
			return new RobotMap(points);
		}
		return new Object();
	}


	public void close() throws IOException{in.close();}
	private MapPoint buildPoint(String s){
		String x=s.substring(0,s.indexOf("|"));
		s=s.substring(s.indexOf("|")+1);
		String y=s.substring(0,s.indexOf("|"));
		s=s.substring(s.indexOf("|")+1);
		String r=s.substring(0,s.indexOf("|"));
		s=s.substring(s.indexOf("|")+1);
		String g=s.substring(0,s.indexOf("|"));
		s=s.substring(s.indexOf("|")+1);
		String b=s;
		x=x.substring(2);
		y=y.substring(2);
		r=r.substring(2);
		g=g.substring(2);
		b=b.substring(2);
		return new MapPoint(Integer.parseInt(x),Integer.parseInt(y),Integer.parseInt(r),Integer.parseInt(g),Integer.parseInt(b));
		
	}
	private Vector<MapPoint> getPoints(String s, char splitter) {
		Vector<MapPoint> v=new Vector<MapPoint>();
		while(s.indexOf(splitter)!=-1){
			String point="";
			point=s.substring(0, s.indexOf(splitter));
			point=point.substring(1,point.length()-1);
			v.add(buildPoint(point));
			s=s.substring(s.indexOf(splitter)+1);
		}
		String point=s;
		point=point.substring(1,point.length()-1);
		v.add(buildPoint(point));
		return v;
	}
}
