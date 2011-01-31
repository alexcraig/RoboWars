package robowars.server.controller;

import java.io.Serializable;

/**
 * Represents a command sent from a mobile client to the RoboWars server.
 * This includes functionality such as setting spectator status, setting
 * ready status, setting the current gametype, and sending gameplay commands.
 * 
 * Valid command formats:
 * CHAT_MESSAGE - String<Chat Message>
 * READY_STATUS - Boolean
 * SPECTATOR_STATUS - Boolean
 * GAME_TYPE_CHANGE - String<Name of Game Type>
 * LAUNCH_GAME - No Data
 * GAMEPLAY_COMMAND - Orientation Floats, String<Optional - Buttons Pressed>
 * DISCONNECT - No Data
 */
public class ClientCommand implements Serializable {
	private static final long serialVersionUID = 1832713935557007848L;

	/** Constants used to indicate command type */
	public static final int CHAT_MESSAGE = 0;
	public static final int READY_STATUS = 1;
	public static final int SPECTATOR_STATUS = 2;
	public static final int GAME_TYPE_CHANGE = 3;
	public static final int LAUNCH_GAME = 4;
	public static final int GAMEPLAY_COMMAND = 5;
	public static final int DISCONNECT = 6;
	
	/** 
	 * Boolean flag used to indicate status for status changing messages
	 * (Changing ready or spectator status)
	 */
	private Boolean boolData;
	
	/**
	 * String field used for holding data for commands requiring a string
	 * (Chat messages, game type changes, and an optional "buttons pressed"
	 * string for gameplay commands)
	 */
	private String stringData;
	
	/** The type of the command (constants defined in ClientCommand) */
	private Integer commandType;
	
	/** 
	 * Floats storing the orientation of the phone (normalized to a +1 to -1 range).
	 * These should only be non-null for gameplay commands.
	 */
	private Float azimuth, pitch, roll;
	
	/** 
	 * Generates a new ClientCommand with all fields (except commandType)
	 * set to null.
	 * @param commandType	The type of ClientCommand to generate
	 */
	public ClientCommand(int commandType) {
		this.commandType = commandType;
		boolData = null;
		stringData = null;
		azimuth = null;
		pitch = null;
		roll = null;
	}
	
	/**
	 * @return	The type of the command (constants defined in ClientCommand)
	 */
	public int getCommandType() {
		return commandType;
	}
	
	/**
	 * Sets the boolean data carried by the command.
	 * @param data	The boolean value to be carried by the command
	 */
	public void setBoolData(Boolean data) {
		boolData = data;
	}
	
	/**
	 * @return	The boolean data carried by the command.
	 */
	public Boolean getBoolData() {
		return boolData;
	}
	
	/**
	 * Sets the string data carried by the command.
	 * @param data	The string value to be carried by the command
	 */
	public void setStringData(String data) {
		stringData = data;
	}
	
	/**
	 * @return	The string data carried by the command
	 */
	public String getStringData() {
		return stringData;
	}
	
	/**
	 * Sets the orientation data carried by the command. All orientation values
	 * should be scaled to a range of 1 to -1.
	 * @param azimuth	The azimuth (compass heading) of the client (usually not used)
	 * @param pitch	The pitch of the client
	 * @param roll	The roll of the client
	 */
	public void setOrientation(float azimuth, float pitch, float roll) {
		this.azimuth = azimuth;
		this.pitch = pitch;
		this.roll = roll;
	}
	
	/**
	 * @return	The azimuth value carried by the command (or null if none was set)
	 */
	public Float getAzimuth() {
		return azimuth;
	}
	
	/**
	 * @return	The pitch value carried by the command (or null if none was set)
	 */
	public Float getPitch() {
		return pitch;
	}
	
	/**
	 * @return	The roll value carried by the command (or null if none was set)
	 */
	public Float getRoll() {
		return roll;
	}
	
	/**
	 * Converts a string from the RoboWars V0.1 command format into a ClientCommand
	 * object. Valid formats are:
	 * 
	 * m:<message> - chat message 
	 * r:<t or f> - set ready state
	 * s:<t or f> - set pure spectator state
	 * g:<game_type_string> - set game type
	 * c:<x,y,z> or c:<x,y,z>button_string  or c:button_string
	 * 		a command string to be passed to a paired robot
	 * 		(Note: If no gyro is available tilt should always be <0,0,0>
	 * l - launch game
	 * q - disconnect
	 * 
	 * @param commandString	The string to generate a command from (WARNING: case sensitive)
	 * @return	The generated client command, or null if no command could be generated.
	 */
	public static ClientCommand parse(String commandString) {
		if(commandString.equals("l")) {
			return new ClientCommand(LAUNCH_GAME);
			
		} else if (commandString.equals("q")) {
			return new ClientCommand(DISCONNECT);
			
		} else if (commandString.startsWith("r:")) {
			ClientCommand readyCmd = new ClientCommand(READY_STATUS);
			if(commandString.substring(2, 3).equals("t")) {
				readyCmd.setBoolData(true);
				return readyCmd;
			} else if (commandString.substring(2,3).equals("f")) {
				readyCmd.setBoolData(false);
				return readyCmd;
			}
			
		} else if (commandString.startsWith("s:")) {
			ClientCommand specCmd = new ClientCommand(SPECTATOR_STATUS);
			if(commandString.substring(2, 3).equals("t")) {
				specCmd.setBoolData(true);
				return specCmd;
			} else if (commandString.substring(2,3).equals("f")) {
				specCmd.setBoolData(false);
				return specCmd;
			}
			
		} else if (commandString.startsWith("g:")) {
			ClientCommand gameCmd = new ClientCommand(GAME_TYPE_CHANGE);
			gameCmd.setStringData(commandString.substring(2));
			return gameCmd;
			
		} else if (commandString.startsWith("m:")) {
			ClientCommand chatCmd = new ClientCommand(CHAT_MESSAGE);
			chatCmd.setStringData(commandString.substring(2));
			return chatCmd;
			
		} else if (commandString.startsWith("c:")) {
			ClientCommand gameplayCmd = new ClientCommand(GAMEPLAY_COMMAND);
			
			if(commandString.substring(2,2).equals("<") && commandString.contains(">")) {
				// Orientation vector was provided
				String vectorString = commandString.substring(3, commandString.indexOf(">"));
				commandString = commandString.substring(commandString.indexOf(">") + 1);
				
				Float azimuth, pitch, roll = null;
				try {
					// Read float values from string
					azimuth = Float.parseFloat(vectorString.substring(0, vectorString.indexOf(",")));
					vectorString = vectorString.substring(vectorString.indexOf(",") + 1);
					
					pitch = Float.parseFloat(vectorString.substring(0, vectorString.indexOf(",")));
					vectorString = vectorString.substring(vectorString.indexOf(",") + 1);
					
					roll = Float.parseFloat(vectorString);
					
					gameplayCmd.setOrientation(azimuth, pitch, roll);
				} catch (NumberFormatException e) {
					// Error reading orientation values, return null
					return null;
				}
			}
			
			// If any string was supplied beyond the orientation string, add it to the generated command
			if(commandString.length() > 0) {
				gameplayCmd.setStringData(commandString);
			}
			return gameplayCmd;
		}
		
		return null;
	}
}
