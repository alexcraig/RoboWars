package robowars.shared.model;

import java.util.ArrayList;
import lejos.robotics.Pose;

public class TankSimulation extends GameModel {
	
	private int initialHealth;
	private int projectileRange;
	private int initialObstacles;
	private ArrayList<Projectile> projectiles;
	
	public static int projectileSpeed = 20;

	/**
	* Default Constructor with test values for initialisation.
	*/
	public TankSimulation() {
		this.initialHealth = 1;
		this.projectileRange = 150;
		this.initialObstacles = 0;
		gameType = GameType.TANK_SIMULATION;
		projectiles = new ArrayList<Projectile>();
		initVariables();
	}

	public TankSimulation(int initialHealth, int projectileRange, int initialObstacles) {
		this.initialHealth = initialHealth;
		this.projectileRange = projectileRange;
		this.initialObstacles = initialObstacles;
		projectiles = new ArrayList<Projectile>();
		initVariables();
	}

	public void updateGameState(long timeElapsed) {
		for(Projectile p : projectiles){
			p.updatePosition((int) timeElapsed);
			if(p.getDistanceTraveled() > projectileRange){
				projectiles.remove(p);
				entities.remove(p);
			}
			
			for(GameRobot r : robots){
				if(p.checkCollision(r)){
					r.decreaseHealth(1);
				}
			}	
		}
		
		for(GameRobot r1 : robots){
			for(GameRobot r2 : robots){
				if(r1.checkCollision(r2)){
					r1.setCommand(RobotCommand.stop());
					r2.setCommand(RobotCommand.stop());
				}
			}
		}
		
		
		
		if(checkGameOver()){
			notifyListeners(GameEvent.GAME_OVER);
		}
	}
	
	public boolean checkGameOver() {
		for(GameRobot r : robots){
			if(r.getHealth() <= 0){
				//if()
				//notifyListeners(GameEvent.PLAYER_2_WINS);
				return true;
			}
		}
		return false;
	}

	public void processCommand(RobotCommand command) {
		super.processCommand(command);
	}

	public boolean isValidCommand(RobotCommand command){
		return true;
	}
	
	public void generateProjectile(GameRobot robot) {
		Pose projectilePose = robot.clonePose();
		projectilePose.moveUpdate(10);
		Projectile newProjectile = new Projectile(projectilePose,projectileSpeed, 0);
		entities.add(newProjectile);
		projectiles.add(newProjectile);
	}
	
	public ControlType getControlType() {
		return ControlType.TANK;
	}

}

