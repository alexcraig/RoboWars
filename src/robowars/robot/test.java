package robowars.robot;


import java.io.OutputStream;

import lejos.nxt.comm.NXTConnection;
import lejos.nxt.comm.USB;
import lejos.pc.comm.NXTComm;
import lejos.pc.comm.NXTCommException;
import lejos.pc.comm.NXTCommFactory;

public class test {
	public static void main (String args[]){
			NXTComm nxtComm = null;
			try {
				nxtComm = NXTCommFactory.createNXTComm(NXTCommFactory.USB);
				nxtComm.open(nxtComm.search(null, NXTCommFactory.USB)[0]);
			} catch (NXTCommException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			OutputStream dataOut=nxtComm.getOutputStream();	
	}
}
