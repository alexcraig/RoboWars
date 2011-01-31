package robowars.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;

import robowars.server.controller.ClientCommand;
import robowars.server.controller.LobbyChatEvent;
import robowars.server.controller.LobbyGameEvent;
import robowars.server.controller.LobbyRobotEvent;
import robowars.server.controller.LobbyUserEvent;

/**
 * A command line configurable TCP connection simulator.
 */
public class ClientSimulator {
	
	/** The network socket to communicate through */
	private Socket streamSocket;
	
	/** The writer to the  log file */
	private PrintWriter logOut;
	
	/** Buffered reader to read from console */
	private BufferedReader consoleIn;
	
	/** The stream to write out to the socket */
	private ObjectOutputStream socketOut;
	
	/** The stream to read in from the socket */
	private ObjectInputStream socketIn;

	/**
	 * ClientSimulator Constructor
	 * 
	 * @param hostname		The host address to connect to
	 * @param port			The port to attempt a connection on
	 * @param username		The username to send to the server upon connection
	 * @param logFilename	The name of the output file to log to
	 */
	public ClientSimulator(String hostname, int port, String username, String logFilename) {
		
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
			socketOut = new ObjectOutputStream(streamSocket.getOutputStream());
			socketIn = new ObjectInputStream(streamSocket.getInputStream());
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
					Object incomingMessage;
					try {
						while (true) {
							incomingMessage = socketIn.readObject();
							if(incomingMessage == null) break;
							
							String msgText = "";
							if(incomingMessage instanceof LobbyChatEvent) msgText = ((LobbyChatEvent)incomingMessage).toString();
							if(incomingMessage instanceof LobbyGameEvent) msgText = ((LobbyGameEvent)incomingMessage).toString();
							if(incomingMessage instanceof LobbyUserEvent) msgText = ((LobbyUserEvent)incomingMessage).toString();
							if(incomingMessage instanceof LobbyRobotEvent) msgText = ((LobbyRobotEvent)incomingMessage).toString();
							
							SimpleDateFormat dateFormat = new SimpleDateFormat("[yyyy/MM/dd h:mm:ss a] ");
							StringBuffer sb = new StringBuffer();
							sb.append("Recv: ");
							sb.append(dateFormat.format(new Date()));
							sb.append(msgText);
							logOut.println(sb.toString());
							System.out.println(sb.toString());
						}
					} catch (IOException e) {
						System.out.println("Lost connection with server, terminating listen thread.");
						System.exit(1);
					} catch (ClassNotFoundException e) {
						System.out.println("Error in reading message from server.");
						System.exit(1);
					}
				}
			}).start();
			
			// Automatically send protocol string and username
			logAndSendUTF("RoboWars V0.2");
			logAndSendUTF(username);
			
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
				logAndSendCmd("q");
				System.out.println("Exiting client simulator.");
				break;
			} else {
				logAndSendCmd(sendMessage);
			}
		}
	}

	/**
	 * Sends the passed message to the network as a plain UTF string, and logs 
	 * the message to screen and to log file. This should only be used to send the
	 * protocol string and username (all subsequent communications should use
	 * serialized ClientCommands).
	 * @param message	The message to send / log
	 */
	private void logAndSendUTF(String message) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("[yyyy/MM/dd h:mm:ss a] ");
		StringBuffer sb = new StringBuffer();
		sb.append("Sent: ");
		sb.append(dateFormat.format(new Date()));
		sb.append(message);
		logOut.println(sb.toString());
		System.out.println(sb.toString());
		
		try {
			socketOut.writeUTF(message);
			socketOut.reset();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Attempts to parse the passed string into a ClientCommand, and sends the
	 * serialized ClientCommand to the server. If no valid client command could
	 * be generated, the entire string is sent as a chat message.
	 * @param message	The client command string to parse. See ClientCommand.parse()
	 * 					for valid formats.
	 */
	private void logAndSendCmd(String cmdString) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("[yyyy/MM/dd h:mm:ss a] ");
		StringBuffer sb = new StringBuffer();
		sb.append("Sent: ");
		sb.append(dateFormat.format(new Date()));
		sb.append(cmdString);
		logOut.println(sb.toString());
		System.out.println(sb.toString());
		
		ClientCommand cmd = ClientCommand.parse(cmdString);
		if(cmd == null) {
			cmd = new ClientCommand(ClientCommand.CHAT_MESSAGE);
			cmd.setStringData(cmdString);
		}
		
		try {
			socketOut.writeObject(cmd);
			socketOut.reset();
		} catch (IOException e) {
			e.printStackTrace();
		}
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
	 * 				[2] - username - the username to send the server upon connection
	 * 				[3] - logging file - the name of the file to log client activity to
	 */
	public static void main(String args[]) {
		if(args.length>=4){
			try {
				int connectionPort = Integer.parseInt(args[1]);
				ClientSimulator c = new ClientSimulator(args[0], connectionPort, args[2], args[3]); 
				c.interactive();
				c.close();
			} catch (NumberFormatException e) {
				System.out.println("Invalid port specified.");
				System.out.println("Argument syntax is: <host_IP> <port> <username> <logging_file_path>");
				System.exit(1);
			}
		}
		else{
			System.out.println("Invalid Number of Arguments.");
			System.out.println("Argument syntax is: <host_IP> <port> <username> <logging_file_path>");
			System.exit(1);
		}
	}
}