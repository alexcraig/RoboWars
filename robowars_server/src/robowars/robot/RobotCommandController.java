package robowars.robot;
import java.io.*;

import robowars.shared.model.CommandType;
import robowars.shared.model.RobotCommand;
import robowars.shared.model.RobotMap;
import lejos.nxt.LCD;
import lejos.nxt.comm.*;
import lejos.robotics.Pose;

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
		 this.move=movement;
		 colorSensor=new ColorSensor(dataOut, move);
		 new Thread(new Runnable(){
			private Pose previousPose, currentPose;
			private final int IGNORE_THRESHOLD=5;
			private int ignore=0;
			public void run() {
				while(colorSensor==null){
					try {
						Thread.sleep(50);
					} catch (InterruptedException e) {
					}
				}
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
								if(colorSensor!=null){
									dataOut.writeObject(currentPose);
								}
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
					try {
						sleep(333);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
					
					}
				}
			} 
		 }).start();
	}
	public void run(){
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
				else if(input instanceof RobotMap){
					if(colorSensor==null){
						System.out.println("Map Received");
						colorSensor=new ColorSensor(dataOut, move);
						System.out.println("Sensor Made");
						colorSensor.start();
						System.out.println("Sensor Started");
					}
					else{
						System.out.println("Map 2 Received");
						colorSensor.yield();
						colorSensor=new ColorSensor(dataOut, move);
						colorSensor.start();
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
}

