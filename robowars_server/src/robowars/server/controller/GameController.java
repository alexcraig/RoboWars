package robowars.server.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;

import robowars.robot.ColorSensor;
import robowars.shared.model.ControlType;
import robowars.shared.model.FreeTest;
import robowars.shared.model.GameEvent;
import robowars.shared.model.GameListener;
import robowars.shared.model.GameModel;
import robowars.shared.model.GameType;
import robowars.shared.model.LightCycles;
import robowars.shared.model.Posture;
import robowars.shared.model.RobotCommand;
import robowars.shared.model.RobotMap;
import robowars.shared.model.TankSimulation;

/**
 * Manages communication with an instance of GameModel. This classes 
 * responsibilities include storing user / robot control pairs, broadcasting
 * game state changes to connected players, passing robot position updates
 * to the game model, and launching or terminating games.
 * 
 * @author Alexander Craig
 */
public class GameController implements Runnable, GameListener {
	/** The logger used by this class */
	private static Logger log = Logger.getLogger(GameController.class);
	
	/** Scaling factors for client orientation input */
	public static final float PITCH_SCALING_FACTOR = RobotCommand.MAX_SPEED;
	public static final float ROLL_SCALING_FACTOR = (float)200;
	
	/** dimensions for Grid */
	public static final float DOT_SPACING=6.35f;
	public static final int COLS=21;
	
	/** 
	 * If a calculated move value is less than RobotCommand.MAX_SPEED / STOP THRESHOLD,
	 * a STOP command will be sent instead (effectively sets what fraction of max
	 * move speed is required to send a move command).
	 */
	public static final float STOP_THRESHOLD = 15;
	
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
	 * Performs no action if the player or robot is null.
	 * @param player	The user to issue remote commands
	 * @param robot	The robot to be controlled
	 */
	public void addPlayer(UserProxy player, RobotProxy robot) {
		if(player == null || robot == null) {
			return;
		}
		
		synchronized(controlPairs) {
			controlPairs.add(new ControlPair(player, robot));
			player.setGameController(this);
			robot.setGameController(this);
		}
		
		model.addRobot(robot.getRobot());
		log.debug("Added control pair: " + player.getUser().getUsername() + " <-> " 
				+ robot.getIdentifier());
	}
	
