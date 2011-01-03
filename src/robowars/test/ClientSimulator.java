package robowars.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;

import robowars.server.controller.SystemControl;

/**
 * A command line configurable TCP connection simulator.
 */
public class ClientSimulator {
	
	/** The network socket to communicate through */
	private Socket streamSocket;
	
	/** The writers to the connection socket and log file */
	private PrintWriter socketOut, logOut;
	
	/** Buffered readers to read from console, and socket */
	private BufferedReader consoleIn, socketIn;

	/**
	 * ClientSimulator Constructor
	 * 
	 * @param hostname		The host address to connect to
	 * @param port			The port to attempt a connection on
	 * @param logFilename	The name of the output file to log to
	 */
	public ClientSimulator(String hostname, int port, String logFilename) {
		
		try {
			// Bind a socket to any available port on the local host machine.
			streamSocket = new Socket(hostname.split(":")[0], port);
			System.out.println("Client simulator connected to " + streamSocket.getInetAddress().getHostName());
		} catch (UnknownHostException e1) {
			System.err.println("Hostname is unknown.");
			System.exit(1);
		} catch (IOException e2) {
			System.err.println("Couldn't get port.");
			System.exit(1);
		}
		
		try {
			// Setup the socket input/output and console input
			socketOut = new PrintWriter(streamSocket.getOutputStream(), true);
			socketIn = new BufferedReader(new InputStreamReader(streamSocket.getInputStream()));
			consoleIn = new BufferedReader(new InputStreamReader(System.in));
			
			// Setup the log file
			File logOutFile = new File(logFilename);
			if (logOutFile.exists()) {
				System.out.println("Log file exists, appending to existing log file.");
			} else {
				System.out.println("Generating new log file.");
				logOutFile.createNewFile();
			}
			logOut = new PrintWriter(new FileWriter(logOutFile, true), true);
			
			// Start the thread to receive messages from the socket
			new Thread(new Runnable() {

				@Override
				public void run() {
					String incomingMessage;
					try {
						while ((incomingMessage = socketIn.readLine()) != null) {
							SimpleDateFormat dateFormat = new SimpleDateFormat("[yyyy/MM/dd h:mm:ss a] ");
							StringBuffer sb = new StringBuffer();
							sb.append("Recv: ");
							sb.append(dateFormat.format(new Date()));
							sb.append(incomingMessage);
							logOut.println(sb.toString());
							System.out.println(sb.toString());
							
							// Addition - Automatic response to protocol string
							if(incomingMessage.equals(SystemControl.USER_PROTOCOL_VERSION)) {
								logAndSend(SystemControl.USER_PROTOCOL_VERSION);
							}
						}
					} catch (IOException e) {
						System.out.println("Lost connection with server, terminating listen thread.");
						System.exit(1);
					}
				}
			}).start();
			
		} catch (IOException e2) {
			System.err.println("Couldn't get I/O connection:");
			System.err.println(e2.getMessage());
			System.exit(1);
		}
	}
	
	/**
	 * Starts the interactive mode of the client simulator. The client will run
	 * until a "QUIT" command is entered by the user.
	 */
	public void interactive() {
		System.out.println("Client simulator succesfully initiated connection.");
		System.out.println("Type any message to send string, or \"QUIT\" to exit client.");
		
		while(true) {
			String sendMessage = "";
			System.out.print("> ");
			try {
				sendMessage = consoleIn.readLine();
			} catch (IOException e) {
				System.err.println("Error fetching input from console.");
				e.printStackTrace();
				break;
			}
			
			if(sendMessage.equals("QUIT")) {
				System.out.println("Exitting client simulator.");
				break;
			} else {
				logAndSend(sendMessage);
			}
		}
	}

	/**
	 * Sends the passed message to the network, and logs the message to screen and to log file.
	 * @param message	The message to send / log
	 */
	private void logAndSend(String message) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("[yyyy/MM/dd h:mm:ss a] ");
		StringBuffer sb = new StringBuffer();
		sb.append("Sent: ");
		sb.append(dateFormat.format(new Date()));
		sb.append(message);
		logOut.println(sb.toString());
		System.out.println(sb.toString());
		socketOut.println(message);
	}

	/**
	 * Closes all possible open streams.
	 */
	public void close() {
		try {
			socketOut.close();
			consoleIn.close();
			streamSocket.close();
			logOut.close();
		} catch (IOException e) {
			System.err.println("Couldn't get I/O for the connection");
			System.exit(1);
		}
	}

	/**
	 * Starts the TCP client simulator
	 * @param args	[0] - hostname / ip - the hostname or ip address to connect to
	 * 				[1] - port - the port to attempt a connection on
	 * 				[2] - logging file - the name of the file to log client activity to
	 */
	public static void main(String args[]) {
		if(args.length>=3){
			try {
				int connectionPort = Integer.parseInt(args[1]);
				ClientSimulator c = new ClientSimulator(args[0], connectionPort, args[2]); 
				c.interactive();
				c.close();
			} catch (NumberFormatException e) {
				System.out.println("Invalid port specified.");
				System.out.println("Argument syntax is: <host_IP> <port> <logging_file_path>");
				System.exit(1);
			}
		}
		else{
			System.out.println("Invalid Number of Arguments.");
			System.out.println("Argument syntax is: <host_IP> <port> <logging_file_path>");
			System.exit(1);
		}
	}
}