package robowars.test;

import lejos.pc.comm.NXTInfo;
import lejos.robotics.navigation.SimpleNavigator;
import lejos.robotics.navigation.TachoPilot;

import org.apache.log4j.Logger;

import robowars.server.controller.RobotProxy;
import robowars.server.controller.ServerLobby;
import robowars.shared.model.RobotCommand;

/**
 * A version of RobotProxy will all actual network interaction
 * disabled (used for testing purposes).
 */
public class TestRobotProxy extends RobotProxy {
	public static final float TEST_WHEEL_DIAMETER = 20;
	public static final float TEST_TRACK_WIDTH = 60;
	public static final int MOTOR_UPDATE_MILLIS = 250;
	
	
	/** The logger used by this class */
	private static Logger log = Logger.getLogger(TestRobotProxy.class);
	
	/** Pilot to simulate robot movement */
	private TachoPilot pilot;
	
	/** Navigator to track robot position */
	private SimpleNavigator navigator;
	
	/** Simulated motors */
	private TestTachoMotor testMotorA, testMotorB;
	
	/**
	 * Generates a new TestRobotProxy with a dummy name and protocol
	 * @param lobby	The lobby that the TestRobotProxy should add itself to
	 * @param identifier	The identifier of the robot being simulated
	 */
	public TestRobotProxy(ServerLobby lobby, String identifier) {
		super(lobby, new NXTInfo(0, identifier, "dummy:address"));
		
		testMotorA = new TestTachoMotor();
		testMotorB = new TestTachoMotor();
		pilot = new TachoPilot(TEST_WHEEL_DIAMETER, TEST_TRACK_WIDTH, 
				testMotorA, testMotorB);
		navigator = new SimpleNavigator(pilot);
		
		// Generate a new thread to continually update the tachometer count
		// on each of the simulated motors.
		new Thread(new Runnable() {
			@Override
			public void run() {
				while(true) {
					// Update the motors and navigator
					testMotorA.updateTachoValue();
					testMotorB.updateTachoValue();
					navigator.updatePose();
					
					// Report the new position to the game controller (if it exists)
					if(getGameController() != null) {
						getGameController().updateRobotPosition(TestRobotProxy.this, 
								navigator.getPose());
					}
					
					try {
						Thread.sleep(MOTOR_UPDATE_MILLIS);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}).start();
		
		navigator.setMoveSpeed(5);
		navigator.setTurnSpeed(90);
		
		log.info("Generated test robot with identifier: " + identifier);
		
	}
	
	/**
	 * Adds the testing proxy to the proxy's associated lobby.
	 * @param nxtInfo	<not used>
	 */
	public void openConnection(NXTInfo nxtInfo) {
		log.info("Registering test robot proxy: " + getIdentifier());
		getServerLobby().registerRobot(this);
	}

	/**
	 * Logs commands sent to the robot proxy
	 */
	public void sendCommand(RobotCommand command) {
		getRobot().setLastCommand(command);
		log.debug("Wrote to robot: " + getIdentifier() + " - " + command.toString());
		
		switch(command.getType()) {
		case SET_POSITION:
			navigator.setPose(command.getPos());
			break;
		case MOVE_CONTINUOUS:
			navigator.setMoveSpeed(command.getThrottle());
			navigator.forward();
			break;
		case TURN_ANGLE_LEFT:
			navigator.rotate(command.getTurnBearing());
			break;
		case TURN_ANGLE_RIGHT:
			navigator.rotate(-command.getTurnBearing());
			break;
		case STOP:
			navigator.stop();
			break;
		}
	}
}
