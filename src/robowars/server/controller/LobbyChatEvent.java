package robowars.server.controller;

import robowars.shared.model.GameType;

/**
 * Event class indicating that a server lobby has broadcasted a chat message
 * that should be displayed on all views and broadcast to connected
 * users.
 */
public class LobbyChatEvent extends ServerLobbyEvent {
	
	/** The message that the chat event should carry */
	private String message;
	
	/**
	 * Generates a new LobbyRobotEvent
	 * @param src	The ServerLobby that generated the event
	 * @param message	The message to be carried by the event
	 */
	public LobbyChatEvent(ServerLobby src, String message) {
		super(src, ServerLobbyEvent.EVENT_CHAT_MESSAGE);
		this.message = message;
	}
	
	/**
	 * @return	The message to be carried by the event
	 */
	public String getMessage() {
		return message;
	}
	
	/**
	 * @see ServerLobbyEvent#serialize()
	 */
	public String serialize() {
		return "[" + getEventType() + "|" + getMessage() + "]";
	}
}
