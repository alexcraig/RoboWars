package robowars.server.controller;

import robowars.shared.model.User;

/**
 * Event class indicating that the state of a user connected to a server
 * lobby has changed (either the player has joined/left the server or their
 * spectator / ready status has been modified.
 */
public class LobbyUserEvent extends ServerLobbyEvent {
	
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
			return "[ERROR]";
		}
	}
}
