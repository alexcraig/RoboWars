package robowars.server.controller;

import static org.junit.Assert.*;

import java.util.Vector;

import lejos.robotics.Pose;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import robowars.shared.model.CommandType;
import robowars.shared.model.ControlType;
import robowars.shared.model.GameEvent;
import robowars.shared.model.GameType;
import robowars.shared.model.RobotCommand;
import robowars.test.*;

/**
 * Unit tests for GameController
 * 
 * @author Alexander Craig
 */
public class GameControllerTest {
	
	private GameController testController;
	private ServerLobby testLobby;
	private TestUserProxy user1;
	private TestUserProxy user2;
	private TestRobotProxy robot1;
	private TestRobotProxy robot2;

	@Before
	public void setUp() throws Exception {
		testLobby = new ServerLobby("TestLobby", 10, 10);
		user1 = new TestUserProxy(testLobby, "TestUser1");
		user2 = new TestUserProxy(testLobby, "TestUser2");
		robot1 = new TestRobotProxy(testLobby, "TestRobot1");
		robot2 = new TestRobotProxy(testLobby, "TestRobot2");
		testController = new GameController(testLobby, GameType.FREETEST);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGameController() {
		// Ensure the correct GameModel is generated
		testController = new GameController(testLobby, GameType.FREETEST);
		assertEquals(GameType.FREETEST, testController.getGameModel().getGameType());
		
		testController = new GameController(testLobby, GameType.TANK_SIMULATION);
		assertEquals(GameType.TANK_SIMULATION, testController.getGameModel().getGameType());
		
		testController = new GameController(testLobby, GameType.LIGHTCYCLES);
		assertEquals(GameType.LIGHTCYCLES, testController.getGameModel().getGameType());
	}

	@Test
	public void testPlayersRobots() {
		// Tests methods:
		// addPlayer, isPlayer, isActiveRobot, getPairedRobot
		
		assertEquals(false, testController.isActiveRobot(robot1));
		assertEquals(false, testController.isPlayer(user1));
		assertEquals(null, testController.getPairedRobot(user1));
		
		testController.addPlayer(user1, robot1);
		assertEquals(true, testController.isPlayer(user1));
		assertEquals(false, testController.isSpectator(user1));
		assertEquals(robot1, testController.getPairedRobot(user1));
		assertEquals(true, testController.isActiveRobot(robot1));
		assertEquals(robot1.getRobot(), testController.getGameModel().getGameRobot(robot1.getIdentifier()));
		
		// Ensure that null robots or players are never added
		testController.addPlayer(user2, null);
		assertEquals(false, testController.isPlayer(user2));
		assertEquals(false, testController.isSpectator(user2));
		assertEquals(false, testController.isActiveRobot(null));
		assertEquals(null, testController.getPairedRobot(user2));
		
		testController.addPlayer(null, robot2);
		assertEquals(null, testController.getPairedRobot(null));
		assertEquals(false, testController.isActiveRobot(robot2));
		assertEquals(false, testController.isPlayer(null));
	}

	@Test
	public void testSpectators() {
		// Tests methods:
		// addSpectator, isSpectator
		
		testController.addSpectator(user1);
		assertEquals(true, testController.isSpectator(user1));
		
		// Ensure the player is not registered as a player
		assertEquals(false, testController.isPlayer(user1));
		
		testController.addSpectator(null);
		assertEquals(false, testController.isSpectator(null));
	}

	@Test
	public void testRun() {
		testController.addPlayer(user1, robot1);
		testController.addSpectator(user2);
		robot1.sendCommand(RobotCommand.setPosition(new Pose(2000, 2000, 0)));
		
		// Launch the game
		new Thread(testController).start();
		
		// Ensure game events are being propagated when robot state changes
		robot1.sendCommand(RobotCommand.moveContinuous(5));
		// Give the robot proxy time to report the position change
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		assertEquals(true, user1.getLastEvent() instanceof GameEvent);
		robot1.sendCommand(RobotCommand.stop());
		
		// Ensure that commands from the user are propagated to robots correctly
		user1.processGameplayCommand((float)0, (float)0, (float)0, "w");
		// Give the robot proxy time to receive the command
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		assertEquals(CommandType.MOVE_CONTINUOUS, robot1.getRobot().getLastCommand().getType());
		robot1.sendCommand(RobotCommand.stop());
		
		// Ensure termination works
		testController.triggerTermination();
		assertEquals(true, testController.isTerminating());
		// Give the game controller time to terminate
		try {
			Thread.sleep(1500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		assertEquals(null, testController.getGameModel());
	}

	@Test
	public void testGenerateCommand() {
		RobotCommand genCommand;
		Vector<Float> orientation = new Vector<Float>();
		orientation.add((float)0);
		orientation.add((float)0);
		
		// Test SNAKE control type
		ControlType type = ControlType.SNAKE;
		genCommand = testController.generateCommand(null, "", type);
		assertEquals(null, genCommand);
		genCommand = testController.generateCommand(null, "w", type);
		assertEquals(CommandType.MOVE_CONTINUOUS, genCommand.getType());
		// Ensure invalid orientation is ignored
		genCommand = testController.generateCommand(orientation, "a", type);
		assertEquals(CommandType.TURN_ANGLE_LEFT, genCommand.getType());
		// Ensure valid orientation is ignored
		orientation.add((float)1);
		genCommand = testController.generateCommand(orientation, "d", type);
		assertEquals(CommandType.TURN_ANGLE_RIGHT, genCommand.getType());
		genCommand = testController.generateCommand(null, "s", type);
		assertEquals(CommandType.STOP, genCommand.getType());
		genCommand = testController.generateCommand(null, "wa", type);
		assertEquals(CommandType.MOVE_CONTINUOUS, genCommand.getType());
		
		// Test TANK control type
		type = ControlType.TANK;
		orientation = new Vector<Float>();
		orientation.add((float)0);
		
		// Ensure buttons commands override orientation
		genCommand = testController.generateCommand(orientation, "w", type);
		assertEquals(CommandType.MOVE_CONTINUOUS, genCommand.getType());
		genCommand = testController.generateCommand(orientation, "a", type);
		assertEquals(CommandType.ROLLING_TURN, genCommand.getType());
		assertEquals(200, genCommand.getTurnBearing(), 0.05);
		genCommand = testController.generateCommand(orientation, "d", type);
		assertEquals(CommandType.ROLLING_TURN, genCommand.getType());
		assertEquals(-200, genCommand.getTurnBearing(), 0.05);
		genCommand = testController.generateCommand(orientation, "s", type);
		assertEquals(CommandType.STOP, genCommand.getType());
		
		// Ensure invalid orientation is ignored
		genCommand = testController.generateCommand(orientation, "", type);
		assertEquals(null, genCommand);
		orientation.add((float)2);
		orientation.add((float)0);
		genCommand = testController.generateCommand(orientation, "", type);
		assertEquals(null, genCommand);
		orientation.remove(1);
		orientation.add((float)1);
		orientation.add((float)1);
		genCommand = testController.generateCommand(orientation, "", type);
		assertEquals(null, genCommand);
		
		// Ensure valid orientations are processed correctly (with null
		// or non-null button string)
		orientation = new Vector<Float>();
		orientation.add((float)0);
		orientation.add((float)0);
		orientation.add((float)0);
		genCommand = testController.generateCommand(orientation, "", type);
		assertEquals(CommandType.STOP, genCommand.getType());
		orientation.remove(1);
		orientation.add(1, (float)1);
		genCommand = testController.generateCommand(orientation, null, type);
		assertEquals(CommandType.ROLLING_TURN, genCommand.getType());
		assertEquals(RobotCommand.MAX_SPEED, genCommand.getThrottle(), 0.05);
	}
}
