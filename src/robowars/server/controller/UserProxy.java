package robowars.server.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Manages communications with a single user connected through an existing 
 * TCP socket. 
 */
public class UserProxy implements Runnable {
	private static final String PROTOCOL_VERSION = "RoboWarsV0.1";
	
	/** The username of the connected user */
	private String username;

	/** Reader for client input */
	private BufferedReader inputStream;
	
	/** Writer for client output */
	private PrintWriter outputStream;
	
	/** The socket to generate input/output streams for */
	private Socket userSocket;
	
	/** Flag to determine if the connection handshake has been performed */
	private boolean handshakeComplete;
	
	/** The "ready" status of the user (used to determine if a new game can start */
	private boolean isReady;
	
	/** The server lobby that manages the user */
	private ServerLobby lobby;
	
	/**
	 * Generates a new UserProxy
	 * @param clientSocket	The connected socket to service
	 * @param lobby		The server lobby the user should join once the connection
	 * 					handshake is complete
	 */
	public UserProxy(Socket clientSocket, ServerLobby lobby) {
		this.userSocket=clientSocket;
		this.lobby = lobby;
		inputStream = null;
		outputStream = null;
		handshakeComplete = false;
		username = null;
		isReady = false;
	}
	
	public void run(){
		
		System.out.println("UserProxy: Opening input/output streams.");
		try {
			this.inputStream = new BufferedReader(new InputStreamReader(userSocket.getInputStream()));
			this.outputStream = new PrintWriter(userSocket.getOutputStream(), true);
		} catch (IOException e) {
			System.out.println("UserProxy: ERROR - failed to open input/output streams.");
			e.printStackTrace();
		}

		try {
			
			// Write out the protocol version string
			synchronized(outputStream) {
				outputStream.println(PROTOCOL_VERSION);
			}
			// Handshake and UDP connection should happen here
			username = inputStream.readLine();
			outputStream.println("Connected to: " + lobby.getServerName());
			lobby.addUser(this);
			
			String incomingMessage;
			
			// Read strings from socket until connection is terminated
			while ((incomingMessage = inputStream.readLine()) != null) {
				System.out.println("UserProxy: Received: " + incomingMessage);
				handle(incomingMessage);
			}
			System.out.println("UserProxy: Client terminated connection with server.");
			
		} catch (IOException e) {
			System.out.println("UserProxy: Client terminated connection with server.");
		} finally {
			lobby.removeUser(this);
			try {
				userSocket.close();
			} catch (IOException e) {
				System.out.println("UserProxy: WARNING - could not close client socket.");
			}
		}
		
	}
	
	/**
	 * Sends a string message to the user.
	 * @param message	The message to send
	 */
	public void sendMessage(String message) {
		synchronized(outputStream) {
			outputStream.println(message);
		}
	}
	
	/**
	 * Dispatches user input to the relevant processing functions based on the input received.
	 * 
	 * Commands:
	 * h - hello
	 * m<x,y,z> - movement <x,y,z> angle on each axis
	 * s - shoot
	 * q - quit
	 */
	private void handle(String command){
		// Just broadcast message for now (testing)
		lobby.broadcastMessage(username + ": " + command);
		
		if(command.equals("h")){}
		else if(command.contains("m")){}
		else if(command.equals("s")){}
	}
	
	/**
	 * Checks whether the user has successfully performed the authentication
	 * handshake (and therefore a username has been supplied).
	 * @return true if the user has performed the connection handshake.
	 */
	public boolean isConnected() {
		return (handshakeComplete && username != null);
	}
	
	/**
	 * Sets the ready status of the user.
	 * @param isReady	The ready status of the user (true if a new game can start)
	 */
	public void setReady(boolean isReady) {
		this.isReady = isReady;
	}
	
	/**
	 * @return	The username of the connected user
	 */
	public String getUsername() {
		return username;
	}
}