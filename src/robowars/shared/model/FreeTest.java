package robowars.shared.model;

import java.util.Vector;
import lejos.robotics.Pose;

public class FreeTest extends GameModel{

	private Obstacle theWall;
	
	public FreeTest() {
		super.gameType = GameType.FREETEST;
		super.initVariables();
		Pose p = new Pose(100,0,0);
		theWall = new Obstacle(p,10,10,0,false,false,0);
		entities.add(theWall);
	}
	
	public void updateGameState(long timeElapsed){
		if(robots.get(0).checkCollision(theWall))
			for (GameListener l : listeners) {
				l.gameStateChanged(new GameEvent(this, GameEvent.COLLISION_DETECTED));
			}
	}
	
	public boolean checkGameOver(){
		return  false;
	}
	
	public boolean isValidCommand(RobotCommand command){
		return true;
	}
}
