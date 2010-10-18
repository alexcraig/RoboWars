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
}
