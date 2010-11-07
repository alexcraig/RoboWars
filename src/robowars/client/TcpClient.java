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
public class TcpClient extends Thread implements MessageType
{
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
		handshake();
		
        /* Run forever, handling incoming messages. */
        String response;
        try { while ((response = in.readLine()) != null) handle(response); }
        catch (IOException e) { model.printMessage(ERROR, "Lost connection to the server."); }
        finally {
        	try {
        		sendMessage("q");		// Quit
				out.close();
				in.close();
				socket.close();
				model.printMessage(EVENT, "Socket Disconnected!");
			} catch (IOException e) {
				model.printMessage(ERROR, "Could not close socket.");
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
	 * Provide initial information to the server.
	 */
	public void handshake()
	{
		if (model.getMyUser() != null)
		{
			
		}
		sendMessage("JoeUser");	
	}
	
	/**
	 * @param message
	 * Determines what to do, given an input message.
	 */
	public void handle(String message)
	{
		//TODO: Handle de-serializing.
		model.printMessage(EVENT, message);
	}
	
	/**
	 * @param message
	 * Sends a command to the server.
	 */
	public void sendMessage(String message)
	{
		if (connected) out.println(message);
	}
	
	private void printMessage(int type, String message)
	{
		model.printMessage(type, message);
	}
}
