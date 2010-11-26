package robowars.shared.model;

import java.util.ArrayList;
import java.util.Vector;
import lejos.robotics.Pose;

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

	public boolean startGame(){
		super.startGame();
		if(inProgress){
			for (GameRobot r : robots){
				r.setCommand(new RobotCommand(CommandType.MOVE_CONTINUOUS,0));
			}
			return inProgress;
		}
		return inProgress;
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

	public boolean updateRobotPosition(String identifier, Pose pose) {
		GameRobot robot = null;
		for(GameRobot r : robots){
			if(r.getRobotId() == identifier)
				robot = r;
		}

		if(robot == null)
			return false;

		if(robot.getPose().getHeading() != robot.getLastPose().getHeading()){
			spawnNewWall(robot);
		}

		return super.updateRobotPosition(identifier, pose);
	}

	public void spawnNewWall(GameRobot robot) {
		Obstacle newWall = new Obstacle(robot.getPose(),0,wallWidth,1,false,false,0);
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
					robots.get(0).setCommand(new RobotCommand(CommandType.STOP,2));
					listener.gameStateChanged(new GameEvent(this, GameEvent.PLAYER_1_WINS));
					inProgress = false;
					return true;
				}
				if(robots.get(1).checkCollision(e)){
					robots.get(1).setCommand(new RobotCommand(CommandType.STOP,2));
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
	
	public boolean isValidCommand(RobotCommand command){
		if (command.getType() == CommandType.MOVE_CONTINUOUS ||
				command.getType() == CommandType.TURN_RIGHT_ANGLE_LEFT ||
				command.getType() == CommandType.TURN_RIGHT_ANGLE_RIGHT) {
			return true;
		}else{
			return false;
		}
	}
}