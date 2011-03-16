package robowars.shared.model;

import java.io.Serializable;
import java.util.ArrayList;

public abstract class GameModel implements Serializable{

	protected GameType gameType;
	protected int minimumPlayers;
	protected float arenaSize;
	protected boolean inProgress;
	protected ArrayList<GameEntity> entities;
	protected ArrayList<GameRobot> robots;
	protected int numRobots;
	protected transient ArrayList<GameListener> listeners;

	public static final float DEFAULT_ARENA_SIZE = 140;

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
		arenaSize = DEFAULT_ARENA_SIZE;
		listeners = new ArrayList<GameListener>();
		inProgress = false;
		entities = new ArrayList<GameEntity>();
		robots = new ArrayList<GameRobot>();
		minimumPlayers = gameType.getMinimumPlayers();
		numRobots = 0;
		entities.addAll(Obstacle.createArenaBoundary());
	}

	public void addListener(GameListener listener){
		this.listeners.add(listener);
	}

	public synchronized void notifyListeners(int eventID){
		for (GameListener l : listeners) {
			l.gameStateChanged(new GameEvent(this, eventID));
		}
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

	public boolean updateRobotPosition(String identifier, Posture Posture) {
		GameRobot robot = null;

		for(GameRobot r : robots){
			if(r.getRobotId() == identifier)
				robot = r;
		}

		if(robot == null)
			return false;//error, robot with specified identifier doesn't exist.

		robot.setPosture(Posture);
		notifyListeners(GameEvent.ROBOT_MOVED);
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
	
	public ArrayList<GameRobot> getGameRobotList(){
		return robots;
	}

	public void processCommand(RobotCommand command) {
		if(isValidCommand(command))
			return;
	}

	public abstract boolean isValidCommand(RobotCommand command);

	public abstract ControlType getControlType();
	
	public float getArenaSize(){
		return arenaSize;
	}

	public abstract boolean checkGameOver();

	public void addRobot(GameRobot newRobot) {
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
	
	public abstract void generateProjectile(GameRobot robot);
	
	public void addEntity(GameEntity e){
		entities.add(e);
	}

	private byte[] serializeState() {
		return null;
	}

	private byte[] marshallState() {
		return null;
	}

}