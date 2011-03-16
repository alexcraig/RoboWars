package robowars.robot;

import lejos.nxt.Button;
import lejos.nxt.LCD;

public class RobotTestMain {
	public static void main (String args[]){
		RobotCommandController controller=new RobotCommandController(new RobotMovement(), true);
		controller.start();
		while (true) {
			if (Button.ESCAPE.isPressed()) LCD.drawString("ESCAPE", 0, 0); System.exit(0);
		}
	}
}
