package robowars.robot;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import robowars.shared.model.CommandType;
import robowars.shared.model.RobotCommand;


public class Listener implements KeyListener{
	private LejosOutputStream dataOut;
	public Listener(OutputStream dataOut){
		this.dataOut=new LejosOutputStream(dataOut);
	}
	@Override
	public void keyPressed(KeyEvent arg0) {
		if(arg0.getKeyCode()==KeyEvent.VK_UP){
			try {
				System.out.println("Up");
				dataOut.writeObject(RobotCommand.moveContinuous(100));
				dataOut.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if(arg0.getKeyCode()==KeyEvent.VK_LEFT){
			try {
				System.out.println("Left");
				dataOut.writeObject(RobotCommand.turnAngleLeft(90));
				dataOut.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if(arg0.getKeyCode()==KeyEvent.VK_RIGHT){
			try {
				System.out.println("Right");
				dataOut.writeObject(RobotCommand.turnAngleRight(90));
				dataOut.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if(arg0.getKeyCode()==KeyEvent.VK_S){
			try {
				System.out.println("Stop");
				dataOut.writeObject(RobotCommand.stop());
				dataOut.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if(arg0.getKeyCode()==KeyEvent.VK_X){
			try {
				System.out.println("Exit");
				dataOut.writeObject(RobotCommand.exit());
				dataOut.flush();
				dataOut.close();
				System.exit(0);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyTyped(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
	}

}
