package robowars.server.controller;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.apache.log4j.PropertyConfigurator;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import robowars.shared.model.GameType;
import robowars.test.TestLobbyListener;
import robowars.test.TestRobotProxy;
import robowars.test.TestUserProxy;

public class ServerLobbyTest {
	public static int TEST_MAX_PLAYERS = 4;
	public static int TEST_MAX_ROBOTS = 4;
	ServerLobby testLobby;
	TestLobbyListener testListener;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		// Use log4j config file "log_config.properties"
		PropertyConfigurator.configure("config/log_config.properties");
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		testLobby = new ServerLobby("Test Server", TEST_MAX_ROBOTS, 
				TEST_MAX_PLAYERS);
		testListener = new TestLobbyListener();
		testLobby.addLobbyStateListener(testListener);
	}

	@After
	public void tearDown() throws Exception {
		testLobby.removeLobbyStateListener(testListener);
		testLobby = null;
		testListener = null;
	}

	@Test
	public void testServerLobby() {
		// Ensure construction conditions hold true
		assertEquals("Test Server", testLobby.getServerName());
		assertEquals(GameType.getDefault(), testLobby.getCurrentGameType());
		assertEquals(null, testLobby.getCurrentGame());
	}

	@Test
	public void testAddLobbyStateListener() {
		TestLobbyListener testAdd = new TestLobbyListener();
		
		// Test double and null addition
		testLobby.addLobbyStateListener(testAdd);
		testLobby.addLobbyStateListener(testAdd);
		testLobby.addLobbyStateListener(null);
		
		testLobby.broadcastMessage("Test Chat");
		
		// Check that chat message event was received properly
		assertEquals("Test Chat", testAdd.getLastChatEvent().getMessage());
		assertEquals(1, testAdd.getNumEvents());
		testLobby.removeLobbyStateListener(testAdd);
	}

	@Test
	public void testRemoveLobbyStateListener() {
		TestLobbyListener testRemove = new TestLobbyListener();
		testLobby.addLobbyStateListener(testRemove);
		
		// Test valid removal
		testLobby.removeLobbyStateListener(testRemove);
		
		// Test null and invalid removal
		testLobby.removeLobbyStateListener(null);
		testLobby.removeLobbyStateListener(testRemove);
		
		// Make sure listener was removed (no events received)
		testLobby.broadcastMessage("Test Chat");
		assertEquals(null, testRemove.getLastChatEvent());
		assertEquals(0, testRemove.getNumEvents());
	}

	@Test
	public void testAddUserProxy() {
		ArrayList<TestUserProxy> testUsers = new ArrayList<TestUserProxy>();
		for(int i = 0; i < TEST_MAX_PLAYERS + 1; i++) {
			TestUserProxy testUser = new TestUserProxy(testLobby,
					"TestUser" + Integer.toString(i));
			testUsers.add(testUser);
		}
		
		// Test valid additions
		for(int i = 0; i < TEST_MAX_PLAYERS; i++) {
			testLobby.addUserProxy(testUsers.get(i));
			assertEquals(testUsers.get(i).getUser(), 
					testListener.getLastUserEvent().getUser());
			assertEquals(ServerLobbyEvent.EVENT_PLAYER_JOINED,
					testListener.getLastUserEvent().getEventType());
		}
		
		// Test addition over maximum and null addition
		testListener.clearNumEvents();
		assertEquals(false, testLobby.addUserProxy(testUsers.get(TEST_MAX_PLAYERS)));
		assertEquals(false, testLobby.addUserProxy(null));
		assertEquals(0, testListener.getNumEvents());
		
		while(!testUsers.isEmpty()) {
			TestUserProxy testUser = testUsers.remove(0);
			testLobby.removeUserProxy(testUser);
			testUser = null;
		}
	}

	@Test
	public void testRemoveUser() {
		TestUserProxy testUser = new TestUserProxy(testLobby, "User1");
		TestUserProxy testUser2 = new TestUserProxy(testLobby, "User2");
		testLobby.addUserProxy(testUser);
		
		testListener.clearNumEvents();
		
		// Test null removal
		testLobby.removeUserProxy(null);
		assertEquals(0, testListener.getNumEvents());
		
		// Test removal of unregistered user
		testLobby.removeUserProxy(testUser2);
		assertEquals(0, testListener.getNumEvents());
		
		// Test valid removal
		testLobby.removeUserProxy(testUser);
		assertEquals(testUser.getUser(), 
				testListener.getLastUserEvent().getUser());
		assertEquals(ServerLobbyEvent.EVENT_PLAYER_LEFT, 
				testListener.getLastUserEvent().getEventType());
	}

	@Test
	public void testRegisterRobot() {
		ArrayList<RobotProxy> testRobots = new ArrayList<RobotProxy>();
		for(int i = 0; i < TEST_MAX_ROBOTS + 1; i++) {
			RobotProxy testRobot = new TestRobotProxy(testLobby, "RegisterRobot" + Integer.toString(i));
			testRobots.add(testRobot);
			// Test valid registration (test proxies call register upon creation)
			if(i < TEST_MAX_ROBOTS) {
				assertEquals(testRobots.get(i), 
						testListener.getLastRobotEvent().getRobot());
				assertEquals(ServerLobbyEvent.EVENT_ROBOT_REGISTERED,
						testListener.getLastRobotEvent().getEventType());
			}
		}
		
		// Test registration over maximum and null registration
		testListener.clearNumEvents();
		assertEquals(false, testLobby.registerRobot(testRobots.get(TEST_MAX_ROBOTS)));
		assertEquals(false, testLobby.registerRobot(null));
		assertEquals(0, testListener.getNumEvents());
		
		while(!testRobots.isEmpty()) {
			RobotProxy testRobot = testRobots.remove(0);
			testLobby.unregisterRobot(testRobot);
			testRobot = null;
		}
	}

	@Test
	public void testUnregisterRobot() {
		RobotProxy testRobot = new TestRobotProxy(testLobby, "RegisteredRobot");
		RobotProxy testRobot2 = new TestRobotProxy(testLobby, "UnregisteredRobot");
		testLobby.unregisterRobot(testRobot2);
		
		testListener.clearNumEvents();
		
		// Test null removal
		testLobby.removeUserProxy(null);
		assertEquals(0, testListener.getNumEvents());
		
		// Test removal of unregistered robot
		testLobby.unregisterRobot(testRobot2);
		assertEquals(0, testListener.getNumEvents());
		
		// Test valid removal
		testLobby.unregisterRobot(testRobot);
		assertEquals(testRobot, testListener.getLastRobotEvent().getRobot());
		assertEquals(ServerLobbyEvent.EVENT_ROBOT_UNREGISTERED, 
				testListener.getLastRobotEvent().getEventType());
	}

	@Test
	public void testBroadcastMessage() {
		// Test valid chat message
		testLobby.broadcastMessage("Test Message");
		assertEquals(1, testListener.getNumEvents());
		assertEquals("Test Message", testListener.getLastChatEvent().getMessage());
		testListener.clearNumEvents();
		
		// Test null broadcast
		testLobby.broadcastMessage(null);
		assertEquals(0, testListener.getNumEvents());
	}

	@Test
	public void testBroadcastUserStateUpdate() {
		TestUserProxy testUser = new TestUserProxy(testLobby, "Test User");
		
		// Test state update of unregistered user (should be no event generated)
		testLobby.broadcastUserStateUpdate(testUser);
		assertEquals(0, testListener.getNumEvents());
		
		// Test null user
		testLobby.broadcastUserStateUpdate(null);
		assertEquals(0, testListener.getNumEvents());
		
		// Test valid state update
		testLobby.addUserProxy(testUser);
		testLobby.broadcastUserStateUpdate(testUser);
		assertEquals(testUser.getUser(), testListener.getLastUserEvent().getUser());
	}

	@Test
	public void testSetGameType() {
		// Test valid game types
		testLobby.setGameType(GameType.TANK_SIMULATION);
		assertEquals(GameType.TANK_SIMULATION, testLobby.getCurrentGameType());
		testLobby.setGameType(GameType.LIGHTCYCLES);
		assertEquals(GameType.LIGHTCYCLES, testLobby.getCurrentGameType());
		
		// Test null game type
		testLobby.setGameType(null);
		assertEquals(GameType.LIGHTCYCLES, testLobby.getCurrentGameType());
	}

	@Test
	public void testGameInProgress() {
		TestUserProxy user = new TestUserProxy(testLobby, "Test User");
		user.getUser().setReady(true);
		RobotProxy robot = new TestRobotProxy(testLobby, "test:mac:addr");
		
		testLobby.addUserProxy(user);
		testLobby.registerRobot(robot);
		
		assertEquals(false, testLobby.gameInProgress());
		
		testLobby.launchGame();
		
		assertEquals(true, testLobby.gameInProgress());
		
		testLobby.endCurrentGame();
		
		// Add a delay to ensure termination completes (separate thread)
		try { Thread.sleep(200); } catch (InterruptedException e) {
			e.printStackTrace();
		}
		assertEquals(false, testLobby.gameInProgress());
		
		testLobby.removeUserProxy(user);
		testLobby.unregisterRobot(robot);
	}

	@Test
	public void testGetCurrentGame() {
		TestUserProxy user = new TestUserProxy(testLobby, "Test User");
		user.getUser().setReady(true);
		RobotProxy robot = new TestRobotProxy(testLobby, "test:mac:addr");
		
		testLobby.addUserProxy(user);
		testLobby.registerRobot(robot);
		
		assertEquals(null, testLobby.getCurrentGame());
		
		testLobby.launchGame();
		
		assertTrue(testLobby.getCurrentGame() != null);
		
		testLobby.endCurrentGame();
		
		// Add a delay to ensure termination completes (separate thread)
		try { Thread.sleep(200); } catch (InterruptedException e) {
			e.printStackTrace();
		}
		assertEquals(null, testLobby.getCurrentGame());
		
		testLobby.removeUserProxy(user);
		testLobby.unregisterRobot(robot);
	}

	@Test
	public void testGetCurrentGameType() {
		assertEquals(GameType.getDefault(), testLobby.getCurrentGameType());
		testLobby.setGameType(GameType.FREETEST);
		assertEquals(GameType.FREETEST, testLobby.getCurrentGameType());
		testLobby.setGameType(GameType.LIGHTCYCLES);
		assertEquals(GameType.LIGHTCYCLES, testLobby.getCurrentGameType());
	}

	@Test
	public void testGetServerName() {
		assertEquals("Test Server", testLobby.getServerName());
	}
	
	@Test
	public void testGetRobotProxy() {
		RobotProxy testRobot = new TestRobotProxy(testLobby, "Registered");
		assertEquals(null, testLobby.getRobotProxy("NotRegistered"));
		assertEquals(testRobot, testLobby.getRobotProxy("Registered"));
		assertEquals(null, testLobby.getRobotProxy(null));
	}
	
	@Test
	public void testIsUsernameRegistered() {
		TestUserProxy user = new TestUserProxy(testLobby, "RegisteredName");
		testLobby.addUserProxy(user);
		
		assertEquals(true, testLobby.isUsernameRegistered("RegisteredName"));
		assertEquals(false, testLobby.isUsernameRegistered("NotRegistered"));
		assertEquals(false, testLobby.isUsernameRegistered(null));
	}

	@Test
	public void testLaunchGame() {
		TestUserProxy user = new TestUserProxy(testLobby, "Test User");
		testLobby.addUserProxy(user);
		user.getUser().setReady(true);
		testLobby.setGameType(GameType.FREETEST);
		
		// Assert that game will not start with no robot registered
		testLobby.launchGame();
		assertEquals(false, testLobby.gameInProgress());
		
		// Assert that the game will not start with no readied players
		RobotProxy robot = new TestRobotProxy(testLobby, "test:mac:addr");
		user.getUser().setReady(false);
		testLobby.launchGame();
		assertEquals(false, testLobby.gameInProgress());
		
		// Assert that the game will not start with less than the minimum
		// players connected
		testLobby.setGameType(GameType.TANK_SIMULATION);
		RobotProxy robot2 = new TestRobotProxy(testLobby, "test:mac:addr2");
		user.getUser().setReady(true);
		testLobby.launchGame();
		assertEquals(false, testLobby.gameInProgress());
		
		// Assert that the game will not start when not all upcoming players are
		// readied
		TestUserProxy user2 = new TestUserProxy(testLobby, "Test User 2");
		testLobby.addUserProxy(user2);
		user2.getUser().setReady(false);
		testLobby.launchGame();
		assertEquals(false, testLobby.gameInProgress());
		
		// Assert that game will not start if a pure spectator would be
		// required to play
		user2.getUser().setReady(true);
		user2.getUser().setPureSpectator(true);
		testLobby.launchGame();
		assertEquals(false, testLobby.gameInProgress());
		
		
		// Assert that game starts when all players are ready and no players
		// are spectators, and that a game launch event was generated
		user2.getUser().setPureSpectator(false);
		testLobby.launchGame();
		assertEquals(true, testLobby.gameInProgress());
		assertEquals(ServerLobbyEvent.EVENT_GAME_LAUNCH,
				testListener.getLastGameEvent().getEventType());

		testLobby.endCurrentGame();
	}

	@Test
	public void testEndCurrentGame() {
		TestUserProxy user = new TestUserProxy(testLobby, "Test User");
		user.getUser().setReady(true);
		RobotProxy robot = new TestRobotProxy(testLobby, "test:mac:addr");
		
		testLobby.addUserProxy(user);
		testLobby.registerRobot(robot);
		testLobby.launchGame();
		
		// Ensure a game is running
		assertTrue(testLobby.getCurrentGame() != null);
		
		testLobby.endCurrentGame();
		// Add a delay to ensure termination completes (separate thread)
		try { Thread.sleep(200); } catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		// Ensure the game terminated, and that a game termination event was
		// generated
		assertEquals(null, testLobby.getCurrentGame());
		assertEquals(ServerLobbyEvent.EVENT_GAME_OVER,
				testListener.getLastGameEvent().getEventType());	
	}
}
