package robowars.shared.model;

import java.util.EventListener;

public interface GameListener extends EventListener{

	public void gameStateChanged(GameEvent event);
}
