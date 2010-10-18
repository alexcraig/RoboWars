package robowars.server.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Manages communications with a single user connected through an existing 
 * TCP socket. 
 */
public class UserProxy implements Runnable {

	/** Reader for client input */
	private BufferedReader input;
	
	/** Writer for client output */
	private PrintWriter output;
	
	/** The socket to generate input/output streams for */
	private Socket clientSocket;
	
	/**
	 * Generates a new UserProxy
	 * @param clientSocket	The connected socket to service
	 */
	public UserProxy(Socket clientSocket) {
		this.clientSocket=clientSocket;
		input = null;
		output = null;
	}
	
	public void run(){
		
		System.out.println("UserProxy: Opening input/output streams.");
		try {
			this.input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			this.output = new PrintWriter(clientSocket.getOutputStream(), true);
		} catch (IOException e) {
			System.out.println("UserProxy: ERROR - failed to open input/output streams.");
			e.printStackTrace();
		}
		
		String incomingMessage;
		try {
			
			// Read strings from socket until connection is terminated
			while ((incomingMessage = input.readLine()) != null) {
				System.out.println("UserProxy: Received: " + incomingMessage);
				handle(incomingMessage);
			}
			System.out.println("UserProxy: Client terminated connection with server.");
			
		} catch (IOException e) {
			System.out.println("UserProxy: Client terminated connection with server.");
		} finally {
			try {
				clientSocket.close();
			} catch (IOException e) {
				System.out.println("UserProxy: WARNING - could not close client socket.");
			}
		}
		
	}
	
	/**
	 * Dispatches user input to the relevant processing functions based on the input received.
	 * 
	 * Commands:
	 * h - hello
	 * m<x,y,z> - movement <x,y,z> angle on each axis
	 * s - shoot
	 * q - quit
	 */
	private void handle(String command){
		if(command.equals("h")){}
		else if(command.contains("m")){}
		else if(command.equals("s")){}
	}
}