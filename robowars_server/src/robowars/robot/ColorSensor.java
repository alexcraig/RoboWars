package robowars.robot;

import java.io.IOException;

import robowars.shared.model.MapPoint;
import robowars.shared.model.RobotMap;

import lejos.nxt.ColorLightSensor;
import lejos.nxt.SensorPort;
import lejos.robotics.Pose;
import java.io.IOException;
import lejos.nxt.ColorLightSensor;
import lejos.nxt.LCD;
import lejos.nxt.SensorPort;
import lejos.robotics.Pose;
import lejos.robotics.Colors.Color;

public class ColorSensor extends Thread{
	private ColorLightSensor sensor;
	private RobotMap map;
	private LejosOutputStream output;
	private RobotMovement move;
	private static final float ROBOT_RADIUS=10;
	private boolean test;
	public ColorSensor(LejosOutputStream output, RobotMovement move){
		this(output, move, false);
	}
	public ColorSensor(LejosOutputStream output, RobotMovement move, boolean test){
		sensor=new ColorLightSensor(SensorPort.S1, ColorLightSensor.TYPE_COLORFULL);
		sensor.setFloodlight(true);
		sensor.setType(ColorLightSensor.TYPE_COLORFULL);
		this.map=generate();
		this.output=output;
		this.move=move;
		this.test=test;
	}
	public void run(){
		if(!test){
			while(true){
				System.out.println("Reading started");
	    		int [] vals=new int[4];
	            sensor.readValues(vals);
	            int color=sensor.readValue();
	            if(color>lejos.robotics.Colors.WHITE&&color<lejos.robotics.Colors.BLACK){
	            	Pose p=move.getPosition();
	            	float heading=p.getHeading();
	            	float dy=0;
	            	float dx=0;
	            	//a
	            	if(heading>-1&&heading<90){
	            		dy=(float) (ROBOT_RADIUS*Math.sin(heading));
	            		dx=(float) (ROBOT_RADIUS*Math.cos(heading));
	            	}
	            	//s
	            	else if(heading>89&&heading<180){
	            		heading=heading-90;
	            		dx=-(float) (ROBOT_RADIUS*Math.sin(heading));
	            		dy= (float) (ROBOT_RADIUS*Math.cos(heading));
	            	}
	            	//t
	            	else if(heading>179&&heading<270){
	            		heading=heading-180;
	            		dy=-(float) (ROBOT_RADIUS*Math.sin(heading));
	            		dx=-(float) (ROBOT_RADIUS*Math.cos(heading));
	            	}
	            	//c
	            	else if(heading>269&&heading<361){
	            		heading-=270;
	            		dx= (float) (ROBOT_RADIUS*Math.sin(heading));
	            		dy=-(float) (ROBOT_RADIUS*Math.cos(heading));
	            	}
	            	MapPoint point=map.getPoint(p.getX()+dx, p.getY()+dy, p.getHeading(), color);
	            	move.setPos(new Pose(point.getX()-dx, point.getY()-dy,p.getHeading()));
	            	try {
						output.writeObject(move.getPosition());
					} catch (IOException e) {
					}
	            }
	            
			}		
		}
		/*TEST MODE*/
		else{
	        while (true)
	        {
	    		int [] vals=new int[4];
	    		int [] rawVals=new int[4];
	            sensor.readValues(vals);
	            sensor.readRawValues(rawVals);
	            LCD.clearDisplay();
				LCD.drawString(vals[0]+" "+vals[1]+" "+vals[2],0,4);
				try {
					output.writeObject(new Pose(vals[0],vals[1],vals[2]));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					System.out.println("Write fail");
					try {
						sleep(5000);
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
					}
				}
			}
		}
	}
	private RobotMap generate(){
		int ROWS=21;
		int COL=21;
		int SPACE=2;
		RobotMap map=new RobotMap();
		for(int r=ROWS; r>1; r--){
			for(int c=COL; c>1; c--){
				if(r%2==0&&c%2==0)map.addPoint(new MapPoint(c*SPACE, r*SPACE, lejos.robotics.Colors.BLUE));
				else if(r%2==1&&c%2==0)map.addPoint(new MapPoint(c*SPACE, r*SPACE, lejos.robotics.Colors.RED));
				else if(r%2==0&&c%2==1)map.addPoint(new MapPoint(c*SPACE, r*SPACE, lejos.robotics.Colors.YELLOW));
				else if(r%2==1&&c%2==1)map.addPoint(new MapPoint(c*SPACE, r*SPACE, lejos.robotics.Colors.GREEN));
			}
		}
		return map;
	}
}

