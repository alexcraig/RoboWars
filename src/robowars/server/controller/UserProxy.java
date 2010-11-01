package robowars.server.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import org.apache.log4j.Logger;

import robowars.shared.model.GameType;
import robowars.shared.model.User;

/**
 * Manages communications with a single user connected through an existing 
 * TCP socket. 
 */
public class UserProxy implements Runnable, ServerLobbyListener {
	/** The logger used by this class */
	private static Logger log = Logger.getLogger(UserProxy.class);
	
	/** 
	 * The User object which stores information on the state of the user
	 * connected to this proxy.
	 */
	private User user;

	/** Reader for client input */
	private BufferedReader inputStream;
	
	/** Writer for client output */
	private PrintWriter outputStream;
	
	/** The socket to generate input/output streams for */
	private Socket userSocket;
	
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
			String name = inputStream.readLine();
			user = new User(name);
			log.debug("Client username: " + user.getUsername());
			
			synchronized(outputStream) {
				outputStream.println(user.getUsername() + " connected to: " + lobby.getServerName());
			}
			
			if (lobby.addUserProxy(this)) {
				// Read strings from socket until connection is terminated
				String incomingMessage;
				while ((incomingMessage = inputStream.readLine()) != null) {
					log.debug("Received: " + incomingMessage);
					handleInput(incomingMessage);
				}
				log.info(user.getUsername() + " terminated connection with server.");
			} else {
				outputStream.println("[Error - Server Full]");
			}
			
		} catch (IOException e) {
			log.info("Client terminated connection with server.");
		} finally {
			lobby.removeUserProxy(this);
			user = null;
			try {
				outputStream.close();
				inputStream.close();
				userSocket.close();
			} catch (IOException e) {
				log.error("Could not close client socket.");
			}
		}
		
	}
	
	/**
	 * Sets the user object that this proxy is managing communication with.
	 * @param user	The user object that this proxy is managing communication with
	 */
	public void setUser(User user) {
		this.user = user;
	}
	
	/**
	 * @return	The User object this proxy is managing communication with
	 */
	public User getUser() {
		return user;
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
	 * handshake (determined by checking if an associated User object exists).
	 * @return true if the proxy is associated with a valid User object
	 */
	public boolean isConnected() {
		return (user != null);
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
		if(user == null) {
			log.error("Proxy attempted to handle input with no associated user.");
			return;
		}
		
		if(command.startsWith("m:")) {
			lobby.broadcastMessage(user.getUsername() + ": " + command.substring(2));
			
		} else if(command.startsWith("r:")) {
			
			if (command.substring(2,3).equalsIgnoreCase("t")) {
				user.setReady(true);
			} else if (command.substring(2,3).equalsIgnoreCase("f")) {
				user.setReady(false);
			}
			lobby.broadcastUserStateUpdate(this);
			
		} else if(command.startsWith("s:")) {
			
			if (command.substring(2,3).equalsIgnoreCase("t")) {
				user.setPureSpectator(true);
			} else if (command.substring(2,3).equalsIgnoreCase("f")) {
				user.setPureSpectator(false);
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
		if(user.isPureSpectator()) {
			log.debug("Game launch blocked (" + user.getUsername() + " is a pure spectator)");
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
		log.debug(event.serialize());
		sendMessage(event.serialize());
	}
}