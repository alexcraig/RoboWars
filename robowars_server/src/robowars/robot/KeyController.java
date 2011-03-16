package robowars.robot;

import java.io.*;

import javax.swing.JFrame;

import org.apache.log4j.Logger;

import robowars.server.controller.RobotProxy;

import lejos.pc.comm.NXTComm;
import lejos.pc.comm.NXTCommException;
import lejos.pc.comm.NXTCommFactory;
import lejos.robotics.Pose;
public class KeyController {
	static OutputStream dataOut;
	static LejosInputStream dataIn;
	private static Logger log = Logger.getLogger(RobotProxy.class);
	public static void main (String args[]){
	    NXTComm nxtComm=null;
		try {
			//nxtComm = NXTCommFactory.createNXTComm(NXTCommFactory.USB);
			//nxtComm.open(nxtComm.search("NXT", NXTCommFactory.USB)[0]);
			nxtComm = NXTCommFactory.createNXTComm(NXTCommFactory.BLUETOOTH);
			nxtComm.open(nxtComm.search("NXT", NXTCommFactory.BLUETOOTH)[0]);
		} catch (NXTCommException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		dataOut=nxtComm.getOutputStream();
		dataIn=new LejosInputStream(nxtComm.getInputStream());
		JFrame frame=new JFrame();
		Listener listen=new Listener(dataOut);
		frame.addKeyListener(listen);
		frame.setVisible(true);
		
		new Thread(new Runnable(){

			@Override
			public void run() {
				while(true){
					try {
						Pose p=(Pose)dataIn.readObject();
						System.out.println(p.getX()+" "+p.getY()+" "+p.getHeading());
						log.info(p.getX()+" "+p.getY()+" "+p.getHeading());
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
			}
			
		}).start();
	}
}
