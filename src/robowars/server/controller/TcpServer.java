package robowars.server.controller;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Listens for incoming TCP connections to a specified port, and generates a new
 * UserProxy for each new connection. This class also ensures that each
 * UserProxy is associated with an instance of ServerLobby and MediaServer.
 */
public class TcpServer implements Runnable {

	/** The port number that the server should listen for incoming connections on */
	private int listenPort;

	/**
	 * Generates a new instance of TcpServer
	 * @param port	The port number to listen for incoming connections on
	 */
	public TcpServer(int port) {
		listenPort = port;
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
		System.out.println("TcpServer: Succesfully opened listen socket on port " + listenPort);

		while (true) { // Run forever, accepting and servicing connections
			
			Socket clientSocket = null;
			try {
				clientSocket = socket.accept(); // Get client connection
			} catch (IOException e) {
				e.printStackTrace();
			}

			// Print details on connected client
			System.out.println("Client: " + clientSocket.getInetAddress().getHostAddress()
					+ " Port: " + clientSocket.getPort());

			UserProxy newProxy = new UserProxy(clientSocket);
			new Thread(newProxy).start();
		}
	}
}