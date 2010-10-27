package robowars.robot;

import lejos.nxt.Motor;
import lejos.robotics.navigation.*;

public class RobotMovement {
	private TachoPilot pilot;
	private final static int MINDSTORM_WIDTH=15;
	private final static int WHEEL_HEIGHT=3;
	private final static int SPEED=10;
	public RobotMovement(){
		pilot=new TachoPilot(WHEEL_HEIGHT,MINDSTORM_WIDTH, Motor.C, Motor.A, true);
		pilot.setSpeed(SPEED);
	}
	
	public void moveForward() {
		// TODO Auto-generated method stub
		if(pilot.isMoving())pilot.stop();
		pilot.forward();
	}
	public void moveBackwards() {
		// TODO Auto-generated method stub
		if(pilot.isMoving())pilot.stop();
		pilot.backward();
	}
	public void turnRight() {
		// TODO Auto-generated method stub
		if(pilot.isMoving())pilot.stop();
		pilot.rotate(10);
	}
	public void turnLeft() {
		// TODO Auto-generated method stub
		if(pilot.isMoving())pilot.stop();
		pilot.rotate(-10);
	}
}
