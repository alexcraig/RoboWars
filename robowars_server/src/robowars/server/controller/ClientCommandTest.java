package robowars.server.controller;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class ClientCommandTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testParse() {
		ClientCommand resultCmd;
		
		// Test launch command
		resultCmd = ClientCommand.parse("l");
		assertEquals(ClientCommand.LAUNCH_GAME, resultCmd.getCommandType());
		
		// Test quit command
		resultCmd = ClientCommand.parse("q");
		assertEquals(ClientCommand.DISCONNECT, resultCmd.getCommandType());
		
		// Test ready commands
		resultCmd = ClientCommand.parse("r:t");
		assertEquals(ClientCommand.READY_STATUS, resultCmd.getCommandType());
		assertEquals(true, resultCmd.getBoolData());
		resultCmd = ClientCommand.parse("r:f");
		assertEquals(ClientCommand.READY_STATUS, resultCmd.getCommandType());
		assertEquals(false, resultCmd.getBoolData());
		resultCmd = ClientCommand.parse("r:q");
		assertEquals(null, resultCmd);
		
		// Test spectator commands
		resultCmd = ClientCommand.parse("s:t");
		assertEquals(ClientCommand.SPECTATOR_STATUS, resultCmd.getCommandType());
		assertEquals(true, resultCmd.getBoolData());
		resultCmd = ClientCommand.parse("s:f");
		assertEquals(ClientCommand.SPECTATOR_STATUS, resultCmd.getCommandType());
		assertEquals(false, resultCmd.getBoolData());
		resultCmd = ClientCommand.parse("s:q");
		assertEquals(null, resultCmd);
		
		// Test gametype command
		resultCmd = ClientCommand.parse("g:Free Test");
		assertEquals(ClientCommand.GAME_TYPE_CHANGE, resultCmd.getCommandType());
		assertEquals("Free Test", resultCmd.getStringData());
		resultCmd = ClientCommand.parse("g:Tank Simulation");
		assertEquals(ClientCommand.GAME_TYPE_CHANGE, resultCmd.getCommandType());
		assertEquals("Tank Simulation", resultCmd.getStringData());
		
		// Test chat message
		resultCmd = ClientCommand.parse("m:Test Chat:Test");
		assertEquals(ClientCommand.CHAT_MESSAGE, resultCmd.getCommandType());
		assertEquals("Test Chat:Test", resultCmd.getStringData());
		
		// Test gameplay commands
		// Only orientation vector provided
		resultCmd = ClientCommand.parse("c:<1,2,3>");
		assertEquals(ClientCommand.GAMEPLAY_COMMAND, resultCmd.getCommandType());
		assertEquals(1, resultCmd.getAzimuth(), 0.05);
		assertEquals(2, resultCmd.getPitch(), 0.05);
		assertEquals(3, resultCmd.getRoll(), 0.05);
		
		// Only buttons provided
		resultCmd = ClientCommand.parse("c:wasd");
		assertEquals(ClientCommand.GAMEPLAY_COMMAND, resultCmd.getCommandType());
		assertEquals(0, resultCmd.getAzimuth(), 0.05);
		assertEquals(0, resultCmd.getPitch(), 0.05);
		assertEquals(0, resultCmd.getRoll(), 0.05);
		assertEquals("wasd", resultCmd.getStringData());
		
		// Both provided
		resultCmd = ClientCommand.parse("c:<1,2,3>wasd");
		assertEquals(ClientCommand.GAMEPLAY_COMMAND, resultCmd.getCommandType());
		assertEquals(1, resultCmd.getAzimuth(), 0);
		assertEquals(2, resultCmd.getPitch(), 0);
		assertEquals(3, resultCmd.getRoll(), 0);
		assertEquals("wasd", resultCmd.getStringData());
		
		// Invalid cases
		resultCmd = ClientCommand.parse("c:<1,2a,3>");
		assertEquals(null, resultCmd);
		resultCmd = ClientCommand.parse("unprefixed chat message");
		assertEquals(null, resultCmd);
	}

}
