package robowars.robot;

import robowars.shared.model.RobotMap;
import lejos.nxt.Button;
import lejos.nxt.LCD;
import lejos.nxt.comm.Bluetooth;
import lejos.nxt.comm.NXTConnection;
/*
 * Main Method used to start colour sensor for testing purposes.
 */
public class ColorSensorMain {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		NXTConnection connection = Bluetooth.waitForConnection();
		final ColorSensor cs=new ColorSensor(new LejosOutputStream(connection.openDataOutputStream()), new RobotMovement(), true);
		new Thread(new Runnable(){
			public void run(){
				cs.read();
			}
		}).start();
		while (true) {
			if (Button.ESCAPE.isPressed()) LCD.drawString("ESCAPE", 0, 0); System.exit(0);
		}
	}

}
