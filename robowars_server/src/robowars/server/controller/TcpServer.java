package robowars.server.controller;

import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

import robowars.server.view.AdminView;

/**
 * Listens for incoming TCP connections to a specified port, and generates a new
 * UserProxy for each new connection. This class also ensures that each
 * UserProxy is associated with an instance of ServerLobby and MediaServer.
 */
public class TcpServer implements Runnable {
	/** The logger used by this class */
	private static Logger log = Logger.getLogger(TcpServer.class);
	
	/** The port number that the server should listen for incoming connections on */
	private int listenPort;
	
	/** The server lobby that users connecting to the server should join */
	private ServerLobby lobby;
	
	/** The media streamer that should serve video to users connecting to the server */
	private MediaStreamer mediaStreamer;
	
	/** The socket used to listen for incoming TCP/IP connections */
	private ServerSocket serverSocket;

	/**
	 * Generates a new instance of TcpServer
	 * @param port	The port number to listen for incoming connections on
	 * @param lobby The server lobby that users connecting to the server should join
	 */
	public TcpServer(int port, ServerLobby lobby) {
		listenPort = port;
		
		try {
			serverSocket = new ServerSocket(listenPort); // Setup listening port
		} catch (BindException e) {
			log.error("ServerSocket already open on port: " + listenPort + ", terminating.");
			JOptionPane.showMessageDialog(null, 
					"Specified listening port already in use.\nPlease ensure that an instance " +
					"of RoboWars is not already running.",
					"Initialization Error - Terminating Application", JOptionPane.ERROR_MESSAGE);
			System.exit(1);
		} catch (IOException e) {
			log.error("Unrecognized exception opening server socket, terminating.");
			e.printStackTrace();
			System.exit(1);
		}
		
		log.info("Succesfully opened listen socket on port " + listenPort);
		
		this.lobby = lobby;
		mediaStreamer = new MediaStreamer();
	}

	/**
	 * Listens for incoming TCP/IP connections on the server socket, and generates
	 * a new UserProxy for each incoming connection.
	 */
	public void run() {
		while (true) { // Run forever, accepting and servicing connections
			
			Socket clientSocket = null;
			try {
				clientSocket = serverSocket.accept(); // Get client connection
			} catch (IOException e) {
				e.printStackTrace();
			}

			// Print details on connected client
			log.info("New client connection: " + clientSocket.getInetAddress().getHostAddress()
					+ " Port: " + clientSocket.getPort());

			UserProxy newProxy = new UserProxy(clientSocket, lobby, mediaStreamer);
			new Thread(newProxy).start();
		}
	}
	
	/**
	 * @return	The media streamer managing camera selection and video streaming.
	 */
	public MediaStreamer getMediaStreamer() {
		return mediaStreamer;
	}
}