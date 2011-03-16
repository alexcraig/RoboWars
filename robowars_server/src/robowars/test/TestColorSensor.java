package robowars.test;


import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JPanel;


import lejos.pc.comm.NXTComm;
import lejos.pc.comm.NXTCommException;
import lejos.pc.comm.NXTCommFactory;
import lejos.robotics.Pose;

import org.apache.log4j.Logger;

import robowars.robot.LejosInputStream;
import robowars.robot.LejosOutputStream;
import robowars.robot.Listener;
import robowars.robot.SquareComponent;
import robowars.server.controller.RobotProxy;

public class TestColorSensor {
	private static LejosInputStream  dataIn;
	private static OutputStream  dataOut;
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
		dataOut=nxtComm.getOutputStream();
		JFrame frame=new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    frame.getContentPane().add(component);
	    frame.setSize(200,120);
	    frame.setVisible(true);
	    Object input=new Object();
	    try {
			while((input=dataIn.readObject())!=null){
				if(input instanceof Pose){
					System.out.println("Pose Received");
				}
				else if(input instanceof Vector){
					Vector v=(Vector)input;
					System.out.println("Vector Received "+(Integer)v.get(0)+" "+(Integer)v.get(1)+" "+(Integer)v.get(2));
					component.updateColor((Integer)v.get(0),(Integer)v.get(1),(Integer)v.get(2));
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}

