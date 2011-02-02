package robowars.server.controller;


/**
 * Event class indicating that a server lobby has broadcasted a chat message
 * that should be displayed on all views and broadcast to connected
 * users.
 */
public class LobbyChatEvent extends ServerLobbyEvent {
	private static final long serialVersionUID = -4763514958441257998L;
	
	/** The message that the chat event should carry */
	private String message;
	
	/**
	 * Generates a new LobbyChatEvent
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
	 * @return	A plain text string description of the event
	 */
	public String toString() {
		switch(getEventType()) {
		case EVENT_CHAT_MESSAGE:
			return message;
		default:
			return "UNKNOWN EVENT TYPE";
		}
	}
}
