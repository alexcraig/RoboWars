package robowars.server.controller;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import robowars.shared.model.GameType;

/**
 * Manages any state of the server which is unrelated to actual real-time 
 * game play. This classes responsibilities include managing the lists of 
 * connected robots and users, selecting game types, selecting user / robot 
 * pairs, and launching new games (generating instances of GameController).
 */
public class ServerLobby {
	
	/** The logger used by this class */
	private static Logger log = Logger.getLogger(ServerLobby.class);
	
	/** The name of the server */
	private String serverName;
	
	/** The maximum number of connected users */
	private int maxUsers;
	
	/** The maximum number of connected robots */
	private int maxRobots;
	
	/** 
	 * A list of all currently connected user proxies. Users are ordered by the
	 * time since they last controlled a robot. New users are added to the end
	 * of the queue. 
	 * */
	private List<UserProxy> users;
	
	/** A list of all currently connected robot proxies */
	private List<RobotProxy> robots;
	
	/** 
	 * A list of listeners who should receive events when the lobby status changes
	 */
	private List<ServerLobbyListener> listeners;
	
	/** The currently selected game type (used when a new game is launched) */
	private GameType selectedGameType;
	
	/** 
	 * The controller for the game currently in progress (null if no game
	 * is in progress).
	 */
	private GameController currentGame;
	
	/**
	 * Generates a new, empty server lobby.
	 * @param serverName	The name of the server
	 * @param maxRobots		The maximum number of robots to accept
	 * @param maxUsers		The maximum number of users to accept
	 */
	public ServerLobby(String serverName, int maxRobots, int maxUsers) {
		this.serverName = serverName;
		this.maxRobots = maxRobots;
		this.maxUsers = maxUsers;
		selectedGameType = GameType.getDefault();
		users = new ArrayList<UserProxy>();
		robots = new ArrayList<RobotProxy>();
		listeners = new ArrayList<ServerLobbyListener>();
	}
	
	/**
	 * Registers a ServerLobbyListener to listen on events from the server lobby.
	 * @param listener	The listener to add
	 */
	public synchronized void addLobbyStateListener(ServerLobbyListener listener) {
		if(listener == null) return;
		if(!listeners.contains(listener)) {
			listeners.add(listener);
		}
	}
	
	/**
	 * Removes a ServerLobbyListener from the listener list.
	 * @param listener	The listener to remove
	 */
	public synchronized void removeLobbyStateListener(ServerLobbyListener listener) {
		if(listener == null) return;
		listeners.remove(listener);
	}
	
	/**
	 * Adds a user to the end of the list of connected users. The user
	 * must already be connected (i.e. has gone through the authentication
	 * and user name selection handshake).
	 * @param userProxy	The user to add
	 * @return True if the user was successfully added, false if not
	 */
	public synchronized boolean addUserProxy(UserProxy userProxy) {
		if(userProxy == null || !userProxy.isConnected()) {
			log.error("Attempt to add null or unconnected user proxy.");
			return false;
		}
		
		boolean addSuccess = false;
		if(users.size() < maxUsers) {
			users.add(userProxy);
			addSuccess = true;
			log.debug(userProxy.getUser().getUsername() + " added to lobby.");
			listeners.add(userProxy);
			for(ServerLobbyListener listener : listeners) {
				listener.userStateChanged(new LobbyUserEvent(this, ServerLobbyEvent.EVENT_PLAYER_JOINED, userProxy.getUser()));
			}
		} else {
			if(userProxy != null) {
				log.debug(userProxy.getUser().getUsername() + " attempted to join full server.");
			}
		}
		return addSuccess;
	}
	
	/**
	 * Removes a user from the list of connected users.
	 * @param userProxy	The user to remove
	 */
	public synchronized void removeUserProxy(UserProxy userProxy) {
		if(users.remove(userProxy)) {
			log.debug(userProxy.getUser().getUsername() + " removed from lobby.");
			for(ServerLobbyListener listener : listeners) {
				listener.userStateChanged(new LobbyUserEvent(this, ServerLobbyEvent.EVENT_PLAYER_LEFT, userProxy.getUser()));
			}
			listeners.remove(userProxy);
			
			// TODO: If user is currently playing, terminate the game
		}
	}
	
