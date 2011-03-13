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
	private RoboWarsTachoPilot pilot;
	private RoboWarsNavigator navigator;
	private final static float MINDSTORM_WIDTH=11;
	private final static float WHEEL_HEIGHT=(float) 4.3;
	private final static float MAX_SPEED=75;
	private static final float STEER_MAX = (float)200;
	
	public RobotMovement(){
		pilot=new RoboWarsTachoPilot(WHEEL_HEIGHT,MINDSTORM_WIDTH, Motor.C, Motor.A, true);
		pilot.setSpeed((int) MAX_SPEED);
		navigator=new RoboWarsNavigator(pilot);
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
		float turnBearing=command.getTurnBearing();
		float throttle=command.getThrottle();
		if(turnBearing>STEER_MAX||turnBearing<-STEER_MAX)turnBearing=turnBearing/(Math.abs(turnBearing)/STEER_MAX);
		if(throttle>STEER_MAX||throttle<-STEER_MAX)throttle=MAX_SPEED;
		navigator.setMoveSpeed(throttle);
		navigator.setTurnSpeed((Math.abs(command.getTurnBearing())/STEER_MAX)*MAX_SPEED);
		navigator.steer(turnBearing, throttle);
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
