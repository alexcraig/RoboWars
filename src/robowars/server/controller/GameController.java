package robowars.server.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import lejos.robotics.Pose;

import org.apache.log4j.Logger;

import robowars.shared.model.CommandType;
import robowars.shared.model.ControlType;
import robowars.shared.model.FreeTest;
import robowars.shared.model.GameListener;
import robowars.shared.model.GameEvent;
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
	private volatile boolean terminateFlag;
	
	/**
	 * Generates a new GameController
	 * @param lobby	The server lobby to notify when the game is complete.
	 */
	public GameController(ServerLobby lobby, GameType gameType) {
		this.lobby = lobby;
		controlPairs = new ArrayList<ControlPair>();
		spectators = new ArrayList<UserProxy>();
		terminateFlag = false;
		generateGameModel(gameType);
	}
	
	/**
	 * Generates a control pair from the provided user and robot proxy.
	 * @param player	The user to issue remote commands
	 * @param robot	The robot to be controlled
	 */
	public void addPlayer(UserProxy player, RobotProxy robot) {
		synchronized(controlPairs) {
			controlPairs.add(new ControlPair(player, robot));
			player.setGameController(this);
			robot.setGameController(this);
		}
		
		model.addRobot(robot.getIdentifier());
		log.debug("Added control pair: " + player.getUser().getUsername() + " <-> " 
				+ robot.getIdentifier());
	}
	
	/**
	 * Adds a spectator to the game
	 * @param player	The player to spectate
	 */
	public void addSpectator(UserProxy player) {
		synchronized(spectators) {
			spectators.add(player);
		}
	}
	
	/**
	 * @param player The player proxy to check against
	 * @return	True if the passed player proxy is part of a robot control pair
	 */
	public boolean isPlayer(UserProxy player) {
		synchronized(controlPairs) {
			for(ControlPair pair : controlPairs) {
				if(pair.getUserProxy() == player) {
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * @param robot	The robot proxy to check against
	 * @return	True if the passed robot proxy is involved in a running game.
	 */
	public boolean isActiveRobot(RobotProxy robot) {
		synchronized(controlPairs) {
			for(ControlPair pair : controlPairs) {
				if(pair.getRobotProxy() == robot) {
					return true;
				}
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
		synchronized(controlPairs) {
			for (ControlPair pair : controlPairs) {
				if(pair.getUserProxy() == player) {
					return pair.getRobotProxy();
				}
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
		lobby.broadcastMessage("<Server> Game launched.");
		long gameStartTime = System.currentTimeMillis();
		long lastUpdateTime = gameStartTime;
		long timeElapsed = 0;
		
		while(!terminateFlag) {
			timeElapsed = System.currentTimeMillis() - lastUpdateTime;
			lastUpdateTime = System.currentTimeMillis();
			
			// Update game physics
			model.updateGameState(timeElapsed);
			
			// Fetch and send any required commands to robots
			synchronized(controlPairs) {
				for(ControlPair pair : controlPairs) {
					RobotCommand command = 
						model.getCurrentRobotCommand(pair.getRobotProxy().getIdentifier());
					if(command != null) {
						pair.getRobotProxy().sendCommand(command);
					}
				}
			}
			
			// Check for game termination state if the termination
			// trigger is not already set.
			if(!terminateFlag) {
				terminateFlag = model.checkGameOver();
			}
			
			// TESTING - 60 Second Limit
			if(lastUpdateTime - gameStartTime >= 60000) {
				terminateFlag = true;
				lobby.broadcastMessage("<Server> 60 Second testing duration expired.");
			}
		}
		
		lobby.broadcastMessage("<Server> Game terminating.");
		terminateGame();
	}
	
	/**
	 * Sets the termination flag of the GameController. This will cause the
	 * controller to terminate the game and notify the lobby of the termination
	 * on the next run through the game loop.
	 */
	public void triggerTermination() {
		terminateFlag = true;
	}
	

	/**
	 * @return	True if the termination flag has been set.
	 */
	public synchronized boolean isTerminating() {
		return terminateFlag;
	}
	/** 
	 * Signals the lobby to remove references to the current game, and clears
	 * all references to user and robot proxies.
	 */
	private synchronized void terminateGame() {
		log.info("Game terminating.");
		
		for(ControlPair pair : controlPairs) {
			pair.getUserProxy().clearGameController();
			pair.getRobotProxy().clearGameController();
			model.removeRobot(pair.getRobotProxy().getIdentifier());
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
	 * Generates a new instance of a GameModel subclass based on the passed
	 * GameType. Generates a game of the default game type if a null gameType
	 * was passed.
	 */
	private void generateGameModel(GameType gameType) {
		if(gameType == null) {
			gameType = GameType.getDefault();
		}
		
		switch(gameType) {
		case LIGHTCYCLES:
			model = new LightCycles();
			break;
		case TANK_SIMULATION:
			model = new TankSimulation();
			break;
		case FREETEST:
			model = new FreeTest();
			break;
		}
		model.addListener(this);
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
			return;
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
			
			// TODO: Maybe make getControlType() a method of the model subclasses?
			if(model instanceof LightCycles) {
				command = generateCommand(tilt, buttons, ControlType.SNAKE);
			} else if (model instanceof TankSimulation || model instanceof FreeTest) {
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
	private RobotCommand generateCommand(Vector<Float> tilt, String buttons, ControlType controlType) {
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
	
	public void updateRobotPosition(RobotProxy robot, Pose newPos) {
		log.debug("Got robot position update:\n\tRobot: " + robot.getIdentifier()
				+ "\n\tX: " + newPos.getX() + "\tY: " + newPos.getY() + "\tHeading: "
				+ newPos.getHeading());
		model.updateRobotPosition(robot.getIdentifier(), newPos);
	}
	
	public void gameStateChanged(GameEvent event){
		
	}
}
