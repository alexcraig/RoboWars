package robowars.test;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Vector;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import robowars.shared.model.*;

public class LightCyclesTest {
	
	GameModel model;
	
	@Before
	public void setUp() throws Exception {
		model.generateGameModel(GameType.LIGHTCYCLES);
	}

	@After
	public void tearDown() throws Exception {
		
	}
	
	@Test
	public void testGameModel(){
		model.addRobot(new String("Robot1"));
		model.addRobot(new String("Robot2"));
		model.startGame();
		testCreatedRobots();
		for(int i = 0; i <= 51; i++){
			//model.updateRobotPosition("Robot1", pos, heading)
			model.updateGameState(1);
		}
	}
	
	@Test
	public void testCreatedRobots(){
		int num = 0;
		ArrayList<GameRobot> robots = new ArrayList<GameRobot>();
		for (GameEntity e : model.getEntities()){
			if(e instanceof GameRobot)
				num++;
				robots.add((GameRobot) e);
		}
			assertEquals(2, num);
			assertTrue(robots.get(0).getX() == 0);
			assertTrue(robots.get(0).getY() == 0);
			assertTrue(robots.get(0).getX() == model.DEFAULT_ARENA_SIZE);
			assertTrue(robots.get(0).getY() == model.DEFAULT_ARENA_SIZE);
			
	}
	
}