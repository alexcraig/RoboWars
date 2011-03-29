package robowars.test;

import java.util.EventObject;

import robowars.server.controller.ServerLobby;
import robowars.server.controller.UserProxy;
import robowars.shared.model.User;

/**
 * Testing user proxy with network components disabled.
 */
public class TestUserProxy extends UserProxy {
	/** The last EventObject received by this proxy */
	EventObject lastEvent;
	
	public TestUserProxy(ServerLobby lobby, String username) {
		super(null, lobby, null);
		setUser(new User(username, null));
		lastEvent = null;
	}

	/**
	 * Overrides the actual sendMessage method to avoid using the
	 * network socket.
	 */
	public void sendMessage(String message) {}
	
	/**
	 * Overrides the actual sendEvent method to avoid using the
	 * network socket, and records the event.
	 */
	public void sendEvent(EventObject event) {
		lastEvent = event;
	}
	
	/**
	 * @return	The last event received by this proxy.
	 */
	public EventObject getLastEvent() {
		return lastEvent;
	}
}
