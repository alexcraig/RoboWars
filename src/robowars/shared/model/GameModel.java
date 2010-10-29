package robowars.shared.model;

import java.io.Serializable;
import java.util.Vector;
import java.util.ArrayList;

public abstract class GameModel implements Serializable{
	
	protected GameType gameType;
	protected ControlType controlType;
	protected int minimumPlayers;
	protected Vector<Float> arenaSize; //Why are we using floats in our vectors and not integers?
	protected boolean inProgress;
	protected ArrayList<GameEntity> entities;
	
	public static final float DEFAULT_ARENA_SIZE = 100;
	
	public static GameModel generateGameModel(GameType gameType){
		if(gameType == GameType.LIGHTCYCLES)
			return new LightCycles();
		if(gameType == GameType.TANK_SIMULATION)
			return new TankSimulation();
		return null;
	}
	
	public void initVariables() {
		arenaSize = new Vector<Float>(2);
		inProgress = false;
		entities = new ArrayList<GameEntity>();
		minimumPlayers = gameType.getMinimumPlayers();
		
		arenaSize.add(DEFAULT_ARENA_SIZE);//x
		arenaSize.add(DEFAULT_ARENA_SIZE);//y
	}
	
	public void startGame() {
		if(entities.get(0) != null && entities.get(1) != null)
			inProgress = true;
	}
	
	public ArrayList<GameEntity> getEntities() {
		return entities;
	}
	
	public void updateGameState(int timeElapsed) {
		
	}
	
	public void updateRobotPosition(String identifier, Vector<Float> pos, Vector<Float> heading) {
		GameRobot robot = (GameRobot) entities.get(0);
		if(robot.getRobotId() != identifier)
			robot = (GameRobot) entities.get(1);
		if(robot.getRobotId() != identifier)
			return;//error, robot with specified identifier doesn't exist.
		
		robot.setPosition(pos);
		robot.setHeading(heading);
	}
	
	public RobotCommand getCurrentRobotCommand(String identifier) {
		return null;
	}
	
	public GameRobot getGameRobot(String identifier) {
		//Indices 0 and 1 should always store the two robots. Probably not going to keep it this way cause it seems messy.
		GameRobot robot = (GameRobot) entities.get(0);
		if(robot.getRobotId() == identifier){
			return robot;
		}else if(robot.getRobotId() == identifier)
			return robot;
		else
			return null;
	}
	
	public void processCommand(RobotCommand command) {
		if(isValidCommand(command))
			return;
	}
	
	public boolean isValidCommand(RobotCommand command){
		if (gameType == GameType.LIGHTCYCLES){
			if (command.getType() == CommandType.MOVE_CONTINUOUS || 
					command.getType() == CommandType.TURN_RIGHT_ANGLE_LEFT || 
					command.getType() == CommandType.TURN_RIGHT_ANGLE_RIGHT) {
				return true;
			}else{
				return false;
			}
		}else if (gameType == GameType.TANK_SIMULATION){
			return true; //Any command restrictions in TankSimulation?
		}else{
			return true;
		}
	}
	
	public ControlType getControlType(){
		return controlType;
	}
	
	public boolean checkGameOver() {
		return false;
	}
	
	public void addRobot(String identifier) {
		//In the constructor of GameRobot, two parameters id and robotid, what's the difference?
		entities.add(new GameRobot(new Vector<Float>(),new Vector<Float>(),1,1,0,1,identifier)); 
	}
	
	private byte[] serializeState() {
		return null;
	}
	
	private byte[] marshallState() {
		return null;
	}
	
}
