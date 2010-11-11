<<<<<<< HEAD
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
=======
package robowars.shared.model;

import java.util.ArrayList;
import java.util.Vector;

public class LightCycles extends GameModel {
	
	/**The constant width of the walls. */
	private float wallWidth;
	
	/** The amount of time can pass before the wall starts shrinking.
	 * If zero, the walls are persistent.*/
	private long wallFadeTime;
	private ArrayList<Obstacle> wallsOne;
	private ArrayList<Obstacle> wallsTwo;
	
	/**
	 * Default Constructor with test values for initialisation.
	 */
	public LightCycles() {
		this.wallWidth = 10;
		this.wallFadeTime = 100;
		wallsOne = new ArrayList<Obstacle>();
		wallsTwo = new ArrayList<Obstacle>();
		super.gameType = GameType.LIGHTCYCLES;
		super.initVariables();
	}
	
	public LightCycles(float wallLength, int wallFadeTime) {
		this.wallWidth = wallLength;
		this.wallFadeTime = wallFadeTime;
		wallsOne = new ArrayList<Obstacle>();
		wallsTwo = new ArrayList<Obstacle>();
		super.gameType = GameType.LIGHTCYCLES;
		super.initVariables();
	}
	
	public void updateGameState(long timeElapsed) {
		
		for(Obstacle w1 : wallsOne){
			w1.passTime(timeElapsed);
		}
		for(Obstacle w2 : wallsTwo){
			w2.passTime(timeElapsed);
		}
			
		Obstacle w1 = wallsOne.get(wallsOne.size() - 1);
		Obstacle w2 = wallsTwo.get(wallsTwo.size() - 1);
		
		if(w1.getTime() > wallFadeTime){
			// TODO: Casting here may cause an overflow
			w1.shrink((int)(w1.getTime() - wallFadeTime));
			w1.passTime(wallFadeTime - w1.getTime());
			if(w1.getLength() <= 0) {
				wallsOne.remove(w1);
				entities.remove(w1);
			}
		}
		
		if(w2.getTime() > wallFadeTime){
			// TODO: Casting here may cause an overflow
			w2.shrink((int)(w2.getTime() - wallFadeTime));
			w2.passTime(wallFadeTime - w2.getTime());
			if(w2.getLength() <= 0) {
				wallsTwo.remove(w2);
				entities.remove(w2);
			}
		}
		
		if(checkGameOver()){
			listener.gameStateChanged(new GameEvent(this, GameEvent.GAME_OVER));
		}
			
		
	}
	
	public boolean updateRobotPosition(String identifier, Vector<Float> pos, Vector<Float> heading) {
		GameRobot robot = null;
		for(GameRobot r : robots){
			if(r.getRobotId() == identifier)
				robot = r;
		}
		
		if(robot == null)
			return false;
		
		if(!heading.equals(robot.getLastHeading())){
			spawnNewWall(robot);
		}
				
		return super.updateRobotPosition(identifier, pos, heading);
	}
	
	public void spawnNewWall(GameRobot robot) {
		Obstacle newWall = new Obstacle(robot.getPosition(),robot.getHeading(),0,wallWidth,1,false,false,0);
		entities.add(newWall);
		if(robot == super.robots.get(0)){
			wallsOne.add(0, newWall);
		}else{ 
			wallsTwo.add(0, newWall);
		}
	}
	
	public boolean checkGameOver() {
		for(GameEntity e : entities){
			if (e instanceof Obstacle){ 
				if(robots.get(0).checkCollision(e)){
					listener.gameStateChanged(new GameEvent(this, GameEvent.PLAYER_1_WINS));
					inProgress = false;
					return true;
				}
				if(robots.get(1).checkCollision(e)){
					listener.gameStateChanged(new GameEvent(this, GameEvent.PLAYER_2_WINS));
					inProgress = false;
					return true;
				}
			}
		}
		return false;
	}
	
	public void processCommand(RobotCommand command) {
		super.processCommand(command);
	}
	
}
>>>>>>> 9a5d31a390e7d072a22f5916cd27c8da2d306806
