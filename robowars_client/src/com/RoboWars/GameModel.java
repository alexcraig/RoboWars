package com.RoboWars;

import robowars.shared.model.GameType;

public class GameModel
{
	private static final int DEFAULT_PLAYERS = 2;
	
	private GameType gameType;			// The type of game we're going to play.
	
	private boolean gameInProgress;		// Is the game already running?
	private int numberOfPlayers;		// Number of participants in the game.
	private Player[] playerList;		// Contains health information, location, ammo, etc.
	
	public GameModel()
	{
		numberOfPlayers = DEFAULT_PLAYERS;
		playerList = new Player[numberOfPlayers];
		gameInProgress = false;
	}
	
	/**
	 * 
	 * @param numberOfPlayers	Version of GameModel
	 */
	public GameModel(int numberOfPlayers)
	{
		this.numberOfPlayers = numberOfPlayers;
		playerList = new Player[numberOfPlayers];
		gameInProgress = false;
	}
	
	/**
	 * 
	 * @return	Whether the game is in progress.
	 */
	public boolean gameInProgress() { return gameInProgress; }
	public void startGame() { gameInProgress = true; }
	public void endGame() { gameInProgress = false; }
	
	/**
	 * 
	 * @param playerNumber	The 1-based index of the player
	 * @param player		The Player object to store
	 * 
	 * Store the given Player in the model (1-based index).
	 * Trying to set player 0 will do nothing.
	 * Example: The first player, Player 1, has a playerNumber of 1, and so on.
	 * 
	 */
	public void setPlayer(int playerNumber, Player player)
	{
		if (playerNumber >= 1) playerList[playerNumber-1] = player;
	}
	
	/**
	 * 
	 * @param player	The 1-based index of which player to get.
	 * @return			The Player object referenced by int player.
	 * 
	 * Attempting to get the 0-based (or any negative index) player will return null.
	 */
	public Player getPlayer(int player)
	{
		if (player >= 1) return playerList[player-1];
		else return null;
	}
	
	public void setGameType(GameType gameType) { this.gameType = gameType; }
	public GameType getGameType() { return gameType; }
}