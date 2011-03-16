package robowars.shared.model;


public class FreeTest extends GameModel{
	
	public FreeTest() {
		super.gameType = GameType.FREETEST;
		super.initVariables();
	}
	
	public void updateGameState(long timeElapsed){	
		for(GameRobot r : robots){
			for(GameEntity e : entities){
				if(!r.equals(e)){
					if(r.checkCollision(e)){
						notifyListeners(GameEvent.COLLISION_DETECTED);
					}
				}
			}
		}
	}
	
	public boolean checkGameOver(){
		return false;
	}
	
	
	public void generateProjectile(GameRobot robot){}
	
	public boolean isValidCommand(RobotCommand command){
		return true;
	}
	
	public ControlType getControlType() {
		return ControlType.TANK;
	}
}
