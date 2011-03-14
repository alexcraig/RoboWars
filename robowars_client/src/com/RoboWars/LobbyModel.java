package com.RoboWars;

import java.util.Observable;

public class LobbyModel extends Observable
{	
	/* Class constants. Define the colour of message to print. */
	public static final int ERROR	= 0;	// Red
	public static final int CHAT	= 1;	// White
	public static final int EVENT	= 2;	// Green
	public static final int ADMIN	= 3;	// Blue
	
	private User myUser;
	private String version;
	
	private String chatBuffer;
	private int chatBufferType;
	private boolean newChatMessage;
	
	private String users;
	private boolean usersUpdated;
	
	public LobbyModel()
	{
		myUser = null;
		chatBuffer = null;
		chatBufferType = 0;
		
		users = "";
		
		newChatMessage = false;
		usersUpdated = false;
	}
	
	/**
	 * Get the local user.
	 * @return	The user Object of the client.
	 */
	public User getMyUser() { return myUser; }
	
	/**
	 * Set the local user.
	 * @param user
	 */
	public void setMyUser(User user) { myUser = user; }
	
	/**
	 * Check if the chat buffer contains a new entry.
	 * @return
	 */
	public boolean newChatMessage() { return newChatMessage; }
	
	/**
	 * Check if the userlist has changed.
	 * @return
	 */
	public boolean usersUpdated() { return usersUpdated; }
	
	/**
	 * Get the game version.
	 */
	public String getVersion() { return version; }
	
	/**
	 * Set the game version.
	 */
	public void setVersion(String version) { this.version = version; }
	
	/**
	 * Gets the last message sent over the network.
	 * @return
	 */
	public String getChatBuffer()
	{
		newChatMessage = false;
		return chatBuffer;
	}
	
	/**
	 * Gets all users currently connected.
	 * @return
	 */
	public String getUsers()
	{
		usersUpdated = false;
		return users;
	}
	
	/**
	 * Add a message to the chat buffer and notify the view.
	 * @param type
	 * @param message
	 */
	public void printMessage(int type, String message)
	{
		chatBuffer = message;
		chatBufferType = type;
		newChatMessage = true;
		
		setChanged();
		notifyObservers();
	}
	
	/**
	 * Append a user to the userlist and notify the view.
	 * @param user
	 */
	public void userJoined(String user)
	{
		users += user + "\n";
		usersUpdated = true;
		
		setChanged();
		notifyObservers();
	}
	
	/**
	 * If the user exists, remove them and notify the view.
	 * @param user
	 */
	public void userLeft(String user)
	{
		String temp = users;
		if (!users.replaceFirst(user + "\n", "").equals(temp))
		{
			usersUpdated = true;
			
			setChanged();
			notifyObservers();
		}
	}

	public int getChatBufferType() {
		return chatBufferType;
	}
}
