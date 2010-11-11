package robowars.client;

import java.util.ArrayList;
import java.util.Observable;

public class LobbyModel extends Observable implements MessageType
{
	private User myUser;
	
	private ArrayList<String> chat;
	private ArrayList<User> users;
	
	public LobbyModel()
	{
		myUser = null;
		
		chat = new ArrayList<String>();
		users = new ArrayList<User>();
	}
	
	public User getMyUser()
	{
		return myUser;
	}
	
	public void setMyUser(User user)
	{
		myUser = user;
	}
	
	public void printMessage(int type, String message)
	{
		chat.add(message);
		setChanged();
		notifyObservers(chat);
	}
	
	public void userJoined(User user)
	{
		users.add(user);
		setChanged();
		notifyObservers(users);
	}
	
	public void userLeft(User user)
	{
		if (users.remove(user))
		{
			setChanged();
			notifyObservers(users);
		}
	}
}
