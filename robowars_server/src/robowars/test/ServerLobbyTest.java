package robowars.test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.ArrayList;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import robowars.server.controller.LobbyChatEvent;
import robowars.server.controller.LobbyGameEvent;
import robowars.server.controller.LobbyRobotEvent;
import robowars.server.controller.LobbyUserEvent;
import robowars.server.controller.RobotProxy;
import robowars.server.controller.ServerLobby;
import robowars.server.controller.ServerLobbyEvent;
import robowars.server.controller.ServerLobbyListener;
import robowars.server.controller.UserProxy;
import robowars.shared.model.GameType;
import robowars.shared.model.User;

public class ServerLobbyTest {
	public static int TEST_MAX_PLAYERS = 4;
	public static int TEST_MAX_ROBOTS = 4;
	ServerLobby testLobby;
	TestLobbyListener testListener;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
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
			RobotProxy testRobot = new TestRobotProxy(testLobby, "Robot" + Integer.toString(i));
			testRobots.add(testRobot);
		}
		
		// Test valid registration
		for(int i = 0; i < TEST_MAX_ROBOTS; i++) {
			testLobby.registerRobot(testRobots.get(i));
			assertEquals(testRobots.get(i), 
					testListener.getLastRobotEvent().getRobot());
			assertEquals(ServerLobbyEvent.EVENT_ROBOT_REGISTERED,
					testListener.getLastRobotEvent().getEventType());
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
		testLobby.registerRobot(testRobot);
		
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
		
		assertEquals(testLobby.gameInProgress(), false);
		
		testLobby.launchGame();
		
		assertEquals(testLobby.gameInProgress(), true);
		
		testLobby.endCurrentGame();
		
		assertEquals(testLobby.gameInProgress(), false);
		
		testLobby.removeUserProxy(user);
		testLobby.unregisterRobot(robot);
		
	}

	@Test
	public void testGetCurrentGame() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetCurrentGameType() {
		assertEquals(GameType.getDefault(), testLobby.getCurrentGameType());
		
	}

	@Test
	public void testGetServerName() {
		assertEquals(testLobby.getServerName(), "Test Server");
	}

	@Test
	public void testLaunchGame() {
		fail("Not yet implemented");
	}

	@Test
	public void testEndCurrentGame() {
	}

	@Test
	public void testServerShutdown() {
		fail("Not yet implemented");
	}

	/**
	 * Testing listener to capture events generated by a ServerLobby
	 */
	private class TestLobbyListener implements ServerLobbyListener {
		private LobbyUserEvent lastUserEvent;
		private LobbyRobotEvent lastRobotEvent;
		private LobbyGameEvent lastGameEvent;
		private LobbyChatEvent lastChatEvent;
		private int numEvents;

		public TestLobbyListener() {
			lastUserEvent = null;
			lastRobotEvent = null;
			lastGameEvent = null;
			lastChatEvent = null;
			numEvents = 0;
		}
		
		public int getNumEvents() {
			return numEvents;
		}

		public void clearNumEvents() {
			numEvents = 0;
		}

		@Override
		public void userStateChanged(LobbyUserEvent event) {
			lastUserEvent = event;
			numEvents++;
		}

		@Override
		public void robotStateChanged(LobbyRobotEvent event) {
			lastRobotEvent = event;
			numEvents++;
		}

		@Override
		public void lobbyGameStateChanged(LobbyGameEvent event) {
			lastGameEvent = event;
			numEvents++;
		}

		@Override
		public void lobbyChatMessage(LobbyChatEvent event) {
			lastChatEvent = event;
			numEvents++;
		}
		
		public LobbyUserEvent getLastUserEvent() {
			return lastUserEvent;
		}

		public LobbyRobotEvent getLastRobotEvent() {
			return lastRobotEvent;
		}

		public LobbyGameEvent getLastGameEvent() {
			return lastGameEvent;
		}

		public LobbyChatEvent getLastChatEvent() {
			return lastChatEvent;
		}
	}
	
	/**
	 * Testing user proxy with network components disabled.
	 */
	private class TestUserProxy extends UserProxy {
		public TestUserProxy(ServerLobby lobby, String username) {
			super(null, lobby, null);
			setUser(new User(username));
		}

		/**
		 * Overrides the actual sendMessage method to avoid using the
		 * network socket.
		 */
		public void sendMessage(String message) {}
	}
}
