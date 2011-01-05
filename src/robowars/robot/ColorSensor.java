package robowars.robot;

import java.io.IOException;

import robowars.shared.model.MapPoint;
import robowars.shared.model.RobotMap;

import lejos.nxt.ColorLightSensor;
import lejos.nxt.SensorPort;
import lejos.robotics.Pose;
public class ColorSensor extends Thread{
	private ColorLightSensor sensor;
	private RobotMap map;
	private LejosOutputStream output;
	private RobotMovement move;
	public ColorSensor(LejosOutputStream output, RobotMap map, RobotMovement move){
		sensor=new ColorLightSensor(SensorPort.S1, ColorLightSensor.TYPE_COLORFULL);
		sensor.setFloodlight(true);
		this.map=map;
		this.output=output;
		this.move=move;
	}
	public void run(){
		while(true){
			int[] reading=sensor.getColor();
			if(reading[0]==255&&reading[1]==255&&reading[2]==255){
				MapPoint p=map.getPoint((int)move.getPosition().getX(),(int)move.getPosition().getY());
				move.setPos(new Pose(p.getX(),p.getY(),move.getPosition().getHeading()));
				try {
					output.writeObject(move.getPosition());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
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

