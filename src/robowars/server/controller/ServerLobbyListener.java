package robowars.server.controller;

import java.util.EventListener;

/**
 * Interface defining functions that all classes wishing to listen on a
 * ServerLobby must implement.
 */
public interface ServerLobbyListener extends EventListener {
	
	/**
	 * Called whenever a user joins or leaves the server, and whenever a player's
	 * ready status or spectator status changes.
	 */
	public void userStateChanged(UserStateEvent event);
	
	/**
	 * Called whenever a robot registers or unregisters from the server lobby.
	 */
	public void robotStateChanged(RobotStateEvent event);
	
	/**
	 * Called whenever the server lobby launches or terminates a game.
	 */
	public void lobbyGameStateChanged(LobbyGameEvent event);
	
	/**
	 * Called whenever a chat message is broadcasted through the server lobby.
	 */
	public void lobbyChatMessage(LobbyChatEvent event);

}
