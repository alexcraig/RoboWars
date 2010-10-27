package robowars.robot;
import java.io.*;

import lejos.nxt.comm.*;
import lejos.nxt.*;
public class robotMain {
	private static DataOutputStream dataOut;
	private static DataInputStream dataIn;
	public static void main (String args[]){
	    RobotMovement move = new RobotMovement();
		NXTConnection connection = USB.waitForConnection();
		dataOut=connection.openDataOutputStream();
		dataIn=connection.openDataInputStream();
		int input;
		try {
			while((input=dataIn.readChar())!=-1){
				if(input==1){
				 LCD.drawString("Forward",3,4);
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