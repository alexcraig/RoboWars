package robowars.shared.model;

public enum GameType {
	TANK_SIMULATION, LIGHTCYCLES, FREETEST;
	
	public static GameType getDefault() {
		return FREETEST;
	}
	
	/**
	 * @return	The minimum number of players required to start a game
	 */
	public int getMinimumPlayers() {
		switch(this) {
		case TANK_SIMULATION:
			return 2;
		case LIGHTCYCLES:
			return 1;
		case FREETEST:
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
		} else if (gameType.equalsIgnoreCase("Free Test")) {
			return FREETEST;
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
		case FREETEST:
			return "Free Test";
		default:
			return null;
		}
	}
}
