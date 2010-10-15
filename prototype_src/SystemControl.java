/*
 * Main Server Class
 * @mwright Oct 15
 */
public class SystemControl {
	private TcpServer server;
	public static void main (String args[]){
		server=new TcpServer();
		server.start();
	}
}
