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
	
	public void updateGameState(int timeElapsed) {
		
	}
	
	public boolean checkGameOver() {
		return false;
	}
	
	public void processCommand(RobotCommand command) {
		super.processCommand(command);
		
		
	}
	
	private void generateProjectile(GameRobot robot) {
		entities.add(new Projectile(robot.getPosition(),robot.getHeading(),1,1,1,0));
	}

}
