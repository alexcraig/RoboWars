package robowars.robot;
import lejos.robotics.Colors.Color;

import lejos.nxt.ColorLightSensor;
import lejos.nxt.SensorPort;
public class ColorSensor {
	static ColorLightSensor sensor;
	public static void main (String args[]){
		sensor=new ColorLightSensor(SensorPort.S1, ColorLightSensor.TYPE_COLORFULL);
		for(int i=0; i<100; i++){
			sensor.setFloodlight(true);
			if(i%2==0)sensor.setFloodlight(Color.RED);
			if(i%3==0)sensor.setFloodlight(Color.BLUE);
			if(i%5==0)sensor.setFloodlight(Color.WHITE);
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			sensor.setFloodlight(false);
		}
		sensor.setFloodlight(false);
	}
}
