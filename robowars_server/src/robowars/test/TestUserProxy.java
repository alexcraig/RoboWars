package robowars.test;

import java.util.EventObject;

import robowars.server.controller.ServerLobby;
import robowars.server.controller.UserProxy;
import robowars.shared.model.User;

/**
 * Testing user proxy with network components disabled.
 */
class TestUserProxy extends UserProxy {
	public TestUserProxy(ServerLobby lobby, String username) {
		super(null, lobby, null);
		setUser(new User(username, null));
	}

	/**
	 * Overrides the actual sendMessage method to avoid using the
	 * network socket.
	 */
	public void sendMessage(String message) {}
	
	/**
	 * Overrides the actual sendEvent method to avoid using the
	 * network socket.
	 */
	public void sendEvent(EventObject event) {}
}
