package robowars.server.controller;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import sun.rmi.runtime.Log;

import com.lti.civil.CaptureDeviceInfo;
import com.lti.civil.CaptureException;
import com.lti.civil.CaptureObserver;
import com.lti.civil.CaptureStream;
import com.lti.civil.CaptureSystem;
import com.lti.civil.Image;
import com.lti.civil.VideoFormat;
import com.lti.civil.awt.AWTImageConverter;
import com.lti.civil.impl.jni.NativeCaptureSystemFactory;

/**
 * Class intended to provide a simple testing bed for video streaming
 * implementation.
 * 
 * @author Alexander Craig
 */
public class ImageServer implements CaptureObserver, Runnable {
	private static Logger log = Logger.getLogger(ImageServer.class);
	
	public static long IMAGE_WRITE_INTERVAL = 200;
	public static int VIDEO_HEIGHT = 240;
	public static int VIDEO_WIDTH = 320;
	
	/** Port to serve images from */
	private int serverPort;
	
	/** Device to capture images from */
	private CaptureDeviceInfo captureDevice;
	
	/** The stream to receive images from */
	private CaptureStream captureStream;
	
	private ArrayList<ImageServerClient> clients;
	
	private int counter;
	private long lastImageWrite;
	
	public ImageServer(int port) {
		serverPort = port;
		captureDevice = null;
		clients = new ArrayList<ImageServerClient>();
		counter = 0;
		lastImageWrite = 0;
	}
	
	public void setCaptureDevice() {
		// RegistryDefaults.registerAll(RegistryDefaults.FMJ);
		// GlobalCaptureDevicePlugger.addCaptureDevices();
		
		// Establish a capture device to serve images from
		NativeCaptureSystemFactory captureFactory = new NativeCaptureSystemFactory();
		try {
			CaptureSystem captureSystem = captureFactory.createCaptureSystem();
			captureSystem.init();
			List<com.lti.civil.CaptureDeviceInfo> devices = captureSystem.getCaptureDeviceInfoList();
			
			for(com.lti.civil.CaptureDeviceInfo device : devices) {
				log.info("LTI-Civil found device: " + device.getDescription() + " - " + device.getDeviceID());
				captureDevice = device; // Note: Capture device always set to first device detected
				break;
			}
			
			captureStream = captureSystem.openCaptureDeviceStream(captureDevice.getDeviceID());
			VideoFormat selectedFormat = null;
			for(VideoFormat v : captureStream.enumVideoFormats()) {
				log.info("Supported video format: " + v.getWidth() + "x" + v.getHeight() + " - " + v.getFPS() 
						+ " fps - " + v.getFormatType() + "(" + v.RGB24 + " RGB24, " + v.RGB32 + " RGB32)");
				if(selectedFormat == null && v.getWidth() == VIDEO_WIDTH && v.getHeight() == VIDEO_HEIGHT) {
					selectedFormat = v;
					captureStream.setVideoFormat(v);
					log.info("Format selected.");
				}
			}
			
			selectedFormat = captureStream.getVideoFormat();
			log.info("Starting capture stream, format:\n" + selectedFormat.getWidth() 
					+ "x" + selectedFormat.getHeight() + " - " + selectedFormat.getFPS() 
					+ " fps - " + selectedFormat.getFormatType() + "(" + selectedFormat.RGB24 
					+ " RGB24, " + selectedFormat.RGB32 + " RGB32)");
			captureStream.setObserver(this);
			captureStream.start();
			
		} catch (CaptureException e) {
			e.printStackTrace();
		}
	}
	
	public void waitForConnections() throws IOException {
		// Establish the listen socket.
		ServerSocket listenSocket = new ServerSocket(serverPort);
		
		log.info("ImageServer initialized and waiting on port: " + serverPort);
		
		// Process HTTP service requests in an infinite loop.
		while (true) {
		    // Listen for a TCP connection request.
			Socket clientSocket = listenSocket.accept();
			log.info("Got new connection from: " + clientSocket.getInetAddress().getCanonicalHostName());
	
		    // Start a new thread to handle the new client.
			ImageServerClient newClient = new ImageServerClient(clientSocket);
			new Thread(newClient).start();
		    synchronized(clients) {
		    	clients.add(newClient);
		    }
		}
	}
	
	public void removeClient(ImageServerClient client) {
		synchronized(clients) {
			clients.remove(this);
		}
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws IOException {
		PropertyConfigurator.configure("config/log_config.properties");
		new Thread(new ImageServer(33331)).start();
    }
	
	@Override
	public void onError(CaptureStream stream, CaptureException err) {
		log.error("Error in capture stream:\n" + err.getMessage());
	}

	@Override
	public void onNewImage(CaptureStream stream, Image image) {
		//if(System.currentTimeMillis() - IMAGE_WRITE_INTERVAL > lastImageWrite) {
			//log.info("Image conversion.");
			// try {
				// ImageIO.write(bufImage, "jpeg", new File("jpgTest" + counter + ".jpg")); // Testing
				synchronized(clients) {
					for(ImageServerClient client : clients) {
						client.writeImageToCaptureStream(image);
					}
				}
			//} catch (IOException e) {
				//e.printStackTrace();
			//}
			//counter++;
			//lastImageWrite = System.currentTimeMillis();
		//}
		
	}
	
	@Override
	public void run() {
		this.setCaptureDevice();
		try {
			this.waitForConnections();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Thread implementation to read images from a capture source and serve
	 * them over the network as JPEGs
	 */
	private class ImageServerClient implements Runnable {
		private Socket clientSocket;
		private OutputStream socketOut;
		private BufferedReader socketIn;
		
		private boolean terminateFlag;
		private boolean gotAck;
		
		private Object ackLock;
		
		public ImageServerClient(Socket clientSocket) {
			this.clientSocket = clientSocket;
			terminateFlag = false;
			gotAck = true;
			ackLock = new Object();
			
			try {
				socketOut = clientSocket.getOutputStream();
				socketIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			} catch (IOException e) {
				log.error("Error getting socket streams");
				return;
			}
		}
		
		public void writeImageToCaptureStream(Image image) {
			if(socketOut != null) {
				try {
					synchronized(ackLock) {
						if(gotAck) {
							synchronized(socketOut) {
								// Note: Image must be sent as png or gif (Android does not appear to support
								// decoding of jpg?)
								ImageIO.write(AWTImageConverter.toBufferedImage(image), "png", socketOut);
								socketOut.flush();
								log.info("Wrote image to socket.");
							}
							gotAck = false;
						}
					}
				} catch (IOException e) {
					log.error("Error writing JPEG to socket");
					gotAck = false;
					terminateFlag = true;
				}
			}
		}

		@Override
		public void run() {
			String reply = "";
			try {
				while(!terminateFlag) {
					// log.info("Reading data from client: " + clientSocket.getInetAddress().getCanonicalHostName());
					reply = socketIn.readLine();
					if(reply == null) break;
					
					// log.info("Read reply: " + reply);
					if(reply.equalsIgnoreCase("ACK")) {
						synchronized(ackLock) {
							gotAck = true;
							log.info("Got ACK");
						}
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				log.info("Terminating client.");
				removeClient(this);
				try {
					clientSocket.close();
					socketIn.close();
					socketOut.close();
				} catch (IOException e) {
					log.error("Error closing client socket");
					e.printStackTrace();
				}
			}
			
		}
	}
}
