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
		assertEquals(testLobby.getServerName(), "Test Server");
		assertEquals(testLobby.getCurrentGameType(), GameType.getDefault());
		assertEquals(testLobby.getCurrentGame(), null);
	}

	@Test
	public void testAddLobbyStateListener() {
		TestLobbyListener testAdd = new TestLobbyListener();
		testLobby.addLobbyStateListener(testAdd);
		testLobby.addLobbyStateListener(testAdd);
		testLobby.addLobbyStateListener(null);
		testLobby.broadcastMessage("Test Chat");
		assertEquals(testAdd.getLastChatEvent().getMessage(), "Test Chat");
		assertEquals(testAdd.getNumEvents(), 1);
		testLobby.removeLobbyStateListener(testAdd);
	}

	@Test
	public void testRemoveLobbyStateListener() {
		TestLobbyListener testRemove = new TestLobbyListener();
		testLobby.addLobbyStateListener(testRemove);
		testLobby.removeLobbyStateListener(testRemove);
		testLobby.removeLobbyStateListener(null);
		testLobby.broadcastMessage("Test Chat");
		assertEquals(testRemove.getLastChatEvent(), null);
		assertEquals(testRemove.getNumEvents(), 0);
	}

	@Test
	public void testAddUser() {
		ArrayList<TestUserProxy> testUsers = new ArrayList<TestUserProxy>();
		for(int i = 0; i < TEST_MAX_PLAYERS + 1; i++) {
			TestUserProxy testUser = new TestUserProxy(testLobby);
			testUser.setUsername("TestUser" + Integer.toString(i));
			testUsers.add(testUser);
		}
		
		for(int i = 0; i < TEST_MAX_PLAYERS; i++) {
			testLobby.addUser(testUsers.get(i));
			assertEquals(testListener.getLastUserEvent().getUser(), 
					testUsers.get(i));
			assertEquals(testListener.getLastUserEvent().getEventType(),
					ServerLobbyEvent.EVENT_PLAYER_JOINED);
		}
		
		testListener.clearNumEvents();
		assertEquals(testLobby.addUser(testUsers.get(TEST_MAX_PLAYERS)), false);
		assertEquals(testLobby.addUser(null), false);
		assertEquals(testListener.getNumEvents(), 0);
		
		while(!testUsers.isEmpty()) {
			TestUserProxy testUser = testUsers.remove(0);
			testLobby.removeUser(testUser);
			testUser = null;
		}
	}

	@Test
	public void testRemoveUser() {
		TestUserProxy testUser = new TestUserProxy(testLobby);
		TestUserProxy testUser2 = new TestUserProxy(testLobby);
		testLobby.addUser(testUser);
		
		testListener.clearNumEvents();
		
		testLobby.removeUser(null);
		assertEquals(testListener.getNumEvents(), 0);
		testLobby.removeUser(testUser2);
		assertEquals(testListener.getNumEvents(), 0);
		testLobby.removeUser(testUser);
		assertEquals(testListener.getLastUserEvent().getUser(), testUser);
		assertEquals(testListener.getLastUserEvent().getEventType(),
				ServerLobbyEvent.EVENT_PLAYER_LEFT);
	}

	@Test
	public void testRegisterRobot() {
		ArrayList<RobotProxy> testRobots = new ArrayList<RobotProxy>();
		for(int i = 0; i < TEST_MAX_ROBOTS + 1; i++) {
			RobotProxy testRobot = new RobotProxy("Robot" + Integer.toString(i));
			testRobots.add(testRobot);
		}
		
		for(int i = 0; i < TEST_MAX_ROBOTS; i++) {
			testLobby.registerRobot(testRobots.get(i));
			assertEquals(testListener.getLastRobotEvent().getRobot(), 
					testRobots.get(i));
			assertEquals(testListener.getLastRobotEvent().getEventType(),
					ServerLobbyEvent.EVENT_ROBOT_REGISTERED);
		}
		
		testListener.clearNumEvents();
		assertEquals(testLobby.registerRobot(testRobots.get(TEST_MAX_ROBOTS)), false);
		assertEquals(testLobby.registerRobot(null), false);
		assertEquals(testListener.getNumEvents(), 0);
		
		while(!testRobots.isEmpty()) {
			RobotProxy testRobot = testRobots.remove(0);
			testLobby.unregisterRobot(testRobot);
			testRobot = null;
		}
	}

	@Test
	public void testUnregisterRobot() {
		RobotProxy testRobot = new RobotProxy("RegisteredRobot");
		RobotProxy testRobot2 = new RobotProxy("UnregisteredRobot");
		testLobby.registerRobot(testRobot);
		
		testListener.clearNumEvents();
		
		testLobby.removeUser(null);
		assertEquals(testListener.getNumEvents(), 0);
		testLobby.unregisterRobot(testRobot2);
		assertEquals(testListener.getNumEvents(), 0);
		testLobby.unregisterRobot(testRobot);
		assertEquals(testListener.getLastRobotEvent().getRobot(), testRobot);
		assertEquals(testListener.getLastRobotEvent().getEventType(),
				ServerLobbyEvent.EVENT_ROBOT_UNREGISTERED);
	}

	@Test
	public void testBroadcastMessage() {
		testLobby.broadcastMessage("Test Message");
		assertEquals(testListener.getNumEvents(), 1);
		assertEquals(testListener.getLastChatEvent().getMessage(), "Test Message");
		testListener.clearNumEvents();
		testLobby.broadcastMessage(null);
		assertEquals(testListener.getNumEvents(), 0);
	}

	@Test
	public void testBroadcastUserStateUpdate() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetGameType() {
		fail("Not yet implemented");
	}

	@Test
	public void testGameInProgress() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetCurrentGame() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetCurrentGameType() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetServerName() {
		assertEquals(testLobby.getServerName(), "TestServer");
	}

	@Test
	public void testLaunchGame() {
		fail("Not yet implemented");
	}

	@Test
	public void testClearCurrentGame() {
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
		public TestUserProxy(ServerLobby lobby) {
			super(null, lobby, null);
		}

		/**
		 * Overrides the actual sendMessage method to avoid using the
		 * network socket.
		 */
		public void sendMessage(String message) {}
	}
}
