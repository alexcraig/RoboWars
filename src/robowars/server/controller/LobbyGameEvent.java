package robowars.server.controller;

import robowars.shared.model.GameType;

/**
 * Event class indicating that a server lobby has either launched or terminated
 * a game.
 */
public class LobbyGameEvent extends ServerLobbyEvent {
	
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
	 * @see ServerLobbyEvent#serialize()
	 */
	public String serialize() {
		switch(getEventType()) {
		case ServerLobbyEvent.EVENT_GAME_LAUNCH:
		case ServerLobbyEvent.EVENT_GAME_OVER:
		case ServerLobbyEvent.EVENT_GAMETYPE_CHANGE:
			return "[" + getEventType() + "|" + gameType.toString() + "]";
		default:
			return "[ERROR]"; // TODO: Throw exception here
		}
	}
}
