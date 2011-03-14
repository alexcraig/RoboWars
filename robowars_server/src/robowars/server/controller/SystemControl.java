package robowars.server.controller;

import javax.swing.UIManager;

import org.apache.log4j.PropertyConfigurator;

import robowars.server.view.AdminView;
import robowars.test.TestRobotProxy;

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
	public static final String USER_PROTOCOL_VERSION = "RoboWars V0.2";
	public static final String ROBOT_PROTOCOL_VERSION = "RoboWars V0.1";
	
	public static void main (String args[]){
		// Use log4j config file "log_config.properties"
		PropertyConfigurator.configure("config/log_config.properties");
		
		// Set Swing look and feel
		try {
		    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch (Exception e) {
		   try {
		        UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
		    }
		    catch (Exception e2) {
		        System.err.println("Unable to load default look and feel.");
		        System.exit(1); // Might not want to exit
		    }
		}

		// Generate the server lobby to manage user and robot connections
		ServerLobby lobby = new ServerLobby("RoboWars Test Server", 6, 10);
		
		// Start a new TCP server listening on port 33330
		TcpServer tcpServer = new TcpServer(33330, lobby);
		new Thread(tcpServer).start();
		
		// Start the NXT Bluetooth discovery server
		final BluetoothServer bluetooth = new BluetoothServer(lobby);
		new Thread(new Runnable() {
			@Override
			public void run() {
				bluetooth.initRobotDetection();
			}
		}).start();
		
		// Generate the administrator GUI
		new AdminView(USER_PROTOCOL_VERSION, lobby, tcpServer.getMediaStreamer(),
				bluetooth);
		
		// TESTING
		//new TestRobotProxy(lobby, "TestRobot1");
		// new TestRobotProxy(lobby, "TestRobot2");
	}
	
}
