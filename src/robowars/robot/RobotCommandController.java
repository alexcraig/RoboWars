package robowars.robot;

import java.io.*;

import robowars.shared.model.RobotCommand;
import robowars.shared.model.CommandType;
import lejos.nxt.comm.*;

public class RobotCommandController extends Thread{
	private RobotMovement move;
	private ObjectOutputStream dataOut;
	private ObjectInputStream dataIn;
	
	public RobotCommandController(){
		 NXTConnection connection = USB.waitForConnection();
		 try {
			dataOut=new ObjectOutputStream(connection.openDataOutputStream());
			dataIn=new ObjectInputStream(connection.openDataInputStream());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 move=new RobotMovement();
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
				dataOut.writeObject(move.getPosition());
			}
		} catch (IOException e) {
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
