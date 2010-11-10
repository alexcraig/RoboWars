package robowars.test;

import org.apache.log4j.Logger;

import lejos.pc.comm.NXTInfo;
import robowars.server.controller.RobotProxy;
import robowars.server.controller.ServerLobby;
import robowars.shared.model.RobotCommand;

/**
 * A version of RobotProxy will all actual network interaction
 * disabled (used for testing purposes).
 */
public class TestRobotProxy extends RobotProxy {
	/** The logger used by this class */
	private static Logger log = Logger.getLogger(TestRobotProxy.class);
	
	/**
	 * Generates a new TestRobotProxy with a dummy name and protocol
	 * @param lobby	The lobby that the TestRobotProxy should add itself to
	 * @param identifier	The identifier of the robot being simulated
	 */
	public TestRobotProxy(ServerLobby lobby, String identifier) {
		super(lobby, new NXTInfo(0, identifier, "dummy:address"));
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
		log.info(this.getIdentifier() + " Command Sent: " + command.getType());
	}
}
