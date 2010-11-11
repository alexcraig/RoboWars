<<<<<<< HEAD
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
	 * Generates a new control pair from a user and robot proxy
	 * @param user	The user proxy issuing remote control commands
	 * @param robot	The robot proxy of the robot under control
	 */
	public ControlPair(UserProxy user, RobotProxy robot) {
		this.user = user;
		this.robot = robot;
	}
	
	/**
	 * @return	The user proxy of the control pairing
	 */
	public UserProxy getUserProxy() {
		return user;
	}
	
	/**
	 * @return	The robot proxy of the control pairing
	 */
	public RobotProxy getRobotProxy() {
		return robot;
	}
}
=======
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
	 * Generates a new control pair from a user and robot proxy
	 * @param user	The user proxy issuing remote control commands
	 * @param robot	The robot proxy of the robot under control
	 */
	public ControlPair(UserProxy user, RobotProxy robot) {
		this.user = user;
		this.robot = robot;
	}
	
	/**
	 * @return	The user proxy of the control pairing
	 */
	public UserProxy getUserProxy() {
		return user;
	}
	
	/**
	 * @return	The robot proxy of the control pairing
	 */
	public RobotProxy getRobotProxy() {
		return robot;
	}
}
>>>>>>> 9a5d31a390e7d072a22f5916cd27c8da2d306806
