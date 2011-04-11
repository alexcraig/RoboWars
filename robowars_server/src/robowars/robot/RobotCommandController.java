package robowars.robot;

import java.io.*;

import robowars.shared.model.CommandType;
import robowars.shared.model.RobotCommand;
import lejos.nxt.LCD;
import lejos.nxt.comm.*;
import lejos.robotics.Pose;
/**
 * Main control point of the robot.
 * It starts both the colorSensor and the 
 * positionTracker. After both have started it
 * acts as the proxy to the server.
 */
public class RobotCommandController extends Thread{
	private RobotMovement move;
	private LejosOutputStream dataOut;
	private LejosInputStream dataIn;
	private ColorSensor colorSensor;
	
	
	public RobotCommandController(RobotMovement movement){
		 NXTConnection connection = Bluetooth.waitForConnection();
		 LCD.drawString("Connected",0,0);
		 dataOut=new LejosOutputStream(connection.openDataOutputStream());
		 dataIn=new LejosInputStream(connection.openDataInputStream());
		 LCD.drawString("Streams opened",0,1);
		 this.move=movement;
		 LCD.drawString("Move created", 0, 3);
		 colorSensor=new ColorSensor(dataOut, move, false);
		 new Thread(colorSensor).start();
		 try {
			Thread.sleep(1000);
		 } catch (InterruptedException e2) {
		 }
		 new Thread(positionTracker).start();
	}
	/**
	 * Proxy function used to poll and read for commands which are then acted upon.
	 */
	public void run(){
		System.out.println("INSStarted");
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e2) {
		}
		Object input=null;
		try {
			while((input=dataIn.readObject())!=null){
				if(input instanceof RobotCommand){
					RobotCommand command= (RobotCommand)input;
					if(command.getType()==CommandType.SET_POSITION){
						move.setPos(command.getPos());
					}
					if(command.getType()==CommandType.TURN_ANGLE_LEFT){
						move.turnLeft(command.getTurnBearing());
					}
					if(command.getType()==CommandType.TURN_ANGLE_RIGHT){
						move.turnRight(command.getTurnBearing());
					}
					if(command.getType()==CommandType.MOVE_CONTINUOUS){
						move.moveContinuous(command);
					}
					if(command.getType()==CommandType.ROLLING_TURN){
						move.rollingTurn(command);
					}
					if(command.getType()==CommandType.STOP){
						move.stop();
					}
					if(command.getType()==CommandType.EXIT){
						System.exit(0);
					}
				}
				else{
					System.out.println("UNREADABLE OBJECT");
				}
			}
		} catch (IOException e) {
			System.exit(0);
		} 
		System.out.println("Received null terminating");
		try {
			dataIn.close();
			dataOut.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			
		}
		System.exit(0);
		
	}
	/** position relay will periodically send the position of the robot back to the server */
	private Runnable positionTracker=new Runnable(){
		private Pose previousPose, currentPose;
		private final int IGNORE_THRESHOLD=5;
		private int ignore=0;
		public void run() {
		LCD.drawString("PTStarted",0,5);
		while(true){
			if(move!=null){
				try {
					if(previousPose==null){
						currentPose=move.getPosition();
						previousPose=currentPose;
					}
					else{
						currentPose=move.getPosition();
					}
					if(currentPose!=previousPose||ignore>IGNORE_THRESHOLD){
						LCD.drawString("Position Updated"+currentPose,0,2);
					dataOut.writeObject(currentPose);
					previousPose=currentPose;
					ignore=0;
				}
				else{
					ignore++;
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.out.println("EXCEPTION:"+ e);
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
							}
							System.exit(0);
						}
					}
				}
			} 
	 };
}

