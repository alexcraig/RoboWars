package robowars.server.controller;

/**
 * Event class indicating that the state of a user connected to a server
 * lobby has changed (either the player has joined/left the server, their
 * spectator / ready status has been modified, or their latest chat message
 * has changed).
 */
public class LobbyUserEvent extends ServerLobbyEvent {
	
	/** The proxy representing the user who's state has changed */
	private UserProxy user;
	
	/**
	 * Generates a new LobbyUserEvent
	 * @param src	The ServerLobby that generated the event
	 * @param type	The type of the event (constants defined in ServerLobbyEvent)
	 * @param user	The user the event refers to
	 */
	public LobbyUserEvent(ServerLobby src, int type, UserProxy user) {
		super(src, type);
		this.user = user;
	}
	
	/**
	 * @return	The user this event refers to
	 */
	public UserProxy getUser() {
		return user;
	}
	
	/**
	 * @see ServerLobbyEvent#serialize()
	 */
	public String serialize() {
		switch(getEventType()) {
		case ServerLobbyEvent.EVENT_PLAYER_JOINED:
			return "[" + ServerLobbyEvent.EVENT_PLAYER_JOINED + "|" + user.getUsername() + "]";
		case ServerLobbyEvent.EVENT_PLAYER_LEFT:
			return "[" + ServerLobbyEvent.EVENT_PLAYER_LEFT + "|" + user.getUsername() + "]";
		case ServerLobbyEvent.EVENT_PLAYER_STATE_CHANGE:
			return "[" + ServerLobbyEvent.EVENT_PLAYER_STATE_CHANGE + "|" + user.getUsername()
				+ "|" + user.isPureSpectator() + "|" + user.isReady() + "]";
		default:
			return "[ERROR]"; // TODO: Throw exception here
		}
	}
}
