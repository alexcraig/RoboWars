package com.RoboWars;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import robowars.server.controller.ClientCommand;
import robowars.server.controller.LobbyChatEvent;
import robowars.server.controller.LobbyGameEvent;
import robowars.server.controller.LobbyRobotEvent;
import robowars.server.controller.LobbyUserEvent;
import robowars.server.controller.ServerLobbyEvent;
import robowars.shared.model.CameraPosition;
import robowars.shared.model.GameEvent;
import android.util.Log;

/**
 * @author Steve Legere
 * @author Alex Craig
 * @version 03/03/2011
 * 
 * Handles TCP connection to the server.
 * @see robowars.server.controller
 */
public class TcpClient extends Thread
{
	/* Models modified by incoming packets from server. */
	private LobbyModel lobbyModel;
	private GameModel gameModel;
	
	/* Server information. */
	private String IPAddress;
	private int port;
	private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private boolean connected;
    
    /**
     * Default constructor.
     * 
     * @param lobbyModel	The current state of the lobby.
     * @param gameModel		The current state of the game.
     */
	public TcpClient(LobbyModel lobbyModel, GameModel gameModel)
	{
		this.lobbyModel = lobbyModel;
		this.gameModel = gameModel;
		this.connected = false;
	}
	
	public void run()
    {
		if (!handshake()) return;
		
		Object response;
        
        /* Run forever, handling incoming messages. */
        try {
        	while (true) {
        		response = in.readObject();
        		if(response == null) break;
        		
        		// Log.i("RoboWars", "Read object.");
        		if(response instanceof String) {
        			Log.i("RoboWars", "Read string: " + (String)response);
        			printMessage(LobbyModel.EVENT, (String)response);
        		} else if (response instanceof ServerLobbyEvent) {
        			Log.i("RoboWars", "Read lobby event.");
        			handle((ServerLobbyEvent)response);
        		} else if (response instanceof GameEvent) {
        			Log.i("RoboWars", "Read game event.");
        			handle((GameEvent)response);
        		}
        		
        		Thread.yield();
        	}
        } catch (IOException e) { printMessage(LobbyModel.ERROR, "Lost connection to the server."); 
        } catch (ClassNotFoundException e) { printMessage(LobbyModel.ERROR, "Could not deserialize message from server."); 
        } finally {
        	try {
        		// Send disconnection message, then close socket.
        		sendClientCommand(new ClientCommand(ClientCommand.DISCONNECT));
				out.close();
				in.close();
				socket.close();
				printMessage(LobbyModel.EVENT, "Socket Disconnected!");
			} catch (IOException e) {
				printMessage(LobbyModel.ERROR, "Could not close socket.");
			}
        }
    }
	
	/**
	 * Connect to the server's IP and port over TCP.
	 * 
	 * @param IPAddress		IPv4 address of the server (ie "127.0.0.1").
	 * @param port			The port to connect to (by default 33330).
	 */
	public void connect(String IPAddress, int port)
	{
		this.IPAddress = IPAddress;
		this.port = port;
		
		printMessage(LobbyModel.EVENT, "Connecting...");
		try {
            socket = new Socket(IPAddress, port);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
            
            connected = true;
            
            printMessage(LobbyModel.EVENT, "Connected!");
        } catch (UnknownHostException e1) {
            printMessage(LobbyModel.ERROR, "Could not resolve host.");
            printMessage(LobbyModel.ERROR, "Address: " + IPAddress + ":" + port);
        } catch (IOException e2) {
            printMessage(LobbyModel.ERROR, "Could not get I/O for the connection.");
            printMessage(LobbyModel.ERROR, "Address: " + IPAddress + ":" + port);
        }
		
        if (connected) this.start();
	}
	