	/**
	 * Adds a spectator to the game
	 * @param player	The player to spectate
	 */
	public void addSpectator(UserProxy player) {
		if(player == null) return;
		
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
	 * @param player The player proxy to check against
	 * @return	True if the passed player proxy is registered as a spectator
	 */
	public boolean isSpectator(UserProxy spectator) {
		synchronized(spectators) {
			for(UserProxy spec : spectators) {
				if(spec == spectator) {
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
		RobotMap map=ColorSensor.generate(COLS,COLS,DOT_SPACING);
		synchronized(controlPairs) {
			for(int i=0; i<controlPairs.size(); i++){
				RobotProxy proxy=controlPairs.get(i).getRobotProxy();
				
				// KLUDGE: Don't send map based position updates to test robots,
				// as this causes problems with testing
				if(proxy instanceof robowars.test.TestRobotProxy) continue;
				
				proxy.sendCommand(RobotCommand.setPosition(map.getStartPoint(i)));
			}
		}
		log.info("Robot Map and starting points sent");
		log.info("Game execution starting.");
		lobby.broadcastMessage("<Server> Game launched.");
		long gameStartTime = System.currentTimeMillis();
		long lastUpdateTime = gameStartTime;
		long timeElapsed = 0;
		model.startGame();
		while(!terminateFlag) {
			timeElapsed = System.currentTimeMillis() - lastUpdateTime;
			lastUpdateTime = System.currentTimeMillis();
			
			// Update game physics
			try {
				model.updateGameState(timeElapsed);
			} catch (NullPointerException e) {
				log.error("Error in model physics, terminating game.");
				terminateFlag = true;
				break;
			}
			
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
			/*
			if(lastUpdateTime - gameStartTime >= 60000) {
				terminateFlag = true;
				lobby.broadcastMessage("<Server> 60 Second testing duration expired.");
			}
			*/
			
			Thread.yield();
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
			// TODO: Commands to reset position should be dispatched here
			pair.getRobotProxy().sendCommand(RobotCommand.stop());
			
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
	 * @param orientation	The orientation of the client's device 
	 * 						3D Vector - <Azimuth, Pitch, Roll>
	 * @param buttons	A string of all buttons pressed by the client
	 */
	public void processInput(UserProxy player, Vector<Float> orientation, String buttons) {
		// Ensure user input is valid and log the input
		if(model == null) {
			log.error("Input ignored - no game model loaded.");
			return;
		}
		
		if(player == null || (orientation != null && orientation.size() != 3)) {
			log.error("Input ignored - null played proxy or invalid orientation vector specified.");
			return;
		}
		
		// Set buttons to an empty string if none was provided
		if(buttons == null) buttons = "";
		
		if(orientation != null) {
			log.info("Got command from " + player.getUser().getUsername() +": orientation: <" 
					+ orientation.get(0) + "," + orientation.get(1) + "," + orientation.get(2) + ">  Buttons: <" 
					+ buttons + ">");
		} else {
			log.info("Got command from " + player.getUser().getUsername() +": Buttons: <" 
					+ buttons + ">");
		}
		
		RobotProxy pairedRobot = getPairedRobot(player);
		
		// Generate a projectile if required
		if(model != null && buttons.contains("f")) {
			model.generateProjectile(pairedRobot.getRobot());
		}
		
		// If a game is in session and a robot is currently paired to the player
		// supplying input, generate a command and send it to the paired robot
		if(pairedRobot != null && model != null) {
			RobotCommand command = null;
			
			command = generateCommand(orientation, buttons, model.getControlType());
			log.info("Generated command: " + command);
			if(command != null && model.isValidCommand(command)) {
				log.info("Sending command: " + command);
				pairedRobot.sendCommand(command);
			}
		}
	}
	
	/**
	 * Compares two RobotCommands and returns the command with the higher priority
	 * @param commandFromUser	The first command to be compared
	 * @param commandFromModel	The second command to be compared
	 * @return	The command with the higher priority
	 */
	private RobotCommand comparePriority(RobotCommand commandFromUser, RobotCommand commandFromModel) {
		if(commandFromUser.getPriority() >= commandFromModel.getPriority())
			return commandFromUser;
		else
			return commandFromModel;
	}
	
	/**
	 * Generates a RobotCommand based on the passed orientation, buttons pressed and
	 * control scheme.
	 * @param orientation	The orientation of the client's gyroscope (3D Vector)
	 * @param buttons	The buttons pressed by the client
	 * @param controlType	The orientation of the client's device 
	 * 						3D Vector - <Azimuth, Pitch, Roll>. These values
	 * 						must be scaled to the range [-1, 1]
	 * @return	A valid RobotCommand, or null if no command should be issued.
	 */
	public RobotCommand generateCommand(Vector<Float> orientation, String buttons, 
			ControlType controlType) {
		if(buttons == null) {
			buttons = "";
		}
		
		switch(controlType) {
		case TANK:
			// Assume that button input always overrides tilt controls
			if(buttons.contains("w")) {
				return RobotCommand.moveContinuous(RobotCommand.MAX_SPEED);
			} else if (buttons.contains("a")) {
				return RobotCommand.rollingTurn(RobotCommand.MAX_SPEED, 200);
			} else if (buttons.contains("d")) {
				return RobotCommand.rollingTurn(RobotCommand.MAX_SPEED, -200);
			} else if (buttons.contains("s")) {
				return RobotCommand.stop();
			}
			
			// Scale vector input
			if(orientation != null && orientation.size() == 3) {
				if(orientation.get(1) > 1 || orientation.get(1) < -1
						|| orientation.get(2) < -1 || orientation.get(2) > 1) {
					return null;
				}
				
				float moveSpeed = orientation.get(1) * PITCH_SCALING_FACTOR;
				int turnRate = (int)(orientation.get(2) * ROLL_SCALING_FACTOR);
				
				if(moveSpeed < RobotCommand.MAX_SPEED / STOP_THRESHOLD 
						&& moveSpeed > -RobotCommand.MAX_SPEED / STOP_THRESHOLD) {
					return RobotCommand.stop();
				} else {
					return RobotCommand.rollingTurn(moveSpeed, turnRate);
				}
			}
			
		case SNAKE:
			if(buttons.contains("w")) {
				return RobotCommand.moveContinuous(RobotCommand.MAX_SPEED);
			} else if (buttons.contains("a")) {
				return RobotCommand.turnAngleLeft(90);
			} else if (buttons.contains("d")) {
				return RobotCommand.turnAngleRight(90);
			} else if (buttons.contains("s")) {
				return RobotCommand.stop();
			}
			break;
		default: 
			break;
		}
		
		return null;
	}
	
	/**
	 * Updates the position of a robot. This method should be called by a
	 * RobotProxy whenever it receives new position data from the remote robot.
	 * @param robot	The proxy providing the position data
	 * @param newPose	The new pose of the robot (position and heading)
	 */
	public void updateRobotPosition(RobotProxy robot, Posture newPosture) {
		log.debug("Got robot position update:\n\tRobot: " + robot.getIdentifier()
				+ "\n\tX: " + newPosture.getX() + "\tY: " + newPosture.getY() + "\tHeading: "
				+ newPosture.getHeading());
		
		if(model != null) {
			model.updateRobotPosition(robot.getIdentifier(), newPosture);
		}
	}
	
	/**
	 * @return	The GameModel managed by this GameController
	 */
	public GameModel getGameModel(){
		return model;
	}
	
	/**
	 * Sends updated game state to clients whenever the game state changes.
	 */
	public void gameStateChanged(GameEvent event){
		synchronized(controlPairs) {
			for(ControlPair p : controlPairs) {
				p.getUserProxy().sendEvent(event);
			}
		}
		synchronized(spectators) {
			for(UserProxy p : spectators) {
				p.sendEvent(event);
			}
		}
	}
}
