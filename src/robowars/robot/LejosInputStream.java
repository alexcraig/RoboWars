package robowars.robot;

import java.io.*;

import lejos.robotics.Pose;

import robowars.shared.model.CommandType;
import robowars.shared.model.RobotCommand;

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
				return new RobotCommand(CommandType.MOVE_CONTINUOUS);
			}
			else if(type.equals("TURN_RIGHT_ANGLE_RIGHT")){
				return new RobotCommand(CommandType.TURN_RIGHT_ANGLE_RIGHT);
			}
			else if(type.equals("TURN_RIGHT_ANGLE_LEFT")){
				return new RobotCommand(CommandType.TURN_RIGHT_ANGLE_LEFT);
			}
			else if(type.equals("STOP")){
				return new RobotCommand(CommandType.STOP);
			}
			else if(type.equals("EXIT")){
				return new RobotCommand(CommandType.EXIT);
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
		return new Object();
	}
	public void close() throws IOException{in.close();}
}
