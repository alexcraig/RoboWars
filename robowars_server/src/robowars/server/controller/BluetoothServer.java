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
	/**
	 * Continually search for unregistered robots, and generate a RobotProxy
	 * whenever a previously unseen robot is found.
	 */
	public void run() {		

		while(true) {			
			try {
				NXTComm nxtComm = NXTCommFactory.createNXTComm(NXTCommFactory.BLUETOOTH);
				String[] nxtName={"NXT", "NXT2"};
				for(int i=0;i<nxtName.length; i++){
					NXTInfo[] allNxts = nxtComm.search(nxtName[i], NXTCommFactory.BLUETOOTH);
					
					for(NXTInfo nxt : allNxts) {
						if(lobby.getRobotProxy(nxt.name) == null) {
							log.info("Discovered NXT: " + nxt.name);
							new RobotProxy(lobby, nxt);
						}
					}
				}
				// Arbitrary delay between Bluetooth searches to prevent a robot
				// from being detected twice before it is registered to the lobby
				
				// TODO: Allow Bluetooth searches to be triggered from the admin
				// 		 panel
				Thread.sleep(10000);
				
			} catch (NXTCommException e) {
				log.error("Bluetooth device could not be found. Robot registration will not be supported.");
				return;
			} catch (InterruptedException e) {
				log.error("Bluetooth server thread interrupted.");
				e.printStackTrace();
				return;
			}
			
		}
	}
}
