package robowars.robot;

/** 
 * RoboMovement.java
 * Thread safe controller for sending enacting commands on the navigator
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
	private final static float MINDSTORM_WIDTH=(float)15.62;
	//actual width is 4.19cm adjusted for gear ratio
	//private final static float WHEEL_HEIGHT=(float) 14.20;
	private final static float WHEEL_HEIGHT=(float)4.19;
	private final static float MAX_SPEED=200;
	private static final float STEER_MAX = (float)200;
	
	public RobotMovement(){
		pilot=new RoboWarsTachoPilot(WHEEL_HEIGHT,MINDSTORM_WIDTH, Motor.C, Motor.A, true);
		pilot.setSpeed((int) MAX_SPEED);
		navigator=new RoboWarsNavigator(pilot);
	}
	/**moves the robot in a straight line forward*/
	public synchronized void moveForward() {
		if(pilot.isMoving())navigator.stop();
		navigator.forward();
	}
	/**updates the position of the robot*/
	public synchronized void updatePosition(){
		navigator.updatePosition();
	}
	/**moves the robot in a straight line backwards*/
	public synchronized void moveBackwards() {
		if(pilot.isMoving())navigator.stop();
		navigator.backward();
	}
	/**rotates the robot f degrees*/
	public synchronized void turnRight(float f) {
		if(pilot.isMoving())navigator.stop();
		navigator.rotate(f);
	}
	/**rotates the robot -f degrees*/
	public synchronized void turnLeft(float f) {
		if(pilot.isMoving())navigator.stop();
		navigator.rotate(-f);
	}
	/** enacts a rolling turn */
	public synchronized void rollingTurn(RobotCommand command){
		float turnBearing=command.getTurnBearing();
		float throttle=command.getThrottle();
		if(turnBearing>STEER_MAX||turnBearing<-STEER_MAX)turnBearing=turnBearing/(Math.abs(turnBearing)/STEER_MAX);
		if(throttle>MAX_SPEED||throttle<-MAX_SPEED)throttle=(throttle/Math.abs(throttle))*MAX_SPEED;
		navigator.setMoveSpeed(throttle);
		navigator.setTurnSpeed((Math.abs(command.getTurnBearing())/STEER_MAX)*MAX_SPEED);
		navigator.steer(turnBearing, throttle);
	}
	/**speeds up the rotation speed by a factor of 1.1*/
	public synchronized void speedUp(){
		if(pilot.getMoveSpeed()<pilot.getMoveMaxSpeed())navigator.setMoveSpeed((pilot.getMoveSpeed()*(float)1.1));
	}
	/**slows down the rotation speed by a factor of 1.1*/
	public synchronized void slowDown(){
		if(pilot.getMoveSpeed()>1)navigator.setMoveSpeed((pilot.getMoveSpeed()/(float)1.1));
	}
	/**stops the robot*/
	public synchronized void stop(){
		System.out.println("Stop");
		navigator.stop();
	}
	/** sets the robots position*/
	public synchronized void setPos(Pose newPos){
		navigator.setPose(newPos);
	}
	/** return the robots Pose*/
	public Pose getPosition(){
		return navigator.getPose();
	}
	/** move continusously either forwards or backwards*/
	public synchronized void moveContinuous(RobotCommand command) {
		navigator.updatePosition();
		navigator.steer(0, command.getThrottle());
	}
}
