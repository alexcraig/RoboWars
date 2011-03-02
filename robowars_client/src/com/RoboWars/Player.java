package com.RoboWars;

import android.graphics.Point;

public class Player
{
	private static final int DEFAULT_HEALTH = 100;
	private static final int DEFAULT_AMMO	= 10;
	
	private int health, ammo;
	private Point location;
	
	public Player(Point point)
	{
		health 		= DEFAULT_HEALTH;
		ammo 		= DEFAULT_AMMO;
		location	= point;
	}
	
	public int getHealth() { return health; }
	public void setHealth(int health) { this.health = health; }
	public int getAmmo() { return ammo; }
	public void setAmmo(int ammo) { this.ammo = ammo; }
	public Point getLocation() { return location; }
	public void setLocation(Point location) { this.location = location; }
}
