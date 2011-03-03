package com.RoboWars;

import android.graphics.Point;
import robowars.shared.model.GameType;

public class GameModel
{
	private static final int DEFAULT_PLAYERS 		= 2;
	private static final float DEFAULT_ARENA_SIZE 	= robowars.shared.model.GameModel.DEFAULT_ARENA_SIZE;
	
	/* Game information. */
	private GameType gameType;			// The type of game we're going to play.
	private float arenaSize;			// The size of the arena.
	private boolean gameInProgress;		// Is the game already running?
	private int numberOfPlayers;		// Number of participants in the game.
	
	/* Location of objects to draw. */
	private Player[] playerList;		// Contains health information, location, ammo, etc.
	private Point[] wallLocations;		// Contains locations of the walls.
	
	/**
	 * Default constructor.
	 */
	public GameModel()
	{
		numberOfPlayers = DEFAULT_PLAYERS;
		playerList = new Player[numberOfPlayers];
		gameInProgress = false;
	}
	
	/**
	 * Constructor for an alternative number of players (default is 2).
	 * 
	 * @param numberOfPlayers	Number of players playing.
	 */
	public GameModel(int numberOfPlayers)
	{
		this.numberOfPlayers = numberOfPlayers;
		playerList = new Player[numberOfPlayers];
		gameInProgress = false;
	}
	
	/**
	 * @return	Whether the game is in progress.
	 */
	public boolean gameInProgress() { return gameInProgress; }
	
	/**
	 * Start the game.
	 */
	public void startGame() { gameInProgress = true; }
	
	/**
	 * End the game.
	 */
	public void endGame() { gameInProgress = false; }
	
	/**
	 * 
	 * Store the given Player in the model (1-based index).
	 * Trying to set player 0 will do nothing.
	 * Example: The first player, Player 1, has a playerNumber of 1, and so on.
	 * 
	 * @param playerNumber	The 1-based index of the player
	 * @param player		The Player object to store
	 */
	public void setPlayer(int playerNumber, Player player)
	{
		if (playerNumber >= 1) playerList[playerNumber-1] = player;
	}
	
	/**
	 * Attempting to get the 0-based (or any negative index) player will return null.
	 * 
	 * @param player	The 1-based index of which player to get.
	 * @return			The Player object referenced by int player.
	 */
	public Player getPlayer(int player)
	{
		if (player >= 1) return playerList[player-1];
		else return null;
	}
	
	/**
	 * 
	 * @param gameType	The type of game.
	 */
	public void setGameType(GameType gameType) { this.gameType = gameType; }
	
	/**
	 * 
	 * @return	The type of game.
	 */
	public GameType getGameType() { return gameType; }

	/**
	 * 
	 * @param arenaSize		The size of the arena (square).
	 */
	public void setArenaSize(float arenaSize) {
		this.arenaSize = arenaSize;
	}

	/**
	 * 
	 * @return	The size of the arena (square).
	 */
	public float getArenaSize() {
		return arenaSize;
	}
}