	/**
	 * Registers a robot to be considered for remote control pairing.
	 * @param robot	The proxy of the robot to add
	 * @return True if the robot was successfully added, false if not
	 */
	public synchronized boolean registerRobot(RobotProxy robot) {
		boolean addSuccess = false;
		if(robots.size() < maxRobots) {
			robots.add(robot);
			addSuccess = true;
			log.debug("Robot: " + robot.getIdentifier() + " added to lobby.");
			for(ServerLobbyListener listener : listeners) {
				listener.robotStateChanged(new LobbyRobotEvent(this, ServerLobbyEvent.EVENT_ROBOT_REGISTERED, robot));
			}
		}
		return addSuccess;
	}

	/**
	 * Removes a robot from the list of robots considered for remote
	 * control paring.
	 * @param robot	The proxy of the robot to remove
	 */
	public synchronized void unregisterRobot(RobotProxy robot) {
		if(robots.remove(robot)) {
			log.debug("Robot: " + robot.getIdentifier() + " removed from lobby.");
			for(ServerLobbyListener listener : listeners) {
				listener.robotStateChanged(new LobbyRobotEvent(this, ServerLobbyEvent.EVENT_ROBOT_UNREGISTERED, robot));
			}
			
			// TODO: Stop game in progress if robot was in use
		}
	}
	
	/**
	 * Generates and broadcasts a chat event to all registered event listeners
	 * @param user	The chat message to be sent
	 */
	public synchronized void broadcastMessage(String message) {
		if(message == null) {
			log.error("Attempted to broadcast null message.");
			return;
		}
		
		log.debug(message);
		for(ServerLobbyListener listener : listeners) {
			listener.lobbyChatMessage(new LobbyChatEvent(this, message));
		}
	}
	
	/**
	 * Generates and broadcasts a player state update event to all registered
	 * listeners for the specified user. This should be called when the spectator
	 * or ready state of a user changes. The passed user must be registered
	 * with this server lobby, otherwise no action is taken.
	 * 
	 * @param userProxy	The userProxy to broadcast state for
	 */
	public synchronized void broadcastUserStateUpdate(UserProxy userProxy) {
		if(users.contains(userProxy)) {
			log.debug(userProxy.getUser().getUsername() + " state: < Ready = " + 
					userProxy.getUser().isReady()
					+ ", Spectator = " + userProxy.getUser().isPureSpectator() + ">");
			for(ServerLobbyListener listener : listeners) {
				listener.userStateChanged(new LobbyUserEvent(this, ServerLobbyEvent.EVENT_PLAYER_STATE_CHANGE, 
						userProxy.getUser()));
			}
		}
	}
	
	/**
	 * Sets the game type for the next game to be launched, and sets the
	 * ready status of all connected clients to false.
	 * @param newType	The game type of the next game to launch
	 */
	public synchronized void setGameType(GameType newType) {
		if(newType == null) return;
		
		this.selectedGameType = newType;
		for(ServerLobbyListener listener : listeners) {
			listener.lobbyGameStateChanged(new LobbyGameEvent(this, LobbyGameEvent.EVENT_GAMETYPE_CHANGE, selectedGameType));
		}
		
		for(UserProxy userProxy : users) {
			if(userProxy.getUser().isReady()) {
				userProxy.getUser().setReady(false);
				broadcastUserStateUpdate(userProxy);
			}
		}
	}
	
	/**
	 * @return	True if a game is currently in progress
	 */
	public boolean gameInProgress() {
		return currentGame != null;
	}
	
	/**
	 * @return	The controller object of the game currently in progress (null
	 * 			if no game is in progress)
	 */
	public GameController getCurrentGame() {
		return currentGame;
	}
	
	/**
	 * @return	The currently selected game type
	 */
	public GameType getCurrentGameType() {
		return selectedGameType;
	}
	
