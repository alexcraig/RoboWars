package robowars.test;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.log4j.Logger;

import robowars.robot.LejosInputStream;
import robowars.robot.Listener;
import robowars.robot.SquareComponent;
import robowars.server.controller.RobotProxy;
import lejos.pc.comm.NXTComm;
import lejos.pc.comm.NXTCommException;
import lejos.pc.comm.NXTCommFactory;
import lejos.robotics.Pose;

public class TestRobotSuite {
	static OutputStream dataOut;
	static LejosInputStream dataIn;
	private static Logger log = Logger.getLogger(RobotProxy.class);
	static SquareComponent component;
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
		JTextField field=new JTextField(100);
		JFrame frame=new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		//JPanel panel=new JPanel();
		//panel.setSize(200,100);
		//panel.add(component);
	    //frame.getContentPane().add(panel);
	    frame.setSize(200,150);
	    frame.getContentPane().add(field);
	    frame.setVisible(true);
	    Listener listen=new Listener(dataOut);
	    frame.addKeyListener(listen);
	    Object input=new Object();
	    try {
			while((input=dataIn.readObject())!=null){
				if(input instanceof Pose){
					Pose p=(Pose)input;
					field.setText("X: "+p.getX()+" Y: "+p.getY()+" H: "+p.getHeading());
				}
				else if(input instanceof Vector){
					Vector v=(Vector)input;
					component.updateColor((Integer)v.get(0),(Integer)v.get(1),(Integer)v.get(2));
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}

