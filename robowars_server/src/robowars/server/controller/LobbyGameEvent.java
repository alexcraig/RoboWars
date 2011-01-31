package robowars.server.controller;

import robowars.shared.model.GameType;

/**
 * Event class indicating that a server lobby has either launched or terminated
 * a game.
 */
public class LobbyGameEvent extends ServerLobbyEvent {
	private static final long serialVersionUID = -4326889526179283306L;
	
	/** The type of the game that was just launched or terminated */
	private GameType gameType;
	
	/**
	 * Generates a new LobbyGameEvent
	 * @param src	The ServerLobby that generated the event
	 * @param type	The type of the event (constants defined in ServerLobbyEvent)
	 * @param gameType	The type of the game that was just launched or terminated
	 */
	public LobbyGameEvent(ServerLobby src, int type, GameType gameType) {
		super(src, type);
		this.gameType = gameType;
	}
	
	/**
	 * @return	The type of game that was just launched or terminated.
	 */
	public GameType getGameType() {
		return gameType;
	}
	
	/**
	 * @return	A plain text string description of the event
	 */
	public String toString() {
		switch(getEventType()) {
		case EVENT_GAMETYPE_CHANGE:
			return "Selected game type changed to: " + gameType + " (Minimum players = " 
				+ gameType.getMinimumPlayers() + ").";
		case EVENT_GAME_LAUNCH:
			return "New game launched (" + gameType + ").";
		case EVENT_GAME_OVER:
			return "Game terminated.";
		default:
			return "UNKNOWN EVENT TYPE";
		}
	}
}
