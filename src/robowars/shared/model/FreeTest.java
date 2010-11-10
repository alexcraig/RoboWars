package robowars.shared.model;

import java.util.Vector;

public class FreeTest extends GameModel{

	private Obstacle theWall;
	
	public FreeTest() {
		super.gameType = GameType.FREETEST;
		super.initVariables();
		Vector<Float> v = new Vector<Float>();
		v.add(Float.valueOf(100));
		v.add(Float.valueOf(0));
		theWall = new Obstacle(v,null,10,10,0,false,false,0);
		entities.add(theWall);
	}
	
	public void updateGameState(long timeElapsed){
		if(robots.get(0).checkCollision(theWall))
			listener.gameStateChanged(new GameEvent(this, GameEvent.COLLISION_DETECTED));
	}
	
	public boolean checkGameOver(){
		return  false;
	}
}
