package com.RoboWars;

public class GameModel
{
	private static final int DEFAULT_PLAYERS = 2;
	
	private boolean gameInProgress;
	private int numberOfPlayers;
	private Player[] playerList;		// Contains health information, location, ammo, etc.
	
	public GameModel()
	{
		numberOfPlayers = DEFAULT_PLAYERS;
		playerList = new Player[numberOfPlayers];
		gameInProgress = false;
	}
	
	public GameModel(int numberOfPlayers)
	{
		this.numberOfPlayers = numberOfPlayers;
		playerList = new Player[numberOfPlayers];
		gameInProgress = false;
	}
	
	public boolean gameInProgress() { return gameInProgress; }
	public void startGame() { gameInProgress = true; }
	public void endGame() { gameInProgress = false; }
	public Player getPlayer(int player) { return playerList[player]; }
}