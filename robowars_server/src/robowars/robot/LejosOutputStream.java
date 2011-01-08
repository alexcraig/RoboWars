package robowars.robot;

/**
 * LejosOutputStream.java
 * This Class is used to send objects from either the client or the mindstorm
 * it overcomes the problems with the Lejos library where objects cannot be properly
 * output
 * @author mwright
 */
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
	/**
	 * This is the function to actually send the object it takes 4 steps
	 * 1. A string is created with the correct object tag
	 * 2. The .toSting function of the object is called and added to the string
	 * 3. the string is turned into a byte array
	 * 4. the bytes are output followed by a null terminator
	 * @param o
	 * @throws IOException
	 */
	public synchronized void writeObject(Object o) throws IOException{
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
	/**
	 * Converts the input string into a byte array so we can transfer them
	 * @param inputText
	 * @return Array of Bytes
	 */
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
