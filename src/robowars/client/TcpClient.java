package robowars.client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.IOException;

import java.net.Socket;
import java.net.UnknownHostException;

/**
 * @author Steve Legere
 * @version 5/11/2010
 * 
 * Handles TCP connection to the server.
 * @see robowars.server.controller
 */
public class TcpClient extends Thread
{
	public static final int ERROR	= 0;
	public static final int CHAT	= 1;
	public static final int EVENT	= 2;
	
	private LobbyModel model;
	
	private String IPAddress;
	private int port;
	
	private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private boolean connected;
    
	public TcpClient(LobbyModel model)
	{
		this.model = model;
		this.connected = false;
	}
	
	public void run()
    {
		if (!handshake()) return;
		
        String response;
        
        /* Run forever, handling incoming messages. */
        try { while ((response = in.readLine()) != null) handle(response); }
        catch (IOException e) { printMessage(ERROR, "Lost connection to the server."); }
        finally {
        	try {
        		sendMessage("q");
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
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            
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
	 * @param message
	 * Sends a command to the server.
	 */
	public void sendMessage(String message)
	{
		if (connected) out.println(message);
	}
	
	/**
	 * Provide initial information to the server.
	 */
	private boolean handshake()
	{
		User user = model.getMyUser();
		if (user != null) {
			sendMessage(user.getName());
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
	private void handle(String message)
	{
		if (message.startsWith("[0"))
		{
			// User joined lobby.
			String username = message.substring(3, message.length()-1);
			model.userJoined(username);
			printMessage(EVENT, username + " has connected to the server.");
		}
		else if (message.startsWith("[1"))
		{
			// User left lobby.
			String username = message.substring(3, message.length()-1);
			model.userLeft(username);
			printMessage(EVENT, username + " has left the server.");
		}
		else if (message.startsWith("[2"))
		{
			// Chat message event.
			String msg = message.substring(3, message.length()-1);
			model.printMessage(CHAT, msg);
		}
		else if (message.startsWith("[3"))
		{
			// Robot joined server.
			String msg = message.substring(3, message.length()-1);
			model.printMessage(EVENT, msg);
		}
		else if (message.startsWith("[4"))
		{
			// Robot left server.
			String msg = message.substring(3, message.length()-2);
			model.printMessage(EVENT, msg);
		}
		else if (message.startsWith("[5"))
		{
			// Player state changed.
			String msg = message.substring(3, message.length()-2);
			model.printMessage(EVENT, msg);
		}
		else if (message.startsWith("[6"))
		{
			// Game launch.
			String msg = "Launching game...";
			model.printMessage(EVENT, msg);
		}
		else if (message.startsWith("[7"))
		{
			// Game ended.
			String msg = "Game terminated.";
			model.printMessage(EVENT, msg);
		}
		else if (message.startsWith("[8"))
		{
			// Gametype change.
			String msg = "Game mode change to: " + message.substring(3, message.length()-2);
			model.printMessage(EVENT, msg);
		}
		else
		{
			// Not a command... Just print for now.
			model.printMessage(EVENT, message);
		}
	}
	
	
	private void printMessage(int type, String message)
	{
		model.printMessage(type, message);
	}
}
