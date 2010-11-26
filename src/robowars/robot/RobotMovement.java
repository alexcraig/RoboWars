package robowars.robot;


import robowars.shared.model.RobotCommand;
import lejos.nxt.Motor;
import lejos.robotics.*;
import lejos.robotics.navigation.*;

public class RobotMovement {
	private TachoPilot pilot;
	private SimpleNavigator navigator;
	private final static int MINDSTORM_WIDTH=15;
	private final static int WHEEL_HEIGHT=3;
	private final static int SPEED=100;
	public RobotMovement(){
		pilot=new TachoPilot(WHEEL_HEIGHT,MINDSTORM_WIDTH, Motor.C, Motor.A, true);
		pilot.setSpeed(SPEED);
		navigator=new SimpleNavigator(pilot);
	}
	
	public void moveForward() {
		// TODO Auto-generated method stub
		if(pilot.isMoving())pilot.stop();
		pilot.travel(15);
	}
	public void moveBackwards() {
		// TODO Auto-generated method stub
		if(pilot.isMoving())pilot.stop();
		pilot.travel(-15);
	}
	public void turnRight() {
		// TODO Auto-generated method stub
		if(pilot.isMoving())pilot.stop();
		pilot.rotate(90);
	}
	public void turnLeft() {
		// TODO Auto-generated method stub
		if(pilot.isMoving())pilot.stop();
		pilot.rotate(-90);
	}
	public void speedUp(){
		if(pilot.getMoveSpeed()<pilot.getMoveMaxSpeed())pilot.setMoveSpeed((pilot.getMoveSpeed()*(float)1.1));
	}
	public void slowDown(){
		if(pilot.getMoveSpeed()>1)pilot.setMoveSpeed((pilot.getMoveSpeed()/(float)1.1));
	}
	public void stop(){
		pilot.stop();
	}
	public void setPos(Pose newPos){
		navigator.setPose(newPos);
	}
	public Pose getPosition(){return navigator.getPose();}

	public void moveContinuous(RobotCommand command) {
		navigator.setMoveSpeed(pilot.getMoveMaxSpeed()*command.getThrottle());
		while(true){
			moveForward();
		}
	}
}
