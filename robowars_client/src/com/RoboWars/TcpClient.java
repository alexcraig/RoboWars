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
import robowars.shared.model.GameEvent;
import android.util.Log;

/**
 * @author Steve Legere
 * @version 5/11/2010
 * 
 * Handles TCP connection to the server.
 * @see robowars.server.controller
 */
public class TcpClient extends Thread
{
	private static final int ERROR	= 0;
	private static final int CHAT	= 1;
	private static final int EVENT	= 2;
	
	private LobbyModel model;
	
	private String IPAddress;
	private int port;
	
	private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private boolean connected;
    
	public TcpClient(LobbyModel model)
	{
		this.model = model;
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
        			printMessage(EVENT, (String)response);
        		} else if (response instanceof ServerLobbyEvent) {
        			Log.i("RoboWars", "Read lobby event.");
        			handle((ServerLobbyEvent)response);
        		} else if (response instanceof GameEvent) {
        			Log.i("RoboWars", "Read game event.");
        		}
        		
        		Thread.yield();
        	}
        } catch (IOException e) { printMessage(ERROR, "Lost connection to the server."); 
        } catch (ClassNotFoundException e) { printMessage(ERROR, "Could not deserialize message from server."); 
        } finally {
        	try {
        		sendClientCommand(new ClientCommand(ClientCommand.DISCONNECT));
				out.close();
				in.close();
				socket.close();
				printMessage(EVENT, "Socket Disconnected!");
			} catch (IOException e) {
				printMessage(ERROR, "Could not close socket.");
			}
        }
    }
	
	public void connect(String IPAddress, int port)
	{
		this.IPAddress = IPAddress;
		this.port = port;
		
		printMessage(EVENT, "Connecting...");
		try {
            socket = new Socket(IPAddress, port);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
            
            connected = true;
            
            printMessage(EVENT, "Connected!");
        } catch (UnknownHostException e1) {
            printMessage(ERROR, "Could not resolve host.");
            printMessage(ERROR, "Address: " + IPAddress + ":" + port);
        } catch (IOException e2) {
            printMessage(ERROR, "Could not get I/O for the connection.");
            printMessage(ERROR, "Address: " + IPAddress + ":" + port);
        }
		
        if (connected) this.start();
	}
	
	/**
	 * Sends a string to the server in UTF format. This should only be used
	 * for the connection handshake (protocol string and username), as all
	 * further communication should used RoboWars protocol 
	 * (Serialized ServerLobbyEvents and ClientCommands).
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
		String version = model.getVersion();
		User user = model.getMyUser();
		if (user != null) {
			sendUTFString(version);
			sendUTFString(user.getName());
			return true;
		}
		else {
			printMessage(ERROR, "No username set.");
			return false;
		}
	}
	
	/**
	 * @param message
	 * Determines what to do, given an input message.
	 */
	private void handle(ServerLobbyEvent event)
	{
		// User events
		if(event instanceof LobbyUserEvent) {
			LobbyUserEvent userEvent = (LobbyUserEvent)event;
			switch(userEvent.getEventType()) {
			case ServerLobbyEvent.EVENT_PLAYER_JOINED:
				// Player joined
				model.userJoined(userEvent.getUser().getUsername());
				printMessage(EVENT, event.toString());
				return;
			case ServerLobbyEvent.EVENT_PLAYER_LEFT:
				// Player left
				model.userLeft(userEvent.getUser().getUsername());
				printMessage(EVENT, event.toString());
				return;
			case ServerLobbyEvent.EVENT_PLAYER_STATE_CHANGE:
				// Player state changed
				model.printMessage(EVENT, event.toString());
				return;
			default:
				return;
			}
		}
		
		// Robot events
		if(event instanceof LobbyRobotEvent) {
			LobbyRobotEvent robotEvent = (LobbyRobotEvent)event;
			model.printMessage(EVENT, event.toString());
		}
		
		// Game events
		if(event instanceof LobbyGameEvent) {
			LobbyGameEvent gameEvent = (LobbyGameEvent)event;
			model.printMessage(EVENT, event.toString());
		}
		
		// Chat events
		if(event instanceof LobbyChatEvent) {
			LobbyChatEvent chatEvent = (LobbyChatEvent)event;
			model.printMessage(EVENT, event.toString());
		}
	}
	
	private void printMessage(int type, String message)
	{
		model.printMessage(type, message);
	}
}
