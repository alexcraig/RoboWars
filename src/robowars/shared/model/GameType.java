package robowars.shared.model;

public enum GameType {
	TANK_SIMULATION, LIGHTCYCLES;
	
	public static GameType getDefault() {
		return LIGHTCYCLES;
	}
}
