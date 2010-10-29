package robowars.shared.model;


public class LightCycles extends GameModel {
	private float wallLength;
	private float wallFadeTime;
	
	/**
	 * Default Constructor with test values for initialisation.
	 */
	public LightCycles() {
		this.wallLength = 10;
		this.wallFadeTime = 1;
	}
	
	public LightCycles(float wallLength, float wallFadeTime) {
		this.wallLength = wallLength;
		this.wallFadeTime = wallFadeTime;
	}
	
	public void updateGameState(int timeElapsed) {
		
	}
	
	public boolean checkGameOver() {
		return false;
	}
	
	public void processCommand(RobotCommand command) {
		super.processCommand(command);
	}
	
}
