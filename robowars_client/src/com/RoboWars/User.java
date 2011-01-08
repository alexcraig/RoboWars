package com.RoboWars;

/**
 * 
 * @author Steve Legere
 * @version 5/11/2010
 * Keeps track of user's name and ping.
 * 
 */
public class User
{
	private String name;
	private int ping;
	
	private final int DEFAULT_PING = -1;
	
	public User(String name)
	{
		this.name = name;
		this.ping = DEFAULT_PING;
	}
	
	public String getName()
	{
		return name;
	}
	
	public int getPing()
	{
		return ping;
	}

	public void setPing(int ping)
	{
		this.ping = ping;
	}
}
