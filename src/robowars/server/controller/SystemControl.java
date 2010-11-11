package robowars.server.controller;

import org.apache.log4j.PropertyConfigurator;

import robowars.server.view.AdminView;

/**
 * Entry point for the RoboWars server. This class is responsible for launching
 * all other main components of the system (TcpServer, BluetoothServer, 
 * AdminView and SystemLobby)
 */
public class SystemControl {
	/** 
	 * The application version string (sent to clients when they first connect
	 * to ensure version mismatches between client and server do not occur, and
	 * also used in administrator GUI)
	 */
	public static final String VERSION_STRING = "RoboWars V0.1";
	
	public static void main (String args[]){
		// Use log4j config file "log_config.properties"
		PropertyConfigurator.configure("config/log_config.properties");

		// Generate the server lobby to manage user and robot connections
		ServerLobby lobby = new ServerLobby("RoboWars Test Server", 6, 10);
		
		// Generate the administrator GUI
		new AdminView(VERSION_STRING, lobby);
		
		// Start a new TCP server listening on port 33330
		new Thread(new TcpServer(33330, lobby)).start();
		
		// Start the NXT Bluetooth discovery server
		new Thread(new BluetoothServer(lobby)).start();
	}
	
}
