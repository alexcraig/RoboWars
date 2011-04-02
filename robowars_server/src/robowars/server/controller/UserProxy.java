package robowars.server.controller;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.EventObject;
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
	private ObjectInputStream inputStream;
	
	/** Writer for client output */
	private ObjectOutputStream outputStream;
	
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
			this.outputStream = new ObjectOutputStream(userSocket.getOutputStream());
			this.inputStream = new ObjectInputStream(userSocket.getInputStream());
		} catch (IOException e) {
			log.error("Failed to open input/output streams.");
			e.printStackTrace();
		}

		try {
			// Read protocol string, ensure protocol matches
			String protocol = inputStream.readUTF();
			log.info("Read protocol string: " + protocol);
			
			if(!protocol.equals(SystemControl.USER_PROTOCOL_VERSION)) {
				sendMessage("Error - Protocol Mismatch (Got: \"" + protocol + "\", Expected: \""
						+ SystemControl.USER_PROTOCOL_VERSION + ")");
				return;
			} else {
				sendMessage("Valid Protocol - Enter Username");
			}
			
			// User name selection
			String name = inputStream.readUTF();
			log.info("Read name: " + name);
			if(lobby.isUsernameRegistered(name)) {
				// Close the connection to the client if the selected username already exists
				sendMessage("Selected Username Already In Use - Please Reconnect.");
				return;
			}
			
			// Generate user object and add to server lobby
			setUser(new User(name, userSocket.getInetAddress()));
			log.debug("Client username: " + user.getUsername());
			sendMessage(user.getUsername() + " connected to: " + lobby.getServerName());
			
			if (lobby.addUserProxy(this)) {
				// If a game is in progress, send a game launch event to the client
				// (so that camera information can be determined)
				if(lobby.gameInProgress()) {
					sendEvent(new LobbyGameEvent(lobby, 
							ServerLobbyEvent.EVENT_GAME_LAUNCH, lobby.getCurrentGameType()));
				}
				
				// Read strings from socket until connection is terminated
				Object incomingMessage;
				try {
					
					// Note: Input stream should only ever be read by this thread,
					// and therefore does not need to be synchronized.
					while ((incomingMessage = inputStream.readObject()) != null) {
						if(incomingMessage instanceof ClientCommand) {
							ClientCommand clientCmd = (ClientCommand)incomingMessage;
							handleInput(clientCmd);
						}
					}
					
				} catch (ClassNotFoundException e) {
					log.info("Class for incoming message could not be determined.");
					e.printStackTrace();
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
		sendEvent(new LobbyChatEvent(lobby, message));
		log.debug("Message sent to client: " + message);
	}
	
	/**
	 * Sends a EventObject (or any of its subclasses) to the connected client.
	 * This is typically used for sending both ServerLobbyEvents and
	 * GameEvents
	 * 
	 * @param e	The event to send to the client
	 */
	public void sendEvent(EventObject event) {
		// TODO: May want to find a better place to handle this processing
		// If the outgoing event is a LobbyGameEvent, attach the information
		// for the currently selected media device
		if(event instanceof LobbyGameEvent) {
			if(mediaStreamer.getActiveCamera() != null) {
				((LobbyGameEvent)event).setCameraPosition(mediaStreamer.getActiveCamera().getPosition());
			}
		}
		
		synchronized(outputStream) {
			try {
				outputStream.writeObject(event);
				
				// Must be called to ensure writing a modified object
				// does not just send a reference to the previously written object
				// (which would not have any modified values)
				outputStream.reset(); 
			} catch (IOException e) {
				e.printStackTrace();
			}
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
	 * Investigates the type of the passed ClientCommand, and performs the
	 * action associated with the specified command type.
	 */
	private void handleInput(ClientCommand cmd){
		if(user == null) {
			log.error("Proxy attempted to handle input with no associated user.");
			return;
		}
		
		switch(cmd.getCommandType()) {
		case ClientCommand.LAUNCH_GAME:
				// Request to launch game
				processGameLaunch();
				break;
				
		case ClientCommand.DISCONNECT:
			// Disconnect message
			terminateConnection();
			break;
		
		case ClientCommand.READY_STATUS:
			// Request to change ready status
			processChangedReadyState(cmd.getBoolData());
			break;
			
		case ClientCommand.SPECTATOR_STATUS:
			// Request to change spectator status
			processChangedSpectatorState(cmd.getBoolData());
			break;
			
		case ClientCommand.CHAT_MESSAGE:
			// Chat message
			processChatMessage(cmd.getStringData());
			break;
			
		case ClientCommand.GAME_TYPE_CHANGE:
			// Request to change game type
			processGameTypeChange(cmd.getStringData());
			break;
			
		case ClientCommand.GAMEPLAY_COMMAND:
			// Gameplay command
			processGameplayCommand(cmd.getAzimuth(), cmd.getPitch(), cmd.getRoll(),
					cmd.getStringData());
			break;
			
		default:
			break;
		}
	}
	
	/**
	 * Handles user input which requests a new game be launched.
	 */
	public void processGameLaunch() {
		if(user.isPureSpectator()) {
			log.debug("Game launch blocked (" + user.getUsername() + " is a pure spectator)");
			sendMessage("Spectators may not launch a new game.");
		} else {
			lobby.launchGame();
		}
	}
	
	/**
	 * Broadcasts a chat message through the ServerLobby
	 * @param message	The chat message to be broadcast
	 */
	public void processChatMessage(String message) {
		lobby.broadcastMessage(user.getUsername() + ": " + message);
	}
	
	/**
	 * Processes a request to change the lobby's selected game type.
	 * @param gameTypeString	The string representation of the
	 * 							desired game type.
	 */
	public void processGameTypeChange(String gameTypeString) {
		if(GameType.parseString(gameTypeString) != null) {
			lobby.setGameType(GameType.parseString(gameTypeString));
		}
	}
	
	/**
	 * Sets the ready state of the user and broadcasts the event through
	 * the ServerLobby
	 * @param newState	The new ready state (true or false)
	 */
	public void processChangedReadyState(boolean newState) {
		user.setReady(newState);
		lobby.broadcastUserStateUpdate(this);
	}
	
	/**
	 * Sets the spectator state of the user and broadcasts the event through
	 * the ServerLobby
	 * @param newState	The new spectator state (either true or false)
	 */
	public void processChangedSpectatorState(boolean newState) {
		user.setPureSpectator(newState);
		lobby.broadcastUserStateUpdate(this);
	}
	
	/**
	 * Generates a orientation vector from float inputs, and passes the input
	 * to the GameController for processing. All values will be clamped to
	 * the range [1, -1], so care should be taken to perform client side
	 * input scaling.
	 * 
	 * @param azimuth	The azimuth of the client device
	 * @param pitch		The pitch of the client device
	 * @param roll		The roll of the client device
	 * @param buttons	A string containing any buttons pressed by the client
	 */
	public void processGameplayCommand(Float azimuth, Float pitch, Float roll, String buttons) {
		// Ignore commands from unpaired players
		if(controller == null) { return; }
		
		if(azimuth == null || pitch == null || roll == null) {
			// Case where no orientation was supplied
			log.info("None or incomplete orientation vector supplied with gameplay command.");
			controller.processInput(this, null, buttons);
		} else {
			// Case where orientation was supplied
			Vector<Float> orientation = new Vector<Float>();
			orientation.addElement(clamp(azimuth, -1, 1));
			orientation.addElement(clamp(pitch, -1, 1));
			orientation.addElement(clamp(roll, -1, 1));
			controller.processInput(this, orientation, buttons);
		}
		
		return;
		
	}

	@Override
	/** @see ServerLobbyListener#userStateChanged(LobbyUserEvent) */
	public void userStateChanged(LobbyUserEvent event) {
		sendEvent(event);
	}

	@Override
	/** @see ServerLobbyListener#robotStateChanged(LobbyRobotEvent) */
	public void robotStateChanged(LobbyRobotEvent event) {
		sendEvent(event);;
	}

	@Override
	/** @see ServerLobbyListener#lobbyGameStateChanged(LobbyGameEvent) */
	public void lobbyGameStateChanged(LobbyGameEvent event) {
		sendEvent(event);
	}

	@Override
	/** @see ServerLobbyListener#lobbyChatMessage(LobbyChatEvent) */
	public void lobbyChatMessage(LobbyChatEvent event) {
		sendEvent(event);
	}
	
	/**
	 * Clamps an input value between a provided minimum and maximum, and returns
	 * the value (this is primarily used to ensure that vector input is in the
	 * 1 to -1 range).
	 * @param input	The input value
	 * @param min	The minimum output value
	 * @param max	The maximum output value
	 * @return	The input value, clamped to the provided minimum and maximum
	 */
	private float clamp(float input, float min, float max) {
		if(input > max) return max;
		if(input < min) return min;
		return input;
	}
}