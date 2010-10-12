import java.net.*;  // for Socket
import java.io.*;   // for IOException and Input/OutputStream

public class UserProxy extends Thread {

	private InputStream input;
	private OutputStream output;
	private Socket clientSocket;
	private int BUFSIZE = 32;   // Size of receive buffer
	public UserProxy(Socket clientSocket, InputStream input, OutputStream output) {
		this.clientSocket=clientSocket;
		this.input=input;
		this.output=output;
	}
	public void run(){
		byte[] buffer=new byte[BUFSIZE];
		try {
			while(input.read(buffer)!=-1){
				System.out.println("MESSAGE RECEIVED: "+buffer);
				if(buffer.toString()=="q")break;
				else handle(buffer.toString());
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			clientSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/*
	 * commands:
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