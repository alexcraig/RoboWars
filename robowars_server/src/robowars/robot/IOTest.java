package robowars.robot;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import lejos.robotics.Pose;

import robowars.shared.model.RobotCommand;

public class IOTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		LejosOutputStream out;
		try {
			out = new LejosOutputStream(new FileOutputStream("iotest.txt"));
			out.writeObject(RobotCommand.moveContinuous(100));
			out.writeObject(RobotCommand.stop());
			out.writeObject(RobotCommand.exit());
			out.writeObject(RobotCommand.turnAngleLeft(90));
			out.writeObject(RobotCommand.turnAngleRight(90));
			out.writeObject(RobotCommand.rollingTurn(100, 90));
			out.writeObject(new Pose(0,(float)1.0,(float) 3.4));
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
			System.out.println(in.readObject());
			System.out.println(in.readObject());
			System.out.println(in.readObject());
			System.out.println(in.readObject());
			System.out.println(in.readObject());
			System.out.println(in.readObject());
			//pose
			Pose p=(Pose)in.readObject();
			System.out.println("POSE: "+p.getX()+" "+p.getY()+" "+p.getHeading());
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}

}
