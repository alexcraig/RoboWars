package robowars.server.controller;

import java.util.ArrayList;

import org.apache.log4j.Logger;

import lejos.pc.comm.NXTComm;
import lejos.pc.comm.NXTCommException;
import lejos.pc.comm.NXTCommFactory;
import lejos.pc.comm.NXTInfo;

/**
 * Uses Bluetooth to discover and attempt a connection to any available NXT
 * robots. A new RobotProxy is generated for every successfully initiated
 * connection.
 */
public class BluetoothServer implements Runnable {
	/** The logger used by this class */
	private static Logger log = Logger.getLogger(BluetoothServer.class);
	
	/** The server lobby that robots discovered by this Bluetooth server should join */
	private ServerLobby lobby;

	/**
	 * Generates a new instance of BluetoothServer
	 * @param lobby The server lobby that robots discovered by this Bluetooth server should join
	 */
	public BluetoothServer(ServerLobby lobby) {
		this.lobby = lobby;
	}

	@Override
	public void run() {
		// TODO: Properly ensure duplicate connections don't occur (check ServerLobby
		// to see if robot already exists?)
		
<<<<<<< HEAD
		ArrayList<String> triedInfos = new ArrayList<String>();
=======
		ArrayList<String> triedInfoNames = new ArrayList<String>();
>>>>>>> 9a5d31a390e7d072a22f5916cd27c8da2d306806
		
		// Generate NXTComm object to manage Bluetooth connections
		while(true) {
			
			try {
				NXTComm nxtComm = NXTCommFactory.createNXTComm(NXTCommFactory.BLUETOOTH);
			
				NXTInfo[] allNxts = nxtComm.search("NXT",NXTCommFactory.BLUETOOTH);
				for(NXTInfo nxt : allNxts) {
<<<<<<< HEAD
					if(!triedInfos.contains(nxt.name)) {
						log.info("Discovered NXT: " + nxt.name);
						new RobotProxy(lobby, nxt);
						triedInfos.add(nxt.name);
=======
					if(!triedInfoNames.contains(nxt.name)) {
						log.info("Discovered NXT: " + nxt.name);
						new RobotProxy(lobby, nxt);
						triedInfoNames.add(nxt.name);
>>>>>>> 9a5d31a390e7d072a22f5916cd27c8da2d306806
					}
				}
				
			} catch (NXTCommException e) {
				log.error("Bluetooth device could not be found. Robot registration will not be supported.");
<<<<<<< HEAD
=======
				return;
>>>>>>> 9a5d31a390e7d072a22f5916cd27c8da2d306806
			}
			
		}

		
	}
}
