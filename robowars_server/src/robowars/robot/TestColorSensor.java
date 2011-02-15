package robowars.robot;


import java.io.IOException;
import javax.swing.JFrame;


import lejos.pc.comm.NXTComm;
import lejos.pc.comm.NXTCommException;
import lejos.pc.comm.NXTCommFactory;
import lejos.robotics.Pose;

import org.apache.log4j.Logger;

import robowars.server.controller.RobotProxy;

public class TestColorSensor {
	private static LejosInputStream dataIn;
	private static Logger log = Logger.getLogger(RobotProxy.class);
	private static SquareComponent component;
	public static void main (String args[]){
	    NXTComm nxtComm=null;
		try {
			//nxtComm = NXTCommFactory.createNXTComm(NXTCommFactory.USB);
			//nxtComm.open(nxtComm.search("NXT", NXTCommFactory.USB)[0]);
			nxtComm = NXTCommFactory.createNXTComm(NXTCommFactory.BLUETOOTH);
			nxtComm.open(nxtComm.search("NXT2", NXTCommFactory.BLUETOOTH)[0]);
		} catch (NXTCommException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		component=new SquareComponent();
		dataIn=new LejosInputStream(nxtComm.getInputStream());
		JFrame frame=new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    frame.getContentPane().add(component);
	    frame.setSize(400,400);
	    frame.setVisible(true);  
	    Object input=new Object();
	    try {
			while((input=dataIn.readObject())!=null){
				Pose p=(Pose)input;
				component.updateColor((int)p.getX(),(int)p.getY(),(int)p.getHeading());
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


}

