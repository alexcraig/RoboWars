package robowars.robot;

import java.io.*;

import lejos.pc.comm.NXTComm;
import lejos.pc.comm.NXTCommException;
import lejos.pc.comm.NXTCommFactory;

public class ConsoleController {
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

		try {
			for(int i=0; i<100; i++){
				dataOut.write(1);
				dataOut.flush();
				Thread.sleep(10000);
				dataOut.write(3);
				dataOut.flush();
				Thread.sleep(1000);
				dataOut.write(2);
				dataOut.flush();
				Thread.sleep(1000);
				dataOut.write(4);
				dataOut.flush();
				Thread.sleep(1000);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			dataOut.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
