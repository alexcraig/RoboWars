package robowars.robot;
/** 
 * RoboMovement.java
 * Thread safe controller for the mindstorm robot
 * @author mwright
 */

import robowars.shared.model.RobotCommand;
import lejos.nxt.LCD;
import lejos.nxt.Motor;
import lejos.robotics.*;
import lejos.robotics.navigation.*;

public class RobotMovement {
	private TachoPilot pilot;
	private SimpleNavigator navigator;
	private final static float MINDSTORM_WIDTH=11;
	private final static float WHEEL_HEIGHT=(float) 4.3;
	private final static int SPEED=75;
	int count=0;
	
	public RobotMovement(){
		pilot=new TachoPilot(WHEEL_HEIGHT,MINDSTORM_WIDTH, Motor.C, Motor.A, true);
		pilot.setSpeed(SPEED);
		navigator=new SimpleNavigator(pilot);
	}
	
	public synchronized void moveForward() {
		if(pilot.isMoving())navigator.stop();
		navigator.forward();
	}
	public synchronized void updatePosition(){
		navigator.updatePosition();
	}
	public synchronized void moveBackwards() {
		if(pilot.isMoving())navigator.stop();
		navigator.backward();
	}
	public synchronized void turnRight(float f) {
		if(pilot.isMoving())navigator.stop();
		navigator.rotate(f);
	}
	public synchronized void turnLeft(float f) {
		if(pilot.isMoving())navigator.stop();
		navigator.rotate(-f);
	}
	public synchronized void rollingTurn(RobotCommand command){
		count++;
		LCD.drawString("RC:"+count, 0,4);
		navigator.setMoveSpeed(command.getThrottle());
		navigator.steer((int)(command.getTurnBearing()));
	}
	public synchronized void speedUp(){
		if(pilot.getMoveSpeed()<pilot.getMoveMaxSpeed())navigator.setMoveSpeed((pilot.getMoveSpeed()*(float)1.1));
	}
	public synchronized void slowDown(){
		if(pilot.getMoveSpeed()>1)navigator.setMoveSpeed((pilot.getMoveSpeed()/(float)1.1));
	}
	public synchronized void stop(){
		System.out.println("Stop");
		navigator.stop();
	}
	public synchronized void setPos(Pose newPos){
		navigator.setPose(newPos);
	}
	public Pose getPosition(){
		return navigator.getPose();
	}

	public synchronized void moveContinuous(RobotCommand command) {
		navigator.updatePosition();
		navigator.steer(0);
	}
}
