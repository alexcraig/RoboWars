package robowars.shared.model;

import java.io.Serializable;
import java.util.ArrayList;

public class Obstacle extends GameEntity implements Serializable{

	private static final long serialVersionUID = 2354988184568266965L;
	private int strength;
	private long lifeTime;
	private boolean destroyOnContact;
	private boolean destroyByProjectile;

	public Obstacle(Posture Posture, Vector shape[], int strength, boolean destroyOnContact, boolean destroyByProjectile, int id) {
		super(Posture, shape, id);
		this.strength=strength;
		this.destroyByProjectile=destroyByProjectile;
		this.destroyOnContact=destroyOnContact;
		lifeTime = 0;
	}

	public Obstacle(Posture Posture, Vector shape[], int id) {
		super(Posture, shape, id);
		this.strength=0;
		this.destroyByProjectile=false;
		this.destroyOnContact=false;
		lifeTime = 0;
	}
	
	/**
	 * This Obstacle constructor is for use by the Light Cycles game mode.
	 * It creates an obstacle of a rectangular shape with width 10 and length 0.
	 * Obstacle destruction is set to false.
	 * @param Posture The Posture where this obstacle will be centred.
	 */
	public Obstacle(Posture Posture) {
		super(Posture, new Vector[] {new Vector(0,5), 
				new Vector(0,-5), 
				new Vector(0,-5), 
				new Vector(0,5)}, 1);
		this.strength=0;
		this.destroyByProjectile=false;
		this.destroyOnContact=false;
	}

	public void modifyLength(float amount){
		//TODO: This functionality is only useful for the Lightcycles game mode. Should probably move this code to Lightcycles.java.
		Vector unitV = Vector.createUnitVector(getPosture());
		Vector v1, v2;
		if(amount >= 1){
			v1 = getVertex(0);
			v2 = getVertex(1);
		} else if(amount <= 1){
			v1 = getVertex(2);
			v2 = getVertex(3);
		} else {
			//probably should return with an error instead.
			return;
		}
		
		System.out.println(amount);
		System.out.println(unitV.getX() + "," + unitV.getY());
		
		v1.setX(v1.getX() + unitV.getX() * amount);
		v1.setY(v1.getY() + unitV.getY() * amount);
		v2.setX(v2.getX() + unitV.getX() * amount);
		v2.setY(v2.getY() + unitV.getY() * amount);
		
		
		if(amount >= 1){
			this.setVertex(0, v1);
			this.setVertex(1, v2);
		} else if(amount <= 1){
			this.setVertex(2, v1);
			this.setVertex(3, v2);
		} else {
			return;
		}
	}

	public static ArrayList<Obstacle> createArenaBoundary(){
		ArrayList<Obstacle> boundary = new ArrayList<Obstacle>();
		/*for(int i = 0; i < 4; i++){
			Vector shape[] = new Vector[] {new Vector(0,0), new Vector(0,5), 
				new Vector(GameModel.DEFAULT_ARENA_SIZE,5),
				new Vector(GameModel.DEFAULT_ARENA_SIZE,0)};
			boundary.add(new Obstacle(new Posture(0,0,0), shape));
		}
		boundary.get(1).setPosture(new Posture(GameModel.DEFAULT_ARENA_SIZE, 0, 90));
		boundary.get(2).setPosture(new Posture(GameModel.DEFAULT_ARENA_SIZE, GameModel.DEFAULT_ARENA_SIZE, 90));
		boundary.get(3).setPosture(new Posture(0, GameModel.DEFAULT_ARENA_SIZE, 90));
		*/
		
		Vector shape0[] = new Vector[] {new Vector(0,0), new Vector(0,5), 
				new Vector(GameModel.DEFAULT_ARENA_SIZE,5),
				new Vector(GameModel.DEFAULT_ARENA_SIZE,0)};
		boundary.add(new Obstacle(new Posture(0,0,0), shape0, 100));
		
		Vector shape1[] = new Vector[] {new Vector(0,0), new Vector(0,5), 
				new Vector(GameModel.DEFAULT_ARENA_SIZE,5),
				new Vector(GameModel.DEFAULT_ARENA_SIZE,0)};
		boundary.add(new Obstacle(new Posture(GameModel.DEFAULT_ARENA_SIZE, 0, 90), shape1, 101));
		
		Vector shape2[] = new Vector[] {new Vector(0,0), new Vector(0,5), 
				new Vector(GameModel.DEFAULT_ARENA_SIZE,5),
				new Vector(GameModel.DEFAULT_ARENA_SIZE,0)};
		boundary.add(new Obstacle(new Posture(GameModel.DEFAULT_ARENA_SIZE, GameModel.DEFAULT_ARENA_SIZE, 180), shape2, 102));
		
		Vector shape3[] = new Vector[] {new Vector(0,0), new Vector(0,5), 
				new Vector(GameModel.DEFAULT_ARENA_SIZE,5),
				new Vector(GameModel.DEFAULT_ARENA_SIZE,0)};
		boundary.add(new Obstacle(new Posture(0, GameModel.DEFAULT_ARENA_SIZE, -90), shape3, 103));
		return boundary;
	}
	
	public void passTime(long time){
		lifeTime += time;
	}
	
	public long getTime() {return lifeTime;}
	public boolean isHittable(){return destroyOnContact;}
	public boolean isShootable(){return destroyByProjectile;}
	public int getStrength(){return strength;}

	public float getLength() {
		Vector v = new Vector(getVertex(1).getX() - getVertex(2).getX(), getVertex(1).getY() - getVertex(2).getY());
		return v.magnitude();
	}
}

