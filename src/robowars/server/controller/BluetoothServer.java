package robowars.server.controller;

/**
 * Uses Bluetooth to discover and attempt a connection to any available NXT
 * robots. A new RobotProxy is generated for every successfully initiated
 * connection.
 */
public class BluetoothServer implements Runnable {
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
		// Generate NXTComm object to manage Bluetooth connections
		while(true) {
			/*
			try {
			NXTComm nxtComm = NXTCommFactory.createNXTComm(NXTCommFactory.BLUETOOTH);
			
				NXTInfo[] allNxts = nxtComm.search("NXT",NXTCommFactory.BLUETOOTH);
				for(NXTInfo nxt : allNxts) {
					System.out.println("Discovered NXT: " + nxt.name);
				}
			} catch (NXTCommException e) {
				System.out.println("Bluetooth device could not be found. Robot registration will not be supported.");
			}
			*/
		}

		
	}
}
