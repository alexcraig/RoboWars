package robowars.server.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import org.apache.log4j.Logger;

import robowars.shared.model.GameType;

/**
 * Manages communications with a single user connected through an existing 
 * TCP socket. 
 */
public class UserProxy implements Runnable, ServerLobbyListener {
	/** The logger used by this class */
	private static Logger log = Logger.getLogger(UserProxy.class);
	
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
		
		log.debug("Opening input/output streams.");
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
				outputStream.println(SystemControl.VERSION_STRING);
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
				handleInput(incomingMessage);
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
	 * @return	The ready status of the user.
	 */
	public boolean isReady() {
		return isReady;
	}
	
	/**
	 * @return	The username of the connected user
	 */
	public String getUsername() {
		return username;
	}
	
	/**
	 * @return	True if the user is a pure spectator (has opted-out of robot control)
	 */
	public boolean isPureSpectator() {
		return isPureSpectator;
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
	private void handleInput(String command){	
		if(command.startsWith("m:")) {
			lobby.broadcastMessage(getUsername() + ": " + command.substring(2));
			
		} else if(command.startsWith("r:")) {
			
			if (command.substring(2,3).equalsIgnoreCase("t")) {
				setReady(true);
			} else if (command.substring(2,3).equalsIgnoreCase("f")) {
				setReady(false);
			}
			lobby.broadcastUserStateUpdate(this);
			
		} else if(command.startsWith("s:")) {
			
			if (command.substring(2,3).equalsIgnoreCase("t")) {
				isPureSpectator = true;
			} else if (command.substring(2,3).equalsIgnoreCase("f")) {
				isPureSpectator = false;
			}
			lobby.broadcastUserStateUpdate(this);
			
		}else if(command.startsWith("g:")) {
			
			if(GameType.parseString(command.substring(2)) != null) {
				lobby.setGameType(GameType.parseString(command.substring(2)));
			}
			
		} else if(command.startsWith("l")) {
			processGameLaunch();
		}
	}
	
	/**
	 * Handles user input which requests a new game be launched.
	 */
	private void processGameLaunch() {
		if(isPureSpectator) {
			log.debug("Game launch blocked (" + username + " is a pure spectator)");
			synchronized(outputStream) {
				outputStream.println("Spectators may not launch a new game.");
			}
		} else {
			lobby.launchGame();
		}
	}

	@Override
	/** @see ServerLobbyListener#userStateChanged(LobbyUserEvent) */
	public void userStateChanged(LobbyUserEvent event) {
		log.debug(event.serialize());
		sendMessage(event.serialize());
	}

	@Override
	/** @see ServerLobbyListener#robotStateChanged(LobbyRobotEvent) */
	public void robotStateChanged(LobbyRobotEvent event) {
		log.debug(event.serialize());
		sendMessage(event.serialize());
	}

	@Override
	/** @see ServerLobbyListener#lobbyGameStateChanged(LobbyGameEvent) */
	public void lobbyGameStateChanged(LobbyGameEvent event) {
		log.debug(event.serialize());
		sendMessage(event.serialize());
	}

	@Override
	/** @see ServerLobbyListener#lobbyChatMessage(LobbyChatEvent) */
	public void lobbyChatMessage(LobbyChatEvent event) {
		// TODO Auto-generated method stub
		
	}
}