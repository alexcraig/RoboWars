package robowars.robot;

import java.io.IOException;
import java.io.OutputStream;
import lejos.robotics.Pose;
import robowars.shared.model.RobotCommand;
import robowars.shared.model.RobotMap;

public class LejosOutputStream {
	private OutputStream out;
	public LejosOutputStream(OutputStream out){
		this.out=out;
	}
	public void writeObject(Object o) throws IOException{
		String s=null;
		if(o instanceof RobotCommand){
			s="1";
			s+=((RobotCommand)o).toString();
		}
		else if(o instanceof Pose){
			Pose p=(Pose)o;
			s="2[";
			s+="x:"+p.getX();
			s+="|y:"+p.getY();
			s+="|h:"+p.getHeading()+"]";
		}
		else if(o instanceof RobotMap){
			s="3";
			s+=((RobotMap)o).toString();
		}
		else return;
		byte[] bytes=getBytes(s);
		if(bytes!=null){
			for(int i=0; i<bytes.length-1; i++){
					out.write((int)bytes[i]);
			}
		}
	}
	private byte[] getBytes(String inputText){
	    	//Debug Point
	        byte[] nameBytes = new byte[inputText.length()+1];
	        
	        for(int i=0;i<inputText.length();i++){
	            nameBytes[i] = (byte) inputText.charAt(i);
	        }
	        nameBytes[inputText.length()] = 0;
	 
	        return nameBytes;
	}
	public void flush() throws IOException{out.flush();}
	public void close() throws IOException{out.close();}
}
