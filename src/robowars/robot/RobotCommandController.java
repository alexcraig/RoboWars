package robowars.robot;

import java.io.*;

import lejos.nxt.comm.*;

public class RobotCommandController extends Thread{
	private RobotMovement move;
	private DataOutputStream dataOut;
	private DataInputStream dataIn;
	
	public RobotCommandController(){
		 NXTConnection connection = USB.waitForConnection();
		 dataOut=connection.openDataOutputStream();
		 dataIn=connection.openDataInputStream();
		 move=new RobotMovement();
	}
	public void run(){
		int input;
		try {
			while((input=dataIn.readInt())!=-1){
				if(input==1){
					System.out.println("I GET HERE");
					move.moveForward();
				}
				if(input==2){
					move.moveBackwards();
				}
				if(input==3){
					move.turnRight();
				}
				if(input==4){
					move.turnLeft();
				}
			}
		} catch (IOException e) {
		}
	}
}
