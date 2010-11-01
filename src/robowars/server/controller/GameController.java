package robowars.server.controller;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import robowars.shared.model.GameListener;
import robowars.shared.model.GameModel;
import robowars.shared.model.GameType;
import robowars.shared.model.LightCycles;
import robowars.shared.model.TankSimulation;

/**
 * Manages communication with an instance of GameModel. This classes 
 * responsibilities include storing user / robot control pairs, broadcasting
 * game state changes to connected players, passing robot position updates
 * to the game model, and launching or terminating games.
 */
public class GameController implements Runnable, GameListener {
	/** The logger used by this class */
	private static Logger log = Logger.getLogger(GameController.class);
	
	/** A list of all control pairs registered with this game */
	private List<ControlPair> controlPairs;
	
	/** A list of connected users who are not in active control of a robot */
	private List<UserProxy> spectators;
	
	/** The server lobby to notify when the game is complete. */
	private ServerLobby lobby;
	
	/** The instance of a GameModel subclass that this controller should control */
	private GameModel model;
	
	/**
	 * Generates a new GameController
	 * @param lobby	The server lobby to notify when the game is complete.
	 */
	public GameController(ServerLobby lobby) {
		this.lobby = lobby;
		model = null;
		controlPairs = new ArrayList<ControlPair>();
		spectators = new ArrayList<UserProxy>();
	}
	
	/**
	 * Generates a control pair from the provided user and robot proxy.
	 * @param player	The user to issue remote commands
	 * @param robot	The robot to be controlled
	 */
	public void addPlayer(UserProxy player, RobotProxy robot) {
		controlPairs.add(new ControlPair(player, robot));
		
		log.debug("Added control pair: " + player.getUser().getUsername() + " <-> " 
				+ robot.getIdentifier());
	}
	
	/**
	 * Adds a spectator to the game
	 * @param player	The player to spectate
	 */
	public void addSpectator(UserProxy player) {
		spectators.add(player);
	}
	
	/**
	 * @param player The player proxy to check against
	 * @return	True if the passed player proxy is part of a robot control pair
	 */
	public boolean isPlayer(UserProxy player) {
		for(ControlPair pair : controlPairs) {
			if(pair.getUser() == player) {
				return true;
			}
		}
		return false;
	}

	@Override
	/**
	 * Main loop of GameController (performs real-time physics updates on GameModel)
	 */
	public void run() {
		log.info("Game execution starting.");
		
		try {Thread.sleep(5000);} catch (InterruptedException e) {}; // TESTING
		
		terminateGame();
	}
	
	/** 
	 * Signals the lobby to remove references to the current game, and clears
	 * all references to user and robot proxies.
	 */
	public void terminateGame() {
		log.info("Game terminating.");
		lobby.endCurrentGame();
		lobby = null;
		controlPairs.clear();
		controlPairs = null;
		spectators.clear();
		spectators = null;
		model = null;
	}
	
	/**
	 * Generates a new instance of a GameModel subclass based on the currently
	 */
	public void generateGameModel(GameType gameType) {
		switch(gameType) {
		case LIGHTCYCLES:
			model = new LightCycles();
			break;
		case TANK_SIMULATION:
			model = new TankSimulation();
			break;
		}
	}
}
