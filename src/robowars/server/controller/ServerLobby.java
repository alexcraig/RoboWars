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
	 */
	public void addUser(UserProxy user) {
		synchronized(users) {
			if(users.size() < maxUsers) {
				users.add(user);
			} else {
				// TODO: THROW EXCEPTION
			}
		}
		broadcastMessage(user.getUsername() + " has joined the server.");
	}
	
	/**
	 * Removes a user from the list of connected users.
	 * @param user	The user to remove
	 */
	public void removeUser(UserProxy user) {
		synchronized(users) {
			users.remove(user);
		}
		broadcastMessage(user.getUsername() + " has left the server.");
	}
	
	/**
	 * Registers a robot to be considered for remote control pairing.
	 * @param robot	The proxy of the robot to add
	 */
	public void registerRobot(RobotProxy robot) {
		synchronized(robots) {
			if(robots.size() < maxRobots) {
				robots.add(robot);
			} else {
				// TODO: THROW EXCEPTION
			}
		}
	}

	/**
	 * Removes a robot from the list of robots considered for remote
	 * control paring.
	 * @param robot	The proxy of the robot to remove
	 */
	public void unregisterRobot(RobotProxy robot) {
		synchronized(robots) {
			robots.remove(robot);
		}
	}
	
	/**
	 * Broadcasts a message to all connected users.
	 * @param message	The message to broadcast
	 */
	public void broadcastMessage(String message) {
		log.debug("Broadcasting: " + message);
		synchronized(users) {
			for(UserProxy user : users) {
				user.sendMessage(message);
			}
		}
	}
	
	/**
	 * Sets the game type for the next game to be launched, and sets the
	 * ready status of all connected clients to false.
	 * @param newType	The game type of the next game to launch
	 */
	public void setGameType(GameType newType) {
		this.selectedGameType = newType;
		synchronized(users) {
			for(UserProxy user : users) {
				user.setReady(false);
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
	 * @return	The name of the server
	 */
	public String getServerName() {
		return serverName;
	}
	
	/**
	 * Attempts to launch a new game, given that the required minimum amount of players
	 * and an equal number of connected robots are available.
	 */
	public void launchGame() {
		
	}
}
