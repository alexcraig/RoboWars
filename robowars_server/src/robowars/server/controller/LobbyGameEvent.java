package robowars.server.controller;

import robowars.shared.model.CameraPosition;
import robowars.shared.model.GameType;

/**
 * Event class indicating that a server lobby has either launched or terminated
 * a game.
 * 
 * @author Alexander Craig
 */
public class LobbyGameEvent extends ServerLobbyEvent {
	private static final long serialVersionUID = -4326889526179283306L;
	
	/** The type of the game that was just launched or terminated */
	private GameType gameType;
	
	/** 
	 * Position information on the currently selected camera.
	 * (so that clients can determine the position and orientation to use for rendering)
	 */
	private CameraPosition camera;
	
	/**
	 * Generates a new LobbyGameEvent
	 * @param src	The ServerLobby that generated the event
	 * @param type	The type of the event (constants defined in ServerLobbyEvent)
	 * @param gameType	The type of the game that was just launched or terminated
	 */
	public LobbyGameEvent(ServerLobby src, int type, GameType gameType) {
		super(src, type);
		this.gameType = gameType;
		this.camera = null;
	}
	
	/**
	 * @return	The type of game that was just launched or terminated.
	 */
	public GameType getGameType() {
		return gameType;
	}
	
	/**
	 * Sets the camera controller that should be transmitted with this event
	 * @param cam	The camera controller to transmit with this event
	 */
	public void setCameraPosition(CameraPosition cam) {
		this.camera = cam;
	}
	
	/**
	 * @return	The camera controller associated with this event
	 */
	public CameraPosition getCameraPosition() {
		return camera;
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
