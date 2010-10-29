package robowars.shared.model;


public class LightCycles extends GameModel {
	private float wallLength;
	private int wallFadeTime;
	
	/**
	 * Default Constructor with test values for initialisation.
	 */
	public LightCycles() {
		this.wallLength = 10;
		this.wallFadeTime = 100;
	}
	
	public LightCycles(float wallLength, int wallFadeTime) {
		this.wallLength = wallLength;
		this.wallFadeTime = wallFadeTime;
		super.gameType = GameType.LIGHTCYCLES;
		super.initVariables();
	}
	
	public void updateGameState(int timeElapsed) {
		
		for(int i = 2; i < entities.size(); i++){
			Obstacle w = (Obstacle) entities.get(i);
			w.passTime(timeElapsed);
		}
		Obstacle wall = (Obstacle) entities.get(2);
		if(wall.getTime() > wallFadeTime){
			wall.shrink(wall.getTime() - wallFadeTime);
			wall.passTime(wallFadeTime - wall.getTime());
			if(wall.getLength() <= 0)
				entities.remove(wall);
		}
	}
	
	public boolean checkGameOver() {
		for(int i = 2; i < entities.size(); i++){
			GameEntity wall = entities.get(i);
			if(entities.get(0).checkCollision(wall))
				return true;
			if(entities.get(0).checkCollision(wall))
				return true;
		}
		return false;
	}
	
	public void processCommand(RobotCommand command) {
		super.processCommand(command);
	}
	
}
