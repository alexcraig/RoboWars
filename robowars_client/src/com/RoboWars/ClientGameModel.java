package com.RoboWars;

import java.util.ArrayList;
import java.util.Observable;

import android.util.Log;

import robowars.shared.model.GameEntity;
import robowars.shared.model.GameModel;
import robowars.shared.model.GameRobot;
import robowars.shared.model.Obstacle;
import robowars.shared.model.Projectile;

public class ClientGameModel extends Observable
{
	
	/* Game information. */
	private boolean gameInProgress;		// Is the game already running?
	
	/* Location of objects to draw. */
	private ArrayList<GameRobot> playerList;		// Contains health information, location, ammo, etc.
	private ArrayList<Obstacle> wallLocations;		// Contains locations of the walls.
	private ArrayList<Projectile> projectiles;		
	
	/**
	 * Default constructor.
	 */
	public ClientGameModel()
	{
		playerList = new ArrayList<GameRobot>();
		wallLocations = new ArrayList<Obstacle>();
		projectiles = new ArrayList<Projectile>();
		
		gameInProgress = false;
	}
	
	/**
	 * @return	Whether the game is in progress.
	 */
	public boolean gameInProgress() { return gameInProgress; }
	
	/**
	 * Start the game.
	 */
	public void startGame(GameModel model) {
		gameInProgress = true;

		if (model.getEntities() == null || model.getEntities().size() == 0) Log.e("RoboWars", "getEntities is returning NULL or ZERO.");
		for (GameEntity e : model.getEntities())
		{
			if (e instanceof Obstacle) wallLocations.add((Obstacle)e);
		}
		
		updateModel(model);
	}

	/**
	 * End the game.
	 */
	public void endGame(GameModel serverGameModel) {
		gameInProgress = false;
		setChanged();
		notifyObservers(serverGameModel);
	}

	public void updateModel(GameModel serverGameModel) {
		
		setChanged();
		notifyObservers(serverGameModel);
	}
	
	public ArrayList<GameRobot> getRobots() { return playerList; }
	public ArrayList<Obstacle> getObstacles() { return wallLocations; }
	public ArrayList<Projectile> getProjectiles() { return projectiles; }
}