package robowars.robot;



import java.io.*;

import robowars.shared.model.CommandType;
import robowars.shared.model.RobotCommand;
import lejos.nxt.comm.*;

public class RobotCommandController extends Thread{
	private RobotMovement move;
	private LejosOutputStream dataOut;
	private LejosInputStream dataIn;
	
	public RobotCommandController(RobotMovement movement){
		 NXTConnection connection = USB.waitForConnection();
		 dataOut=new LejosOutputStream(connection.openDataOutputStream());
		 dataIn=new LejosInputStream(connection.openDataInputStream());
		 this.move=movement;
		 new Thread(new Runnable(){
			@Override
			public void run() {
				while(true){
					if(move!=null){
						try {
							dataOut.writeObject(move.getPosition());
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
		 }).start();
	}
	
	public void run(){
		Object input;
		try {
			while((input=dataIn.readObject()) != null){
				RobotCommand command= (RobotCommand)input;
				if(command.getType()==CommandType.SET_POSITION){
					move.setPos(command.getPos());
				}
				if(command.getType()==CommandType.TURN_RIGHT_ANGLE_LEFT){
					move.turnLeft();
				}
				if(command.getType()==CommandType.TURN_RIGHT_ANGLE_RIGHT){
					move.turnRight();
				}
				if(command.getType()==CommandType.MOVE_CONTINUOUS){
					move.moveContinuous(command);
				}
				if(command.getType()==CommandType.STOP){
					move.stop();
				}
				if(command.getType()==CommandType.EXIT){
					System.exit(0);
				}
				dataOut.writeObject(move.getPosition());
			}
		} catch (IOException e) {
		} 
	}
}