	/**
	 * @return	The name of the server
	 */
	public String getServerName() {
		return serverName;
	}
	
	/**
	 * Attempts to launch a new game, given that the required minimum amount of players
	 * and an equal number of connected robots are available.
	 */
	public synchronized void launchGame() {
		// Only one game can be running at a time
		if (gameInProgress()) {
			log.debug("Game launch requested, but game is in progress.");
			broadcastMessage("<Server> Game launch requested, but game is in progress.");
			return;
		}
		
		if(selectedGameType == null) {
			log.error("Attempted game launch with null game type.");
			return;
		}
		
		// Ensure enough robots are available for the selected game type
		int availableRobots = robots.size();
		log.debug(availableRobots + " robot(s) available for pairing.");
		if(availableRobots < selectedGameType.getMinimumPlayers()) {
			log.debug("Insufficient robots to launch selected game type.");
			broadcastMessage("<Server> Insufficient robots to launch selected game type.");
			return;
		}
		
		// Select the players who will be paired to robots, and ensure they are all ready
		List<UserProxy> players = new ArrayList<UserProxy>();
		for(UserProxy userProxy : users) {
			if(!userProxy.getUser().isPureSpectator()) {
				if(players.size() < availableRobots && !userProxy.getUser().isReady()) {
					// If not enough players have been selected to control all available
					// robots and player selected for pairing is not ready, cancel the game launch
					log.debug("Game launch requested, but one or more players are not ready.");
					broadcastMessage("<Server> Game launch requested, but one or more players are not ready.");
					return;
				} else if (players.size() < availableRobots && userProxy.getUser().isReady()) {
					// If the game still requires more players and the player is ready,
					// add them to the list of players
					players.add(userProxy);
					
					if(players.size() == availableRobots) {
						break; // All players have been found
					}
				}
			}
		}
		
		// Cancel the launch if insufficient players for the game to launch were found
		if(players.size() < selectedGameType.getMinimumPlayers()) {
			log.debug("Insufficient players available for game launch (minimum = " 
					+ selectedGameType.getMinimumPlayers() + ")");
			broadcastMessage("<Server> Insufficient players available for game launch (minimum = " 
					+ selectedGameType.getMinimumPlayers() + ")");
			return;
		}
		
		log.debug(players.size() + " player(s) available for pairing.");
		
		// Generate the game controller and register control pairs
		log.debug("Launching game of type: " + selectedGameType.toString());
		
		currentGame = new GameController(this, selectedGameType);
		
		for(UserProxy player : players) {
			users.remove(player);
			RobotProxy pairedRobot = robots.remove(0);
			log.debug("Pairing: " + player.getUser().getUsername() + " <-> " + pairedRobot.getIdentifier());
			currentGame.addPlayer(player, pairedRobot);
			users.add(player);
			robots.add(pairedRobot);
		}
		
		// Add all remaining players as spectators
		for(UserProxy spectator : users) {
			if(!players.contains(spectator)) {
				currentGame.addSpectator(spectator);
			}
		}
		
		// Notify all listeners that a game is starting
		for(ServerLobbyListener listener : listeners) {
			listener.lobbyGameStateChanged(new LobbyGameEvent(this, LobbyGameEvent.EVENT_GAME_LAUNCH, selectedGameType));
		}
		
		// Start the game
		new Thread(currentGame).start();
	}
	
	/**
	 * Ends the current game (removing all references to users and robots from
	 * the game controller), and removes all references to the game controller. This
	 * should be called when a game's end condition is met or when an active player
	 * leaves the server.
	 */
	public synchronized void endCurrentGame() {
		currentGame = null; // TODO: More complete termination
		for(ServerLobbyListener listener : listeners) {
			listener.lobbyGameStateChanged(new LobbyGameEvent(this, LobbyGameEvent.EVENT_GAME_OVER, selectedGameType));
		}
	}
	
	/**
	 * Sends a shutdown message to all connected users and cleanly closes
	 * connections with all users and robots connected to the server
	 */
	public void serverShutdown() {
		// TODO: Implement
	}
}
