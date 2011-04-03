package robowars.server.controller;

/**
 * Event class indicating that a robot proxy has either joined or left a
 * server lobby.
 * 
 * @author Alexander Craig
 */
public class LobbyRobotEvent extends ServerLobbyEvent {
	private static final long serialVersionUID = -6157884753933448378L;

	/** The proxy representing the robot who's state has changed */
	private transient RobotProxy robot;
	
	/** 
	 * The identifier of the robot (stored separately as the RobotProxy object
	 * will not be serialized for network transmission).
	 */
	private String robotIdentifier;
	
	/**
	 * Generates a new LobbyRobotEvent
	 * @param src	The ServerLobby that generated the event
	 * @param type	The type of the event (constants defined in ServerLobbyEvent)
	 * @param user	The robot the event refers to
	 */
	public LobbyRobotEvent(ServerLobby src, int type, RobotProxy robot) {
		super(src, type);
		this.robot = robot;
		robotIdentifier = robot.getIdentifier();
	}
	
	/**
	 * @return	The robot this event refers to
	 */
	public RobotProxy getRobot() {
		return robot;
	}
	
	/**
	 * @return	The identifier of the robot this event refers to (will provide a valid
	 * 			result even after serialization)
	 */
	public String getRobotIdentifier() {
		return robotIdentifier;
	}
	
	/**
	 * @return	A plain text string description of the event
	 */
	public String toString() {
		switch(getEventType()) {
		case EVENT_ROBOT_REGISTERED:
			return robotIdentifier + " has registered with the server.";
		case EVENT_ROBOT_UNREGISTERED:
			return robotIdentifier + " has unregistered with the server.";
		default:
			return "UNKNOWN EVENT TYPE";
		}
	}
}
