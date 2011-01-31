package robowars.server.controller;

import robowars.shared.model.User;

/**
 * Event class indicating that the state of a user connected to a server
 * lobby has changed (either the player has joined/left the server or their
 * spectator / ready status has been modified.
 */
public class LobbyUserEvent extends ServerLobbyEvent {
	private static final long serialVersionUID = -830593124712928721L;
	
	/** The user who's state has changed */
	private User user;
	
	/**
	 * Generates a new LobbyUserEvent
	 * @param src	The ServerLobby that generated the event
	 * @param type	The type of the event (constants defined in ServerLobbyEvent)
	 * @param user	The user the event refers to
	 */
	public LobbyUserEvent(ServerLobby src, int type, User user) {
		super(src, type);
		this.user = user;
	}
	
	/**
	 * @return	The user this event refers to
	 */
	public User getUser() {
		return user;
	}
	
	/**
	 * @return	A plain text string description of the event
	 */
	public String toString() {
		switch(getEventType()) {
		case EVENT_PLAYER_JOINED:
			return user.getUsername() + " has joined the server.";
		case EVENT_PLAYER_LEFT:
			return user.getUsername() + " has left the server.";
		case EVENT_PLAYER_STATE_CHANGE:
			return "< " + user.getUsername() + ": Ready = " + user.isReady()
				+ ", Spectator = " + user.isPureSpectator() + " >";
		default:
			return "UNKNOWN EVENT TYPE";
		}
	}
}
