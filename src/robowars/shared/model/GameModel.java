package robowars.shared.model;

import java.io.Serializable;
import java.util.Vector;
import java.util.ArrayList;
import lejos.robotics.Pose;

public abstract class GameModel implements Serializable{

	protected GameType gameType;
	protected ControlType controlType;
	protected int minimumPlayers;
	protected Vector<Float> arenaSize;
	protected boolean inProgress;
	protected ArrayList<GameEntity> entities;
	protected ArrayList<GameRobot> robots;
	protected int numRobots;
	protected GameListener listener;

	public static final float DEFAULT_ARENA_SIZE = 100;

	public static GameModel generateGameModel(GameType gameType){
		if(gameType == GameType.LIGHTCYCLES)
			return new LightCycles();
		if(gameType == GameType.TANK_SIMULATION)
			return new TankSimulation();
		if(gameType == GameType.FREETEST)
			return new FreeTest();
		return null;
	}

	public void initVariables() {
		arenaSize = new Vector<Float>(2);
		inProgress = false;
		entities = new ArrayList<GameEntity>();
		robots = new ArrayList<GameRobot>();
		minimumPlayers = gameType.getMinimumPlayers();
		numRobots = 0;

		arenaSize.add(DEFAULT_ARENA_SIZE);//x
		arenaSize.add(DEFAULT_ARENA_SIZE);//y
	}

	public void addListener(GameListener listener){
		this.listener = listener;
	}

	public boolean startGame() {

		for(int i = 0; i < minimumPlayers; i++){
			if(robots.get(i) == null){
				return inProgress;
			}
		}
		inProgress = true;
		return inProgress;
	}

	public ArrayList<GameEntity> getEntities() {
		return entities;
	}

	public abstract void updateGameState(long timeElapsed);

	public boolean updateRobotPosition(String identifier, Pose pose) {
		GameRobot robot = null;

		for(GameRobot r : robots){
			if(r.getRobotId() == identifier)
				robot = r;
		}

		if(robot == null)
			return false;//error, robot with specified identifier doesn't exist.

		robot.setPose(pose);
		return true;
	}

	public RobotCommand getCurrentRobotCommand(String identifier) {
		for(GameRobot r : robots){
			if(r.getRobotId() == identifier){
				return r.getCommand();
			}
		}
			return null;
	}

	public GameRobot getGameRobot(String identifier) {

		for (GameRobot robot : robots){
			if (robot.getRobotId() == identifier)
				return robot;
			}
		return null;
	}

	public void processCommand(RobotCommand command) {
		if(isValidCommand(command))
			return;
	}

	public abstract boolean isValidCommand(RobotCommand command);

	public ControlType getControlType(){
		return controlType;
	}

	public abstract boolean checkGameOver();

	// TODO: These methods should probably accept an existing GameRobot as
	//       RobotProxy's now persistently store a GameRobot object

	public void addRobot(String identifier) {
		//In the constructor of GameRobot, two parameters id and robotid, what's the difference?
		//GameRobot newRobot = new GameRobot(new Vector<Float>(),new Vector<Float>(),1,1,0,1,identifier);
		GameRobot newRobot;
		switch(numRobots){
			case 0:
				newRobot = new GameRobot(1,1,identifier);break;
			case 1:
				Pose p = new Pose(DEFAULT_ARENA_SIZE,DEFAULT_ARENA_SIZE,180);
				newRobot = new GameRobot(p,1,1,0,1,identifier);break;
			default:
				newRobot = new GameRobot(1,1,identifier);break;
			}
		entities.add(newRobot);
		robots.add(newRobot);
	}
	
	/**
	 * Removes a GameRobot from the list of robots in play for this game.
	 * @param identifier	The string identifier of the robot to remove
	 */
	public void removeRobot(String identifier) {
		for(int i = 0; i < robots.size(); i++) {
			if(robots.get(i).getRobotId().equals(identifier)) {
				robots.remove(i);
				return;
			}
		}
	}

	private byte[] serializeState() {
		return null;
	}

	private byte[] marshallState() {
		return null;
	}

}