package robowars.server.controller;

import lejos.pc.comm.NXTComm;
import lejos.pc.comm.NXTCommException;
import lejos.pc.comm.NXTCommFactory;
import lejos.pc.comm.NXTInfo;

import org.apache.log4j.Logger;

/**
 * Uses Bluetooth to discover and attempt to open a connection to any available 
 * NXT robots. A new RobotProxy is generated for every successfully initiated
 * connection.
 * 
 * @author Alexander Craig
 */
public class BluetoothServer {
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
	
	/**
	 * Initiates a single search for new robots to connect, and generates a new
	 * RobotProxy for each robot which is detected (that is not already registered
	 * with the ServerLobby).
	 */
	public void initRobotDetection() {
		new Thread(new Runnable() {
			@Override
			public void run() {
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
				} catch (NXTCommException e) {
					log.error("Bluetooth device could not be found. Robot registration will not be supported.");
					return;
				}
			}
		}).start();
	}
}
