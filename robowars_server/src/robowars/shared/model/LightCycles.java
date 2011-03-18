package robowars.shared.model;

import java.util.ArrayList;

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
		this.wallFadeTime = 5000;
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
			//wallsOne.add(new Obstacle(robots.get(0).getPosture()));
			//wallsTwo.add(new Obstacle(robots.get(1).getPosture()));
			for (GameRobot r : robots){
				r.setCommand(RobotCommand.moveContinuous(RobotCommand.MAX_SPEED));
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

		if(!wallsOne.isEmpty()){
			Obstacle head = wallsOne.get(0);
			Obstacle tail = wallsOne.get(wallsOne.size() - 1);
			head.modifyLength((float) timeElapsed);
			if(wallFadeTime != 0 && tail.getTime() > wallFadeTime){
				// TODO: Casting here may cause an overflow
				tail.modifyLength((float) (wallFadeTime - tail.getTime()));
				tail.passTime(wallFadeTime - tail.getTime());
				if(tail.getLength() <= 0) {
					wallsOne.remove(tail);
					entities.remove(tail);
				}
			}
		}
		
		if(!wallsTwo.isEmpty()){
			Obstacle head = wallsTwo.get(0);
			Obstacle tail = wallsTwo.get(wallsTwo.size() - 1);
			head.modifyLength((float) timeElapsed);
			if(wallFadeTime != 0 && tail.getTime() > wallFadeTime){
				// TODO: Casting here may cause an overflow
				tail.modifyLength((float)(wallFadeTime - tail.getTime()));
				tail.passTime(wallFadeTime - tail.getTime());
				if(tail.getLength() <= 0) {
					wallsTwo.remove(tail);
					entities.remove(tail);
				}
			}
		}
		
		

		

		if(checkGameOver()){
			//notifyListeners(null, GameEvent.GAME_OVER);
		}

	}

	public boolean updateRobotPosition(String identifier, Posture Posture) {
		GameRobot robot = null;
		for(GameRobot r : robots){
			if(r.getRobotId() == identifier)
				robot = r;
		}

		if(robot == null)
			return false;

		if(Posture.getHeading() % 90 == 0 && Posture.getHeading() != robot.getPosture().getHeading()){
			spawnNewWall(robot, Posture);
		}

		return super.updateRobotPosition(identifier, Posture);
	}

	public void spawnNewWall(GameRobot robot,Posture Posture) {
		Obstacle newWall = new Obstacle(Posture);
		entities.add(newWall);
		if(robot == robots.get(0)){
			wallsOne.add(0, newWall);
		}else{
			wallsTwo.add(0, newWall);
		}
	}

	public boolean checkGameOver() {
		
		for(Obstacle o: wallsOne){
			if(robots.get(0).checkCollision(o) && !o.equals(wallsOne.get(0))) {
				robots.get(0).setCommand(RobotCommand.stop());
				//notifyListeners(null, GameEvent.PLAYER_2_WINS);
				inProgress = false;
				System.out.println("Collision between robot1 and wallsOne");
				return true;
			}
			if(robots.get(1).checkCollision(o)) {
				robots.get(1).setCommand(RobotCommand.stop());
				//notifyListeners(null, GameEvent.PLAYER_1_WINS);
				inProgress = false;
				System.out.println("Collision between robot2 and wallsOne");
				return true;
			}
		}
		
		for(Obstacle o: wallsTwo){
			if(robots.get(1).checkCollision(o) && !o.equals(wallsTwo.get(0))) {
				robots.get(1).setCommand(RobotCommand.stop());
				//notifyListeners(null ,GameEvent.PLAYER_1_WINS);
				inProgress = false;
				System.out.println("Collision between robot2 and wallsTwo");
				return true;
			}
			if(robots.get(0).checkCollision(o)) {
				robots.get(0).setCommand(RobotCommand.stop());
				//notifyListeners(null, GameEvent.PLAYER_2_WINS);
				inProgress = false;
				System.out.println("Collision between robot1 and wallsTwo");
				return true;
			}
		}
		return false;
	}
	
	public void generateProjectile(GameRobot robot) {}

	public void processCommand(RobotCommand command) {
		super.processCommand(command);
	}
	
	public boolean isValidCommand(RobotCommand command){
		if (command.getType() == CommandType.MOVE_CONTINUOUS ||
				command.getType() == CommandType.TURN_ANGLE_LEFT ||
				command.getType() == CommandType.TURN_ANGLE_RIGHT) {
			return true;
		}else{
			return false;
		}
	}
	
	public ControlType getControlType() {
		return ControlType.SNAKE;
	}
}