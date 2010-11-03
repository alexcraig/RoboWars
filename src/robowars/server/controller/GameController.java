package robowars.server.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;

import robowars.shared.model.CommandType;
import robowars.shared.model.ControlType;
import robowars.shared.model.GameListener;
import robowars.shared.model.GameModel;
import robowars.shared.model.GameType;
import robowars.shared.model.LightCycles;
import robowars.shared.model.RobotCommand;
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
	 * Flag to determine when real time updating of game state should occur. As
	 * soon as this flag becomes true the controller thread will terminate and 
	 * remove all references to a game model and user / robot proxies.
	 */
	private boolean terminateFlag;
	
	/**
	 * Generates a new GameController
	 * @param lobby	The server lobby to notify when the game is complete.
	 */
	public GameController(ServerLobby lobby) {
		this.lobby = lobby;
		model = null;
		controlPairs = new ArrayList<ControlPair>();
		spectators = new ArrayList<UserProxy>();
		terminateFlag = false;
	}
	
	/**
	 * Generates a control pair from the provided user and robot proxy.
	 * @param player	The user to issue remote commands
	 * @param robot	The robot to be controlled
	 */
	public synchronized void addPlayer(UserProxy player, RobotProxy robot) {
		controlPairs.add(new ControlPair(player, robot));
		player.setGameController(this);
		robot.setGameController(this);
		
		log.debug("Added control pair: " + player.getUser().getUsername() + " <-> " 
				+ robot.getIdentifier());
	}
	
	/**
	 * Adds a spectator to the game
	 * @param player	The player to spectate
	 */
	public synchronized void addSpectator(UserProxy player) {
		spectators.add(player);
	}
	
	/**
	 * @param player The player proxy to check against
	 * @return	True if the passed player proxy is part of a robot control pair
	 */
	public synchronized boolean isPlayer(UserProxy player) {
		for(ControlPair pair : controlPairs) {
			if(pair.getUserProxy() == player) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Returns the RobotProxy that has been paired to a specified UserProxy
	 * @param player	The UserProxy to find a paired robot for
	 * @return	The paired RobotProxy, or null if the player is not part of a valid
	 * 			control pair
	 */
	public synchronized RobotProxy getPairedRobot(UserProxy player) {
		for (ControlPair pair : controlPairs) {
			if(pair.getUserProxy() == player) {
				return pair.getRobotProxy();
			}
		}
		
		return null;
	}

	@Override
	/**
	 * Main loop of GameController (performs real-time physics updates on GameModel)
	 */
	public void run() {
		log.info("Game execution starting.");
		lobby.broadcastMessage("<Server> Game launched - 60 second duration.");
		
		while(!terminateFlag) {
			try {
				Thread.sleep(60000);
				terminateFlag = true;
			}
			catch (InterruptedException e) {}; // TESTING
		}
		
		lobby.broadcastMessage("<Server> Game terminating.");
		terminateGame();
	}
	
	/** 
	 * Signals the lobby to remove references to the current game, and clears
	 * all references to user and robot proxies.
	 */
	public synchronized void terminateGame() {
		log.info("Game terminating.");
		
		for(ControlPair pair : controlPairs) {
			pair.getUserProxy().clearGameController();
			pair.getRobotProxy().clearGameController();
		}
		
		controlPairs.clear();
		controlPairs = null;
		spectators.clear();
		spectators = null;
		
		lobby.endCurrentGame();
		lobby = null;
		
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
	
	/**
	 * Takes user input from a UserProxy and issues a corresponding RobotCommand
	 * to their paired robot (if any).
	 * @param player	The player proxy that received the input
	 * @param tilt	The tilt of the client's gyroscope (3D Vector)
	 * @param buttons	A string of all buttons pressed by the client
	 */
	public void processInput(UserProxy player, Vector<Float> tilt, String buttons) {
		if(model == null) {
			log.error("Input ignored - no game model loaded.");
			return ;
		}
		
		if(player == null || (tilt != null && tilt.size() != 3)) {
			log.error("Input ignored - null played proxy or invalid tilt vector specified.");
			return;
		}
		
		if(tilt != null) {
			log.info("Got command from " + player.getUser().getUsername() +": Tilt: <" 
					+ tilt.get(0) + "," + tilt.get(1) + "," + tilt.get(2) + ">  Buttons: <" 
					+ buttons + ">");
		} else {
			log.info("Got command from " + player.getUser().getUsername() +": Buttons: <" 
					+ buttons + ">");
		}
		
		RobotProxy pairedRobot = getPairedRobot(player);
		if(pairedRobot != null && model != null) {
			RobotCommand command = null;
			if(model instanceof LightCycles) {
				command = generateCommand(tilt, buttons, ControlType.SNAKE);
			} else if (model instanceof TankSimulation) {
				command = generateCommand(tilt, buttons, ControlType.TANK);
			} else {
				log.error("Unrecognized game type, no control type available.");
			}
			
			if(command != null && model.isValidCommand(command)) {
				pairedRobot.sendCommand(command);
			}
		}
	}
	
	/**
	 * Generates a RobotCommand based on the passed tilt, buttons pressed and
	 * control scheme.
	 * @param tilt	The tilt of the client's gyroscope (3D Vector)
	 * @param buttons	The buttons pressed by the client
	 * @param controlType	The control scheme to use for generating commands
	 * @return	A valid RobotCommand, or null if no command should be issued.
	 */
	private RobotCommand generateCommand(Vector<Float> tilt, String buttons, 
			ControlType controlType) {
		// TODO: Ignore control type for now
		// Using W A S D commands for testing
		if(buttons.contains("w")) {
			return new RobotCommand(CommandType.MOVE_CONTINUOUS);	
		} else if (buttons.contains("a")) {
			return new RobotCommand(CommandType.TURN_RIGHT_ANGLE_LEFT);
		} else if (buttons.contains("d")) {
			return new RobotCommand(CommandType.TURN_RIGHT_ANGLE_RIGHT);
		} else if (buttons.contains("s")) {
			return new RobotCommand(CommandType.STOP);
		}
		
		return null;
	}
	
	public void updateRobotPosition(RobotProxy robot, Vector<Float> position, 
			Vector<Float> heading) {
	}
}
