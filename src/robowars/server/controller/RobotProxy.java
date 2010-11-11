package robowars.server.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Vector;

import org.apache.log4j.Logger;

import robowars.shared.model.CommandType;
import robowars.shared.model.GameRobot;
import robowars.shared.model.RobotCommand;

import lejos.pc.comm.NXTComm;
import lejos.pc.comm.NXTCommException;
import lejos.pc.comm.NXTCommFactory;
import lejos.pc.comm.NXTInfo;

/**
 * Manages communications with a single connected NXT robot.
 */
public class RobotProxy {
	/** The logger used by this class */
	private static Logger log = Logger.getLogger(RobotProxy.class);
	
	/** The NXTComm object to use to initiate communication with the robot. */
	NXTComm nxtComm;
	
	/** Stream for robot output */
	private OutputStream outputStream;
	
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
	
	/**
	 * Generates a new robot proxy
	 * @param identifier	A string identifier for this robot
	 */
	public RobotProxy(ServerLobby lobby, NXTInfo nxtInfo) {
		// Use test sizes for now, actual dimensions should probably be sent
		// by the robot
		this.lobby = lobby;
		robot = new GameRobot(50, 50, nxtInfo.name);
		controller = null;
		nxtComm = null;
		outputStream = null;
		
		openConnection(nxtInfo);
	}
	
	private void openConnection(NXTInfo nxtInfo) {
		try {
			nxtComm = NXTCommFactory.createNXTComm(NXTCommFactory.BLUETOOTH);
			nxtComm.open(nxtInfo);
		} catch (NXTCommException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		outputStream = nxtComm.getOutputStream();
		lobby.registerRobot(this);
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
	 * @return	The identifier of the robot (either the "friendly" NXT
	 * name or the MAC address of the robot.
	 */
	public String getIdentifier() {
		return robot.getRobotId();
	}
	
	/**
	 * Sends a serialized RobotCommand to the connected robot through Bluetooth
	 * @param command	The RobotCommand to send
	 */
	public void sendCommand(RobotCommand command) {
		if(outputStream != null) {
			synchronized(outputStream) {
				try {
					switch(command.getType()) {
					case MOVE_CONTINUOUS:
						outputStream.write(1);
						outputStream.flush(); break;
					case TURN_RIGHT_ANGLE_LEFT:
						outputStream.write(3);
						outputStream.flush(); break;
					case TURN_RIGHT_ANGLE_RIGHT:
						outputStream.write(4);
						outputStream.flush(); break;
					case STOP:
						outputStream.write(2);
						outputStream.flush(); break;
					}
				} catch (IOException e) {
					log.error("Could not write to robot output stream.");
				}
			}
		}
	}
}
