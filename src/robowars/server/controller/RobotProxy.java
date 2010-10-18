package robowars.server.controller;

/**
 * Manages communications with a single connected NXT robot.
 */
public class RobotProxy {
	/** 
	 * A string identifier for this robot proxy (either the "friendly" NXT
	 * name or the MAC address of the robot.
	 */
	private String identifier;
	
	/**
	 * Generates a new robot proxy
	 * @param identifier	A string identifier for this robot
	 */
	public RobotProxy(String identifier) {
		this.identifier = identifier;	
	}
	
	/**
	 * @return	The identifier of the robot (either the "friendly" NXT
	 * name or the MAC address of the robot.
	 */
	public String getIdentifier() {
		return identifier;
	}
}
