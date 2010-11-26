package robowars.robot;

public class robotMain {

	
	public static void main (String args[]){
		RobotCommandController controller=new RobotCommandController(new RobotMovement());
		controller.start();
	}
}
