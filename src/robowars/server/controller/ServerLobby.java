package robowars.server.controller;

import java.util.List;
import java.util.ArrayList;

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
	}
	
	/**
	 * Adds a user to the end of the list of connected users.
	 * @param user	The user to add
	 * @return True if the user was successfully added, false if not
	 */
	public synchronized boolean addUser(UserProxy user) {
		boolean addSuccess = false;
		if(users.size() < maxUsers) {
			users.add(user);
			addSuccess = true;
			log.debug(user.getUsername() + " added to lobby.");
		}
		if (addSuccess) broadcastMessage(user.getUsername() + " has joined the server.");
		return addSuccess;
	}
	
	/**
	 * Removes a user from the list of connected users.
	 * @param user	The user to remove
	 */
	public synchronized void removeUser(UserProxy user) {
		users.remove(user);
		broadcastMessage(user.getUsername() + " has left the server.");
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
		}
		return addSuccess;
	}

	/**
	 * Removes a robot from the list of robots considered for remote
	 * control paring.
	 * @param robot	The proxy of the robot to remove
	 */
	public synchronized void unregisterRobot(RobotProxy robot) {
		robots.remove(robot);
	}
	
	/**
	 * Broadcasts a message to all connected users.
	 * @param message	The message to broadcast
	 */
	public synchronized void broadcastMessage(String message) {
		log.info("Broadcasting: " + message);
		for(UserProxy user : users) {
			user.sendMessage(message);
		}
	}
	
	/**
	 * Sets the game type for the next game to be launched, and sets the
	 * ready status of all connected clients to false.
	 * @param newType	The game type of the next game to launch
	 */
	public synchronized void setGameType(GameType newType) {
		this.selectedGameType = newType;
		for(UserProxy user : users) {
			user.setReady(false);
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
		if (gameInProgress()) {
			log.info("Game launch requested, but game is in progress.");
			return; // Only one game can be running at a time
		}
		
		// Ensure all players are ready before launching a game
		// TODO: Should only consider users who will actually be paired
		for(UserProxy user : users) {
			if(!user.isReady() && !user.isPureSpectator()) {
				broadcastMessage("Game launch requested, but one or more players are not ready.");
				return;
			}
		}
		
		// Check how many players are available who are not flagged as spectators
		int availablePlayers = 0;
		for (UserProxy user : users) {
			if (!user.isPureSpectator()) {
				availablePlayers++;
			}
		}
		log.debug(availablePlayers + " player available for pairing.");
		int availableRobots = robots.size();
		log.debug(availableRobots + " robot available for pairing.");
		
		// If enough players and robots are available, launch a game
		if(availablePlayers >= selectedGameType.getMinimumPlayers() &&
				availableRobots >= selectedGameType.getMinimumPlayers()) {
			log.info("Launching game of type: " + selectedGameType.toString());
			currentGame = new GameController(this, selectedGameType);
			
			// Generate a control pairs while pairs of unused players and robots remain
			int playerPairs = 
				availablePlayers >= availableRobots ? availableRobots : availablePlayers;
				
			log.debug("Generating " + playerPairs + " control pairs.");
			for(int i = 0; i < playerPairs; i++) {
				UserProxy player = users.remove(0);
				if(!player.isPureSpectator()) {
					RobotProxy robot = robots.remove(0); 
					currentGame.addPlayer(player, robot);
					robots.add(robot);
					log.debug("Pairing: " + player.getUsername() + " <-> " + robot.getIdentifier());
				} else {
					i--;
				}
				users.add(player);
			}
			
			// Add all remaining players as spectators
			for(UserProxy user : users) {
				if(!currentGame.isPlayer(user)) {
					currentGame.addSpectator(user);
					log.debug("Adding spectator: " + user.getUsername());
				}
			}
			
			// Start the game
			new Thread(currentGame).start();
		} else {
			log.info("Insufficient players available for game launch (minimum = " 
					+ selectedGameType.getMinimumPlayers() + ")");
		}

	}
	
	/**
	 * Removes all references to the currently running game. This should be
	 * called by the game controller just before game termination.
	 */
	public void clearCurrentGame() {
		currentGame = null;
	}
}
