package robowars.shared.model;

import java.util.ArrayList;

public class TankSimulation extends GameModel {
	
	private int initialHealth;
	private int projectileRange;
	private int initialObstacles;
	private ArrayList<Projectile> projectiles;
	
	public static final int projectileSpeed = 1;

	/**
	* Default Constructor with test values for initialisation.
	*/
	public TankSimulation() {
		this.initialHealth = 1;
		this.projectileRange = 40;
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
		ArrayList<Projectile> removedProjectiles = new ArrayList<Projectile>();
		for(Projectile p : projectiles){
			p.updatePosition((int) timeElapsed);
			
			for(GameEntity e : entities){
				if(!(e instanceof Projectile)){
					if(p.checkCollision(e)){
						if(e instanceof GameRobot){
							GameRobot r = (GameRobot) e;
							r.decreaseHealth(1);
						}
						notifyListeners(GameEvent.PROJECTILE_HIT);
						removedProjectiles.add(p);
				}
				}
			}	
			
			if(p.getDistanceTraveled() > projectileRange){
				removedProjectiles.add(p);
			}
		}
		projectiles.removeAll(removedProjectiles);
		entities.removeAll(removedProjectiles);
		
		for(GameRobot r1 : robots){
			for(GameRobot r2 : robots){
				if(!r1.equals(r2))
					if(r1.checkCollision(r2)){
						r1.setCommand(RobotCommand.stop());
						r2.setCommand(RobotCommand.stop());
					}
			}
		}
		
		for(GameRobot r : robots){
			for(GameEntity e : entities){
				if(!r.equals(e) && !projectiles.contains(e))
					if(r.checkCollision(e)){
						r.setCommand(RobotCommand.stop());
					}
			}
		}
		
		System.out.println("Robot1 Health: " + robots.get(0).getHealth());
		System.out.println("Robot2 Health: " + robots.get(1).getHealth());
		
		
		
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

	public boolean isValidCommand(RobotCommand command){
		return true;
	}
	
	public void generateProjectile(GameRobot robot) {
		Posture projectilePosture = robot.clonePosture();
		projectilePosture.moveUpdate(15);
		Projectile newProjectile = new Projectile(projectilePosture,projectileSpeed, 0);
		entities.add(newProjectile);
		projectiles.add(newProjectile);
		notifyListeners(GameEvent.PROJECTILE_FIRED);
	}
	
	public ControlType getControlType() {
		return ControlType.TANK;
	}

}

