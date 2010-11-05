package robowars.shared.model;

import java.util.EventObject;

public class GameEvent extends EventObject{

	public static final int GAME_START = 0;
	public static final int GAME_OVER = 1;
	public static final int COLLISION_DETECTED = 2;
	public static final int PROJECTILE_FIRED = 3;
	public static final int PROJECTILE_HIT = 4;
	public static final int PLAYER_1_WINS = 5;
	public static final int PLAYER_2_WINS = 6;

	
	private int type;
	
	public GameEvent(GameModel model, int type){
		super(model);
		this.type = type;
	}
	
	public int getEventType() {
		return type;
	}
}
