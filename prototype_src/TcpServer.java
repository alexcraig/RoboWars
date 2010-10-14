import java.net.*;  // for Socket, ServerSocket, and InetAddress
import java.util.ArrayList;
import java.io.*;   // for IOException and Input/OutputStream

public class TcpServer extends Thread{
	
	private int PORT= 33330;
	private ArrayList<UserProxy> proxies;
	public void run(){
   
	  proxies=new ArrayList<UserProxy>();
	  ServerSocket socket = null;
	  try {
		  socket = new ServerSocket(PORT);
	  } catch (IOException e) {
			
		  e.printStackTrace();
	  }

	  while(true) { // Run forever, accepting and servicing connections
	    	Socket clientSocket = null;
			try {
				clientSocket = socket.accept();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}     // Get client connection
	
			System.out.println("Client: " +clientSocket.getInetAddress().getHostAddress() + " Port: " +clientSocket.getPort());
			InputStream in = null;
			OutputStream out = null;
			try {
				in = clientSocket.getInputStream();
				out = clientSocket.getOutputStream();
			} catch (IOException e) {
				e.printStackTrace();
			}
			UserProxy newProxy=new UserProxy(clientSocket,in,out);
			proxies.add(newProxy);
			newProxy.start();
	 	}
  	}
}