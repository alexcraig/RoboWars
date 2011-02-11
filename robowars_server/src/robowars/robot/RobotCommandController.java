package robowars.robot;

import java.io.*;

import robowars.shared.model.*;

import lejos.nxt.LCD;
import lejos.nxt.comm.*;
import lejos.robotics.Pose;

public class RobotCommandController extends Thread{
	private RobotMovement move;
	private LejosOutputStream dataOut;
	private LejosInputStream dataIn;

	
	public RobotCommandController(RobotMovement movement){
		 NXTConnection connection = Bluetooth.waitForConnection();
		 LCD.drawString("Connected",0,0);
		 dataOut=new LejosOutputStream(connection.openDataOutputStream());
		 dataIn=new LejosInputStream(connection.openDataInputStream());
		 this.move=movement;
		 new Thread(new Runnable(){
			private Pose previousPose, currentPose;
			private final int IGNORE_THRESHOLD=3;
			private int ignore=0;
			public void run() {
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

