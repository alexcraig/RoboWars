package robowars.server.controller;


import java.io.IOException;

import org.apache.log4j.Logger;

import robowars.robot.LejosInputStream;
import robowars.robot.LejosOutputStream;
import robowars.shared.model.CommandType;
import robowars.shared.model.GameRobot;
import robowars.shared.model.RobotCommand;
import robowars.shared.model.RobotMap;

import lejos.pc.comm.NXTComm;
import lejos.pc.comm.NXTCommException;
import lejos.pc.comm.NXTCommFactory;
import lejos.pc.comm.NXTInfo;
import lejos.robotics.Pose;

/**
 * Manages communications with a single connected NXT robot.
 */
public class RobotProxy {
	/** The logger used by this class */
	private static Logger log = Logger.getLogger(RobotProxy.class);
	
	/** The NXTComm object to use to initiate communication with the robot. */
	private NXTComm nxtComm;
	
	/** 
	 * Object used as a lock to ensure the input and output streams are never
	 * used at the same time.
	 */
	private Object ioLock;
	
	/** Stream for robot output */
	private LejosOutputStream outputStream;
	
	/** Stream for robot input (needs a separate thread to continually read) */
	private LejosInputStream inputStream;
	
	/** The server lobby that the robot should register with. */
	private ServerLobby lobby;
	
	/** 
	 * The GameController object managing the game this proxy is
	 * participating in (null if no game is in progress).
	 */
	private GameController controller;
	
	/**
	 * The GameRobot instance that stores details on the robot managed
	 * by this proxy.
	 */
	private GameRobot robot;
	
	int commandSent=0;
	
	/**
	 * Generates a new robot proxy
	 * @param identifier	A string identifier for this robot
	 */
	public RobotProxy(ServerLobby lobby, NXTInfo nxtInfo) {
		this.lobby = lobby;
		ioLock = new Object();
		
		// Use test sizes for now, actual dimensions should probably be sent
		// by the robot
		robot = (new GameRobot(nxtInfo.name));
		
		controller = null;
		nxtComm = null;
		outputStream = null;
		
		openConnection(nxtInfo);
	}

	/**
	 * Opens a connection to the remote robot using the provided NXTInfo
	 * object.
	 * @param nxtInfo	An NXTInfo object defining the robot to be connected
	 * 					to (usually from a call to search on an NXTComm object)
	 */
	public void openConnection(NXTInfo nxtInfo) {
		// Open a Bluetooth connection to the specified NXT robot, and
		// open to output stream to write to the robot
		try {
			nxtComm = NXTCommFactory.createNXTComm(NXTCommFactory.BLUETOOTH);
			nxtComm.open(nxtInfo);
		} catch (NXTCommException e) {
			log.error("Error opening a connection to robot: " + getIdentifier());
			return;
		}
		
		outputStream = new LejosOutputStream(nxtComm.getOutputStream());
		inputStream = new LejosInputStream(nxtComm.getInputStream());
		new Thread(new PositionReader()).start();
		
		// Register the robot with the server lobby
		lobby.registerRobot(this);
	}
	
	/**
	 * @return	The lobby that the RobotProxy is associated with.
	 */
	public ServerLobby getServerLobby() {
		return lobby;
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
	 * @return	The GameController for the currently active game, or
	 * 			null if no game is active.
	 */
	public GameController getGameController() {
		return controller;
	}
	
	/**
	 * @return	The identifier of the robot (either the "friendly" NXT
	 * name or the MAC address of the robot.
	 */
	public String getIdentifier() {
		return getRobot().getRobotId();
	}
	
	/**
	 * Sends a serialized RobotCommand to the connected robot through Bluetooth
	 * @param command	The RobotCommand to send
	 */
	public void sendCommand(RobotCommand command) {
		if(outputStream != null) {
			try {
				//synchronized(ioLock) {
					outputStream.writeObject(command);
					getRobot().setLastCommand(command);
					log.info("Wrote to robot: " + getIdentifier() + " - " + command.toString()+" "+commandSent++);
				//}
			} catch (IOException e) {
				log.error("Error writing command to robot: " + getIdentifier());
				return;
			}
		} else {
			log.error("Attempted to send command to robot: " + getIdentifier()
					+ ", but output stream is null.");
			return;
		}
		
		if(command.getType() == CommandType.SET_POSITION) {
			getRobot().setPose(command.getPos());
		}
	}

	/**
	 * @return The GameRobot object representing the robot that the proxy
	 * is managing communication with.
	 */
	public GameRobot getRobot() {
		return robot;
	}

	/**
	 * A thread which continually reads the input stream from the robot
	 * and propagates position updates to the GameRobot and
	 * GameController.
	 */
	private class PositionReader implements Runnable {
		@Override
		public void run() {
			Object readObj = null;
            
            try {
				while (true) {
				    
					//synchronized(ioLock) {
						readObj = inputStream.readObject();
					//}
					
					if(readObj == null) break;
					
				    if (readObj instanceof Pose) {
				    	Pose newPos = (Pose)readObj;

				    	if(controller != null) {
				    		// If the robot is currently active in a game,
					    	// send the position update through the game
				    		// controller...
				    		controller.updateRobotPosition(RobotProxy.this, newPos);
				    	} else { 
				    		// ... otherwise, directly update the game
				    		// robot instance (position updates to inactive
				    		// robots do not need to be propagated and do
				    		// not need to pass through the game controller)
				    		getRobot().setPose(newPos);
				    	}
				    }
				}
				
			} catch (IOException e) {
				e.printStackTrace();
			}  finally {
				log.info("Closing input stream from robot: " + getIdentifier());
				try {
					inputStream.close();
				} catch (IOException e) {
					log.error("Error closing input stream from robot: " + getIdentifier());
				}
			}
		}
	}

	public void sendMap(RobotMap map) {
		try {
			outputStream.writeObject(map);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
