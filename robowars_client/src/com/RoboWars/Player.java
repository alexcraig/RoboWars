package com.RoboWars;

import android.graphics.Point;

/**
 * @author Steve
 *
 * The in-game representation of a player.
 */
public class Player
{
	private static final int DEFAULT_HEALTH 	= 100;
	private static final int DEFAULT_AMMO		= 10;
	private static final Point DEFAULT_POINT	= new Point(0,0);
	private static final String DEFAULT_NAME	= "Robo Dude";
	
	private String name;
	private int health, ammo;
	private Point location;
	
	/**
	 * Default constructor. All values set to default values.
	 */
	public Player()
	{
		name		= DEFAULT_NAME;
		health 		= DEFAULT_HEALTH;
		ammo 		= DEFAULT_AMMO;
		location	= DEFAULT_POINT;
	}
	
	/**
	 * Alternative constructor; sets the name of the player. All other values
	 * are set to the default values.
	 * 
	 * @param name	The name of the created player.
	 */
	public Player (String name)
	{
		this.name	= name;
		health 		= DEFAULT_HEALTH;
		ammo 		= DEFAULT_AMMO;
		location	= DEFAULT_POINT;
	}
	
	/**
	 * @return	Current health of the player.
	 */
	public int getHealth() { return health; }
	
	/**
	 * @param health	Set the health of the player.
	 */
	public void setHealth(int health) { this.health = health; }
	
	/**
	 * @return	Current ammo the player has.
	 */
	public int getAmmo() { return ammo; }
	
	/**
	 * @param ammo	Set the ammo of the player.
	 */
	public void setAmmo(int ammo) { this.ammo = ammo; }
	
	/**
	 * @return	The Point location of the player.
	 * @see android.graphics.Point
	 */
	public Point getLocation() { return location; }
	
	/**
	 * @param location	The Point location of the player.
	 * @see android.graphics.Point
	 */
	public void setLocation(Point location) { this.location = location; }
	
	/**
	 * @param name	The name of the player.
	 */
	public void setName(String name) { this.name = name; }
	
	/**
	 * @return	The name of the player.
	 */
	public String getName() { return name; }
}
