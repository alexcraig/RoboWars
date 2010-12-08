package robowars.server.controller;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.log4j.Logger;

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

	/**
	 * Generates a new instance of TcpServer
	 * @param port	The port number to listen for incoming connections on
	 * @param lobby The server lobby that users connecting to the server should join
	 */
	public TcpServer(int port, ServerLobby lobby) {
		listenPort = port;
		this.lobby = lobby;
		mediaStreamer = new MediaStreamer();
	}

	/**
	 * Opens a listening socket on the port specified at construction, and generates
	 * a new UserProxy for each incoming connection
	 */
	public void run() {
		
		ServerSocket socket = null;
		
		try {
			socket = new ServerSocket(listenPort); // Setup listening port
		} catch (IOException e) {
			e.printStackTrace();
		}
		log.info("Succesfully opened listen socket on port " + listenPort);

		while (true) { // Run forever, accepting and servicing connections
			
			Socket clientSocket = null;
			try {
				clientSocket = socket.accept(); // Get client connection
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