package robowars.server.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Vector;

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
	 * The GameController object managing the game this proxy is
	 * participating in (null if no game is in progress).
	 */
	private GameController controller;
	
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
		controller = null;
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
			
			// Protocol handshake
			synchronized(outputStream) {
				outputStream.println(SystemControl.USER_PROTOCOL_VERSION);
			}
			
			String protocol= inputStream.readLine();
			
			if(!protocol.equals(SystemControl.USER_PROTOCOL_VERSION)) {
				sendMessage("Error - Protocol Mismatch (try updating your client)");
				return;
			} else {
				sendMessage("Valid Protocol - Enter Username");
			}
			
			// User name selection
			boolean validName = false;
			String name = null;
			
			while(!validName) {
				name = inputStream.readLine();
				if(!lobby.isUsernameRegistered(name)) {
					validName = true;
				} else {
					sendMessage("Selected Username Already In Use - Try Again");
				}
			}
			
			// Generate user object and add to server lobby
			user = new User(name);
			log.debug("Client username: " + user.getUsername());
			sendMessage(user.getUsername() + " connected to: " + lobby.getServerName());
			
			if (lobby.addUserProxy(this)) {
				// Read strings from socket until connection is terminated
				String incomingMessage;
				while ((incomingMessage = inputStream.readLine()) != null) {
					log.debug("Received: " + incomingMessage);
					handleInput(incomingMessage);
				}
				log.info(user.getUsername() + " terminated connection with server.");
			} else {
				sendMessage("Error - Server Full");
			}
			
		} catch (IOException e) {
			log.info("Client terminated connection with server.");
		} finally {
			terminateConnection();
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
	 * Sets the instance of GameController that this UserProxy should pass
	 * input commands to.
	 * @param controller	The GameController instance to pass inputs to.
	 */
	public void setGameController(GameController controller) {
		if(controller != null) {
			this.controller = controller;
		}
	}
	
	/**
	 * Clears all references to a GameController for the proxy.
	 */
	public void clearGameController() {
		controller = null;
	}
	
	/**
	 * Terminates the connection with the User.
	 */
	private void terminateConnection() {
		log.info("Terminating connection.");

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
	
	/**
	 * Dispatches user input to the relevant processing functions based on the input received.
	 * 
	 * Commands:
	 * m:<message> - chat message 
	 * r:<t or f> - set ready state
	 * s:<t or f> - set pure spectator state
	 * g:<game_type_string> - set game type
	 * c:<x,y,z> or c:<x,y,z>button_string  or c:button_string
	 * 		a command string to be passed to a paired robot
	 * 		(Note: If no gyro is available tilt should always be <0,0,0>
	 * l - launch game
	 * q - disconnect
	 */
	private void handleInput(String command){
		if(user == null) {
			log.error("Proxy attempted to handle input with no associated user.");
			return;
		}
		
		if(command.startsWith("c:")) {
			handleGameplayCommand(command.substring(2));
		} else if(command.startsWith("m:")) {
			// Chat message
			lobby.broadcastMessage(user.getUsername() + ": " + command.substring(2));
			
		} else if(command.startsWith("r:")) {
			// Change of ready state
			handleChangeReadyState(command.substring(2,3));
			
		} else if(command.startsWith("s:")) {
			// Change of spectator state
			handleChangeSpectatorState(command.substring(2,3));
			
		} else if(command.startsWith("g:")) {
			// Change of game type
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
	
	/**
	 * Handles client input that corresponds to a change in ready state.
	 * @param newState	The new ready state (either "t" or "f")
	 */
	private void handleChangeReadyState(String newState) {
		if (newState.equalsIgnoreCase("t")) {
			user.setReady(true);
		} else if (newState.equalsIgnoreCase("f")) {
			user.setReady(false);
		} else {
			// Ensures broadcast only occurs if state actually changed
			return;
		}
		
		lobby.broadcastUserStateUpdate(this);
	}
	
	/**
	 * Handles client input that corresponds to a change in spectator state.
	 * @param newState	The new spectator state (either "t" or "f")
	 */
	private void handleChangeSpectatorState(String newState) {
		if (newState.equalsIgnoreCase("t")) {
			user.setPureSpectator(true);
		} else if (newState.equalsIgnoreCase("f")) {
			user.setPureSpectator(false);
		} else {
			// Ensures broadcast only occurs if state actually changed
			return;
		}
		lobby.broadcastUserStateUpdate(this);
	}
	
	/**
	 * Handles user input that should be passed to the game controller
	 * and treated as a remote robot command.
	 * @param command	The command string received from the client
	 */
	private void handleGameplayCommand(String command) {
		// Ignore commands from unpaired players
		if(controller == null) { return; }
		
		// Rough check of orientation validity (use regex instead?)
		if(!command.startsWith("<") || !command.contains(">")) {
			log.info("No orientation information provided with gameplay command.");
			controller.processInput(this, null, command);
			return;
		}
		
		// Assume valid input at this point
		Vector<Float> orientation = new Vector<Float>();
		
		try {
			// Get azimuth
			Float azimuth = Float.parseFloat(command.substring(1, command.indexOf(",")));
			command = command.substring(command.indexOf(",") + 1);
			log.info("Got AZIMUTH: " + azimuth);
			
			// Get pitch
			Float pitch = Float.parseFloat(command.substring(0, command.indexOf(",")));
			command = command.substring(command.indexOf(",") + 1);
			log.info("Got PITCH: " + pitch);
			
			// Get roll
			Float roll = Float.parseFloat(command.substring(0, command.indexOf(">")));
			command = command.substring(command.indexOf(">") + 1);
			log.info("Got ROLL: " + roll);
			
			// Read orientation floats into a vector
			orientation.addElement(azimuth);
			orientation.addElement(pitch);
			orientation.addElement(roll);
			
			controller.processInput(this, orientation, command);
			
		} catch (NumberFormatException e) {
			log.error("Invalid gameplay command format (gameplay command format invalid).");
			return;
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