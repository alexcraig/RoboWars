package robowars.robot;

import java.io.IOException;

import robowars.shared.model.RobotMap;

import lejos.nxt.ColorLightSensor;
import lejos.nxt.SensorPort;
public class ColorSensor extends Thread{
	private ColorLightSensor sensor;
	private RobotMap map;
	private LejosOutputStream output;
	private RobotMovement move;
	public ColorSensor(LejosOutputStream output, RobotMap map, RobotMovement move){
		sensor=new ColorLightSensor(SensorPort.S1, ColorLightSensor.TYPE_COLORFULL);
		this.map=map;
		this.output=output;
		this.move=move;
	}
	public void run(){
		while(true){
			int[] reading=sensor.getColor();
			//map logic
			//if logic==true
			try {
				output.writeObject(move.getPosition());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				sleep(20);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}

