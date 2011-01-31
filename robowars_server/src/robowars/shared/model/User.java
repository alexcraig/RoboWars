package robowars.shared.model;

import java.io.Serializable;

/**
* Represents a single user connected to the system. This class
* is responsible for storing all information regarding user state,
* such as their selected user name, ready status and spectator
* status.
*/
public class User implements Serializable {
	private static final long serialVersionUID = -933378756508773869L;

	/** The username of the connected user */
	private String username;

	/** The "ready" status of the user (used to determine if a new game can start */
	private boolean isReady;

	/**
	* Flag indicating whether a user is a pure spectator. If true, the user should
	* not be considered for control pairing with robots.
	*/
	private boolean isPureSpectator;

	/**
	* Generates a new User object with a false ready and spectator state.
	* @param username The user name selected by the user.
	*/
	public User(String username) {
		this.username = username;
		isReady = false;
		isPureSpectator = false;
	}

	/**
	* Sets the ready status of the user.
	* @param isReady The ready status of the user (true if a new game can start)
	*/
	public void setReady(boolean isReady) {
		this.isReady = isReady;
	}

	/**
	* @return The ready status of the user.
	*/
	public boolean isReady() {
		return isReady;
	}

	/**
	* Sets the registered username of the connected user
	* @param username The new username
	*/
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	* @return The username of the connected user
	*/
	public String getUsername() {
		return username;
	}

	/**
	* Sets the spectator status of the user.
	* @param isSpectator The new spectator status of the user.
	*/
	public void setPureSpectator(boolean isSpectator) {
		isPureSpectator = isSpectator;
	}

	/**
	* @return True if the user is a pure spectator (has opted-out of robot control)
	*/
	public boolean isPureSpectator() {
		return isPureSpectator;
	}
}

