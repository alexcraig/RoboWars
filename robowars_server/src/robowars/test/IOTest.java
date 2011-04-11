package robowars.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Vector;

import lejos.robotics.Pose;

import robowars.robot.ColorSensor;
import robowars.robot.LejosInputStream;
import robowars.robot.LejosOutputStream;
import robowars.shared.model.RobotCommand;
import robowars.shared.model.RobotMap;

public class IOTest {

	/**
	 * Test class for IOStreams and message protocol
	 * tests the 10 commands and objects for the robots
	 * 
	 * It writes the 10 commands or objects to a file in the 
	 * same manner as they would receive going over bluetooth
	 * which are then read back into the system and verified.
	 */
	public static final float DOT_SPACING=6.35f;
	public static final int COLS=21;
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		RobotCommand mc =RobotCommand.moveContinuous(100);
		RobotCommand stop = null;
		RobotCommand exit =null;
		RobotCommand leftt=null;
		RobotCommand rightt=null;
		LejosOutputStream out=null;
		RobotCommand rollt=null;
		RobotCommand setp=null;
		Vector v=null;
		Pose p=null;
		RobotMap map=null;
		try {
			out = new LejosOutputStream(new FileOutputStream("iotest.txt"));
			out.writeObject(mc);
			stop=RobotCommand.stop();
			out.writeObject(stop);
			exit=RobotCommand.exit();
			out.writeObject(exit);
			leftt=RobotCommand.turnAngleLeft(90);
			out.writeObject(leftt);
			rightt=RobotCommand.turnAngleRight(90);
			out.writeObject(rightt);
			rollt=RobotCommand.rollingTurn(90,100);
			out.writeObject(rollt);
			setp=RobotCommand.setPosition(new Pose(1,2,3));
			out.writeObject(setp);
			p=new Pose(0,(float)1.0,(float) 3.4);
			out.writeObject(p);
			v=new Vector();
			v.addElement(1);
			v.addElement(2);
			v.addElement(3);
			out.writeObject(v);
			map=ColorSensor.generate(COLS,COLS,DOT_SPACING);
			out.writeObject(map);
			out.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		LejosInputStream in;
		try {
			in = new LejosInputStream(new FileInputStream("iotest.txt"));
			System.out.println("MOVE CONTINUOUS: " + (mc.getThrottle()==((RobotCommand)in.readObject()).getThrottle()));
			System.out.println("STOP: "+(stop.getThrottle()==((RobotCommand)in.readObject()).getThrottle()));
			System.out.println("EXIT: "+(exit.getThrottle()==((RobotCommand)in.readObject()).getThrottle()));
			System.out.println("LEFT TURN: "+(leftt.getTurnBearing()==((RobotCommand)in.readObject()).getTurnBearing()));
			System.out.println("RIGHT CONTINUOUS: "+(rightt.getTurnBearing()==((RobotCommand)in.readObject()).getTurnBearing()));
			Object rollC=in.readObject();
			System.out.println("ROLLING TURN: "+(rollt.getTurnBearing()==((RobotCommand)rollC).getTurnBearing()&&rollt.getThrottle()==((RobotCommand)rollC).getThrottle()));
			Pose testP=((RobotCommand)in.readObject()).getPos();
			System.out.println("SET POSE: "+(setp.getPos().getX()==testP.getX()&&setp.getPos().getY()==testP.getY()&&setp.getPos().getHeading()==testP.getHeading()));
			testP=(Pose) in.readObject();
			System.out.println("POSE: "+(p.getX()==testP.getX()&&p.getY()==testP.getY()&&p.getHeading()==testP.getHeading()));
			Vector testV=(Vector)in.readObject();
			System.out.println("COLOR VECTOR:"+(v.get(0)==testV.get(0)&&v.get(1)==testV.get(1)&&v.get(2)==testV.get(2)));
			System.out.println("ROBOT MAP:"+testMap((RobotMap) in.readObject(), map));
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}
	/**
	 * Function to test if each map point has been recovered
	 */
	private static boolean testMap(RobotMap in, RobotMap test){
		for(int i=0; i<in.getPoints().size(); i++){
			for(int x=0; x<((Vector)in.getPoints().get(i)).size(); x++){
				if(((Vector)in.getPoints().get(i)).get(x)!=((Vector)in.getPoints().get(i)).get(x))return false;
			}
		}
		return true;
	}
}
