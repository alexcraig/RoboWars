package robowars.shared.model;

public enum GameType {
	TANK_SIMULATION, LIGHTCYCLES;
	
	public static GameType getDefault() {
		return LIGHTCYCLES;
	}
	
	/**
	 * TODO: Probably shouldn't be hard coded onto an enum
	 * @return	The minimum number of players required to start a game
	 */
	public int getMinimumPlayers() {
		switch(this) {
		case TANK_SIMULATION:
			return 2;
		case LIGHTCYCLES:
			return 1;
		default:
			return 0;
		}
	}
	
	/**
	 * @param gameType	The string representation of a game type
	 * @return	A corresponding GameType object
	 */
	public static GameType parseString(String gameType) {
		if(gameType.equalsIgnoreCase("Light Cycles")) {
			return LIGHTCYCLES;
		} else if (gameType.equalsIgnoreCase("Tank Simulation")) {
			return TANK_SIMULATION;
		} else {
			return null;
		}
	}
	
	/**
	 * @return A string representation of the game type
	 */
	public String toString() {
		switch(this) {
		case TANK_SIMULATION:
			return "Tank Simulation";
		case LIGHTCYCLES:
			return "Light Cycles";
		default:
			return null;
		}
	}
}
