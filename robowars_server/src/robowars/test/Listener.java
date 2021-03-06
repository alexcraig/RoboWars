package robowars.test;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import robowars.robot.LejosOutputStream;
import robowars.server.controller.RobotProxy;
import robowars.shared.model.CommandType;
import robowars.shared.model.RobotCommand;
import java.math.*;
import java.util.Date;

import org.apache.log4j.Logger;
/**
 * Used to act as the keyListener for the test suite
 * @author mwright
 *
 */
public class Listener implements KeyListener{
	private LejosOutputStream dataOut;
	private static Logger log = Logger.getLogger(RobotProxy.class);
	public Listener(OutputStream dataOut){
		this.dataOut=new LejosOutputStream(dataOut);
	}
	
	@Override
	public void keyPressed(KeyEvent arg0) {
		if(arg0.getKeyCode()==KeyEvent.VK_UP){
			try {
				System.out.println("Up");
				dataOut.writeObject(RobotCommand.rollingTurn(100,0));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if(arg0.getKeyCode()==KeyEvent.VK_LEFT){
			try {
				System.out.println("Left");
				dataOut.writeObject(RobotCommand.turnAngleLeft(90));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if(arg0.getKeyCode()==KeyEvent.VK_RIGHT){
			try {
				System.out.println("Right");
				dataOut.writeObject(RobotCommand.turnAngleRight(90));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if(arg0.getKeyCode()==KeyEvent.VK_S){
			try {
				System.out.println("Stop");
				dataOut.writeObject(RobotCommand.stop());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if(arg0.getKeyCode()==KeyEvent.VK_X){
			try {
				System.out.println("Exit");
				dataOut.writeObject(RobotCommand.exit());
				dataOut.close();
				System.exit(0);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if(arg0.getKeyCode()==KeyEvent.VK_D){
			try{
					System.out.println("Rotate Right");
					dataOut.writeObject(RobotCommand.rollingTurn(100, -100));
					Thread.sleep(1000);
					dataOut.writeObject(RobotCommand.stop());
			}
			catch(Exception e){
				
			}
		}
		if(arg0.getKeyCode()==KeyEvent.VK_A){
			try{
				System.out.println("Rotate left");
				dataOut.writeObject(RobotCommand.rollingTurn(100, -100));
				Thread.sleep(1000);
				dataOut.writeObject(RobotCommand.stop());
			}
			catch(Exception e){
				
			}
		}
		if(arg0.getKeyCode()==KeyEvent.VK_W){
			try{
				System.out.println("Rotate forward");
				dataOut.writeObject(RobotCommand.rollingTurn(100, 50));
				Thread.sleep(1000);
				dataOut.writeObject(RobotCommand.rollingTurn(-100, 50));
				Thread.sleep(1000);
				dataOut.writeObject(RobotCommand.stop());
			}
			catch(Exception e){
				
			}
		}    
		if(arg0.getKeyCode()==KeyEvent.VK_Z){
			try{
				System.out.println("Rotate back");
				dataOut.writeObject(RobotCommand.rollingTurn(100, 50));
				Thread.sleep(1000);
				dataOut.writeObject(RobotCommand.rollingTurn(-100, 50));
				Thread.sleep(1000);
				dataOut.writeObject(RobotCommand.stop());
			}
			catch(Exception e){
				
			}
		} 
		if(arg0.getKeyCode()==KeyEvent.VK_DOWN){
			try {
				/*Date time = new Date();
				long millis = time.getTime();
				for(int i = 0 ; i < 100; i++){
					int neg;
					if(Math.random()>.5)neg=1;
					else neg=-1;
					int neg2;
					if(Math.random()>.5)neg2=1;
					else neg2=-1;
					RobotCommand rc=RobotCommand.rollingTurn((float)(Math.random()*100*neg2),(int) ((float)Math.random()*200*neg));
					dataOut.writeObject(rc);
					log.info("Wrote to robot: " +i+" "+rc);
					System.out.println("Wrote to robot: " +i+" "+rc);
					if(Math.random()>.9)dataOut.writeObject(RobotCommand.stop());
					try {
						Thread.sleep((long) (Math.random()*400));
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					//dataOut.writeObject(RobotCommand.stop());
					//System.out.println("Stop " + i);
				}
				long diff = time.getTime() - millis;*/
				System.out.println("Down");
				dataOut.writeObject(RobotCommand.rollingTurn(-100,0));
				
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
