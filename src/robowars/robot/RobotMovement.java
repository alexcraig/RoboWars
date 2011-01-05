package robowars.robot;

/** 
 * RoboMovement.java
 * Thread safe controller for the mindstorm robot
 * @author mwright
 */

import robowars.shared.model.RobotCommand;
import lejos.nxt.Motor;
import lejos.robotics.*;
import lejos.robotics.navigation.*;

public class RobotMovement {
	private TachoPilot pilot;
	private SimpleNavigator navigator;
	private final static int MINDSTORM_WIDTH=110;
	private final static int WHEEL_HEIGHT=43;
	private final static int SPEED=100;
	
	public RobotMovement(){
		pilot=new TachoPilot(WHEEL_HEIGHT,MINDSTORM_WIDTH, Motor.C, Motor.A, true);
		pilot.setSpeed(SPEED);
		navigator=new SimpleNavigator(pilot);
	}
	
	public synchronized void moveForward() {
		// TODO Auto-generated method stub
		if(pilot.isMoving())pilot.stop();
		pilot.travel(15);
	}
	public synchronized void moveBackwards() {
		// TODO Auto-generated method stub
		if(pilot.isMoving())pilot.stop();
		pilot.travel(-15);
	}
	public synchronized void turnRight() {
		// TODO Auto-generated method stub
		if(pilot.isMoving())pilot.stop();
		pilot.rotate(90);
	}
	public synchronized void turnLeft() {
		// TODO Auto-generated method stub
		if(pilot.isMoving())pilot.stop();
		pilot.rotate(-90);
	}
	public synchronized void speedUp(){
		if(pilot.getMoveSpeed()<pilot.getMoveMaxSpeed())pilot.setMoveSpeed((pilot.getMoveSpeed()*(float)1.1));
	}
	public synchronized void slowDown(){
		if(pilot.getMoveSpeed()>1)pilot.setMoveSpeed((pilot.getMoveSpeed()/(float)1.1));
	}
	public synchronized void stop(){
		pilot.stop();
	}
	public synchronized void setPos(Pose newPos){
		navigator.setPose(newPos);
	}
	public Pose getPosition(){return navigator.getPose();}

	public synchronized void moveContinuous(RobotCommand command) {
		navigator.setMoveSpeed(pilot.getMoveMaxSpeed()*command.getThrottle());
		pilot.forward();
	}
}
