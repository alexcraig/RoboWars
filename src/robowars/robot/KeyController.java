package robowars.robot;

import java.io.*;

import javax.swing.JFrame;

import lejos.pc.comm.NXTComm;
import lejos.pc.comm.NXTCommException;
import lejos.pc.comm.NXTCommFactory;
public class KeyController {
	static OutputStream dataOut;
	public static void main (String args[]){
		NXTComm nxtComm = null;
		try {
			nxtComm = NXTCommFactory.createNXTComm(NXTCommFactory.BLUETOOTH);
			nxtComm.open(nxtComm.search(null, NXTCommFactory.BLUETOOTH)[0]);
		} catch (NXTCommException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		dataOut=nxtComm.getOutputStream();
		JFrame frame=new JFrame();
		Listener listen=new Listener(dataOut);
		frame.addKeyListener(listen);
		frame.setVisible(true);
		
	}


}
