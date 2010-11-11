import java.io.*;

import lejos.nxt.comm.*;
import lejos.nxt.*;
import lejos.nxt.Motor;
import lejos.robotics.navigation.*;
public class mindStormMain {
	private static ObjectOutputStream dataOut;
	private static DataInputStream dataIn;
	private static TachoPilot pilot;
	private static SimpleNavigator navigator;
	private final static int MINDSTORM_WIDTH=15;
	private final static int WHEEL_HEIGHT=3;
	private final static int SPEED=100;
	public static void main (String args[]){
	    RobotMovement move = new RobotMovement();
		NXTConnection connection =USB.waitForConnection();
		LCD.drawString("Connected", 1, 3);		
		dataOut=new ObjectOutputStream(connection.openDataOutputStream());
		dataIn=connection.openDataInputStream();
		int input;
		pilot=new TachoPilot(WHEEL_HEIGHT,MINDSTORM_WIDTH, Motor.C, Motor.A, true);
		pilot.setSpeed(SPEED);
		navigator=new SimpleNavigator(pilot);
		try {
			while((input=dataIn.read())!=-1){
				LCD.clearDisplay();
				LCD.drawString(((Integer)input).toString(),2,4);
				if(input==1){
					if(pilot.isMoving())pilot.stop();
					pilot.forward();
				}
				if(input==2){
					if(pilot.isMoving())pilot.stop();
					pilot.backward();
				}
				if(input==3){
					if(pilot.isMoving())pilot.stop();
					pilot.rotate(10);
				}
				if(input==4){
					if(pilot.isMoving())pilot.stop();
					pilot.rotate(-10);
				}
				if(input==5){
					pilot.stop();
				}
				if(input==6){
					if(pilot.getMoveSpeed()<pilot.getMoveMaxSpeed())pilot.setMoveSpeed((pilot.getMoveSpeed()*(float)1.1));
				}
				if(input==7){
					if(pilot.getMoveSpeed()>1)pilot.setMoveSpeed((pilot.getMoveSpeed()/(float)1.1));
				}
				if(input==8){
					System.exit(0);
				}
				dataOut.writeObject(navigator.getPose());
			}
		} catch (IOException e) {
		}
	}
}
