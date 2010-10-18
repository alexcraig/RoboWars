package robowars.server.controller;

/**
 * Entry point for the RoboWars server. This class is responsible for launching
 * all other main components of the system (TcpServer, BluetoothServer, 
 * AdminView and SystemLobby)
 */
public class SystemControl {
	
	public static void main (String args[]){
		
		// Start a new TCP server listening on port 33330
		new Thread(new TcpServer(33330)).start();
	}
	
}
