package robowars.robot;

import java.io.*;

import lejos.nxt.comm.*;

public class RobotCommandController extends Thread{
	private RobotMovement move;
	private ObjectOutputStream dataOut;
	private DataInputStream dataIn;
	
	public RobotCommandController(){
		 NXTConnection connection = USB.waitForConnection();
		 try {
			dataOut=new ObjectOutputStream(connection.openDataOutputStream());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 dataIn=connection.openDataInputStream();
		 move=new RobotMovement();
	}
	
	public void run(){
		int input;
		try {
			while((input=dataIn.readInt())!=-1){
				if(input==1){
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
				if(input==5){
					move.stop();
				}
				if(input==6){move.speedUp();}
				if(input==7)move.slowDown();
				if(input==8)System.exit(0);
				dataOut.writeObject(move.getPosition());
			}
		} catch (IOException e) {
		}
	}
}
