package robowars.test;

import robowars.server.controller.LobbyChatEvent;
import robowars.server.controller.LobbyGameEvent;
import robowars.server.controller.LobbyRobotEvent;
import robowars.server.controller.LobbyUserEvent;
import robowars.server.controller.ServerLobbyListener;

/**
 * Testing listener to capture events generated by a ServerLobby
 */
public class TestLobbyListener implements ServerLobbyListener {
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