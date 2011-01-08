package robowars.shared.model;

public class TankSimulation extends GameModel {

	private int initialHealth;
	private int projectileRange;
	private int initialObstacles;

	/**
	* Default Constructor with test values for initialisation.
	*/
	public TankSimulation() {
		this.initialHealth = 1;
		this.projectileRange = 1;
		this.initialObstacles = 0;
		gameType = GameType.TANK_SIMULATION;
	}

	public TankSimulation(int initialHealth, int projectileRange, int initialObstacles) {
		this.initialHealth = initialHealth;
		this.projectileRange = projectileRange;
		this.initialObstacles = initialObstacles;
	}

	public void updateGameState(long timeElapsed) {

	}

	public boolean checkGameOver() {
		return false;
	}

	public void processCommand(RobotCommand command) {
		super.processCommand(command);
	}

	public boolean isValidCommand(RobotCommand command){
		return true;
	}
	private void generateProjectile(GameRobot robot) {
		entities.add(new Projectile(robot.getPose(),1,1,1,0));
	}
	
	public ControlType getControlType() {
		return ControlType.TANK;
	}

}

