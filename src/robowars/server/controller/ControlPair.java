package robowars.server.controller;

/** 
 * Represents a pairing between a UserProxy and a RobotProxy
 * for the purposes of exchanging control commands.
 */
public class ControlPair {
	/** The user issuing remote control commands */
	private UserProxy user;
	
	/** The robot being controlled */
	private RobotProxy robot;

	/**
	 * Generates a new control pair
	 * @param user	The user issuing remote control commands
	 * @param robot	The robot being controlled
	 */
	public ControlPair(UserProxy user, RobotProxy robot) {
		this.user = user;
		this.robot = robot;
	}
	
	/**
	 * @return	The user proxy of the control pairing
	 */
	public UserProxy getUser() {
		return user;
	}
	
	/**
	 * @return	The robot proxy of the control pairing
	 */
	public RobotProxy getRobot() {
		return robot;
	}
}
