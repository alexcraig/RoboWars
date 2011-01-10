package robowars.robot;
/**
 * LejosInputStream.java
 * 
 * This Class is used to receive objects from either the client or the mindstorm
 * it overcomes the problems with the Lejos library where objects cannot be properly
 * received
 * 
 * @author mwright
 */
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
	/**
	 * Function to return the object from the stream.
	 * @return received object
	 * @throws IOException
	 */
	public synchronized Object readObject() throws IOException{
		int input=in.read();
		//System.out.println("READ STARTING FROM NXT: " + (char)input);
		
		//if robotCommand
		if((char)input=='1'){
			String s="";
			//read in object until finished
			while((input=in.read())!=-1&&(char)input!=']'){
				s+=(char)input;
			}
			//System.out.println("READ COMMAND FROM NXT: " + s);
			
			//decide what command
			s=s.substring(1,s.length());
			String type=s.substring(0,s.indexOf('|'));
			if(type.equals("MOVE_CONTINUOUS")){
				s=s.substring(s.indexOf("speed"));
				s=s.substring(s.indexOf(":")+1,s.indexOf('|'));
				return RobotCommand.moveContinuous(Float.parseFloat(s));
			}
			else if(type.equals("TURN_ANGLE_RIGHT")){
				s=s.substring(s.indexOf("turn"));
				s=s.substring(s.indexOf(":")+1,s.indexOf(']'));
				return RobotCommand.turnAngleRight(Integer.parseInt(s));
			}
			else if(type.equals("TURN_ANGLE_LEFT")){
				s=s.substring(s.indexOf("turn"));
				s=s.substring(s.indexOf(":")+1,s.indexOf(']'));
				return RobotCommand.turnAngleLeft(Integer.parseInt(s));
			}
			else if(type.equals("STOP")){
				return RobotCommand.stop();
			}
			else if(type.equals("EXIT")){
				return RobotCommand.exit();
			}
		}
		//Pose
		else if((char)input=='2'){
			String s="";
			//Read until finished
			while((input=in.read())!=-1&&input!=0){
				s+=(char)input;
				//System.out.println("READ POSE: " + (char)input + "(" + input + ") " + s);
				if((char)input==']') {
					break;
				}
			}
			//System.out.println("READ POSE FROM NXT: " + s);
			
			//find x,y,heading
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
		
		System.out.println("READ TYPE UNRECOGNIZED");
		return new Object();
	}


	public void close() throws IOException{in.close();}
	/**
	 * Function to build the MapPoint from the provided string
	 * @param s
	 * @return build MapPoint
	 */
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
	/**
	 * Function to return a vector of all the MapPoints from a given RoboMap object string
	 * @param s
	 * @param splitter
	 * @return Vector of compiled MapPoints
	 */
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
