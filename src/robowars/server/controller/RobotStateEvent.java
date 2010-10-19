package robowars.server.controller;

/**
 * Event class indicating that a robot proxy has either joined or left a
 * server lobby.
 */
public class RobotStateEvent extends ServerLobbyEvent {
	
	/** The proxy representing the robot who's state has changed */
	private RobotProxy robot;
	
	/**
	 * Generates a new RobotStateEvent
	 * @param src	The ServerLobby that generated the event
	 * @param type	The type of the event (constants defined in ServerLobbyEvent)
	 * @param user	The robot the event refers to
	 */
	public RobotStateEvent(ServerLobby src, int type, RobotProxy robot) {
		super(src, type);
		this.robot = robot;
	}
	
	/**
	 * @return	The robot this event refers to
	 */
	public RobotProxy getRobot() {
		return robot;
	}
	
	/**
	 * @see ServerLobbyEvent#serialize()
	 */
	public String serialize() {
		switch(getEventType()) {
		case ServerLobbyEvent.EVENT_ROBOT_REGISTERED:
			return "[" + ServerLobbyEvent.EVENT_ROBOT_REGISTERED + "|" + robot.getIdentifier() + "]";
		case ServerLobbyEvent.EVENT_ROBOT_UNREGISTERED:
			return "[" + ServerLobbyEvent.EVENT_ROBOT_UNREGISTERED + "|" + robot.getIdentifier() + "]";
		default:
			return "[ERROR]"; // TODO: Throw exception here
		}
	}
}
