package robowars.robot;
import lejos.nxt.*;
public class robotMain {

	
	public static void main (String args[]){
		RobotCommandController controller=new RobotCommandController(new RobotMovement());
		controller.start();
		while (true) {
			if (Button.ESCAPE.isPressed()) LCD.drawString("ESCAPE", 0, 0); System.exit(0);
		}
	}
}
