package robowars.client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.IOException;

import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Client-side TCP connection thread.
 * 
 * TODO: TcpClient is passed a reference to the view, this
 * 		 will be changed shortly.
 */
public class TcpClient extends Thread
{
	private RoboWars view;
	
	private String IPAddress;
	private int port;
	
	private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    
    private boolean connected;
    
	public TcpClient(RoboWars view)
	{
		this.view = view;
		this.connected = false;
	}
	
	public void run()
    {
		/* Initial handshake... */
		sendMessage("JoeUser");		//TODO: Username
		
        /* Run forever, handling incoming messages. */
        String response;
        try { while ((response = in.readLine()) != null) handle(response); }
        catch (IOException e) { view.printMessage("Lost connection to the server."); }
        finally {
        	try {
				out.close();
				in.close();
				socket.close();
				view.printMessage("Socket Disconnected!");
			} catch (IOException e) {
				view.printMessage("Could not close socket.");
			}
        }
    }
	
	public void connect(String IPAddress, int port)
	{
		this.IPAddress = IPAddress;
		this.port = port;
		
		view.printMessage("Connecting...");
		try {
            socket = new Socket(IPAddress, port);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            
            connected = true;
            view.printMessage("Connected!");
        } catch (UnknownHostException e1) {
            view.printMessage("Could not resolve host.");
            view.printMessage("Address: " + IPAddress + ":" + port);
        } catch (IOException e2) {
            view.printMessage("Could not get I/O for the connection.");
            view.printMessage("Address: " + IPAddress + ":" + port);
        }
		
        if (connected) this.start();
	}
	
	public void handle(String message)
	{
		view.printMessage(message);
	}
	
	public void sendMessage(String message)
	{
		if (connected) out.println(message);
	}
}
