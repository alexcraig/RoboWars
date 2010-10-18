package robowars.server.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import org.apache.log4j.Logger;

/**
 * Manages communications with a single user connected through an existing 
 * TCP socket. 
 */
public class UserProxy implements Runnable {
	/** The logger used by this class */
	private static Logger log = Logger.getLogger(UserProxy.class);
	
	/** 
	 * The protocol version string (sent to clients when they first connect
	 * to ensure version mismatches between client and server do not occur)
	 */
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
	
	/** Flag indicating if a video stream should be sent to this user */
	private boolean videoEnabled;
	
	/** 
	 * Flag indicating whether a user is a pure spectator. If true, the user should
	 * not be considered for control pairing with robots.
	 */
	private boolean isPureSpectator;
	
	/** The server lobby that manages the user */
	private ServerLobby lobby;
	
	/** The media server that streams video to the user */
	private MediaStreamer mediaStreamer;
	
	/**
	 * Generates a new UserProxy
	 * @param clientSocket	The connected socket to service
	 * @param lobby		The server lobby the user should join once the connection
	 * 					handshake is complete
	 * @param media		The media streamer that should serve a video feed to this user
	 */
	public UserProxy(Socket clientSocket, ServerLobby lobby, MediaStreamer media) {
		this.userSocket = clientSocket;
		this.lobby = lobby;
		this.mediaStreamer = media;
		inputStream = null;
		outputStream = null;
		handshakeComplete = false;
		username = null;
		isReady = false;
		videoEnabled = false;
		isPureSpectator = false;
	}
	
	public void run(){
		
		System.out.println("UserProxy: Opening input/output streams.");
		try {
			this.inputStream = new BufferedReader(new InputStreamReader(userSocket.getInputStream()));
			this.outputStream = new PrintWriter(userSocket.getOutputStream(), true);
		} catch (IOException e) {
			log.error("Failed to open input/output streams.");
			e.printStackTrace();
		}

		try {
			
			// Write out the protocol version string
			synchronized(outputStream) {
				outputStream.println(PROTOCOL_VERSION);
			}
			
			// Handshake and UDP connection should happen here
			username = inputStream.readLine();
			log.debug("Client username: " + username);
			
			synchronized(outputStream) {
				outputStream.println(username + " connected to: " + lobby.getServerName());
			}
			lobby.addUser(this);
			
			// Read strings from socket until connection is terminated
			String incomingMessage;
			while ((incomingMessage = inputStream.readLine()) != null) {
				log.debug("Received: " + incomingMessage);
				handle(incomingMessage);
			}
			log.info(username + " terminated connection with server.");
			
		} catch (IOException e) {
			log.info("Client terminated connection with server.");
		} finally {
			lobby.removeUser(this);
			try {
				userSocket.close();
			} catch (IOException e) {
				log.error("Could not close client socket.");
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
	 * m:<message> - chat message 
	 * t:<x,y,z> - tilt reading <x,y,z> angle on each axis
	 * b:<button_chars> - button input
	 * r:<t or f> - set ready state
	 * s:<t or f> - set pure spectator state
	 * g:<game_type_string> - set game type
	 * l - launch game
	 * q - disconnect
	 */
	private void handle(String command){	
		if(command.startsWith("m:")) {
			lobby.broadcastMessage(username + ": " + command.substring(2));
		} else if(command.startsWith("r:")) {
			
			if (command.substring(2,3).equalsIgnoreCase("t")) {
				setReady(true);
			} else if (command.substring(2,3).equalsIgnoreCase("f")) {
				setReady(false);
			}
			lobby.broadcastMessage(username + ": ready status is " + isReady);
			
		} else if(command.startsWith("s:")) {
			
			System.out.println(command.substring(2,3));
			if (command.substring(2,3).equalsIgnoreCase("t")) {
				isPureSpectator = true;
			} else if (command.substring(2,3).equalsIgnoreCase("f")) {
				isPureSpectator = false;
			}
			lobby.broadcastMessage(username + ": spectator status is " + isPureSpectator);
			
		}else if(command.startsWith("g:")) {
			
			lobby.broadcastMessage(username + ": requested game type " + command.substring(2));
			
		} else if(command.startsWith("l")) {
			lobby.launchGame();
		}
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