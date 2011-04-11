package robowars.robot;
import lejos.nxt.*;
public class robotMain {

	/** 
	 * Main Class for the robot.
	 * It starts the RobotCommandController, and then acts as the robot's keyboard listener.
	 * @param args
	 */
	public static void main (String args[]){
		RobotCommandController controller=new RobotCommandController(new RobotMovement());
		controller.start();
		/**
		 * listens for the shutdown call.
		 */
		while (true) {
			if (Button.ESCAPE.isPressed()) LCD.drawString("ESCAPE", 0, 0); System.exit(0);
		}
	}
}
