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

import robowars.shared.model.*;

import lejos.robotics.Pose;
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

import lejos.nxt.LCD;
import lejos.robotics.Pose;


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
				if((char)input==']')break;
			}
			//}
			//System.out.println("READ COMMAND FROM NXT: " + s);
			
			//decide what command
			s=s.substring(1,s.length());
			int type=Integer.parseInt(s.substring(0,s.indexOf('|')));
			if(type==CommandType.MOVE_CONTINUOUS.ordinal()){
				s=s.substring(s.indexOf("|")+1);
				s=s.substring(0,s.indexOf("|"));
				return RobotCommand.moveContinuous(Float.parseFloat(s));
			}
			else if(type==CommandType.TURN_ANGLE_RIGHT.ordinal()){
				s=s.substring(s.indexOf("|")+1);
				s=s.substring(s.indexOf("|")+1);
				//System.out.println(s);
				return RobotCommand.turnAngleRight((int)Float.parseFloat(s));
			}
			else if(type==CommandType.TURN_ANGLE_LEFT.ordinal()){
				s=s.substring(s.indexOf("|")+1);
				s=s.substring(s.indexOf("|")+1);
				return RobotCommand.turnAngleLeft((int)Float.parseFloat(s));
			}
			else if (type==CommandType.ROLLING_TURN.ordinal()){
				s=s.substring(s.indexOf("|")+1);
				String speed=s.substring(0,s.indexOf("|"));
				String turn=s.substring(s.indexOf("|")+1);
				return RobotCommand.rollingTurn((int)Float.parseFloat(speed), (int)Float.parseFloat(turn));
			}
			else if (type==CommandType.SET_POSITION.ordinal()){
				s=s.substring(s.indexOf("|")+1);
				s=s.substring(s.indexOf("|")+1);
				s=s.substring(s.indexOf("|")+1);
				String x=s.substring(0,s.indexOf("|"));
				s=s.substring(s.indexOf("|")+1);
				String y=s.substring(0,s.indexOf("|"));
				s=s.substring(s.indexOf("|")+1);
				s=s.substring(s.indexOf("|")+1);
				String c=s;
				return RobotCommand.setPosition(new Pose(Float.parseFloat(x),Float.parseFloat(y),Float.parseFloat(c)));
			}
			else if(type==CommandType.STOP.ordinal()){
				return RobotCommand.stop();
			}
			else if(type==CommandType.EXIT.ordinal()){
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
			return new Pose(Float.parseFloat(x),Float.parseFloat(y),Float.parseFloat(h));
		}
		else if((char) input=='3'){
			String s="";
			while((input=in.read())!=-1&&input!=0){
				s+=(char)input;
			}
			s=s.substring(1);
			s=s.substring(1, s.length()-1);
			System.out.println("Points Generating");
			Vector points=getPoints(s,'[');
			System.out.println("Points Generated"+points.size());
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
			RobotMap map=new RobotMap();
			for(int i=0; i<points.size(); i++){
				LCD.drawInt(i,0,0);
				map.addPoint((MapPoint) points.elementAt(i));
			}
			System.out.println("Map Generated");
			return map;
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
		String color=s;
		return new MapPoint(Integer.parseInt(x),Integer.parseInt(y),Integer.parseInt(color));
		
	}
	/**
	 * Function to return a vector of all the MapPoints from a given RoboMap object string
	 * @param s
	 * @param splitter
	 * @return Vector of compiled MapPoints
	 */
	private Vector getPoints(String s, char splitter) {
		Vector v=new Vector();
		while(s.indexOf(splitter)!=-1&&s!=""){
			String point="";
			point=s.substring(0, s.indexOf(splitter)-1);
			//point=point.substring(1,point.length()-1);
			v.addElement(buildPoint(point));
			s=s.substring(s.indexOf(splitter)+1);
		}
		String point=s;
		point=point.substring(0,point.length()-1);
		v.addElement(buildPoint(point));
		return v;
	}
}