	/**
	 * Sends a string to the server in UTF format. This should only be used
	 * for the connection handshake (protocol string and username), as all
	 * further communication should used RoboWars protocol 
	 * (Serialized ServerLobbyEvents and ClientCommands).
	 * 
	 * @param message	The string to be sent to the server.
	 */
	public void sendUTFString(String message)
	{
		if (connected) {
				synchronized(out) {
				try {
					out.writeUTF(message);
					out.reset();
				} catch (IOException e) {
					// TODO: Properly log / notify user of error
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * Sends a command to the server via TCP.
	 * 
	 * @param cmd	The ClientCommand being sent.
	 * @see robowars.server.controller.ClientCommand
	 */
	public void sendClientCommand(ClientCommand cmd) {
		if (connected) {
			synchronized(out) {
				try {
					out.writeObject(cmd);
					out.reset();
				} catch (IOException e) {
					// TODO: Properly log / notify user of error
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * Provide initial information to the server.
	 */
	private boolean handshake()
	{
		String version = lobbyModel.getVersion();
		User user = lobbyModel.getMyUser();
		if (user != null) {
			sendUTFString(version);
			sendUTFString(user.getName());
			return true;
		}
		else {
			printMessage(LobbyModel.ERROR, "No username set.");
			return false;
		}
	}
	
	/**
	 * Handles TCP packets as they arrive regarding game events.
	 * 
	 * @param event		The EventObject being passed in.
	 */
	private void handle(GameEvent event)
	{
		switch (event.getEventType()) {
			case GameEvent.GAME_START:
				gameModel.startGame();
				//TODO: Start up the OpenGL engine and start the game.
				break;
								
			case GameEvent.GAME_OVER:
				gameModel.endGame();
				//TODO: End the game, show the winner.
				break;
				
			case GameEvent.COLLISION_DETECTED:
				//TODO: Probably nothing here; handled by the server.
				break;
				
			case GameEvent.PROJECTILE_FIRED:
				//TODO: Get location, direction, speed of projectile.
				//TODO: Draw the projectile in OpenGL.
				break;
				
			case GameEvent.PROJECTILE_HIT:
				//TODO: Get location of projectile.
				//TODO: Remove from OpenGL view.
				break;
				
			case GameEvent.PLAYER_1_WINS:
				//TODO: Display which player wins, clear the map, ask for another game.
				break;
				
			case GameEvent.PLAYER_2_WINS:
				//TODO: Display which player wins, clear the map, ask for another game.
			
			case GameEvent.ROBOT_MOVED:
				//TODO: Which robot moved? Update appropriate position and OpenGL.
				break;
				
			case GameEvent.MAP_CHANGED:
				//TODO: Compare the passed model to the current model and make
				//		the appropriate changes.
				break;
				
			default:
				// Unhandled GameEvent.
				printMessage(LobbyModel.ERROR, "Unhandled GameEvent received.");
		}
	}
	
	/**
	 * Handles TCP packets as they arrive regarding lobby events.
	 * 
	 * @param event		The EventObject being passed in.
	 */
	private void handle(ServerLobbyEvent event)
	{
		// User events
		if(event instanceof LobbyUserEvent) {
			LobbyUserEvent userEvent = (LobbyUserEvent)event;
			switch(userEvent.getEventType()) {
			case ServerLobbyEvent.EVENT_PLAYER_JOINED:
				// Player joined
				lobbyModel.userJoined(userEvent.getUser().getUsername());
				printMessage(LobbyModel.EVENT, event.toString());
				return;
			case ServerLobbyEvent.EVENT_PLAYER_LEFT:
				// Player left
				lobbyModel.userLeft(userEvent.getUser().getUsername());
				printMessage(LobbyModel.EVENT, event.toString());
				return;
			case ServerLobbyEvent.EVENT_PLAYER_STATE_CHANGE:
				// Player state changed
				lobbyModel.printMessage(LobbyModel.EVENT, event.toString());
				return;
			default:
				return;
			}
		}
		
		// Robot events
		if(event instanceof LobbyRobotEvent) {
			LobbyRobotEvent robotEvent = (LobbyRobotEvent)event;
			lobbyModel.printMessage(LobbyModel.EVENT, event.toString());
		}
		
		// Game events
		if(event instanceof LobbyGameEvent) {
			LobbyGameEvent gameEvent = (LobbyGameEvent)event;
			if(gameEvent.getCameraPosition() != null) {
				CameraPosition cam = gameEvent.getCameraPosition();
				lobbyModel.printMessage(LobbyModel.EVENT, "Got camera information: <X:" + cam.getxPos()
						+ "|Y:" + cam.getyPos() + "|X:" + cam.getzPos() +"|HorOrien:"
						+ cam.getHorOrientation() + "|VerOrien:" + cam.getVerOrientation()
						+ "|FOV:" + cam.getFov()+ ">");
			}
			lobbyModel.printMessage(LobbyModel.EVENT, event.toString());
		}
		
		// Chat events
		if(event instanceof LobbyChatEvent) {
			LobbyChatEvent chatEvent = (LobbyChatEvent)event;
			lobbyModel.printMessage(LobbyModel.EVENT, event.toString());
		}
	}
	
	/**
	 * Prints a message on the client lobby terminal.
	 * 
	 * @param type		The type of message (defined in this class).
	 * @param message	The message String.
	 */
	private void printMessage(int type, String message)
	{
		lobbyModel.printMessage(type, message);
	}
}
