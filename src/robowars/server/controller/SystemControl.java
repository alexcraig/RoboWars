package robowars.server.controller;

/*robowars.server.controller
 * Main Server Class
 * @mwright Oct 15
 */
public class SystemControl {
	private static TcpServer server;
	public static void main (String args[]){
		server=new TcpServer();
		server.start();
	}
}
